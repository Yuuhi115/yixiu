NEW_FILE_CODE
<script setup>
import {Message, Edit, Search, Download, InfoFilled, Delete} from "@element-plus/icons-vue";
import {onMounted, reactive, ref, computed} from "vue";
import Cookie from "js-cookie";
import {getUserInfo, getUsersByFilter} from "../../api/userApi.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "../../router/index.js";
import {AcceptSuperAdmin} from "../../utils/roleCheckUtils.js";
import {formatTime} from "../../utils/timeUtils.js";
import {JumpToUserProfile} from "../../utils/redirectUtils.js";
import {deleteComment, deleteReply, getUserCommentList, getUserReplyList} from "../../api/communityApi.js";
// import {exportUserStatistics} from "../../api/exportApi.js";

const userInfoRef = ref()

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
})

// 数据相关
const userList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const userFormRef = ref()

// 评论对话框相关
const commentDialogVisible = ref(false)
const commentList = ref([])
const commentLoading = ref(false)
const currentCommentUserId = ref('')

// 回复对话框相关
const replyDialogVisible = ref(false)
const replyList = ref([])
const replyLoading = ref(false)
const currentReplyUserId = ref('')

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 评论分页
const commentPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 回复分页
const replyPagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 表单数据
const userForm = reactive({
  userId: '',
  status: ''
})

// 状态选项
const statusOptions = [
  {label: '正常', value: 1},
  {label: '冻结中', value: 2},
  {label: '禁言中', value: 3}
]

// 排序选项
const sortOptions = [
  { label: '默认', value: 1},
  { label: '最后登录时间', value: 2},
  { label: '获赞数', value: 3},
  { label: '发贴数', value: 4},
  { label: '评论数', value: 5},
  { label: '回复数', value: 6}
]
// 用户角色选项
const roleOptions = [
    { label: '学生', value: 'student'},
    { label: '志愿者', value: 'volunteer'},
    { label: '管理员', value: 'admin'}
]

// 排序顺序选项
const sortOrderOptions = [
  { label: '升序', value: 1},
  { label: '降序', value: 2}
]

// 评论排序选项
const commentSortOptions = [
  { label: '创建日期', value: 1},
  { label: '回复数', value: 2},
  { label: '点赞数', value: 3}
]

// 回复排序选项
const replySortOptions = [
  { label: '创建日期', value: 1},
  { label: '回复数', value: 2},
  { label: '点赞数', value: 3}
]

// 在现有数据属性中添加筛选和排序相关属性
const filterForm = reactive({
  status: 1,
  sortBy: 1,
  sortOrder: 1,
  searchName: '',
  role: 'student'
})

// 评论筛选条件
const commentFilterForm = reactive({
  sortBy: 1,
  sortOrder: 2
})

// 回复筛选条件
const replyFilterForm = reactive({
  sortBy: 1,
  sortOrder: 2
})

// 编辑用户状态
const handleEdit = (row) => {
  userForm.userId = row.userId
  userForm.status = row.status
  dialogVisible.value = true
}

// 提交表单
const submitForm = () => {
  userFormRef.value.validate(async (valid) => {
    if (!AcceptSuperAdmin(userInfo)) {
      ElMessage.error('无操作权限')
      return
    }
    if (valid) {
      submitLoading.value = true
      try {
        const response = await modifyUserStatus(userForm.userId, userForm.status)
        if (response.code === 200) {
          ElMessage.success('修改成功')
          dialogVisible.value = false
          await loadUsersConditions()
        } else {
          ElMessage.error(response.msg || '修改失败')
        }
      } catch (error) {
        ElMessage.error('修改失败：' + error.message)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (userFormRef.value) {
    userFormRef.value.resetFields()
  }
  userForm.userId = ''
  userForm.status = ''
}

// 处理评论点击
const handleCommentClick = async (row) => {
  currentCommentUserId.value = row.userId
  commentPagination.currentPage = 1
  commentFilterForm.sortBy = 1
  commentFilterForm.sortOrder = 2
  await loadCommentList()
  commentDialogVisible.value = true
}

// 加载评论列表
const loadCommentList = async () => {
  commentLoading.value = true
  try {
    const params = {
      userId: currentCommentUserId.value,
      pageNum: commentPagination.currentPage,
      pageSize: commentPagination.pageSize,
      status: 0,
      sortBy: commentFilterForm.sortBy,
      sortOrder: commentFilterForm.sortOrder
    }

    const response = await getUserCommentList(params)
    if (response.code === 200) {
      commentList.value = response.data.list
      commentPagination.total = response.data.total
    } else {
      ElMessage.error(response.msg || '加载评论失败')
    }
  } catch (error) {
    ElMessage.error('加载评论失败：' + error.message)
  } finally {
    commentLoading.value = false
  }
}

// 评论分页大小变化
const handleCommentSizeChange = async (val) => {
  commentPagination.pageSize = val
  commentPagination.currentPage = 1
  await loadCommentList()
}

// 评论页码变化
const handleCommentCurrentChange = async (val) => {
  commentPagination.currentPage = val
  await loadCommentList()
}

// 评论筛选
const handleCommentFilter = async () => {
  commentPagination.currentPage = 1
  await loadCommentList()
}

// 跳转到评论对应的帖子
const jumpToCommentPost = (postId, commentId) => {
  router.push(`/community?postId=${postId}&commentId=${commentId}`)
  commentDialogVisible.value = false
}

// 重置评论表单
const resetCommentForm = () => {
  commentList.value = []
  commentPagination.currentPage = 1
  commentPagination.total = 0
}

// 处理回复点击
const handleReplyClick = async (row) => {
  currentReplyUserId.value = row.userId
  replyPagination.currentPage = 1
  replyFilterForm.sortBy = 1
  replyFilterForm.sortOrder = 2
  await loadReplyList()
  replyDialogVisible.value = true
}

// 加载回复列表
const loadReplyList = async () => {
  replyLoading.value = true
  try {
    const params = {
      userId: currentReplyUserId.value,
      pageNum: replyPagination.currentPage,
      pageSize: replyPagination.pageSize,
      status: 0,
      sortBy: replyFilterForm.sortBy,
      sortOrder: replyFilterForm.sortOrder
    }
    const response = await getUserReplyList(params)
    if (response.code === 200) {
      replyList.value = response.data.list
      replyPagination.total = response.data.total
    } else {
      ElMessage.error(response.msg || '加载回复失败')
    }
  } catch (error) {
    ElMessage.error('加载回复失败：' + error.message)
  } finally {
    replyLoading.value = false
  }
}

// 回复分页大小变化
const handleReplySizeChange = async (val) => {
  replyPagination.pageSize = val
  replyPagination.currentPage = 1
  await loadReplyList()
}

// 回复页码变化
const handleReplyCurrentChange = async (val) => {
  replyPagination.currentPage = val
  await loadReplyList()
}

// 回复筛选
const handleReplyFilter = async () => {
  replyPagination.currentPage = 1
  await loadReplyList()
}

// 跳转到回复对应的帖子
const jumpToReplyPost = (postId, commentId, replyId) => {
  router.push(`/community?postId=${postId}&commentId=${commentId}&replyId=${replyId}`)
  replyDialogVisible.value = false
}

// 重置回复表单
const resetReplyForm = () => {
  replyList.value = []
  replyPagination.currentPage = 1
  replyPagination.total = 0
}

// 处理用户筛选
const handleFilter = async () => {
  const queryParams = buildQueryParams()
  queryParams.pageNum = pagination.currentPage
  queryParams.pageSize = pagination.pageSize

  const response = await getUsersByFilter(queryParams)

  if (response.code === 200) {
    userList.value = response.data.list
    pagination.total = response.data.total
    console.log(userList.value)
  } else {
    ElMessage.error(response.msg)
  }
}

// 构建筛选条件
const buildQueryParams = () => {
  const params = {}

  // 状态条件
  if (filterForm.status !== '' && filterForm.status != null) {
    params.status = filterForm.status
  }

  // 排序条件
  if (filterForm.sortBy !== '' && filterForm.sortBy != null) {
    params.sortBy = filterForm.sortBy
  }
  if (filterForm.sortOrder !== '' && filterForm.sortOrder != null) {
    params.sortOrder = filterForm.sortOrder
  }
  if (filterForm.searchName !== '' && filterForm.searchName != null) {
    params.searchName = filterForm.searchName
  }
  if (filterForm.role !== '' && filterForm.role != null){
    params.role = filterForm.role
  }

  return params
}

// 重置筛选
const resetFilter = () => {
  filterForm.status = 1
  filterForm.sortBy = 1
  filterForm.sortOrder = 1
  pagination.currentPage = 1
  loadUsersConditions()
}

// 分页相关
const handleSizeChange = async (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  await loadUsersConditions()
}

const handleCurrentChange = async (val) => {
  pagination.currentPage = val
  await loadUsersConditions()
}

const loadUsersConditions = async () => {
  await handleFilter()
}

onMounted(async () => {
  await queryUserInfo()
  await loadUsersConditions()
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

// 获取状态标签文本
const getStatusLabel = (status) => {
  const statusMap = {
    0: '封禁',
    1: '正常'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 0:
      return 'danger'  // 封禁
    case 1:
      return 'success'  // 正常
    default:
      return 'info'
  }
}

//删除评论
const deleteUserComment = async (commentId) => {
  ElMessageBox.confirm('确定要删除此评论吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const response = await deleteComment(commentId)
    if (response.code === 200) {
      await loadCommentList()
    } else {
      ElMessage.error(response.msg)
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

const deleteUserReply = async (replyId) => {
  ElMessageBox.confirm('确定要删除此回复吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    const response = await deleteReply(replyId)
    if (response.code === 200) {
      await loadReplyList()
    } else {
      ElMessage.error(response.msg)
    }
  }).catch(() => {
    ElMessage.info('已取消删除')
  })
}

</script>

<template>
  <div class="common-layout">
    <el-container style="height: 100%">
      <el-header class="header_container">
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <h3 class="clickable-title" style="margin-right: 100px" @click="() => router.push('/')">Light 义修帮</h3>
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
                <el-menu-item index="1">用户管理</el-menu-item>
                <el-menu-item index="2" @click="() => router.push('/admin/aiRepository')">知识库管理</el-menu-item>
              </el-menu>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <div class="component-center">
                <el-avatar :fit="'cover'" :src="userInfo.avatar"/>
              </div>
              <div class="component-center">
                <el-badge :is-dot="true" class="item">
                  <el-button type="default" :icon="Message" circle/>
                </el-badge>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-header>

      <!--主界面-->
      <el-main>
        <div class="main-content">
          <!-- 筛选面板 -->
          <el-card class="filter-card">
            <template #header>
              <div class="card-header">
                <span>筛选与排序</span>
              </div>
            </template>

            <el-form :model="filterForm" label-width="100px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="状态">
                    <el-select v-model="filterForm.status" placeholder="请选择状态" clearable style="width: 100%">
                      <el-option
                          v-for="item in statusOptions"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="排序方式">
                    <el-select v-model="filterForm.sortBy" placeholder="请选择排序方式" style="width: 100%">
                      <el-option
                          v-for="option in sortOptions"
                          :key="option.value"
                          :label="option.label"
                          :value="option.value">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="排序顺序">
                    <el-select v-model="filterForm.sortOrder" style="width: 100%">
                      <el-option
                        v-for="option in sortOrderOptions"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="身份">
                    <el-select v-model="filterForm.role" placeholder="请选择身份" style="width: 100%">
                      <el-option
                          v-for="item in roleOptions"
                          :key="item.value"
                          :label="item.label"
                          :value="item.value">
                      </el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label=" " label-width="100px">
                    <el-button type="primary" @click="handleFilter">查询</el-button>
                    <el-button @click="resetFilter">重置</el-button>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>

          <!-- 操作栏 -->
          <div class="toolbar">
            <el-input
                v-model="filterForm.searchName"
                placeholder="搜索用户名或姓名"
                style="width: 300px;"
                clearable
                @keyup.enter="loadUsersConditions"
            >
              <template #append>
                <el-button @click="loadUsersConditions">
                  <el-icon>
                    <Search/>
                  </el-icon>
                </el-button>
              </template>
            </el-input>
          </div>

          <!-- 用户表格 -->
          <el-table
              :data="userList"
              v-loading="loading"
              style="width: 100%; margin-top: 20px; height: 350px"
              stripe
              border
              max-height="400"
          >
            <el-table-column prop="userId" label="用户 ID" width="100">
              <template #default="scope">
                <el-text type="info">
                  {{ scope.row.userId }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="头像" width="80">
              <template #default="scope">
                <el-avatar :size="40" :src="scope.row.avatar"/>
              </template>
            </el-table-column>
            <el-table-column prop="username" label="用户名" width="120">
              <template #default="scope">
                <el-text type="info">
                  {{ scope.row.username }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column prop="postCount" label="发帖数" width="100">
              <template #default="scope">
                <el-text type="info">
                  {{ scope.row.postCount }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column prop="getLikeCount" label="点赞数" width="100">
              <template #default="scope">
                <el-text type="info">
                  {{ scope.row.getLikeCount }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column prop="commentCount" label="评论数" width="100">
              <template #default="scope">
                <el-link
                    type="info"
                    :underline="false"
                    @click="handleCommentClick(scope.row)"
                    style="cursor: pointer;font-weight: bold"
                >
                  {{ scope.row.commentCount }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="replyCount" label="回复数" width="100">
              <template #default="scope">
                <el-link
                    type="info"
                    :underline="false"
                    @click="handleReplyClick(scope.row)"
                    style="cursor: pointer;font-weight: bold"
                >
                  {{ scope.row.replyCount }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="email" label="邮箱" width="180">
              <template #default="scope">
                <el-text type="info">
                  {{ scope.row.email }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)">
                  {{ getStatusLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>

            <el-table-column prop="realName" label="真实姓名" width="120">
              <template #default="scope">
                <el-text type="info">
                  {{ scope.row.realName }}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column prop="lastLogin" label="最后登录" width="180">
              <template #default="scope">
                <el-text type="info">
                  {{ formatTime( scope.row.lastLogin)}}
                </el-text>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button
                    type="primary"
                    :icon="Edit"
                    circle
                    @click="handleEdit(scope.row)"
                    title="修改状态"
                />
                <el-button
                    type="primary"
                    :icon="InfoFilled"
                    circle
                    @click="JumpToUserProfile(scope.row.userId)"
                    title="前往用户中心"
                    style="margin-left: 10px;"
                />
              </template>
            </el-table-column>
          </el-table>

          <!-- 分页 -->
          <el-pagination
              v-model:current-page="pagination.currentPage"
              v-model:page-size="pagination.pageSize"
              :page-sizes="[10, 20, 50, 100]"
              :total="pagination.total"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              style="margin-top: 20px; justify-content: center;"
          />

          <!-- 用户状态编辑对话框 -->
          <el-dialog
              v-model="dialogVisible"
              title="修改用户状态"
              width="500px"
              @close="resetForm"
          >
            <el-form
                ref="userFormRef"
                :model="userForm"
                label-width="80px"
            >
              <el-form-item label="用户 ID">
                <el-input v-model="userForm.userId" disabled/>
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="userForm.status" style="width: 100%;">
                  <el-option
                      v-for="option in statusOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                  />
                </el-select>
              </el-form-item>
            </el-form>
            <template #footer>
              <span class="dialog-footer">
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="submitForm" :loading="submitLoading">
                  确定
                </el-button>
              </span>
            </template>
          </el-dialog>

          <!-- 评论列表对话框 -->
          <el-dialog
              v-model="commentDialogVisible"
              title="用户评论列表"
              width="900px"
              @close="resetCommentForm"
          >
            <div class="comment-filter">
              <el-form :model="commentFilterForm" inline>
                <el-form-item label="排序方式">
                  <el-select v-model="commentFilterForm.sortBy" style="width: 120px">
                    <el-option
                        v-for="option in commentSortOptions"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="排序顺序">
                  <el-select v-model="commentFilterForm.sortOrder" style="width: 100px">
                    <el-option
                        v-for="option in sortOrderOptions"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleCommentFilter">查询</el-button>
                </el-form-item>
              </el-form>
            </div>

            <el-table
                :data="commentList"
                v-loading="commentLoading"
                stripe
                border
                max-height="300"
                height="300"
                style="flex: 1;"
            >
              <el-table-column prop="content" label="评论内容" show-overflow-tooltip/>
              <el-table-column prop="createTime" label="评论时间" width="180">
                <template #default="scope">
                    {{ formatTime( scope.row.createTime)}}
                </template>
              </el-table-column>
              <el-table-column prop="likeCount" label="点赞数" width="80"/>
              <el-table-column prop="replyCount" label="回复数" width="80"/>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button
                      type="primary"
                      size="default"
                      circle
                      :icon="InfoFilled"
                      title="查看评论详情"
                      @click="jumpToCommentPost(scope.row.postId, scope.row.commentId)"
                  />

                  <el-button
                      type="danger"
                      size="default"
                      circle
                      :icon="Delete"
                      title="删除评论"
                      @click="deleteUserComment(scope.row.commentId)"
                  />
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
                v-model:current-page="commentPagination.currentPage"
                v-model:page-size="commentPagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="commentPagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleCommentSizeChange"
                @current-change="handleCommentCurrentChange"                style="margin-top: 20px; justify-content: center;"
            />

            <template #footer>
              <span class="dialog-footer">
                <el-button @click="commentDialogVisible = false">关闭</el-button>
              </span>
            </template>
          </el-dialog>

          <!-- 回复列表对话框 -->
          <el-dialog
              v-model="replyDialogVisible"
              title="用户回复列表"
              width="900px"
              @close="resetReplyForm"
          >
            <div class="reply-filter">
              <el-form :model="replyFilterForm" inline>
                <el-form-item label="排序方式">
                  <el-select v-model="replyFilterForm.sortBy" style="width: 120px">
                    <el-option
                        v-for="option in replySortOptions"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item label="排序顺序">
                  <el-select v-model="replyFilterForm.sortOrder" style="width: 100px">
                    <el-option
                        v-for="option in sortOrderOptions"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value">
                    </el-option>
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleReplyFilter">查询</el-button>
                </el-form-item>
              </el-form>
            </div>

            <el-table
                :data="replyList"
                v-loading="replyLoading"
                stripe
                border
                max-height="400"
            >
              <el-table-column prop="content" label="回复内容" show-overflow-tooltip/>
              <el-table-column prop="likeCount" label="点赞数" width="80"/>
              <el-table-column prop="replyCount" label="回复数" width="80"/>
              <el-table-column label="操作" width="120" fixed="right">
                <template #default="scope">
                  <el-button
                      type="primary"
                      size="default"
                      circle
                      :icon="InfoFilled"
                      title="查看回复详情"
                      @click="jumpToReplyPost(scope.row.postId, scope.row.commentId, scope.row.replyId)"
                  >
                  </el-button>

                  <el-button
                      type="danger"
                      size="default"
                      circle
                      :icon="Delete"
                      title="删除回复"
                      @click="deleteUserReply(scope.row.replyId)"
                  />
                </template>
              </el-table-column>
            </el-table>

            <el-pagination
                v-model:current-page="replyPagination.currentPage"
                v-model:page-size="replyPagination.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                :total="replyPagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleReplySizeChange"
                @current-change="handleReplyCurrentChange"                style="margin-top: 20px; justify-content: center;"
            />

            <template #footer>
              <span class="dialog-footer">
                <el-button @click="replyDialogVisible = false">关闭</el-button>
              </span>
            </template>
          </el-dialog>

        </div>
      </el-main>
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

.main-content {
  height: 100vh;
  width: 800px;
  margin-right: auto;
  margin-left: auto;
  display: flex;
  flex-direction: column;
  padding: 20px;
  border: snow 8px solid;
  border-radius: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background: white;
}

.toolbar {
  display: flex;
  margin-bottom: 10px;
  justify-content: space-between;
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background-color: #fff;
}
.filter-card {
  margin-bottom: 20px;
}
.el-text {
  font-weight: bold;
}
.comment-filter {
  margin-bottom: 15px;
  padding: 10px;
  border-radius: 4px;
}

.reply-filter {
  margin-bottom: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>
