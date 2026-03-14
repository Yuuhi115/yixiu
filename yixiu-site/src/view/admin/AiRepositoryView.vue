<script setup>
import { Edit, Delete, Search, Plus, Download, ArrowUp, ArrowDown, Sort, SwitchButton, Message } from "@element-plus/icons-vue";
import {computed, onMounted, reactive, ref} from "vue";
import Cookie from "js-cookie";
import { getUserInfo } from "../../api/userApi.js";
import { ElMessage, ElMessageBox } from "element-plus";
import router from "../../router/index.js";
import {AcceptAdmin, AcceptSuperAdmin} from "../../utils/roleCheckUtils.js";
import {addKnowledge, deleteKnowledge, getKnowledgeList, rebuildKnowledge, updateKnowledge} from "../../api/AiApi.js";
import {formatTime} from "../../utils/timeUtils.js";
import {useNotificationStore} from "../../stores/notificationInit.js";

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
  role: "",
  status: "",
  lastLogin: "",
})

// 数据相关
const knowledgeList = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const submitLoading = ref(false)
const knowledgeFormRef = ref()

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 排序相关
const sortOrder = ref('DESC') // 'asc' 升序，'desc' 降序，'' 不排序
const sortBy = ref('createTime') // 'createTime' 或 'hitCount'

// 表单数据
const knowledgeForm = reactive({
  knowledgeId: '',
  sourceType: '',
  sourceId: '',
  problem: '',
  solution: '',
  status: ''
})

// 来源类型选项
const sourceTypeOptions = [
  { label: '维修日志', value: 1 },
  { label: '社区帖子', value: 2 },
  { label: '人工录入', value: 3 }
]

// 状态选项
const statusOptions = [
  { label: '禁用', value: 0 },
  { label: '启用', value: 1 }
]

// 展开/收起解决方案
const toggleExpand = (row) => {
  row.expanded = !row.expanded
}

// 表单验证规则
const formRules = {
  sourceType: [
    { required: true, message: '请选择来源类型', trigger: 'change' }
  ],
  problem: [
    { required: true, message: '请输入问题描述', trigger: 'blur' }
  ],
  solution: [
    { required: true, message: '请输入解决方案', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请选择状态', trigger: 'change' }
  ]
}

// 编辑知识
const handleEdit = (row) => {
  knowledgeForm.knowledgeId = row.knowledgeId
  knowledgeForm.sourceType = row.sourceType
  knowledgeForm.sourceId = row.sourceId
  knowledgeForm.problem = row.problem
  knowledgeForm.solution = row.solution
  knowledgeForm.status = row.status
  dialogVisible.value = true
}

// 删除知识
const handleDelete = (row) => {
  ElMessageBox.confirm(
      `确定要删除这条知识吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      if (!AcceptAdmin(userInfo)) {
        ElMessage.error('无操作权限')
        return
      }
      const response = await deleteKnowledge(row.knowledgeId)
      if (response.code !== 200){
        ElMessage.error(response.msg)
      }
      ElMessage.success("删除成功")
      await fetchKnowledgeList()
      await rebuildKnowledge()
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  })
}

const handleStatus = (row) => {
  ElMessageBox.confirm(
      `确定要${row.status === 1 ? '禁用' : '启用'}这条知识吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      if (!AcceptAdmin(userInfo)) {
        ElMessage.error('无操作权限')
        return
      }
      let data = {
        knowledgeId: row.knowledgeId,
        status: row.status === 1 ? 0 : 1
      }
      const response = await updateKnowledge(data)
      if (response.code !== 200){
        ElMessage.error(response.msg)
        return
      }
      ElMessage.success("操作成功")
      await fetchKnowledgeList()
      await rebuildKnowledge()
    } catch (error) {
      ElMessage.error('操作失败：' + error.message)
    }
  })
}

// 提交表单
const submitForm = () => {
  knowledgeFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        let data = {
          sourceType: knowledgeForm.sourceType,
          problem: knowledgeForm.problem,
          solution: knowledgeForm.solution,
          status: knowledgeForm.status
        }
        let response
        if (knowledgeForm.knowledgeId !== ''){
          if (!AcceptAdmin(userInfo)) {
            ElMessage.error('无操作权限')
            return
          }
          data.knowledgeId = knowledgeForm.knowledgeId
          response = await updateKnowledge(data)
          if (response.code !== 200){
            ElMessage.error(response.msg)
            return
          }
          ElMessage.success('编辑成功')
          await rebuildKnowledge()
        }else {
          if (knowledgeForm.sourceType !== 3){
            data.sourceId = knowledgeForm.sourceId
          }
          response = await addKnowledge(data)
          if (response.code !== 200){
            ElMessage.error(response.msg)
            return
          }
          ElMessage.success('添加成功')
        }
        resetForm()
        dialogVisible.value = false
        await fetchKnowledgeList()
      } catch (error) {
        ElMessage.error('编辑失败：' + error.message)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (knowledgeFormRef.value) {
    knowledgeFormRef.value.resetFields()
  }
  knowledgeForm.knowledgeId = ''
  knowledgeForm.sourceType = ''
  knowledgeForm.sourceId = ''
  knowledgeForm.problem = ''
  knowledgeForm.solution = ''
  knowledgeForm.status = ''
}

// 切换排序方式（下拉框选择）
const handleSortByChange = (value) => {
  sortBy.value = value
  sortOrder.value = 'DESC' // 切换排序字段时默认降序
  loadKnowledgeList()
}

// 获取排序图标
const getSortIcon = (field) => {
  if (sortBy.value !== field) return 'Sort'
  return sortOrder.value === 'asc' ? 'ArrowUp' : 'ArrowDown'
}

// 分页相关
const handleSizeChange = async (val) => {
  pagination.pageSize = val
  pagination.currentPage = 1
  await loadKnowledgeList()
}

const handleCurrentChange = async (val) => {
  pagination.currentPage = val
  await loadKnowledgeList()
}

const handleAddKnowledge = () => {
  knowledgeForm.sourceType = 3
  knowledgeForm.status = 1
  dialogVisible.value = true
}

onMounted(async () => {
  await fetchKnowledgeList()
  await queryUserInfo()
  await notificationStore.syncUnreadCount()
})

// 获取知识列表
const fetchKnowledgeList = async () => {
  loading.value = true
  try {
    let params = {
      pageNum: pagination.currentPage,
      pageSize: pagination.pageSize,
      sortBy: sortBy.value,
      sortOrder: sortOrder.value
    }
    const response = await getKnowledgeList(params)
    if (response.code !== 200) {
      ElMessage.error(response.msg)
      return
    }
    knowledgeList.value = response.data.list
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('获取知识列表失败：' + error.message)
    knowledgeList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 加载知识列表（带排序）
const loadKnowledgeList = async () => {
  await fetchKnowledgeList()
}

const queryUserInfo = async () => {
  const token = Cookie.get('Authorization')
  const response = await getUserInfo(token)
  if (response.code !== 200) {
    ElMessage.error(response.msg)
    return
  }
  Object.assign(userInfo, response.data)
}

// 获取来源类型标签文本
const getSourceTypeLabel = (type) => {
  const option = sourceTypeOptions.find(item => item.value === type)
  return option ? option.label : type
}

// 获取状态标签文本
const getStatusLabel = (status) => {
  const statusMap = {
    0: '禁用',
    1: '启用'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 0:
      return 'danger'
    case 1:
      return 'success'
    default:
      return 'info'
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
              <h3 class="clickable-title" style="margin-right: 100px" @click="() => router.push('/')">Light 义修帮</h3>
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
                <el-menu-item index="1" v-if="AcceptAdmin(userInfo)" @click="() => router.push('/admin/userManage')">用户管理</el-menu-item>
                <el-menu-item index="2">知识库管理</el-menu-item>
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
                  <el-button @click="() => router.push('/user/messageCenter')" type="default" :icon="Message" circle/>
                </el-badge>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-header>

      <!--主界面-->
      <el-main>
        <div class="main-content">
          <!-- 操作栏 -->
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="handleAddKnowledge">
              添加知识
            </el-button>

            <div class="sort-buttons">
              <el-select
                  v-model="sortBy"
                  @change="handleSortByChange"
                  placeholder="请选择排序字段"                style="width: 120px;"
              >
                <el-option label="更新时间" value="createTime" />
                <el-option label="命中次数" value="hitCount" />
              </el-select>
              <el-radio-group v-model="sortOrder" @change="loadKnowledgeList">
                <el-radio-button label="DESC">
                  <el-icon><ArrowDown /></el-icon>
                  降序
                </el-radio-button>
                <el-radio-button label="ASC">
                  <el-icon><ArrowUp /></el-icon>
                  升序
                </el-radio-button>
              </el-radio-group>
            </div>
          </div>

          <!-- 知识表格 -->
          <el-table
              :data="knowledgeList"
              v-loading="loading"
              style="width: 100%; margin-top: 20px;"
              stripe
              border
          >
            <el-table-column prop="knowledgeId" label="知识 ID" width="80"/>
            <el-table-column prop="problem" label="问题描述" min-width="200"/>
            <el-table-column prop="solution" label="解决方案" min-width="200">
              <template #default="scope">
                <div class="solution-cell">
                  <el-text
                      :truncated="!scope.row.expanded"
                      :title="scope.row.solution"
                      :class="{ 'expanded': scope.row.expanded }"
                  >
                    {{ scope.row.solution }}
                  </el-text>
                  <el-button
                      v-if="scope.row.solution && scope.row.solution.length > 20"
                      link
                      type="primary"
                      size="small"
                      @click="toggleExpand(scope.row)"
                      class="expand-btn"
                  >
                    {{ scope.row.expanded ? '收起' : '展开' }}
                  </el-button>
                </div>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.status)">
                  {{ getStatusLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="hitCount" label="命中次数" width="90"/>
            <el-table-column prop="sourceType" label="来源类型" width="100">
              <template #default="scope">
                <el-tag>{{ getSourceTypeLabel(scope.row.sourceType) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="sourceId" label="来源 ID" width="100"/>
            <el-table-column prop="createTime" label="创建时间" width="160">
              <template #default="scope">
                <el-tag type="info">
                  {{ formatTime(scope.row.createTime) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" label="更新时间" width="160">
              <template #default="scope">
                <el-tag type="info">{{ formatTime(scope.row.updateTime) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="150" fixed="right">
              <template #default="scope">
                <el-button
                    :type="scope.row.status === 1 ? 'success' : 'danger'"
                    :icon="SwitchButton"
                    circle
                    @click="handleStatus(scope.row)"
                    title="启用/停用"
                />
                <el-button
                    type="primary"
                    :icon="Edit"
                    circle
                    @click="handleEdit(scope.row)"
                    title="编辑"
                />
                <el-button
                    type="danger"
                    :icon="Delete"
                    circle
                    @click="handleDelete(scope.row)"
                    title="删除"
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

          <!-- 知识编辑对话框 -->
          <el-dialog
              v-model="dialogVisible"
              :title="knowledgeForm.knowledgeId ? '编辑知识' : '添加知识'"
              width="600px"
              @close="resetForm"
          >
            <el-form
                ref="knowledgeFormRef"
                :model="knowledgeForm"
                :rules="formRules"
                label-width="100px"
            >
              <el-form-item label="来源类型" prop="sourceType">
                <el-select v-model="knowledgeForm.sourceType" style="width: 100%;" placeholder="请选择来源类型">
                  <el-option
                      v-for="option in sourceTypeOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="来源 ID" prop="sourceId">
                <el-input v-model="knowledgeForm.sourceId" placeholder="请输入来源 ID（如日志 ID、帖子 ID 等）"/>
              </el-form-item>
              <el-form-item label="问题描述" prop="problem">
                <el-input
                    v-model="knowledgeForm.problem"
                    type="textarea"
                    :rows="3"
                    placeholder="请输入问题描述"
                />
              </el-form-item>
              <el-form-item label="解决方案" prop="solution">
                <el-input
                    v-model="knowledgeForm.solution"
                    type="textarea"
                    :rows="4"
                    placeholder="请输入解决方案"
                />
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="knowledgeForm.status" style="width: 100%;" placeholder="请选择状态">
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
  margin-bottom: 20px;
  margin-top: 20px;
  justify-content: space-between;
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background-color: #fff;
}

.sort-buttons {
  display: flex;
  align-items: center;
  gap: 10px;
}

.sort-label {
  font-weight: bold;
  color: #606266;
}
.sort-label {
  font-weight: bold;
  color: #606266;
}

.solution-cell {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: 400px;
}

.solution-cell .el-text {
  flex: 1;
  line-height: 1.5;
}

.solution-cell .el-text.expanded {
  white-space: pre-wrap;
  word-break: break-word;
}

.expand-btn {
  flex-shrink: 0;
  padding: 0;
  margin-top: 2px;
}
</style>
