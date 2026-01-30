<script setup>
import {ref, onMounted, reactive} from 'vue'
import CommunityLayout from '../../components/CommunityLayout.vue'
import { getFavoritePostInfoList } from '../../api/communityApi.js'
import { formatTime } from '../../utils/timeUtils.js'
import { JumpToUserProfile } from '../../utils/redirectUtils.js'
import router from '../../router/index.js'
import { useRoute } from "vue-router";

// 收藏的帖子列表
const favoritePosts = ref([])
const loading = ref(false)
const userId = useRoute().query.userId

onMounted(async () => {
  await loadFavoritePosts()
})

const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 加载收藏的帖子列表
const loadFavoritePosts = async () => {
  loading.value = true
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      userId: userId
    }
    // 修复：传入参数到API函数
    const response = await getFavoritePostInfoList(params)
    if (response.code === 200) {
      favoritePosts.value = response.data.list || []
      pagination.total = response.data.total
    }
  } catch (error) {
    console.error('加载收藏帖子失败:', error)
  } finally {
    loading.value = false
  }
}

// 跳转到帖子详情
const goToPostDetail = (postId) => {
  router.push({
    name: 'CommunityCenter',
    query: { postId: postId }
  })
}

// 页码改变
const handleCurrentChange = (val) => {
  pagination.pageNum = val
  loadFavoritePosts()
}

// 页大小改变
const handleSizeChange = (val) => {
  pagination.pageNum = 1
  pagination.pageSize = val
  loadFavoritePosts()
}
</script>

<template>
  <CommunityLayout active-menu="3" v-slot="{ userInfo }">
    <div class="favorite-posts-container">
      <h2>我的收藏</h2>

      <!-- 收藏帖子列表 -->
      <div v-if="!loading && favoritePosts.length > 0" class="posts-grid">
        <div
            v-for="post in favoritePosts"
            :key="post.postId"
            class="post-item"
            @click="goToPostDetail(post.postId)"
        >
          <div class="post-header">
            <h3 class="post-title">{{ post.title }}</h3>
            <div class="post-tags">
              <el-tag
                  v-for="tag in post.tags"
                  :key="tag.tagId"
                  size="small"
                  type="primary"
                  style="margin-right: 8px;"
              >
                {{ tag.tagName }}
              </el-tag>
            </div>
          </div>
          <div class="user-info">
            <el-text class="create-time">Upload By</el-text>
            <el-avatar :size="30" :src="post.postUserAvatar" style="margin-left: 10px" />
            <div class="user-details">
              <div class="create-time">{{ formatTime(post.createTime) }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <div v-else-if="!loading && favoritePosts.length === 0" class="empty-state">
        <el-empty description="暂无收藏的帖子" />
      </div>

      <!-- 加载状态 -->
      <div v-else class="loading-state">
        <el-skeleton :rows="4" animated />
      </div>

      <!-- 分页组件 -->
      <div v-if="pagination.total > 0" class="pagination-container">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pagination.pageNum"
            :page-sizes="[6, 10, 16, 20]"
            :page-size="pagination.pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total">
        </el-pagination>
      </div>
    </div>
  </CommunityLayout>
</template>

<style scoped>
.favorite-posts-container {
  width: 760px;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.favorite-posts-container h2 {
  margin-top: 0;
  margin-bottom: 20px;
  color: #333;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr); /* 一行两列 */
  gap: 15px;
  margin-bottom: 20px;
}

.post-item {
  padding: 15px;
  border: 1px solid #eee;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s ease;
  height: fit-content; /* 适应内容高度 */
}

.post-item:hover {
  border-color: #409eff;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  transform: translateY(-2px);
}

.post-item:active {
  transform: translateY(0);
}

.user-info {
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.user-details {
  margin-left: 10px;
}

.username {
  font-weight: bold;
  color: #409eff;
  cursor: pointer;
  transition: color 0.3s ease;
}

.username:hover {
  color: #66b1ff;
}

.create-time {
  font-size: 12px;
  color: #999;
}

.post-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.post-title {
  margin: 0 0 10px 0;
  font-size: 16px;
  font-weight: 600;
  color: #333;
  line-height: 1.4;
  cursor: pointer;
  transition: color 0.3s ease;
  word-break: break-all; /* 长标题换行 */
}

.post-title:hover {
  color: #409eff;
}

.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 5px;
  margin-bottom: 10px;
}

.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #999;
  font-size: 16px;
}

.loading-state {
  margin-top: 20px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>
