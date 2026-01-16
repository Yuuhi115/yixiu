<script setup>
import {ref, reactive, onMounted, computed} from 'vue'
import { getUserInfo } from '../../api/userApi.js'
import { getPostList, getAllPostTags, uploadPost, uploadPostImage } from '../../api/communityApi.js'
import Cookie from 'js-cookie'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from "../../router/index.js";
import {Message} from "@element-plus/icons-vue";
import {useNotificationStore} from "../../stores/notificationInit.js";
import {getUnreadNotifyCount} from "../../api/notificationApi.js";
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
  followerCount: 0,
  followingCount: 8,
  postCount: 12
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
const postImages = ref([])

// 动态列表
const dynamicList = ref([])
const loading = ref(false)

// 模拟关注用户的动态提醒
const followingsWithNewPosts = ref([
  { id: 1, name: '小明同学', avatar: 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png', hasNew: true },
  { id: 2, name: '科技达人', avatar: 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png', hasNew: false },
  { id: 3, name: '生活分享家', avatar: 'https://cube.elemecdn.com/9/c2/f6ee8a3c53e1ef722c2f4c348b43epng.png', hasNew: true }
])

onMounted(async () => {
  await queryUserInfo()
  await loadAllTags()
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

// 加载帖子列表
const loadPostList = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: 1,
      pageSize: 20
    }
    const response = await getPostList(params)
    if (response.code === 200) {
      dynamicList.value = response.data.list || []
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    ElMessage.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

// 发布帖子
const publishPost = async () => {
  if (!postContent.value.trim()) {
    ElMessage.warning('请输入帖子内容')
    return
  }

  try {
    const postData = {
      title: '',
      content: postContent.value,
      tagIdList: selectedTag.value ? [parseInt(selectedTag.value)] : []
    }

    const response = await uploadPost(postData)
    if (response.code === 200) {
      ElMessage.success('发布成功')
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
const logout = async () => {
  Cookie.remove('Authorization')
  await router.push('/login')
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
              <span class="number">{{ userInfo.followingCount }}</span>
              <span class="label">关注</span>
            </div>
            <div class="stat-item">
              <span class="number">{{ userInfo.followerCount }}</span>
              <span class="label">粉丝</span>
            </div>
            <div class="stat-item">
              <span class="number">{{ userInfo.postCount }}</span>
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
            <span class="username">{{ user.name }}</span>
          </div>
        </div>
      </el-aside>

      <!-- 主内容区域 -->
      <el-main class="main-content">
        <!-- 发布动态栏 -->
        <div class="publish-box">
          <div class="user-info">
            <el-avatar :size="40" :src="userInfo.avatar" />
            <div class="input-area">
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

                <el-button type="primary" size="small" @click="publishPost">发布</el-button>
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
        <div class="dynamic-list">
          <div
              v-for="(post, index) in dynamicList"
              :key="index"
              class="dynamic-item"
          >
            <div class="user-header">
              <el-avatar :size="40" :src="post.userAvatar || userInfo.avatar" />
              <div class="user-details">
                <div class="username">{{ post.userName || userInfo.username }}</div>
                <div class="time">{{ post.createTime || '刚刚' }}</div>
              </div>
            </div>

            <div class="content">
              <p>{{ post.content }}</p>
            </div>

            <!-- 图片展示 -->
            <div v-if="post.images && post.images.length" class="images-grid">
              <el-image
                  v-for="(img, imgIndex) in post.images.slice(0, 9)"
                  :key="imgIndex"
                  :src="img.url"
                  fit="cover"
                  class="post-image"
                  :preview-src-list="post.images.map(i => i.url)"
                  :initial-index="imgIndex"
              />
            </div>

            <!-- 操作按钮 -->
            <div class="actions">
              <el-button type="text" size="small" icon="Star">
                {{ post.favoriteCount || 0 }}
              </el-button>
              <el-button type="text" size="small" icon="ChatLineRound">
                {{ post.commentCount || 0 }}
              </el-button>
              <el-button type="text" size="small" icon="Position">
                {{ post.likeCount || 0 }}
              </el-button>
            </div>
          </div>

          <!-- 加载更多 -->
          <div v-if="loading" class="loading-more">
            <el-skeleton :rows="4" animated />
          </div>
        </div>
      </el-main>
    </el-container>
  </div>
</template>

<style scoped>
.community-page {
  min-height: 100vh;
  background-color: #f4f5f7;
}

.grid-content {
  border-radius: 4px;
  min-height: 36px;
  height: 100%;
  display: flex;
  justify-content: center;
}


.left-sidebar {
  background-color: white;
  padding: 20px;
  border-right: 1px solid #eee;
  height: calc(100vh - 60px);
  position: fixed;
  overflow-y: auto;
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
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,.1);
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
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,.1);
}

.dynamic-list {
  max-width: 800px;
  margin: 0 auto;
}

.dynamic-item {
  background-color: white;
  border-radius: 8px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0,0,0,.1);
}

.user-header {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.user-details .username {
  font-weight: bold;
  margin-bottom: 2px;
}

.user-details .time {
  font-size: 12px;
  color: #999;
}

.content {
  margin-bottom: 15px;
  line-height: 1.6;
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
  gap: 20px;
  border-top: 1px solid #f5f5f5;
  padding-top: 15px;
}

.loading-more {
  margin-top: 20px;
}
</style>
