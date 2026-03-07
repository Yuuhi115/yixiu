<script setup>
import {
  Document,
  Menu as IconMenu,
  Location,
  Setting,
} from '@element-plus/icons-vue'
import {
  Check,
  Delete,
  Edit,
  Message,
  Search,
  Star,
  Clock,
  CirclePlus,
} from '@element-plus/icons-vue'
import {UserFilled} from '@element-plus/icons-vue'

import {reactive, ref, computed, onUnmounted, onMounted, onBeforeMount, provide, nextTick} from 'vue'
import {ElMessage} from 'element-plus'
import {getUserInfo} from "../api/userApi.js";
import Cookie from "js-cookie";
import router from "../router/index.js";
import {isPolling, startNotifyPoll, stopNotifyPoll} from "../utils/notificationUtils.js";
import {getUnreadNotifyCount} from "../api/notificationApi.js";
import {AcceptAdmin, AcceptVolunteer} from "../utils/roleCheckUtils.js";
import {JumpToRepairForm, JumpToTaskList} from "../utils/redirectUtils.js";
import {useNotificationStore} from "../stores/notificationInit.js";
import {getChatHistory, getChatSession, sendChatMessage} from "../api/AiApi.js";
import {formatTime} from "../utils/timeUtils.js";

const userInfoRef = ref()

const notificationStore = useNotificationStore()


// 使用计算属性自动响应状态变化
const unreadNotifyCount = computed(() => notificationStore.unreadCount)

const userInfo = reactive({
  userId: "",
  username: "",
  realName: "",
  phone: "",
  email: "",
  avatar: "",
  userSignature: "",
  role: "",
  status: "",
  lastLogin: "",
  volunteerInfo: {
    volunteerId: "",
    studentNumber: "",
    majorClass: "",
    grade: "",
    contactType: "",
    contactNumber: "",
  }
})

// 添加加载状态控制
const isLoading = ref(true)

onMounted(async () => {
  try {
    // 并行执行初始化请求
    await Promise.all([
      queryUserInfo(),
      getUnreadNotify(),
      notificationStore.syncUnreadCount()
    ])
    await saveRole()
    await loadHistoryConversations()
  } finally {
    isLoading.value = false
  }
})

const queryUserInfo = async () => {
  const token = Cookie.get('Authorization')
  const response = await getUserInfo(token)
  if (response.code !== 200) {
    ElMessage.error(response.msg)
    return
  }
  // 将返回的用户信息赋值给 userInfo 对象
  Object.assign(userInfo, response.data)
  console.log(userInfo)
}

const saveRole = async () => {
  if (userInfo.userId !== "" && localStorage.getItem('role') === null) {
    localStorage.setItem('role', userInfo.role)
  }
}

// 未读通知数
const getUnreadNotify = async () => {
  const response = await getUnreadNotifyCount()
  if (response.code !== 200) {
    ElMessage.error(response.msg)
    return
  }
  unreadNotifyCount.value = response.data
}

const handleOpen = (key, keyPath) => {
  console.log(key, keyPath)
}
const handleClose = (key, keyPath) => {
  console.log(key, keyPath)
}
const logout = async () => {
  Cookie.remove('Authorization')
  await notificationStore.stopPolling()
  await router.push('/login')
}

/*ai智能问答*/
const messagePagination = reactive({
  pageNum: 1,
  pageSize: 10
})
const chatSessionPagination = reactive({
  pageNum: 1,
  pageSize: 20
})
const conversationId = ref(null)

const messages = ref([
  { role: 'assistant', content: '您好！我是您的电脑维修智能助手，请问有什么可以帮助您的吗？', createTime: new Date() }
])
const inputMessage = ref('')
const scrollbarRef = ref(null)

const currentAiAnswer = ref()
// AI思考中
const isThinking = ref(false)

// 发送消息函数
const sendMessage = async () => {
  if (!inputMessage.value.trim()) return

  // 添加用户消息
  messages.value.push({ role: 'user', content: inputMessage.value, createTime: new Date() })
  scrollToBottom()
  // 清空输入框
  const userMessage = inputMessage.value
  inputMessage.value = ''

  // 设置思考状态为 true
  isThinking.value = true

  try {
    // 调用后端接口发送消息
    let data = {
      question: userMessage,
      conversationId: conversationId.value || null // 如果没有 conversationId，则不传
    }
    const response = await sendChatMessage(data)
    if (response.code === 200) {
      if (!conversationId.value) {
        conversationId.value = response.data.conversationId
        Object.assign(currentAiAnswer, response.data)
      }

      const cleanedAnswer = response.data.answer.replace(/\*/g, '')

      // 添加 AI 回复
      messages.value.push({
        role: 'assistant',
        content: cleanedAnswer,
        createTime: response.data.createTime,
      })
      // 滚动到底部
      scrollToBottom()
    } else {
      ElMessage.error(response.msg)
    }
  } catch (error) {
    ElMessage.error('发送失败，请稍后再试')
  } finally {
    isThinking.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (scrollbarRef.value) {
      scrollbarRef.value.setScrollTop(scrollbarRef.value.wrapRef.scrollHeight)
    }
  })
}

// 获取历史会话列表
const historyConversations = ref([])
const loadHistoryConversations = async () => {
  try {
    let params = {
      pageNum: chatSessionPagination.pageNum,
      pageSize: chatSessionPagination.pageSize
    }
    const response = await getChatSession(params)
    if (response.code === 200) {
      historyConversations.value = response.data.list
    } else {
      ElMessage.error(response.msg)
    }
  } catch (error) {
    ElMessage.error('加载历史会话失败')
  }
}

const selectedConversation = ref(null)

// 切换历史会话
const switchConversation = (id) => {
  conversationId.value = id
  messagePagination.pageNum = 1
  // 加载该会话的消息记录
  loadMessagesByConversation(id)
}

// 开始新对话
const startNewConversation = () => {
  // 清空当前会话 ID 和消息列表
  conversationId.value = null
  messages.value = [
    { role: 'assistant', content: '您好！我是您的电脑维修智能助手，请问有什么可以帮助您的吗？', createTime: new Date() }
  ]
  // 滚动到底部
  scrollToBottom()
}

// 根据 conversationId 加载消息记录
const loadMessagesByConversation = async (conversationId) => {
  try {
    let params = {
      pageNum: messagePagination.pageNum,
      pageSize: messagePagination.pageSize,
      conversationId: conversationId
    }
    // console.log("params:", params)
    const response = await getChatHistory(params)
    if (response.code === 200) {
      messages.value = response.data.list.map(msg => ({
        role: msg.role === 'user' ? 'user' : 'assistant',
        content: msg.role === 'assistant' ? msg.content.replace(/\*/g, '') : msg.content,
        createTime: msg.createTime
      }))
      scrollToBottom()
    } else {
      ElMessage.error(response.msg)
    }
  } catch (error) {
    ElMessage.error('加载消息失败')
  }
}
</script>

<template>
  <div class="common-layout" v-if = "!isLoading">
    <el-container style="height: 100%">
      <!--头部栏-->
      <el-header class="header_container">
        <el-row :gutter="24">
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <h3 class="mb-3" style="margin-right: 100px">Light义修帮</h3>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="grid-content ep-bg-purple"/>
          </el-col>
          <el-col :span="6">
            <div class="grid-content ep-bg-purple"/>
          </el-col>
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <div class="component-center">
                <el-avatar :fit="'cover'" :src="userInfo.avatar"/>
              </div>
              <div class="component-center">
                <el-badge :is-dot="unreadNotifyCount > 0" class="item">
                  <el-button style="margin-bottom: 10px" type="default" @click="() => router.push('/user/messageCenter')" :icon="Message" circle/>
                </el-badge>
              </div>
              <div class="component-center">
                <el-button type="danger" @click="logout">退出登录</el-button>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-header>


      <el-container>
        <!--侧栏-->
        <el-aside class="aside_container">
          <el-menu
              class="el-menu-vertical-demo"
              @open="handleOpen"
              @close="handleClose"
          >
            <el-sub-menu index="1" v-if="userInfo.role === 'student'">
              <template #title>
                <el-icon>
                  <icon-menu />
                </el-icon>
                <span>义修服务</span>
              </template>
              <el-menu-item index="1-1" @click="JumpToRepairForm(userInfo)">
                填写预约问卷
              </el-menu-item>
              <el-menu-item index="1-2" @click="() => router.push('/user/basicInfo')">
                预约信息模板管理
              </el-menu-item>
              <el-menu-item index="1-3" @click="() => router.push('/repair/history')">
                预约历史
              </el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="2" v-if="AcceptAdmin(userInfo)">
              <template #title>
                <el-icon>
                  <icon-menu />
                </el-icon>
                <span>队伍管理</span>
              </template>
              <el-menu-item index="2-1" @click="() => router.push('/admin/memberManage')">
                成员管理
              </el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="3" v-if="AcceptVolunteer(userInfo)">
              <template #title>
                <el-icon>
                  <icon-menu />
                </el-icon>
                <span>站务管理</span>
              </template>
              <el-menu-item index="3-1" v-if="AcceptAdmin(userInfo)" @click="() => router.push('/admin/userManage')">
                用户管理
              </el-menu-item>
              <el-menu-item index="3-2" @click="() => router.push('/admin/aiRepository')">
                知识库管理
              </el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="4" v-if="AcceptVolunteer(userInfo)">
              <template #title>
                <el-icon>
                  <icon-menu />
                </el-icon>
                <span>任务中心</span>
              </template>
              <el-menu-item index="4-1" @click="JumpToTaskList(userInfo, '/taskCenter/list')">
                任务列表
              </el-menu-item>
              <el-menu-item index="4-2" @click="JumpToTaskList(userInfo, '/taskCenter/myTask')">
                我的任务
              </el-menu-item>
            </el-sub-menu>
            <el-sub-menu index="5">
              <template #title>
                <el-icon>
                  <location/>
                </el-icon>
                <span>个人中心</span>
              </template>
              <!--            <el-menu-item-group title="Group One">-->
              <el-menu-item index="5-1" @click="() => router.push('/user/basicInfo')">
                基本信息
              </el-menu-item>
              <!--            </el-menu-item-group>-->
              <!--            <el-menu-item-group title="Group Two">-->
              <el-menu-item index="5-2">我的收藏</el-menu-item>
              <!--            </el-menu-item-group>-->
              <el-menu-item index="5-3" @click="() => router.push('/user/messageCenter')">
<!--                <template #title>item four</template>-->
<!--                <el-menu-item index="1-4-1">item one</el-menu-item>-->
                消息中心
              </el-menu-item>
            </el-sub-menu>
            <el-menu-item index="6" @click="() => router.push('/community')">
              <el-icon>
                <document/>
              </el-icon>
              <span>义修社区</span>
            </el-menu-item>
            <el-menu-item index="7">
              <el-icon>
                <setting/>
              </el-icon>
              <span>设置</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-container>
          <!--主栏-->
          <el-main>
            <!-- AI 智能问答聊天界面 -->
            <el-card class="chat-container" shadow="never">
              <!-- 聊天标题 -->
              <div class="chat-title">广外义修智能AI小助手</div>

              <div class="history-selector">
                <!--新对话按钮-->
                <el-tooltip
                    class="box-item"
                    effect="dark"
                    content="创建新对话"
                    placement="top-start"
                >
                  <div class="history-btn" @click="startNewConversation">
                    <el-icon><CirclePlus /></el-icon>
                  </div>
                </el-tooltip>
                <!-- 历史会话图标按钮 -->
                <el-dropdown @command="switchConversation">
                  <div class="history-btn">
                    <el-icon><Clock /></el-icon>
                  </div>
                  <template #dropdown>
                    <el-dropdown-menu class="history-dropdown-menu">
                      <el-dropdown-item
                          v-for="conv in historyConversations"
                          :key="conv.conversationId"
                          :command="conv.conversationId"
                      >
                        <div class="conversation-item">
                          <div class="headline">{{ conv.headline }}</div>
                          <div class="create-time">{{ formatTime(conv.createTime) }}</div>
                        </div>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>

              <!-- 消息展示区域 -->
              <el-scrollbar class="chat-messages" ref="scrollbarRef">
                <div
                    v-for="(msg, index) in messages"
                    :key="index"
                    class="message-wrapper"
                >
                  <!-- 时间显示 -->
                  <div class="message-time-centered">{{ formatTime(msg.createTime) }}</div>

                  <!-- 消息容器 -->
                  <div :class="['message-container', msg.role === 'user' ? 'user-container' : 'ai-container']">
                    <!-- AI头像 -->
                    <div v-if="msg.role === 'assistant'" class="avatar ai-avatar">
                      <img src="../assets/yixiu-ai.png" alt="AI头像" />
                    </div>
                    <!-- 消息气泡 -->
                    <div :class="['message-bubble', msg.role === 'user' ? 'user-message' : 'ai-message']">
                      <div class="message-content">{{ msg.content }}</div>
                    </div>
                    <!-- 用户头像 -->
                    <div v-if="msg.role === 'user'" class="avatar user-avatar">
                      <img :src="userInfo.avatar" alt="用户头像" />
                    </div>
                  </div>
                </div>

                <!-- AI 思考中的提示 -->
                <div v-if="isThinking" class="message-wrapper">
                  <div class="message-container ai-container">
                    <div class="avatar ai-avatar">
                      <img src="../assets/yixiu-ai.png" alt="AI头像" />
                    </div>
                    <div class="message-bubble ai-message thinking-bubble">
                      <div class="thinking-content">
                        <span class="thinking-dot">●</span>
                        <span class="thinking-dot">●</span>
                        <span class="thinking-dot">●</span>
                        <span class="thinking-text">AI 正在思考中...</span>
                      </div>
                    </div>
                  </div>
                </div>

              </el-scrollbar>

              <!-- 输入区域 -->
              <div class="chat-input-area">
                <el-input
                    v-model="inputMessage"
                    placeholder="Please Enter..."
                    @keyup.enter="sendMessage"
                    size="large"
                    class="input-message"
                />
                <el-button type="primary" @click="sendMessage" size="large">发送</el-button>
              </div>
            </el-card>
          </el-main>
          <!--底部栏-->
<!--          <el-footer>Footer</el-footer>-->
        </el-container>
      </el-container>
    </el-container>
  </div>
</template>


<style scoped>
.el-menu-item {
  background-color: snow;
}
.el-main {
  background-image: url('../assets/login_backgroud.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.el-menu-vertical-demo {
  border-right: 0;
  background-color: snow;
}

.el-row {
  margin-bottom: 5px;
  height: 100%;
}

.el-row:last-child {
  margin-bottom: 0;
}

.el-col {
  border-radius: 4px;
  margin-left: 0;
  margin-right: 0;
}

.grid-content {
  border-radius: 4px;
  min-height: 36px;
  height: 100%;
  display: flex;
  justify-content: center;
}

.item {
  margin-top: 10px;
  margin-right: 30px;
}
.chat-container {
  width: 80%;
  max-width: 1000px;
  margin-top: 10px;
  margin-left: auto;
  margin-right: auto;
  border-radius: 10px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.chat-title {
  font-size: 18px;
  font-weight: bold;
  text-align: center;
  padding: 10px 0;
}

.chat-messages {
  justify-content: flex-start;
  height: 400px;
  padding: 10px;
  overflow-y: auto;
}

.message-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center; /* 水平居中 */
  margin: 15px 0;
}

.message-time-centered {
  font-size: 12px;
  color: #999;
  text-align: center;
  margin-bottom: 5px; /* 与下方内容的间距 */
}

.message-container {
  display: flex;
  align-items: flex-start;
  width: 100%;
}

.ai-container {
  justify-content: flex-start;
}

.user-container {
  justify-content: flex-end;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  margin: 0 10px;
}

.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.ai-avatar {
  order: 1;
}

.user-avatar {
  order: 3;
}

.message-bubble {
  max-width: 65%;
  padding: 12px 15px;
  border-radius: 15px;
  margin: 0 10px;
  white-space: pre-wrap;
}

.ai-message {
  text-align: left;
  background-color: #f0f2f5;
  color: #333;
  border-radius: 15px 15px 15px 5px;
  padding: 12px 15px;
  order: 2;
}

.user-message {
  text-align: right;
  background-color: #409eff;
  color: white;
  border-radius: 15px 15px 5px 15px;
  padding: 12px 15px;
  order: 2;
}

.message-content {
  word-wrap: break-word;
  line-height: 1.4;
}
.thinking-bubble {
  display: inline-flex;
  align-items: center;
  padding: 8px 15px;
}

.thinking-content {
  display: flex;
  align-items: center;
  gap: 4px;
}

.thinking-dot {
  font-size: 20px;
  color: #909399;
  animation: thinking-bounce 1.4s infinite ease-in-out both;
  line-height: 1;
}

.thinking-dot:nth-child(1) {
  animation-delay: -0.32s;
}

.thinking-dot:nth-child(2) {
  animation-delay: -0.16s;
}

@keyframes thinking-bounce {
  0%, 80%, 100% {
    transform: scale(0);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.thinking-text {
  font-size: 13px;
  color: #909399;
  margin-left: 8px;
  font-style: italic;
}

.chat-input-area {
  background-color: #fff;
  border-radius: 95px;
  display: flex;
  gap: 10px;
  padding: 10px;
  border-top: 1px solid #eee;
}

.history-selector {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.conversation-item {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.headline {
  font-weight: bold;
  margin-bottom: 4px;
}

.create-time {
  font-size: 12px;
  color: #999;
}

.history-dropdown-menu {
  max-height: 400px;
  overflow-y: auto;
}

.history-btn {
  height: 30px;
  width: 30px;
  border-radius: 25px;
  transition: all 0.3s ease;
  border: none !important;
  outline: none !important;
}

.history-btn:hover {
  background-color: #f5f5f5;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.history-btn:active {
  transform: translateY(0);
  background-color: #ebebeb;
}

.history-btn .el-icon {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.input-message {
  .el-input-group__prepend {
    border-radius: 95px;
    border: 0;
    box-shadow: 0 0 0 0;
  }

  .el-input__wrapper {
    border-radius: 95px;
    border: 0;
    box-shadow: 0 0 0 0;
  }

  .el-input-group__append {
    border-radius: 95px;
    border: 0;
    box-shadow: 0 0 0 0;
  }
}

</style>