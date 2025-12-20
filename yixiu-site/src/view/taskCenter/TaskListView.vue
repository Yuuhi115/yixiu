<script setup>
import {Message} from "@element-plus/icons-vue";
import {onMounted, reactive, ref} from "vue";
import Cookie from "js-cookie";
import {
  getRepairFormByUserId,
  getUserInfo,
  updateRepairFormStatus
} from "../../api/userApi.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "../../router/index.js";
import {addTaskAssign, applyToJoin, getAllRepairList, getFilteredRepairList} from "../../api/volunteerApi.js";
import {checkVolunteerPermission} from "../../utils/userUtils.js"

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
  volunteerInfo: {
    volunteerId: "",
    studentNumber: "",
    majorClass: "",
    grade: ""
  }
})

// 任务数据
const taskListRef = ref([])

// 筛选条件
const filterForm = reactive({
  createTime: [],
  updateTime: [],
  status: ''
})

// 状态选项
const statusOptions = [
  {label: '已提交待审核', value: 0},
  {label: '审核通过', value: 1},
  {label: '已被接收', value: 2},
  {label: '已完成', value: 3},
  {label: '已取消', value: 4},
  {label: '用户自行解决', value: 5},
  {label: '已被拒绝', value: 6},
]

// 可更改的状态选项
const changeableStatusOptions = [
  {label: '接收', value: 2},
  {label: '完成任务', value: 3},
  {label: '取消任务', value: 4}
]

// 下拉菜单相关
const activeTab = ref('all')

onMounted(async () => {
  await queryUserInfo()
  await loadTaskList()
  await checkVolunteerPermission(localStorage.getItem("role"))
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

// 加载任务列表
const activeNames = ref([])

const loadTaskList = async () => {
  const response = await getAllRepairList()
  if (response.code === 200) {
    // 赋值给 taskListRef 列表
    taskListRef.value = response.data
    console.log(taskListRef.value)
  } else {
    ElMessage.error(response.msg)
  }
}

// 处理任务筛选
const handleFilter = async () => {
  const queryParams = buildQueryParams()
  // console.log('筛选条件:', queryParams)

  const response = await getFilteredRepairList(queryParams)

  if (response.code === 200) {
    taskListRef.value = response.data
    console.log(taskListRef.value)
  } else {
    ElMessage.error(response.msg)
  }

}

// 构建筛选条件
const buildQueryParams = () => {
  const params = {}

  // 时间范围条件
  if (filterForm.createTime?.length) {
    params.createStartTime = filterForm.createTime[0]
    params.createEndTime = filterForm.createTime[1]
  }

  if (filterForm.updateTime?.length) {
    params.updateStartTime = filterForm.updateTime[0]
    params.updateEndTime = filterForm.updateTime[1]
  }

  // 状态条件
  if (filterForm.status !== '' && filterForm.status != null) {
    params.status = filterForm.status
  }

  return params
}

// 重置筛选
const resetFilter = () => {
  filterForm.createTime = []
  filterForm.updateTime = []
  filterForm.status = ''
  loadTaskList()
}

// 获取状态标签
const getStatusLabel = (status) => {
  const option = statusOptions.find(item => item.value === status)
  return option ? option.label : '未知状态'
}

// 获取状态标签类型
const getStatusType = (status) => {
  switch (status) {
    case 0:
      return 'warning'     // 已提交待审核
    case 1:
      return 'primary'  // 审核通过
    case 2:
      return 'primary'  // 已接收
    case 3:
      return 'success'   // 已完成
    case 4:
      return 'danger'    // 已取消
    case 5:
      return 'success'  // 用户自行解决
    case 6:
      return 'danger' //已被拒绝
  }
}

// 切换视图
const switchView = (tab) => {
  activeTab.value = tab
  // 根据标签过滤数据
  console.log('切换到标签:', tab)
}

// 更改任务状态
const changeTaskStatus = (task, newStatus, action) => {
  const statusLabel = changeableStatusOptions.find(opt => opt.value === newStatus)?.label || '未知状态';
  ElMessageBox.confirm(
      `确认要${action}任务(TaskID:${task.requestId})吗？`,
      `${action}任务确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    const response = await updateRepairFormStatus(task.requestId, newStatus);
    if (response.code === 200) {
      ElMessage.success(response.msg);
      task.status = newStatus;
    } else {
      ElMessage.error(response.msg);
    }
  }).catch(() => {
    ElMessage.info('操作已取消');
  });
}

// 拒绝原因对话框相关
const rejectDialogVisible = ref(false)
const rejectForm = reactive({
  taskId: '',
  reason: ''
})
const rejectFormRef = ref()

// 维修日志对话框相关
const completeDialogVisible = ref(false)
const completeForm = reactive({
  taskId: '',
  content: '',     // 维修内容
  duration: '',    // 维修时长
  solution: ''     // 解决方法
})
const completeFormRef = ref()

// 判断是否应该显示操作按钮
const shouldShowActionButtons = (task) => {
  // 志愿者角色
  if (userInfo.role === 'volunteer') {
    return [1, 2].includes(task.status) // 只在状态1和2时显示按钮
  }
  // 管理员角色
  else if (['admin', 'super_admin'].includes(userInfo.role)) {
    return [0, 1, 2].includes(task.status) // 在状态0,1,2时显示按钮
  }
  return false
}

// 接收任务
const addTaskAssignment = async (volunteerId, remarks, task) => {
  ElMessageBox.confirm(
      `确认要接收该任务吗？`,
      `任务接收确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    let data = {
      requestId: task.requestId,
      volunteerId: volunteerId,
      remarks: remarks
    }
    const responseAdd = await addTaskAssign(data)
    if (responseAdd.code === 200) {
      ElMessage.success(responseAdd.msg)
      task.status = 2
    } else {
      ElMessage.error(responseAdd.msg)
    }
  }).catch(() => {
    ElMessage.info('操作已取消');
  });
}

const applyToJoinTask = async (task) => {
  ElMessageBox.confirm(
      `确认要申请加入该任务吗？`,
      `任务申请确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    let data = {
      requestId: task.requestId,
      volunteerId: userInfo.volunteerInfo.volunteerId,
    }
    const responseAdd = await applyToJoin(data)
    if (responseAdd.code === 200) {
      ElMessage.success(responseAdd.data)
    }else {
      ElMessage.error(responseAdd.msg)
    }
  }).catch(()=>{
    ElMessage.info('操作已取消')
  });
}

// 显示拒绝对话框
const showRejectDialog = (task) => {
  rejectForm.taskId = task.requestId
  rejectForm.reason = ''
  rejectDialogVisible.value = true
}

// 显示完成任务对话框
const showCompleteDialog = (task) => {
  completeForm.taskId = task.requestId
  completeForm.content = ''
  completeForm.duration = ''
  completeForm.solution = ''
  completeDialogVisible.value = true
}

// 提交拒绝原因
const submitRejectReason = async () => {
  // 这里需要调用后端API提交拒绝原因
  // await submitRejectReasonAPI(rejectForm)
  // 然后更新任务状态为6
  changeTaskStatus({requestId: rejectForm.taskId}, 6, '拒绝')
  rejectDialogVisible.value = false
}

// 提交维修日志
const submitMaintenanceLog = async () => {
  // 这里需要调用后端API提交维修日志
  // await submitMaintenanceLogAPI(completeForm)
  // 然后更新任务状态为3
  changeTaskStatus({requestId: completeForm.taskId}, 3, '完成')
  completeDialogVisible.value = false
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
                <el-menu-item index="1">任务列表</el-menu-item>
                <el-menu-item index="2" @click="() => router.push('/repair/template')">我的模板</el-menu-item>
                <el-menu-item index="3" @click="() => router.push('/repair/history')">我的历史</el-menu-item>
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
                <span>筛选条件</span>
              </div>
            </template>

            <el-form :model="filterForm" label-width="100px">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="创建时间">
                    <el-date-picker
                        v-model="filterForm.createTime"
                        type="daterange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        style="width: 100%">
                    </el-date-picker>
                  </el-form-item>
                </el-col>

                <el-col :span="12">
                  <el-form-item label="更新时间">
                    <el-date-picker
                        v-model="filterForm.updateTime"
                        type="daterange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        format="YYYY-MM-DD"
                        value-format="YYYY-MM-DD"
                        style="width: 100%">
                    </el-date-picker>
                  </el-form-item>
                </el-col>

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
                  <el-form-item label="操作">
                    <el-button type="primary" @click="handleFilter">查询</el-button>
                    <el-button @click="resetFilter">重置</el-button>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
          </el-card>

          <!-- 视图切换下拉菜单 -->
          <div class="view-switcher">
            <el-badge :value="taskListRef.length" class="records-count" type="primary">
              共 {{ taskListRef.length }} 条任务
            </el-badge>
          </div>

          <!-- 任务列表 -->
          <el-card class="records-card">
            <template #header>
              <div class="card-header">
                <span>维修任务详情</span>
              </div>
            </template>

            <el-empty v-if="taskListRef.length === 0" description="暂无维修任务"/>

            <div class="records-container">
              <el-collapse v-model="activeNames" accordion>
                <el-collapse-item
                    v-for="(task, index) in taskListRef"
                    :key="task.id"
                    :name="index">
                  <template #title>
                    <div class="collapse-header">
                      <span class="record-id">申请编号: {{ task.requestId }}</span>
                      <el-tag :type="getStatusType(task.status)" size="small">
                        {{ getStatusLabel(task.status) }}
                      </el-tag>
                      <span class="record-time">{{ task.createTime }}</span>
                    </div>
                  </template>

                  <div class="record-detail">
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">设备类型:</span>
                          <span class="detail-value">{{ task.deviceType }}</span>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">设备型号:</span>
                          <span class="detail-value">{{ task.deviceModel }}</span>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">联系方式:</span>
                          <span class="detail-value">{{ task.contactType }}</span>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">预约时间:</span>
                          <span class="detail-value">{{ task.appointmentTime }}</span>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">维修地点:</span>
                          <span class="detail-value">{{ task.repairLocation }}</span>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">校区:</span>
                          <span class="detail-value">{{ task.campus === "0" ? '大学城校区' : '白云山校区' }}</span>
                        </div>
                      </el-col>
                    </el-row>

                    <div class="detail-item full-width">
                      <span class="detail-label">问题描述:</span>
                      <span class="detail-value">{{ task.problemDescription }}</span>
                    </div>

                    <div class="detail-item full-width" v-if="task.imgUrl && task.imgUrl.length > 0">
                      <span class="detail-label">故障图片:</span>
                      <div class="detail-value image-gallery">
                        <el-image
                            v-for="(image, imgIndex) in task.imgUrl"
                            :key="imgIndex"
                            :src="image"
                            :preview-src-list="task.imgUrl"
                            :initial-index="imgIndex"
                            fit="cover"
                            class="fault-image"
                            lazy
                        />
                      </div>
                    </div>
                    <div class="detail-item full-width" v-else>
                      <span class="detail-label">故障图片:</span>
                      <span class="detail-value">无图片</span>
                    </div>

                    <div class="detail-footer">
                      <div class="time-info">
                        <span>创建时间: {{ task.createTime }}</span>
                        <span>完成时间: {{ task.completeTime }}</span>
                        <span>更新时间: {{ task.updateTime }}</span>
                      </div>

                      <!-- 状态更改按钮 -->
                      <div class="action-buttons" v-if="shouldShowActionButtons(task)">
                        <!-- 状态0：待审核 - 仅管理员可操作 -->
                        <template v-if="task.status === 0 && ['admin', 'super_admin'].includes(userInfo.role)">
                          <el-button
                              type="primary"
                              @click.stop="changeTaskStatus(task, 1, '通过')">
                            通过
                          </el-button>
                          <el-button
                              type="danger"
                              @click.stop="showRejectDialog(task)">
                            拒绝
                          </el-button>
                        </template>

                        <!-- 状态1：审核通过 - 志愿者和管理员都可接收 -->
                        <template v-else-if="task.status === 1 && ['volunteer', 'admin', 'super_admin'].includes(userInfo.role)">
                          <el-button
                              type="primary"
                              @click.stop="addTaskAssignment(userInfo.volunteerInfo.volunteerId, '', task)">
                            接收
                          </el-button>
                        </template>

                        <!-- 状态2：已被接收 -->
                        <template v-else-if="task.status === 2 && task.repairAssignment && Array.isArray(task.repairAssignment)">
                          <!-- 志愿者和管理员角色 -->
                          <template v-if="['volunteer', 'admin', 'super_admin'].includes(userInfo.role)">
                            <!-- 用户未参与此任务 - 显示申请加入 -->
                            <el-button
                                v-if="!task.repairAssignment.some(assign => assign.volunteerId === userInfo.volunteerInfo.volunteerId)"
                                type="primary"
                                @click.stop="applyToJoinTask(task)">
                              申请加入
                            </el-button>

                            <!-- 用户已申请加入(状态为5) - 显示等待提示 -->
                            <el-text
                                v-else-if="task.repairAssignment.some(assign =>
                                 assign.volunteerId === userInfo.volunteerInfo.volunteerId &&
                                 assign.status === 5)"
                                type="success"
                                size="small">
                              已申请加入，等待回复
                            </el-text>

                            <!-- 用户是任务接收者(状态不为5) - 显示操作按钮 -->
                            <template
                                v-else-if="task.repairAssignment.some(assign =>
                                assign.volunteerId === userInfo.volunteerInfo.volunteerId)">
                              <el-button
                                  type="danger"
                                  @click.stop="changeTaskStatus(task, 4, '取消')">
                                取消任务
                              </el-button>
                              <el-button
                                  type="success"
                                  @click.stop="showCompleteDialog(task)">
                                完成任务
                              </el-button>
                            </template>
                          </template>
                        </template>
                      </div>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </el-card>
        </div>
      </el-main>

      <!-- 拒绝原因对话框 -->
      <el-dialog
          v-model="rejectDialogVisible"
          title="填写拒绝原因"
          width="500px"
      >
        <el-form :model="rejectForm" ref="rejectFormRef">
          <el-form-item label="拒绝原因" prop="reason">
            <el-input
                v-model="rejectForm.reason"
                type="textarea"
                placeholder="请输入拒绝原因"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="rejectDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitRejectReason">确定</el-button>
        </template>
      </el-dialog>

      <!-- 维修日志对话框 -->
      <el-dialog
          v-model="completeDialogVisible"
          title="填写维修日志"
          width="600px"
      >
        <el-form :model="completeForm" ref="completeFormRef">
          <el-form-item label="维修内容" prop="content">
            <el-input
                v-model="completeForm.content"
                type="textarea"
                placeholder="请输入维修内容"
            />
          </el-form-item>
          <el-form-item label="维修时长" prop="duration">
            <el-input
                v-model="completeForm.duration"
                placeholder="请输入维修时长（如：2小时）"
            />
          </el-form-item>
          <el-form-item label="解决方法" prop="solution">
            <el-input
                v-model="completeForm.solution"
                type="textarea"
                placeholder="请输入解决方法"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="completeDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitMaintenanceLog">确定</el-button>
        </template>
      </el-dialog>

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
  height: auto;
  width: 100%;
  max-width: 800px;
  margin-right: auto;
  margin-left: auto;
  padding: 20px;

  display: flex;
  flex-direction: column;
  justify-content: flex-start;

  border: snow 8px solid;
  border-radius: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  background: white;
  overflow-y: auto;
}

.filter-card {
  margin-bottom: 20px;
}

.view-switcher {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.records-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.records-container {
  /* 移除了 max-height 和 overflow-y 属性，取消内部滚动条 */
}

.collapse-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  padding-right: 20px;
}

.record-id {
  font-weight: bold;
}

.record-time {
  font-size: 12px;
  color: #909399;
}

.record-detail {
  padding: 15px 20px;
}

.detail-item {
  display: flex;
  margin-bottom: 10px;
}

.detail-label {
  font-weight: bold;
  width: 100px;
  margin-right: 10px;
  color: #606266;
}

.detail-value {
  flex: 1;
  color: #303133;
}

.full-width {
  width: 100%;
}

.full-width .detail-value {
  display: flex;
  justify-content: center;
  margin-top: 5px;
  padding: 8px;
  margin-left: 10px;
  margin-right: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.detail-footer {
  margin-top: 15px;
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
}

.time-info {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
  margin-bottom: 15px;
}

.image-gallery {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 5px;
}

.fault-image {
  width: 100px;
  height: 100px;
  border-radius: 4px;
  cursor: pointer;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>
