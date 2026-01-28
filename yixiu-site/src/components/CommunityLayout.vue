<!-- src/components/CommonLayout.vue -->
<script setup>
import { ref, reactive, computed, onMounted, provide } from 'vue'
import { getUserInfo } from '../api/userApi.js'
import { Message } from "@element-plus/icons-vue"
import Cookie from 'js-cookie'
import { ElMessage } from 'element-plus'
import router from "../router/index.js"
import { useNotificationStore } from "../stores/notificationInit.js"
import { getUnreadNotifyCount } from "../api/notificationApi.js"
import {getCommunityStatistics, getFollowingUpdates, hasReadUpdate} from "../api/communityApi.js";

// 用户信息状态
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
})

const communityStatistics = reactive({
  followNum: "",
  fansNum: "",
  postNum: "",
  getLikeNum: "",
})

const notificationStore = useNotificationStore()

// 使用计算属性自动响应状态变化
const unreadNotifyCount = computed(() => notificationStore.unreadCount)

// 模拟关注用户的动态提醒
const followingsWithNewPosts = ref([])

onMounted(async () => {
  await queryUserInfo()
  await loadFollowingUpdates()
  await getUnreadNotify()
  await queryCommunityStatistics()
  await notificationStore.syncUnreadCount()
})

// 加载用户信息
const queryUserInfo = async () => {
  const token = Cookie.get('Authorization')
  const response = await getUserInfo(token)
  if (response.code !== 200) {
    ElMessage.error(response.msg)
    return
  }
  Object.assign(userInfo, response.data)
}

const currentUserId = computed(() => {
  return userInfo.userId || '0' // 提供默认值以防用户信息未加载
})

const queryCommunityStatistics = async () => {
  const response = await getCommunityStatistics()
  if (response.code !== 200) {
    ElMessage.error(response.msg)
    return
  }
  // 将返回的社区统计信息赋值给 communityStatistics 对象
  Object.assign(communityStatistics, response.data)
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

// 加载关注用户的更新提醒
const loadFollowingUpdates = async () => {
  const response = await getFollowingUpdates()
  if (response.code === 200) {
    followingsWithNewPosts.value = response.data.map(user => ({
      ...user,
      hasNew: true  // 为每个用户设置 hasNew 为 true
    }))
  } else {
    ElMessage.error(response.msg)
  }
}

// 跳转到对应的用户动态
const goToUserPosts = async (user) => {
  user.hasNew = false
  await router.push({
    path: '/community',
    query: { postUserId: user.userId }
  })
  const response = await hasReadUpdate(user.userId)
  if (response.code !== 200) {
    ElMessage.error(response.msg)
  }
}

const logout = async () => {
  Cookie.remove('Authorization')
  await router.push('/login')
}


defineProps({
  activeMenu: String // 用于标识当前激活的菜单项
})

</script>

<template>
  <div class="community-page">
    <!--头部栏-->
    <el-header class="header_container">
      <el-row :gutter="24">
        <el-col :span="6">
          <div class="grid-content ep-bg-purple">
            <h3 class="clickable-title" style="margin-right: 100px" @click="() => router.push('/')">Light义修帮</h3>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="grid-content ep-bg-purple">
            <el-menu
                :default-active="activeMenu"
                class="el-menu-demo"
                mode="horizontal"
                style="border-bottom: silver solid 1px;background-color: snow"
                :ellipsis="false"
            >
              <el-menu-item index="1" @click="() => router.push('/community')">社区主页</el-menu-item>
              <el-menu-item index="2" @click="() => router.push(`/community/followList/follow/${currentUserId}`)">关注列表</el-menu-item>
              <el-menu-item index="3" @click="() => router.push('/taskCenter/list')">我的收藏</el-menu-item>
              <el-menu-item index="4" @click="() => router.push(`/community/profile/${currentUserId}`)">个人主页</el-menu-item>
            </el-menu>
          </div>
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

    <el-container class="main-container">
      <!-- 左侧用户信息栏 -->
      <el-aside width="200px" class="left-sidebar">
        <div class="user-card">
          <el-avatar :size="60" :src="userInfo.avatar" />
          <h3>{{ userInfo.username }}</h3>
          <p>{{ userInfo.realName }}</p>

          <div class="stats">
            <div class="stat-item" @click="() => router.push('/community/followList/follow/'+currentUserId)">
              <span class="number">{{ communityStatistics.followNum || 0 }}</span>
              <span class="label">关注</span>
            </div>
            <div class="stat-item" @click="() => router.push('/community/followList/fans/'+currentUserId)">
              <span class="number">{{ communityStatistics.fansNum || 0 }}</span>
              <span class="label">粉丝</span>
            </div>
            <div class="stat-item">
              <span class="number">{{ communityStatistics.postNum || 0 }}</span>
              <span class="label">动态</span>
            </div>
          </div>
        </div>

        <div class="followings-section">
          <h4>关注的人有更新</h4>
          <div
              v-for="user in followingsWithNewPosts"
              :key="user.userId"
              class="following-item"
              @click="goToUserPosts(user)"
          >
            <div class="avatar-wrapper">
              <el-avatar :size="30" :src="user.avatar" />
              <span v-if="user.hasNew" class="new-indicator"></span>
            </div>
            <span class="username">{{ user.username }}</span>
          </div>
        </div>
      </el-aside>

      <!-- 主内容区域 -->
      <el-main>
        <div class="main-content">
          <!-- 通过作用域插槽传递 userInfo -->
          <slot :userInfo="userInfo"></slot>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>

.el-main {
  background-image: url('../assets/login_backgroud.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
  height: 100vh;
}

.el-menu-item {
  padding-left: 50px;
  padding-right: 50px;
}

.community-page {
  background-color: #f4f5f7;
}

.header_container {
  width: 100%;
  position: fixed;
  top: 0;
  z-index: 1000;
  background-color: snow;
}

.grid-content {
  border-radius: 4px;
  min-height: 36px;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.left-sidebar {
  background-color: snow;
  padding-left: 20px;
  padding-right: 20px;
  padding-top: 90px;
  height: 100vh;
  position: fixed;
  overflow-y: auto;
  width: 250px;
}

.user-card {
  text-align: center;
  padding-bottom: 20px;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.user-card h3 {
  margin: 10px 0 5px;
  font-size: 16px;
}

.user-card p {
  color: #999;
  margin: 0;
  font-size: 14px;
}

.stats {
  display: flex;
  justify-content: space-around;
  margin-top: 15px;
}

.stat-item {
  text-align: center;
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.stat-item:hover {
  background-color: #f5f5f5;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
}

.stat-item:active {
  transform: translateY(0);
  background-color: #ebebeb;
}

.number {
  display: block;
  font-weight: bold;
  font-size: 16px;
}

.label {
  font-size: 12px;
  color: #999;
}

.followings-section h4 {
  font-size: 14px;
  margin-bottom: 10px;
}

.following-item {
  display: flex;
  align-items: center;
  padding: 5px 0;
  cursor: pointer;
  border-radius: 4px;
  transition: all 0.3s ease;
}

.following-item:hover {
  background-color: #f5f5f5;
  transform: translateX(4px);
}

.following-item:active {
  transform: translateX(0);
  background-color: #ebebeb;
}

.avatar-wrapper {
  position: relative;
  margin-right: 10px;
}

.new-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 8px;
  height: 8px;
  background-color: #fa2828;
  border-radius: 50%;
  border: 2px solid white;
}

.username {
  font-size: 14px;
  color: #666;
}

.main-content {
  margin-left: 220px;
  padding-top: 90px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.el-menu-item {
  padding-left: 50px;
  padding-right: 50px;
}

.component-center {
  margin-top: auto;
  margin-bottom: auto;
  margin-left: 20px;
  margin-right: 20px;
}
.item {
  margin-top: 10px;
  margin-right: 30px;
}
</style>
