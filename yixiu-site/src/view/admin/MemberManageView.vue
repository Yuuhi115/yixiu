<script setup>
import {Message, Edit, Delete, Search, Plus} from "@element-plus/icons-vue";
import {onMounted, reactive, ref, computed} from "vue";
import Cookie from "js-cookie";
import {getUserInfo} from "../../api/userApi.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "../../router/index.js";
import {inviteVolunteer} from "../../api/adminApi.js";
import {getVolunteerInfoListExcludeMyself} from "../../api/volunteerApi.js";

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
const volunteerList = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const dialogVisible = ref(false)
const submitLoading = ref(false)
const volunteerFormRef = ref()

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 表单数据（仅包含可编辑字段）
const volunteerForm = reactive({
  userId: '',
  role: '',  // 身份
  status: '' // 状态
})

// 身份选项
const roleOptions = [
  {label: '普通用户', value: 'student'},
  {label: '管理员', value: 'admin'},
  {label: '志愿者', value: 'volunteer'}
]

// 状态选项
const statusOptions = [
  {label: '退队', value: 0},
  {label: '正常', value: 1},
  {label: '退休', value: 2}
]

// 表单验证规则
const formRules = {
  role: [
    {required: true, message: '请选择身份', trigger: 'change'}
  ],
  status: [
    {required: true, message: '请选择状态', trigger: 'change'}
  ]
}

// 编辑志愿者
const handleEdit = (row) => {
  // 只传递可编辑的字段
  volunteerForm.userId = row.userId
  volunteerForm.role = row.role
  volunteerForm.status = row.volunteerInfo.status
  dialogVisible.value = true
}

// 删除志愿者
const handleDelete = (row) => {
  ElMessageBox.confirm(
      `确定要删除志愿者 ${row.realName} 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
  ).then(async () => {
    try {
      // 调用实际的删除API
      const response = await request({
        url: `/api/admin/volunteers/${row.userId}`,
        method: 'delete'
      })

      if (response.code === 200) {
        ElMessage.success('删除成功')
        await fetchVolunteers()
      } else {
        ElMessage.error(response.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败: ' + error.message)
    }
  })
}

// 提交表单
const submitForm = () => {
  volunteerFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 调用编辑API（只更新身份和状态）
        const response = await request({
          url: `/api/admin/volunteers/${volunteerForm.userId}/role-status`,
          method: 'put',
          data: {
            role: volunteerForm.role,
            status: volunteerForm.status
          }
        })

        if (response.code === 200) {
          ElMessage.success('编辑成功')
          dialogVisible.value = false
          await fetchVolunteers()
        } else {
          ElMessage.error(response.msg || '编辑失败')
        }
      } catch (error) {
        ElMessage.error('编辑失败: ' + error.message)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 重置表单
const resetForm = () => {
  if (volunteerFormRef.value) {
    volunteerFormRef.value.resetFields()
  }
  volunteerForm.userId = ''
  volunteerForm.role = ''
  volunteerForm.status = ''
}

// 分页相关
const handleSizeChange = async (val) => {
  pagination.pageSize = val
  await fetchVolunteers()
}

const handleCurrentChange = async (val) => {
  pagination.currentPage = val
  await fetchVolunteers()
}

onMounted(async () => {
  await fetchVolunteers()
  await queryUserInfo()
})

// 获取志愿者列表
const fetchVolunteers = async () => {
  loading.value = true
  try {
    const response = await getVolunteerInfoListExcludeMyself(pagination.currentPage, pagination.pageSize)
    if (response.code !== 200) {
      ElMessage.error(response.msg)
      return
    }
    volunteerList.value = response.data.list
    pagination.total = response.data.total
  } catch (error) {
    ElMessage.error('获取志愿者列表失败: ' + error.message)
    volunteerList.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

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

// 获取身份标签文本
const getRoleLabel = (role) => {
  const option = roleOptions.find(item => item.value === role)
  return option ? option.label : role
}

// 获取状态标签文本
const getStatusLabel = (status) => {
  const statusMap = {
    0: '退队',
    1: '正常',
    2: '退休'
  }
  return statusMap[status] || status
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  switch (status) {
    case 0:
      return 'warning'  // 退队
    case 1:
      return 'success'  // 正常
    case 2:
      return 'info'     // 退休
    default:
      return 'info'
  }
}

/*志愿者邀请注册相关*/
// 在现有数据属性中添加邀请方式相关属性
const inviteMethod = ref('email') // 'email' 或 'phone'

const inviteDialogVisible = ref(false)
const inviteFormRef = ref()
const inviteLoading = ref(false)

const inviteForm = reactive({
  email: '',
  phone: ''
})

const inviteValue = computed({
  get() {
    return inviteMethod.value === 'email' ? inviteForm.email : inviteForm.phone
  },
  set(value) {
    if (inviteMethod.value === 'email') {
      inviteForm.email = value
    } else {
      inviteForm.phone = value
    }
  }
})

const inviteRules = computed(() => ({
  email: [
    {
      required: inviteMethod.value === 'email',
      message: '请输入邮箱',
      trigger: 'blur'
    },
    {
      pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
      message: '邮箱格式不正确',
      trigger: 'blur'
    }
  ],
  phone: [
    {
      required: inviteMethod.value === 'phone',
      message: '请输入手机号',
      trigger: 'blur'
    },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '手机号格式不正确',
      trigger: 'blur'
    }
  ]
}))



const showAddDialog = () => {
  inviteDialogVisible.value = true
}

const submitInviteForm = () => {
  inviteFormRef.value.validate(async (valid) => {
    if (valid) {
      // 构造确认消息
      const inviteTarget = inviteMethod.value === 'email'
          ? `邮箱 ${inviteForm.email}`
          : `手机号 ${inviteForm.phone}`

      ElMessageBox.confirm(
          `确定要向${inviteTarget}发送邀请吗？`,
          '确认邀请',
          {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          }
      ).then(async () => {
        inviteLoading.value = true
        try {
          // 根据邀请方式调用不同的API参数
          let response
          if (inviteMethod.value === 'email') {
            response = await inviteVolunteer(inviteForm.email)
          } else {
            // 如果后端API支持手机号邀请，需要相应调整
            ElMessage.info("手机号邀请功能开发中")
          }

          if (response.code === 200) {
            ElMessage.success('邀请码已发送')
            inviteDialogVisible.value = false
            resetInviteForm()
          } else {
            ElMessage.error(response.msg || '发送失败')
          }
        } catch (error) {
          ElMessage.error('发送失败: ' + error.message)
        } finally {
          inviteLoading.value = false
        }
      })
    }
  })
}

// 添加重置邀请字段函数
const resetInviteField = () => {
  inviteForm.email = ''
  inviteForm.phone = ''
  if (inviteFormRef.value) {
    inviteFormRef.value.clearValidate()
  }
}

// 更新重置表单函数
const resetInviteForm = () => {
  inviteMethod.value = 'email'
  resetInviteField()
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
                  mode="horizontal"
                  style="border-bottom: silver solid 1px;background-color: snow"
                  :ellipsis="false"
              >
                <el-menu-item index="1">成员管理</el-menu-item>
                <el-menu-item index="2">申请历史</el-menu-item>
                <el-menu-item index="3">我的收藏</el-menu-item>
                <el-menu-item index="4">消息中心</el-menu-item>
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
          <!-- 操作栏 -->
          <div class="toolbar">
            <el-button type="primary" :icon="Plus" @click="showAddDialog">
              邀请志愿者
            </el-button>
            <el-input
                v-model="searchKeyword"
                placeholder="搜索用户名或姓名"
                style="width: 300px;"
                clearable
                @keyup.enter="fetchVolunteers"
            >
              <template #append>
                <el-button @click="fetchVolunteers">
                  <el-icon>
                    <Search/>
                  </el-icon>
                </el-button>
              </template>
            </el-input>
          </div>

          <!-- 志愿者表格 -->
          <el-table
              :data="volunteerList"
              v-loading="loading"
              style="width: 100%; margin-top: 20px;"
              stripe
              border
          >
            <el-table-column prop="userId" label="用户ID" width="100"/>
            <el-table-column label="头像" width="80">
              <template #default="scope">
                <el-avatar :size="40" :src="scope.row.avatar"/>
              </template>
            </el-table-column>
            <el-table-column prop="realName" label="真实姓名" width="120"/>
            <el-table-column prop="username" label="用户名" width="120"/>
            <el-table-column prop="email" label="邮箱" width="180"/>
            <el-table-column prop="phone" label="手机号" width="120"/>
            <el-table-column prop="role" label="身份" width="100">
              <template #default="scope">
                <el-tag>{{ getRoleLabel(scope.row.role) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100">
              <template #default="scope">
                <el-tag :type="getStatusTagType(scope.row.volunteerInfo.status)">
                  {{ getStatusLabel(scope.row.volunteerInfo.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastLogin" label="最后登录" width="180"/>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="scope">
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

          <!-- 志愿者编辑对话框 -->
          <el-dialog
              v-model="dialogVisible"
              title="编辑志愿者"
              width="500px"
              @close="resetForm"
          >
            <el-form
                ref="volunteerFormRef"
                :model="volunteerForm"
                :rules="formRules"
                label-width="80px"
            >
              <el-form-item label="身份" prop="role">
                <el-select v-model="volunteerForm.role" style="width: 100%;">
                  <el-option
                      v-for="option in roleOptions"
                      :key="option.value"
                      :label="option.label"
                      :value="option.value"
                  />
                </el-select>
              </el-form-item>
              <el-form-item label="状态" prop="status">
                <el-select v-model="volunteerForm.status" style="width: 100%;">
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

          <!--邀请注册对话框-->
          <el-dialog
              v-model="inviteDialogVisible"
              title="邀请志愿者"
              width="500px"
              @close="resetInviteForm"
          >
            <el-form
                ref="inviteFormRef"
                :model="inviteForm"
                :rules="inviteRules"
                label-width="80px"
            >
              <el-form-item label="邀请方式">
                <el-radio-group v-model="inviteMethod" @change="resetInviteField">
                  <el-radio label="email">邮箱邀请</el-radio>
                  <el-radio label="phone">手机号邀请</el-radio>
                </el-radio-group>
              </el-form-item>

              <el-form-item
                  :label="inviteMethod === 'email' ? '邮箱' : '手机号'"
                  prop="inviteValue"
              >
                <el-input
                    v-model="inviteValue"
                    :placeholder="inviteMethod === 'email' ? '请输入邮箱地址' : '请输入手机号'"
                    clearable
                />
              </el-form-item>
            </el-form>

            <template #footer>
              <span class="dialog-footer">
                <el-button @click="inviteDialogVisible = false">取消</el-button>
                  <el-button
                      type="primary"
                      @click="submitInviteForm"
                      :loading="inviteLoading"
                  >
                    发送邀请
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
  justify-content: space-between;
}
</style>
