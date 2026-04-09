<script setup>
import {ref, reactive, computed, onMounted, onUnmounted, inject} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Message, Search } from '@element-plus/icons-vue'
import Cookie from 'js-cookie'
import { getUserInfo } from '../../api/userApi.js'
import {changeToRead, getNotifyByFilter, getNotifyList} from "../../api/notificationApi.js"
import router from "../../router/index.js"
import {useNotificationStore} from "../../stores/notificationInit.js";
import {formatTime} from "../../utils/timeUtils.js";
import {JumpToUserProfile} from "../../utils/redirectUtils.js";

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const userInfo = reactive({
  userId: "",
  username: "",
  realName: "",
  phone: "",
  email: "",
  avatar: "",
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

// 消息数据
const notifications = ref([])
const currentNotification = reactive({
  notifyId: '',
  receiverId: '',
  senderId: '',
  senderUsername: '',
  senderAvatar: '',
  title: '',
  content: '',
  type: '',
  isRead: '',
  link: '',
  createTime: ''
})

const notificationStore = useNotificationStore()

// 使用计算属性自动响应状态变化
const unreadNotifyCount = computed(() => notificationStore.unreadCount)

// 生命周期
onMounted(async () => {
  await queryUserInfo()
  await loadNotifications()
  await notificationStore.syncUnreadCount()
})

// 获取用户信息
const queryUserInfo = async () => {
  try {
    const token = Cookie.get('Authorization')
    const response = await getUserInfo(token)
    if (response.code === 200) {
      Object.assign(userInfo, response.data)
    } else {
      ElMessage.error(response.msg)
    }
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 分页和过滤
const loading = ref(false)

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载消息列表
const loadNotifications = async () => {
  loading.value = true
  try {
    const response = await getNotifyList(pagination.currentPage, pagination.pageSize)
    if (response.code !== 200) {
      ElMessage.error(response.msg)
      return
    }
    notifications.value = response.data.list
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('加载消息失败')
  } finally {
    loading.value = false
  }
}

// 筛选条件
const filterForm = reactive({
  type: "",
  searchKeyword: ""
})

// 处理消息筛选
const handleFilter = async () => {
  const queryParams = buildQueryParams()
  const response = await getNotifyByFilter(queryParams)

  if (response.code === 200) {
    notifications.value = response.data.list
    pagination.total = response.data.total
    console.log(notifications.value)
  } else {
    ElMessage.error(response.msg)
  }
}

// 构建筛选条件
const buildQueryParams = () => {
  const params = {}
  // 状态条件
  if (filterForm.type !== '' && filterForm.type != null) {
    params.type = filterForm.type
  }
  if (filterForm.searchKeyword !== '' && filterForm.searchKeyword != null) {
    params.searchKeyword = filterForm.searchKeyword
  }
  params.pageNum = pagination.currentPage
  params.pageSize = pagination.pageSize
  return params
}

// 对话框控制
const detailDialogVisible = ref(false)

// // 计算未读信息数量
// const hasUnread = computed(() => {
//   return notifications.value.some(notification => notification.isRead === 0)
// })

const loadNotificationCondition = () =>{
  if (filterForm.type !== '' || filterForm.searchKeyword !== '') {
    handleFilter()
  } else {
    loadNotifications()
  }
}

// 刷新消息
const refreshNotifications = () => {
  loadNotificationCondition()
}

// 搜索消息
const searchNotifications = () => {
  handleFilter()
}

// 查看消息详情
const viewNotificationDetail = (notification) => {
  Object.assign(currentNotification, notification)
  detailDialogVisible.value = true

  // 如果是未读消息，标记为已读
  if (notification.isRead === 0) {
    markAsRead(notification.notifyId)
  }
}

// 重置当前消息
const resetCurrentNotification = () => {
  Object.keys(currentNotification).forEach(key => {
    currentNotification[key] = ''
  })
}

// 标记为已读
const markAsRead = async (notifyId) => {
  try {
    const response = await changeToRead(notifyId)
    if (response.code !== 200) {
      ElMessage.error(response.msg)
      return
    }

    // 更新本地状态
    const notification = notifications.value.find(item => item.notifyId === notifyId)
    if (notification) {
      notification.isRead = 1
    }

    // 如果当前查看的是这条消息，也更新当前消息状态
    if (currentNotification.notifyId === notifyId) {
      currentNotification.isRead = 1
    }

    ElMessage.success('标记为已读成功')
    loadNotificationCondition()
  } catch (error) {
    ElMessage.error('标记失败')
  }
}

// 全部标记为已读
const markAllAsRead = async () => {
  try {
    // 这里应该调用批量标记为已读的API
    // 暂时遍历所有未读消息进行标记

    const unreadNotifications = notifications.value.filter(item => item.isRead === 0)
    for (const notification of unreadNotifications) {
      await changeToRead(notification.notifyId)
      notification.isRead = 1
    }

    ElMessage.success('全部标记为已读成功')
    loadNotificationCondition()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 跳转到链接
const goToLink = (link) => {
  if (link) {
    router.push(link)
  }
}

// 分页处理
const handleSizeChange = (val) => {
  pagination.pageSize = val
  pagination.pageNum = 1
  loadNotificationCondition()
}

const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadNotificationCondition()
}

// 工具函数
const getTypeDisplayName = (type) => {
  switch (type) {
    case 'SYSTEM': return '系统消息'
    case 'BROADCAST': return '公告'
    case 'USER': return '用户消息'
    default: return '未知类型'
  }
}

const getTypeTagType = (type) => {
  switch (type) {
    case 'SYSTEM': return 'danger'
    case 'BROADCAST': return 'warning'
    case 'USER': return 'success'
    default: return 'info'
  }
}

const getSenderDisplay = (notification) => {
  if (notification.type === 'SYSTEM') return '系统'
  if (notification.type === 'BROADCAST') return '公告'
  return notification.senderUsername || '用户'
}

const getContentPreview = (content) => {
  if (!content) return ''
  // 移除HTML标签并截取前50个字符
  const text = content.replace(/<[^>]*>/g, '')
  return text.length > 50 ? text.substring(0, 50) + '...' : text
}
</script>

<!-- MessageCenterView.vue -->
<template>
  <div class="common-layout">
    <el-container style="height: 100%">
      <el-header class="header_container">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <h3 class="clickable-title" style="margin-right: 100px" @click="() => router.push('/')">Light义修帮</h3>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="grid-content ep-bg-purple">
              <el-menu
                  default-active="2"
                  class="el-menu-demo"
                  mode="horizontal"
                  style="border-bottom: silver solid 1px;background-color: snow"
                  :ellipsis="false"
              >
                <el-menu-item index="1" @click="() => router.push('/user/basicInfo')">基本信息</el-menu-item>
                <el-menu-item index="2">消息中心</el-menu-item>
              </el-menu>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <div class="component-center">
                <el-avatar class="clickable-avatar" @click="() => router.push('/user/basicInfo')" :fit="'cover'" :src="userInfo.avatar"/>
              </div>
              <div class="component-center">
                <el-badge :is-dot="unreadNotifyCount > 0" class="item">
                  <el-button type="default" :icon="Message" circle/>
                </el-badge>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-header>

      <!-- 主界面 -->
      <el-main>
        <div class="main-content">
          <el-card class="message-card">
            <template #header>
              <div class="card-header">
                <span>消息中心</span>
                <div class="header-actions">
                  <el-button type="primary" @click="markAllAsRead">全部标记为已读</el-button>
                  <el-button @click="refreshNotifications">刷新</el-button>
                </div>
              </div>
            </template>

            <!-- 消息筛选 -->
            <div class="filter-section">
              <el-radio-group v-model="filterForm.type" @change="handleFilter">
                <el-radio-button label="">全部</el-radio-button>
                <el-radio-button label="SYSTEM">系统消息</el-radio-button>
                <el-radio-button label="BROADCAST">广播消息</el-radio-button>
                <el-radio-button label="USER">用户消息</el-radio-button>
              </el-radio-group>
              <el-input
                  v-model="filterForm.searchKeyword"
                  placeholder="搜索消息内容..."
                  style="width: 300px; margin-left: 20px"
                  clearable
                  @clear="searchNotifications"
                  @keyup.enter="searchNotifications"
              >
                <template #append>
                  <el-button :icon="Search" @click="searchNotifications"/>
                </template>
              </el-input>
            </div>

            <!-- 消息列表容器 -->
            <div class="messages-container" v-loading="loading">
              <div
                  v-for="notification in notifications"
                  :key="notification.notifyId"
                  class="message-item"
                  :class="{ 'unread': notification.isRead === 0 }"
                  @click="viewNotificationDetail(notification)"
              >
                <!-- 发送方头像或图标 -->
                <div class="sender-avatar">
                  <el-avatar
                      v-if="notification.type === 'USER'"
                      :size="40"
                      :src="notification.senderAvatar || defaultAvatar"
                  />
                  <div v-else class="system-icon">
                    <el-icon :size="24" color="#409eff">
                      <Message />
                    </el-icon>
                  </div>
                </div>

                <!-- 消息内容 -->
                <div class="message-content">
                  <div class="message-header">
                    <span class="sender-name">{{ getSenderDisplay(notification) }}</span>
                    <span class="message-time">{{ formatTime(notification.createTime) }}</span>
                  </div>
                  <div class="message-title">{{ notification.title }}</div>
                </div>

                <!-- 未读标记 -->
                <div v-if="notification.isRead === 0" class="unread-indicator"></div>
              </div>

              <!-- 空状态 -->
              <div v-if="notifications.length === 0 && !loading" class="empty-state">
                <el-empty description="暂无消息" />
              </div>
            </div>

            <!-- 分页 -->
            <div class="pagination-container">
              <el-pagination
                  v-model:current-page="pagination.currentPage"
                  v-model:page-size="pagination.pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  :total="pagination.total"
                  layout="total, sizes, prev, pager, next, jumper"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
              />
            </div>
          </el-card>
        </div>
      </el-main>
    </el-container>

    <!-- 消息详情对话框 -->
    <el-dialog
        v-model="detailDialogVisible"
        :title="''"
        width="600px"
        center
        @close="resetCurrentNotification"
        class="message-detail-dialog"
    >
      <div class="chat-container">
        <!-- 聊天头部 -->
        <div class="chat-header">
          <div class="sender-info">
            <div class="sender-avatar-large">
              <el-avatar
                  v-if="currentNotification.type === 'USER'"
                  :size="40"
                  :src="currentNotification.senderAvatar || defaultAvatar"
              />
              <div v-else class="system-icon-large">
                <el-icon :size="24" color="#409eff">
                  <Message />
                </el-icon>
              </div>
            </div>
            <div class="sender-details">
              <div class="sender-name"
                   @click="() => JumpToUserProfile(currentNotification.senderId)"
              >
                {{ getSenderDisplay(currentNotification) }}
              </div>
              <div class="message-type">
                <el-tag :type="getTypeTagType(currentNotification.type)" size="small">
                  {{ getTypeDisplayName(currentNotification.type) }}
                </el-tag>
              </div>
            </div>
          </div>
          <div class="chat-actions">
            <el-button
                v-if="currentNotification.isRead === 0"
                type="primary"
                size="small"
                @click="markAsRead(currentNotification.notifyId)"
            >
              标记为已读
            </el-button>
            <el-button
                v-if="currentNotification.link"
                type="success"
                size="small"
                @click="goToLink(currentNotification.link)"
            >
              查看详情
            </el-button>
          </div>
        </div>

        <!-- 聊天内容 -->
        <div class="chat-content">
          <div class="message-bubble">
            <div class="message-title">{{ currentNotification.title }}</div>
            <div class="message-text" v-html="currentNotification.content"></div>
            <div class="message-meta">
              <span class="send-time">{{ formatTime(currentNotification.createTime) }}</span>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.grid-content {
  border-radius: 4px;
  min-height: 36px;
  height: 100%;
  display: flex;
  justify-content: center;
}

.el-menu-item {
  padding-left: 50px;
  padding-right: 50px;
}

.el-main {
  background-image: url('../../assets/login_backgroud.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}

.main-content {
  height: 100vh;
  width: 800px;
  margin-right: auto;
  margin-left: auto;
  display: flex;
  flex-direction: column;
  padding: 20px;
  border: snow 8px solid;
  border-radius: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background: white;
}

.message-card {
  width: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-section {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.messages-container {
  height: calc(100vh - 300px);
  overflow-y: auto;
}

.message-item {
  display: flex;
  padding: 15px;
  border-bottom: 1px solid #eee;
  cursor: pointer;
  position: relative;
  transition: background-color 0.2s;
}

.message-item:hover {
  background-color: #f5f7fa;
}

.message-item.unread {
  background-color: #eff6ff;
}

.message-item.unread:hover {
  background-color: #dbeafe;
}

.sender-avatar {
  margin-right: 15px;
}

.system-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-content {
  flex: 1;
  min-width: 0;
}

.message-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
}

.sender-name {
  font-weight: bold;
  color: #409eff;
  margin-bottom: 2px;
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 4px;  /* 添加圆角 */
  display: inline-block;  /* 使内边距生效 */
}

.sender-name:hover {
  background-color: #f0f9ff;  /* 悬浮时背景色 */
  color: #66b1ff;
  transform: translateY(-1px);  /* 悬浮时轻微上移 */
}

.sender-name:active {
  background-color: #ecf5ff;  /* 点击时背景色 */
  transform: translateY(0);  /* 点击时回到原位 */
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.message-title {
  font-weight: 500;
  color: #606266;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  text-align: left;
}

.message-preview {
  font-size: 14px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-indicator {
  position: absolute;
  top: 50%;
  right: 15px;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background-color: red;
}

.empty-state {
  text-align: center;
  padding: 50px 0;
}

/* 聊天对话框样式 */
.chat-container {
  height: 500px;
  display: flex;
  flex-direction: column;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 15px;
  border-bottom: 1px solid #eee;
}

.sender-info {
  display: flex;
  align-items: center;
}

.sender-avatar-large {
  margin-right: 15px;
}

.system-icon-large {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: #ecf5ff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.message-type {
  font-size: 12px;
}

.chat-actions {
  display: flex;
  gap: 10px;
}

.chat-content {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.message-bubble {
  max-width: 100%;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 15px;
}

.message-bubble .message-title {
  font-size: 18px;
  font-weight: 500;
  margin-bottom: 15px;
  color: #303133;
}

.message-bubble .message-text {
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  margin-bottom: 15px;
  white-space: pre-wrap;
}

.message-meta {
  text-align: right;
}

.send-time {
  font-size: 12px;
  color: #909399;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.clickable-avatar {
  cursor: pointer;
}
</style>
