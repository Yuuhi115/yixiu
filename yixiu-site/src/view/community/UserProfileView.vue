<script setup>import {Message} from "@element-plus/icons-vue";
import {computed, onMounted, reactive, ref, nextTick} from "vue";
import Cookie from "js-cookie";
import {getUserInfo} from "../../api/userApi.js";
import {ElMessage} from "element-plus";
import router from "../../router/index.js";
import {useNotificationStore} from "../../stores/notificationInit.js";
import {getUnreadNotifyCount} from "../../api/notificationApi.js";
import {formatTime} from "../../utils/timeUtils.js";
import {
  getUserProfileByUserId
} from "../../api/communityApi.js";
import PostListTemplate from "../../components/PostListTemplate.vue";

const userInfoRef = ref()

const viewerInfo = ref({})  // 当前登录用户信息
const userProfile = ref({})  // 被访问的用户主页信息

onMounted(async () => {
  await queryViewerInfo()
  await getUnreadNotify()

  // 获取被访问用户的主页信息
  const visitedUserId = router.currentRoute.value.params.userId
  if (visitedUserId) {
    await loadUserProfile(visitedUserId)
  }
})

const currentUserId = computed(() => {  //被访问的用户id
  return userProfile.value.userInfoVO?.userId || '0'
})
const currentViewerId = computed(() => {  //当前用户id
  return viewerInfo.value.userId || '0'
})

const notificationStore = useNotificationStore()

// 使用计算属性自动响应状态变化
const unreadNotifyCount = computed(() => notificationStore.unreadCount)

const queryViewerInfo = async () => {
  const token = Cookie.get('Authorization')
  const response = await getUserInfo(token)
  if (response.code !== 200) {
    ElMessage.error(response.msg)
    return
  }
  // 将返回的用户信息赋值给 userInfo 对象
  Object.assign(viewerInfo.value, response.data)
}

const getUnreadNotify = async () => {
  const response = await getUnreadNotifyCount()
  if (response.code !== 200) {
    ElMessage.error(response.msg)
    return
  }
  unreadNotifyCount.value = response.data
}

// 加载用户主页数据
const loadUserProfile = async (userId) => {
  try {
    const response = await getUserProfileByUserId(userId)
    if (response.code === 200) {
      userProfile.value = response.data
    } else {
      ElMessage.error(response.msg)
    }
  } catch (error) {
    console.error('加载用户主页失败:', error)
    ElMessage.error('加载用户主页失败')
  }
}

// 关注/取消关注
const toggleFollow = async () => {
  // 实现关注/取消关注功能
  // 这里需要调用API
  userProfile.value.isFollow = !userProfile.value.isFollow
}

// 格式化联系类型
const getContactTypeText = (contactType) => {
  switch(contactType) {
    case 0: return '手机'
    case 1: return '邮箱'
    case 2: return '微信'
    case 3: return 'QQ'
    default: return '未知'
  }
}

// 格式化志愿者状态
const getStatusText = (status) => {
  switch(status) {
    case 0: return '离线'
    case 1: return '在线'
    case 2: return '忙碌'
    default: return '未知'
  }
}
</script>

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
                  default-active="1"
                  class="el-menu-demo"
                  mode="horizontal"                  style="border-bottom: silver solid 1px;background-color: snow"
                  :ellipsis="false"
              >
                <el-menu-item index="1" @click="() => router.push('/community')">社区主页</el-menu-item>
                <el-menu-item index="2" @click="() => router.push(`/community/followList/follow/${currentViewerId}`)">关注列表</el-menu-item>
                <el-menu-item index="3" @click="() => router.push('/taskCenter/list')">我的收藏</el-menu-item>
                <el-menu-item index="4">个人主页</el-menu-item>
              </el-menu>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <div class="component-center">
                <el-avatar :fit="'cover'" :src="viewerInfo.avatar"/>
              </div>
              <div class="component-center">
                <el-badge :is-dot="unreadNotifyCount > 0" class="item">
                  <el-button type="default" @click="() => router.push('/user/messageCenter')" :icon="Message" circle/>
                </el-badge>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-header>

      <!--主界面-->
      <el-container class="main-container">
        <!-- 左侧用户信息 -->
        <el-aside width="250px" class="user-info-aside">
          <div class="user-card">
            <div class="user-avatar">
              <el-avatar :size="100" :src="userProfile.userInfoVO?.avatar"/>
            </div>
            <div class="user-basic-info">
              <h3>{{ userProfile.userInfoVO?.username }}</h3>
              <p class="signature">{{ userProfile.userInfoVO?.userSignature || '这个人很神秘，什么都没有写...' }}</p>
              <div class="user-role">
                <el-tag size="small" :type="userProfile.role === 'admin' || userProfile.role === 'super_admin' ? 'warning' : 'info'">
                  {{ userProfile.role }}
                </el-tag>
              </div>
            </div>

            <div class="user-stats">
              <div class="stat-item">
                <div class="stat-value">{{ userProfile.communityStatisticDto?.postNum || 0 }}</div>
                <div class="stat-label">帖子</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ userProfile.communityStatisticDto?.followNum || 0 }}</div>
                <div class="stat-label">关注</div>
              </div>
              <div class="stat-item">
                <div class="stat-value">{{ userProfile.communityStatisticDto?.fansNum || 0 }}</div>
                <div class="stat-label">粉丝</div>
              </div>
            </div>

            <div class="visit-info">
              <div class="visit-count">访问量: {{ userProfile.visitedNum || 0 }}</div>
              <div class="last-login">最后登录: {{ userProfile.lastLoginTime || '未知' }}</div>
            </div>

            <div class="follow-section" v-if="currentViewerId !== currentUserId">
              <el-button
                  type="primary"
                  :class="{ 'is-followed': userProfile.isFollow }"
                  @click="toggleFollow"
              >
                {{ userProfile.isFollow ? '已关注' : '关注' }}
              </el-button>
            </div>

            <!-- 志愿者信息（仅当用户是志愿者时显示） -->
            <div class="volunteer-info" v-if="userProfile.volunteerDataVO">
              <h4>志愿者信息</h4>
              <div class="volunteer-detail">
                <p><strong>志愿者ID:</strong> {{ userProfile.volunteerDataVO.volunteerId }}</p>
                <p><strong>年级:</strong> {{ userProfile.volunteerDataVO.grade }}</p>
                <p><strong>状态:</strong>
                  <el-tag :type="userProfile.volunteerDataVO.status === 1 ? 'success' : userProfile.volunteerDataVO.status === 2 ? 'warning' : 'info'">
                    {{ getStatusText(userProfile.volunteerDataVO.status) }}
                  </el-tag>
                </p>
                <p><strong>维修数:</strong> {{ userProfile.volunteerDataVO.fixedNum }}</p>
                <p><strong>完成率:</strong> {{ (userProfile.volunteerDataVO.finishRate * 100).toFixed(2) }}%</p>
                <p><strong>联系方式:</strong> {{ getContactTypeText(userProfile.volunteerDataVO.contactType) }}: {{ userProfile.volunteerDataVO.contactNumber }}</p>
              </div>
            </div>
          </div>
        </el-aside>

        <!-- 中间帖子列表 -->
        <el-main class="post-main">
          <!-- 使用帖子列表组件，并传入用户ID作为过滤参数 -->
          <PostListTemplate v-if="currentUserId !== '0'"
          :initial-filters="{ postUserId: currentUserId }"
          :enable-tag-filter="true"
          :enable-search="true"
          />
        </el-main>

        <!-- 右侧社区数据 -->
        <el-aside width="250px" class="community-data-aside">
          <div class="data-card">
            <h4>社区数据</h4>
            <div class="data-item">
              <div class="data-label">获赞数</div>
              <div class="data-value">{{ userProfile.communityStatisticDto?.getLikeNum || 0 }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">帖子数</div>
              <div class="data-value">{{ userProfile.communityStatisticDto?.postNum || 0 }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">关注数</div>
              <div class="data-value">{{ userProfile.communityStatisticDto?.followNum || 0 }}</div>
            </div>
            <div class="data-item">
              <div class="data-label">粉丝数</div>
              <div class="data-value">{{ userProfile.communityStatisticDto?.fansNum || 0 }}</div>
            </div>

            <!-- 志愿者特定数据 -->
            <div class="volunteer-data" v-if="userProfile.volunteerDataVO">
              <h4>志愿者数据</h4>
              <div class="data-item">
                <div class="data-label">维修数</div>
                <div class="data-value">{{ userProfile.volunteerDataVO.fixedNum }}</div>
              </div>
              <div class="data-item">
                <div class="data-label">完成率</div>
                <div class="data-value">{{ (userProfile.volunteerDataVO.finishRate * 100).toFixed(2) }}%</div>
              </div>
            </div>
          </div>

          <div class="activity-card">
            <h4>近期活动</h4>
            <div class="activity-item">
              <div class="activity-desc">发布了新帖子</div>
              <div class="activity-time">{{ formatTime(userProfile.lastLoginTime) }}</div>
            </div>
          </div>
        </el-aside>
      </el-container>
    </el-container>
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

.main-container {
  height: calc(100vh - 60px);
}

.user-info-aside {
  background-color: snow;
  padding: 20px;
  border-right: 1px solid #eee;
}

.user-card {
  text-align: center;
}

.user-avatar {
  margin-bottom: 15px;
}

.user-basic-info h3 {
  margin: 10px 0 5px 0;
  font-size: 18px;
}

.signature {
  color: #666;
  font-size: 14px;
  margin-bottom: 10px;
  word-break: break-all;
}

.user-role {
  margin-bottom: 15px;
}

.user-stats {
  display: flex;
  justify-content: space-around;
  margin: 20px 0;
  padding: 15px 0;
  border-top: 1px solid #eee;
  border-bottom: 1px solid #eee;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #409EFF;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.visit-info {
  font-size: 12px;
  color: #999;
  margin-bottom: 15px;
}

.follow-section {
  margin-bottom: 20px;
}

.volunteer-info {
  text-align: left;
  padding-top: 15px;
  border-top: 1px solid #eee;
}

.volunteer-info h4 {
  margin-bottom: 10px;
}

.volunteer-detail p {
  margin: 5px 0;
  font-size: 13px;
}

.community-data-aside {
  background-color: snow;
  padding: 20px;
  border-left: 1px solid #eee;
}

.data-card, .activity-card {
  margin-bottom: 20px;
}

.data-card h4, .activity-card h4 {
  margin-bottom: 15px;
  padding-bottom: 8px;
  border-bottom: 1px solid #eee;
}

.data-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #f5f5f5;
}

.data-label {
  color: #666;
}

.data-value {
  font-weight: bold;
  color: #409EFF;
}

.activity-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  font-size: 13px;
}

.activity-desc {
  color: #333;
}

.activity-time {
  color: #999;
  font-size: 12px;
}

.post-main {
  background-color: transparent;
}

.section-nav-menu {
  display: flex;
  align-items: center;
  background-color: white;
  border-radius: 8px;
  padding: 10px 20px;
  margin-bottom: 10px;
  margin-left: auto;
  margin-right: auto;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .1);
  width: 760px;
}

.order-filter {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.post-list {
  width: 800px;
  margin-left: auto;
  margin-right: auto;
}

.post-item {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .1);
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.user-details .username {
  font-weight: bold;
  color: #409eff;
  margin-bottom: 2px;
}

.user-details .time {
  font-size: 12px;
  color: #999;
}

.images-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 8px;
  margin-bottom: 15px;
}

.post-image {
  width: 100%;
  height: 120px;
  border-radius: 4px;
  cursor: pointer;
}

.actions {
  display: flex;
  justify-content: space-around;
  gap: 20px;
  border-top: 1px solid #f5f5f5;
  padding-top: 15px;
}

.actions .el-button {
  width: 200px;
  color: #409EFF;
  border-radius: 10px;
  padding: 8px 16px;
  font-size: 20px;
}

.actions .el-button .el-icon {
  margin-right: 5px;
}

.actions .el-button:hover {
  background-color: #66b1ff;
  border-color: #66b1ff;
  color: white;
}

.content {
  margin-bottom: 15px;
  line-height: 1.4;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.content-text {
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;
  margin-bottom: 8px;
}

.content-text.expanded {
  -webkit-line-clamp: unset;
  overflow: visible;
  display: block;
}

.expand-btn {
  color: #409eff;
  cursor: pointer;
  font-size: 14px;
  margin-right: 10px;
}

.title_tags {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.no-posts {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 16px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, .1);
  margin: 0 auto 10px;
}

.loading-more {
  margin-top: 20px;
}

.user-details {
  padding-left: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.post-detail {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.post-detail .title {
  font-size: 16px;
  font-weight: bold;
}

.is-followed {
  background-color: #ecf5ff;
  color: #409eff;
  border-color: #b3d8ff;
}

/* 评论模块 */
.comment-section {
  margin-top: 15px;
  border-top: 1px solid #f0f0f0;
  background-color: #fafafa;
  border-radius: 8px;
  padding: 15px;
  overflow: hidden;
  max-height: 500px;
}

.comment-input-section {
  text-align: right;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.submit-comment-btn {
  text-align: right;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.comments-list {
  margin-top: 10px;
  min-height: 20px;
  transition: all 0.3s ease;
  max-height: 250px;
  overflow-y: auto;
  padding-right: 8px;
  padding-bottom: 5px; /* 添加底部内边距 */
  box-sizing: border-box; /* 确保内边距不影响尺寸计算 */
}

/* 滚动条样式优化 */
.comments-list::-webkit-scrollbar {
  width: 6px;
}

.comments-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.comments-list::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 10px;
}

.comments-list::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.comment-item {
  padding: 12px 0;
  border-bottom: 1px solid #eee;
  transition: all 0.3s ease;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-header {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.comment-user-info {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-left: 10px;
}

.comment-username {
  display: flex;
  align-items: center;
  font-weight: bold;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-content {
  display: flex;
  justify-content: flex-start;
  margin-left: 40px;
  color: #666;
  line-height: 1.5;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.no-comments {
  text-align: center;
  color: #999;
  padding: 10px 0;
}

.loading-comments {
  padding: 10px 0;
}

/* 回复模块 */
.replies-container {
  margin-left: 40px;
  margin-top: 10px;
  padding-left: 15px;
  border-left: 2px solid #eaeaea;
}

.reply-item {
  padding: 10px 0;
  border-bottom: 1px dashed #eee;
}

.reply-item:last-child {
  border-bottom: none;
}

.reply-header {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.reply-user-info {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-left: 10px;
}

.reply-username {
  display: flex;
  align-items: center;
  font-size: 14px;
}

.reply-time {
  font-size: 12px;
  color: #999;
}

.reply-content {
  text-align: left;
  margin-left: 34px;
  color: #666;
  line-height: 1.4;
  margin-bottom: 5px;
  font-size: 14px;
}

.reply-actions {
  margin-left: 34px;
  text-align: left;
  font-size: 12px;
}

.reply-actions .el-button {
  padding: 2px 4px;
  margin-right: 10px;
}

.loading-replies {
  margin-left: 34px;
  padding: 10px 0;
}
</style>
