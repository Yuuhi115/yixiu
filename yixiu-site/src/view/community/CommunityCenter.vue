<script setup>
import {ref, reactive, onMounted, computed, nextTick} from 'vue'
import { getUserInfo } from '../../api/userApi.js'
import { Star, ChatRound, Position, StarFilled, Promotion} from "@element-plus/icons-vue";
import { ElCollapseTransition } from 'element-plus';
import {
  getPostList,
  getAllPostTags,
  uploadPost,
  uploadPostImage,
  getCommunityStatistics, getFollowingUpdates, addPostLike, addPostFavorite, getCommentListByPostId, addComment
} from '../../api/communityApi.js'
import Cookie from 'js-cookie'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from "../../router/index.js";
import {Message} from "@element-plus/icons-vue";
import {useNotificationStore} from "../../stores/notificationInit.js";
import {getUnreadNotifyCount} from "../../api/notificationApi.js";
import {formatTime} from "../../utils/timeUtils.js";
// 用户信息状态
const userInfo = reactive({
  userId: "",
  username: "",
  realName: "",
  phone: "",
  email: "",
  avatar: "",
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

// 加载用户信息
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

// 标签数据
const tags = ref([])
const selectedTag = ref('')
const postContent = ref('')
const postTitle = ref('')
const postImages = ref([])


// 动态列表
const postList = ref([])
const loading = ref(false)

// 模拟关注用户的动态提醒
const followingsWithNewPosts = ref([])

onMounted(async () => {
  await queryUserInfo()
  await loadAllTags()
  await queryCommunityStatistics()
  await loadFollowingUpdates()
  await loadPostList()
  await getUnreadNotify()
  await notificationStore.syncUnreadCount()
})

// 加载所有标签
const loadAllTags = async () => {
  const response = await getAllPostTags()
  if (response.code === 200) {
    tags.value = response.data
  }
}

const postPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 加载帖子列表
const loadPostList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: postPagination.currentPage,
      pageSize: postPagination.pageSize
    }
    const response = await getPostList(params)
    if (response.code === 200) {
      postList.value = response.data.list || []
      // 为每个帖子添加扩展状态
      postList.value.forEach(post => {
        post.expanded = false; // 默认不展开全部内容
        post.showComments = false  // 控制评论区显示
        post.comments = []         // 存储评论数据
        post.commentInput = ''     // 当前帖子的评论输入框
        post.loadingComments = false // 评论加载状态
      });
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

// 帖子评论分页参数
const commentPagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

const loadComments = async (post) => {
  try {
    let params = {
      postId: post.postId,
      pageNum: commentPagination.currentPage,
      pageSize: commentPagination.pageSize
    }
    const response = await getCommentListByPostId(params)
    if (response.code === 200) {
      post.comments = response.data.list || []
    } else {
      ElMessage.error(response.msg)
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    ElMessage.error('加载评论失败')
  } finally {
    post.loadingComments = false
  }
}

const goToComment = async (post) => {
  // 切换评论区显示状态
  if (post.showComments) {
    post.showComments = false
    return
  }

  // 显示评论区并加载评论
  post.showComments = true
  post.loadingComments = true
  await loadComments(post)
}

// 提交评论函数
const submitComment = async (post) => {
  if (!post.commentInput.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    const commentData = {
      postId: post.postId,
      content: post.commentInput,
    }

    const response = await addComment(commentData)
    if (response.code === 200) {
      ElMessage.success('评论成功')
      post.commentInput = ''
      // 重新加载评论
      await loadComments(post)

      // 更新帖子的评论数
      post.commentNum += 1
    } else {
      ElMessage.error(response.msg)
    }
  } catch (error) {
    console.error('评论失败:', error)
    ElMessage.error('评论失败')
  }
}

// 加载关注用户的更新提醒
const loadFollowingUpdates = async () => {
  const response = await getFollowingUpdates()
  if (response.code === 200) {
    followingsWithNewPosts.value = response.data.map(user => ({
      ...user,
      hasNew: true  // 为每个用户设置 hasNew 为 true
    }))
  }else {
    ElMessage.error(response.msg)
  }
}

// 发布帖子
const publishPostTemporary = async () => {
  ElMessage.info('API开发中')
}
const publishPost = async () => {
  if (!postContent.value.trim()) {
    ElMessage.warning('请输入帖子内容')
    return
  }

  try {
    const postData = {
      title: postTitle.value,
      content: postContent.value,
      tagIdList: selectedTag.value ? [parseInt(selectedTag.value)] : []
    }

    const response = await uploadPost(postData)
    if (response.code === 200) {
      ElMessage.success('发布成功')
      postTitle.value = ''
      postContent.value = ''
      selectedTag.value = ''
      // 重新加载列表
      await loadPostList()
    } else {
      ElMessage.error(response.msg || '发布失败')
    }
  } catch (error) {
    console.error('发布失败:', error)
    ElMessage.error('发布失败')
  }
}

// 处理图片上传
const handleImageUpload = (event) => {
  const files = event.target.files
  for (let i = 0; i < files.length; i++) {
    postImages.value.push(files[i])
  }
}

// 检查是否应该显示展开按钮
const shouldShowExpandButton = (post) => {
  if (!post.content) return false;
  const lines = post.content.split('\n').length + Math.floor(post.content.length / 50); // 粗略估算行数
  return lines > 2;
};

// 切换展开/收起状态
const toggleExpand = (post) => {
  post.expanded = !post.expanded;
};
const logout = async () => {
  Cookie.remove('Authorization')
  await router.push('/login')
}

const toggleLike = async (post) => {
  if (post.isLiked === 0){
    post.likeNum += 1
    post.isLiked = 1
  }else {
    post.likeNum -= 1
    post.isLiked = 0
  }
  const response = await addPostLike(post.postId)
  if (response.code !== 200) {
    ElMessage.error(response.msg)
  }
}

const toggleFavorite = async (post) => {
  if (post.isFavorite === 0){
    post.favoriteNum += 1
    post.isFavorite = 1
  }else {
    post.favoriteNum -= 1
    post.isFavorite = 0
  }
  const response = await addPostFavorite(post.postId)
  if (response.code !== 200) {
    ElMessage.error(response.msg)
  }
}
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

    <el-container class="main-container">
      <!-- 左侧用户信息栏 -->
      <el-aside width="200px" class="left-sidebar">
        <div class="user-card">
          <el-avatar :size="60" :src="userInfo.avatar" />
          <h3>{{ userInfo.username }}</h3>
          <p>{{ userInfo.realName }}</p>

          <div class="stats">
            <div class="stat-item">
              <span class="number">{{ communityStatistics.followNum }}</span>
              <span class="label">关注</span>
            </div>
            <div class="stat-item">
              <span class="number">{{ communityStatistics.fansNum }}</span>
              <span class="label">粉丝</span>
            </div>
            <div class="stat-item">
              <span class="number">{{ communityStatistics.postNum }}</span>
              <span class="label">动态</span>
            </div>
          </div>
        </div>

        <div class="followings-section">
          <h4>关注的人有更新</h4>
          <div
              v-for="user in followingsWithNewPosts"
              :key="user.id"
              class="following-item"
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
          <!-- 发布动态栏 -->
          <div class="publish-box">
            <div class="user-info">
              <el-avatar :size="40" :src="userInfo.avatar" />
              <div class="input-area">
                <el-input
                    style="margin-bottom: 10px;"
                    v-model="postTitle"
                    placeholder="请输入标题..."
                    maxlength="50"
                    show-word-limit
                />
                <el-input
                    v-model="postContent"
                    :rows="3"
                    type="textarea"
                    placeholder="分享新鲜事..."
                    maxlength="500"
                    show-word-limit
                />

                <div class="publish-options">
                  <el-select
                      v-model="selectedTag"
                      placeholder="选择标签"
                      size="small"
                      style="width: 150px; margin-right: 10px;"
                  >
                    <el-option
                        v-for="tag in tags"
                        :key="tag.tagId"
                        :label="tag.tagName"
                        :value="tag.tagId.toString()"
                    />
                  </el-select>

                  <el-upload
                      class="upload-btn"
                      action="#"
                      :auto-upload="false"
                      :show-file-list="false"
                      :on-change="handleImageUpload"
                      multiple
                  >
                    <el-button type="primary" size="small" plain>上传图片</el-button>
                  </el-upload>

                  <el-button type="primary" size="small" @click="publishPostTemporary">发布</el-button>
                </div>
              </div>
            </div>
          </div>

          <!-- 分区导航 -->
          <div class="section-nav">
            <el-tabs v-model="selectedTag" type="card">
              <el-tab-pane label="全部" name=""></el-tab-pane>
              <el-tab-pane
                  v-for="tag in tags.slice(0, 6)"
                  :key="tag.tagId"
                  :label="tag.tagName"
                  :name="tag.tagName"
              ></el-tab-pane>
            </el-tabs>
          </div>

          <!-- 动态列表 -->
          <div class="post-list">
            <div
                v-for="(post, index) in postList"
                :key="index"
                class="post-item"
            >
              <div class="user-header">
                <el-avatar :size="40" :src="post.avatar" />
                <div class="user-details">
                  <div class="username">{{ post.username }}</div>
                  <div class="time">{{ formatTime(post.createTime) }}</div>
                </div>
              </div>

              <div class="post-detail">
                <div class="title_tags">
                  <div class="title">
                    <p>{{ post.title }}</p>
                  </div>
                  <div class="tags">
                    <el-tag
                        v-for="tag in post.tags"
                        :key="tag.tagId"
                        size="small"
                        type="primary"
                        style="margin-right: 10px;"
                    >
                      {{ tag.tagName }}
                    </el-tag>
                  </div>
                </div>
                <div class="content">
                  <div
                      class="content-text"
                      :class="{ expanded: post.expanded }"
                      :style="post.expanded ? {} : {'-webkit-line-clamp': 2}"
                  >
                    <p align="left">{{ post.content }}</p>
                  </div>
                  <div
                      v-if="shouldShowExpandButton(post)"
                      class="expand-btn"
                      @click="toggleExpand(post)"
                  >
                    {{ post.expanded ? '收起' : '展开' }}
                  </div>
                </div>
              </div>

              <!-- 图片展示 -->
              <div v-if="post.imgUrls && post.imgUrls.length" class="images-grid">
                <el-image
                    v-for="(img, imgIndex) in post.imgUrls.slice(0, 9)"
                    :key="imgIndex"
                    :src="img"
                    fit="cover"
                    class="post-image"
                    :preview-src-list="post.imgUrls.map(i => i)"
                    :initial-index="imgIndex"
                />
              </div>

              <!-- 操作按钮 -->
              <div class="actions">
                <el-button type="text" size="large" @click="toggleFavorite(post)">
                  <el-icon v-if="post.isFavorite === 0"><Star /></el-icon>
                  <el-icon v-else><StarFilled /></el-icon>
                  {{ post.favoriteNum || 0 }}
                </el-button>
                <el-button type="text" size="large" @click="goToComment(post)">
                  <el-icon><ChatRound /></el-icon>
                  {{ post.commentNum || 0 }}
                </el-button>
                <el-button type="text" size="large"  @click="toggleLike(post)">
                  <el-icon v-if="post.isLiked === 0"><Position /></el-icon>
                  <el-icon v-else><Promotion /></el-icon>
                  {{ post.likeNum || 0 }}
                </el-button>
              </div>

              <!-- 评论区 -->
              <el-collapse-transition>
                <div v-if="post.showComments" class="comment-section">
                  <!-- 评论输入框 -->
                  <div class="comment-input-section">
                    <el-input
                        v-model="post.commentInput"
                        :rows="3"
                        type="textarea"
                        placeholder="请输入评论..."
                        maxlength="200"
                        show-word-limit
                    />
                    <div class="submit-comment-btn">
                      <el-button type="primary" @click="submitComment(post)">发表评论</el-button>
                    </div>
                  </div>

                  <!-- 评论列表 -->
                  <div class="comments-list">
                    <div v-if="post.loadingComments" class="loading-comments">
                      <el-skeleton :rows="3" animated />
                    </div>
                    <div
                        v-else-if="post.comments.length === 0"
                        class="no-comments"
                    >
                      暂无评论
                    </div>
                    <div
                        v-else
                        v-for="comment in post.comments"
                        :key="comment.commentId"
                        class="comment-item"
                    >
                      <div class="comment-header">
                        <el-avatar :size="30" :src="comment.avatar" />
                        <div class="comment-user-info">
                          <div class="comment-username">
                            <span style="margin-right: 10px">{{ comment.username }}</span>
                            <el-tag v-if="comment.userId === post.userId" size="small" type="success">作者</el-tag>
                          </div>
                          <div class="comment-time">{{ formatTime(comment.createTime) }}</div>
                        </div>
                      </div>
                      <div class="comment-content">{{ comment.content }}</div>
                      <div class="comment-actions">
                        <el-button type="text" size="small" @click="toggleLikeComment(comment)">
                          <el-icon v-if="comment.isLike === 0"><Position /></el-icon>
                          <el-icon v-else><Promotion /></el-icon>
                          {{ comment.likeNum || 0 }}
                        </el-button>
                        <el-button type="text" size="small">回复</el-button>
                      </div>
                    </div>
                  </div>
                </div>
              </el-collapse-transition>
            </div>
            <!-- 加载更多 -->
            <div v-if="loading" class="loading-more">
              <el-skeleton :rows="4" animated />
            </div>
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.el-main {
  background-image: url('../../assets/login_backgroud.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  background-attachment: fixed;
}
.main-content{
  display: flex;
  flex-direction: column;
  align-items: center;
}
.header_container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 1000;
  background-color: snow;
  //box-shadow: 0 2px 8px rgba(0,0,0,.1);
}
.community-page {
  min-height: 100vh;
  background-color: #f4f5f7;
  //background-image: url('../../assets/login_backgroud.png');
  padding-top: 60px; /* 高度根据实际头部栏高度调整 */
}

.grid-content {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}

.left-sidebar {
  background-color: snow;
  padding: 20px;
  //border-right: 1px solid #eee;
  height: calc(100vh - 60px);
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
  padding: 20px;
}

.publish-box {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 10px;
  margin-top: -30px;
  box-shadow: 0 2px 8px rgba(0,0,0,.1);
  width: 760px;
}

.user-info {
  display: flex;
  gap: 15px;
}

.input-area {
  flex: 1;
}

.publish-options {
  display: flex;
  justify-content: flex-end;
  margin-top: 10px;
  gap: 10px;
}

.section-nav {
  background-color: white;
  border-radius: 8px;
  padding: 10px 20px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,.1);
  width: 760px;
}

.post-list {
  width: 800px;
  margin: 0 auto;
}

.post-item {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,.1);
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
  border-radius: 10px;  /* 圆角按钮 */
  padding: 8px 16px;    /* 调整内边距 */
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

/* 当按钮处于激活状态时的样式 */
.actions .el-button.is-active {
  background-color: #3a8ee6;
  border-color: #3a8ee6;
}

.loading-more {
  margin-top: 20px;
}

.user-details{
  padding-left: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.post-detail{
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.post-detail .title{
  font-size: 16px;
  font-weight: bold;
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
.title_tags{
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
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
  padding-bottom: 5px;  /* 添加底部内边距 */
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
  text-align: right;
}

.no-comments {
  text-align: center;
  color: #999;
  padding: 10px 0;
}

.loading-comments {
  padding: 10px 0;
}

</style>
