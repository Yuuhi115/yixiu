<script setup>
import {ref, reactive, onMounted, computed} from 'vue'
import { useRoute } from 'vue-router'
import CommunityLayout from '../../components/CommunityLayout.vue'
// import { getFollowingListByUserId } from '@/api/communityApi.js' // 假设API存在
// import { unfollowUser as apiUnfollowUser } from '@/api/userApi.js' // 假设API存在
import { Search } from "@element-plus/icons-vue";
import {addFollow, cancelFollow, getFollowListByFilter, getUserInfoVO} from "../../api/userApi.js";
import {ElMessage} from "element-plus";
import { Plus } from "@element-plus/icons-vue";
import router from "../../router/index.js";
import {JumpToUserProfile} from "../../utils/redirectUtils.js";

// 获取路由参数
const route = useRoute()
const userId = ref(route.params.userId)
const statisticType = ref(route.params.type)


const otherUserInfo = ref({
  userId: 0,
  username: '',
  avatar: '',
  userSignature: '',
})

// 数据状态
const followingList = ref([])
const filteredFollowingList = ref([])
const filterType = ref('all')

const followListFilterForm = reactive({
  keyword: '',
  status: 1
})

const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0,
})

// 初始化
onMounted(() => {
  fetchOtherUserInfo()
  fetchFollowingList()
})

const fetchOtherUserInfo = async () => {
  try {
    const response = await getUserInfoVO(userId.value)
    if (response.code === 200) {
      otherUserInfo.value = response.data
      console.log('获取用户信息成功:', otherUserInfo.value)
    } else {
      console.error('获取用户信息失败:', response.msg)
    }
  } catch (error) {
    console.error('获取用户信息错误:', error)
  }
}
// 获取关注列表
const fetchFollowingList = async () => {
  try {
    const params = {
      page: pagination.currentPage,
      size: pagination.pageSize,
      keyword: followListFilterForm.keyword,
      status: followListFilterForm.status,
      fansOrFollow: statisticType.value
    }

    const response = await getFollowListByFilter(params)
    if (response.code === 200) {
      followingList.value = response.data.list || []
      pagination.total = response.data.total || 0
    } else {
      console.error('获取关注列表失败:', response.msg)
    }
  } catch (error) {
    console.error('获取关注列表错误:', error)
  }
}

// 搜索处理
const handleSearch = () => {
  followListFilterForm.keyword = followListFilterForm.keyword.trim()
  fetchFollowingList()
  followListFilterForm.keyword = ''
}

// 过滤处理
const handleFilterChange = () => {
  // 这里可以根据filterType重新请求数据或本地过滤
  fetchFollowingList()
}

const handleFansOrFollowChange = () => {
  followListFilterForm.fansOrFollow = statisticType.value
  router.push(`/community/followList/${statisticType.value}/${userId.value}`)
  fetchFollowingList()
}

// 分页处理
const handlePageChange = (page) => {
  pagination.currentPage = page
  fetchFollowingList()
}

// 取消关注
const unfollowUser = async (user) => {
  try {
    const response = await cancelFollow(user.followUserId)
    if (response.code === 200) {
      // 从列表中移除该用户
      user.status = 0
      ElMessage.success('已取消关注')
    } else {
      ElMessage.error(response.msg || '取消关注失败')
    }
  } catch (error) {
    console.error('取消关注错误:', error)
    ElMessage.error('操作失败，请重试')
  }
}

const followUser = async (user) => {
  try {
    const response = await addFollow(user.followUserId)
    if (response.code === 200) {
      user.status = 1
      ElMessage.success('已关注成功')
    } else {
      ElMessage.error(response.msg || '关注失败')
    }
  } catch (error) {
    console.error('关注错误:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 发送消息
const sendMessage = (targetUserId) => {
  router.push(`/message/chat/${targetUserId}`)
}


</script>

<template>
  <CommunityLayout :active-menu="'2'" v-slot="{ userInfo }">
    <div class="following-list-container">
      <div class="list-header">
        <h2 v-if="userInfo.userId === otherUserInfo.userId">
          {{ userInfo.username }} 的{{statisticType === 'follow' ? '关注' : '粉丝'}}列表
        </h2>
        <h2 v-else>
          {{ otherUserInfo.username }} 的{{statisticType === 'follow' ? '关注' : '粉丝'}}列表
        </h2>
        <el-input
            v-model="followListFilterForm.keyword"
            placeholder="搜索关注的用户..."
            style="width: 300px;"
            clearable
            @input="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <div class="filters">
        <el-radio-group v-model="statisticType" @change="handleFansOrFollowChange">
          <el-radio-button label="follow">查看关注</el-radio-button>
          <el-radio-button label="fans">查看粉丝</el-radio-button>
        </el-radio-group>

        <el-radio-group v-model="filterType" @change="handleFilterChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="recent">访问最多</el-radio-button>
        </el-radio-group>
      </div>

      <div class="users-grid">
        <el-card
            v-for="user in followingList"
            :key="user.followUserId"
            class="user-card"
            @click="JumpToUserProfile(user.followUserId)"
        >
          <div class="user-info">
            <el-avatar :size="60" :src="user.followUserAvatar" />
            <div class="user-details">
              <h4>{{ user.followUsername }}</h4>
              <p class="signature">{{ user.followUserSignature || '这个人很神秘，没有留下签名' }}</p>
            </div>
            <div class="actions">
              <template v-if="statisticType === 'follow'">
                <el-button v-if="user.status === 1" size="default" @click.stop="unfollowUser(user)">已关注</el-button>
                <el-button v-else size="default" type="primary" @click.stop="followUser(user)">
                  <el-icon><Plus /></el-icon>
                  关注
                </el-button>
              </template>
            </div>
          </div>

        </el-card>

        <!-- 无数据提示 -->
        <div v-if="followingList.length === 0" class="no-data">
          <el-empty description="暂无关注用户" />
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination">
        <el-pagination
            v-model:current-page="pagination.currentPage"
            v-model:page-size="pagination.pageSize"
            :total="pagination.total"
            layout="total, prev, pager, next, jumper"
            @current-change="handlePageChange"
        />
      </div>
    </div>
  </CommunityLayout>
</template>

<style scoped>
.following-list-container {
  width: 800px;
  margin-top: -30px;
  padding: 20px;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.filters {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.users-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
  margin-bottom: 20px;
  flex: 1;  /* 占据可用空间 */
}

.user-card {
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.user-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.user-info {
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.user-info .el-avatar {
  margin-right: 15px;
  flex-shrink: 0;  /* 防止头像被压缩 */
}

.user-details {
  flex: 1;                    /* 占据剩余空间 */
  min-width: 0;               /* 允许内容溢出隐藏 */
  overflow: hidden;
  display: flex;              /* 使用flex布局 */
  flex-direction: column;     /* 垂直排列 */
  justify-content: center;    /* 垂直居中对齐 */
  text-align: left;           /* 文本左对齐 */
}

.user-details h4 {
  margin: 0 0 0 0;
  font-size: 16px;
  align-self: flex-start;     /* 左对齐 */
}

.signature {
  margin: 5px 0 0 0;
  color: #999;
  font-size: 13px;
  line-height: 1.4;
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

.actions {
  display: flex;
  justify-content: space-between;
}

.actions .el-button {
  margin-left: 5px;
  margin-bottom: 10px;
}

.no-data {
  grid-column: 1 / -1;
  text-align: center;
  padding: 40px 0;
}

.pagination {
  display: flex;
  justify-content: center;
  margin-top: auto;  /* 自动推到底部 */
}

@media (max-width: 768px) {
  .list-header {
    flex-direction: column;
    align-items: stretch;
  }

  .users-grid {
    grid-template-columns: 1fr;
  }
}
</style>
