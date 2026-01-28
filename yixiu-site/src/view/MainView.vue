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
} from '@element-plus/icons-vue'
import {UserFilled} from '@element-plus/icons-vue'

import {reactive, ref, computed, onUnmounted, onMounted, onBeforeMount, provide} from 'vue'
import {ElMessage} from 'element-plus'
import {getUserInfo} from "../api/userApi.js";
import Cookie from "js-cookie";
import router from "../router/index.js";
import {isPolling, startNotifyPoll, stopNotifyPoll} from "../utils/notificationUtils.js";
import {getUnreadNotifyCount} from "../api/notificationApi.js";
import {AcceptAdmin, AcceptVolunteer} from "../utils/roleCheckUtils.js";
import {JumpToRepairForm, JumpToTaskList} from "../utils/redirectUtils.js";
import {useNotificationStore} from "../stores/notificationInit.js";

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
            <el-sub-menu index="3" v-if="AcceptAdmin(userInfo)">
              <template #title>
                <el-icon>
                  <icon-menu />
                </el-icon>
                <span>站务管理</span>
              </template>
              <el-menu-item index="3-1" @click="() => router.push('/admin/userManage')">
                用户管理
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
          <el-main></el-main>
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

</style>