<script setup>
import {Message} from "@element-plus/icons-vue";
import {computed, onMounted, reactive, ref} from "vue";
import Cookie from "js-cookie";
import {
  getRepairFormByUserId,
  getUserInfo,
  updateRepairFormStatus
} from "../../api/userApi.js";
import {ElMessage, ElMessageBox} from "element-plus";
import router from "../../router/index.js";
import {
  addTaskAssign,
  addTaskLog, addTaskLogImg,
  applyToJoin,
  getAllRepairList,
  getFilteredRepairList
} from "../../api/volunteerApi.js";
import {checkVolunteerPermission} from "../../utils/userUtils.js"
import {
  sendCooperateNotify,
  sendRepairTaskAcceptNotification,
  sendRepairTaskApproveNotification,
  sendRepairTaskRejectNotification,
  sendSystemNoticeToUser
} from "../../api/notificationApi.js";
import {Plus} from "@element-plus/icons-vue";
import {
  RepairAssignArrayCheck, ThisVolunteerIsAttended,
  ThisVolunteerNotAttended,
  ThisVolunteerWaitingForApplyResult
} from "../../utils/conditionJudgeUtils.js";
import {useNotificationStore} from "../../stores/notificationInit.js";
import {addKnowledge} from "../../api/AiApi.js";
import {formatTime} from "../../utils/timeUtils.js";
import {sendTaskApproveEmail} from "../../api/adminApi.js";

const notificationStore = useNotificationStore()

// 使用计算属性自动响应状态变化
const unreadNotifyCount = computed(() => notificationStore.unreadCount)

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
    grade: "",
    contactType: "",
    contactNumber: "",
  }
})

// 任务数据
const taskListRef = ref([])

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

const taskListLoading = ref(false)

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
  {label: '已完成评价', value: 7}
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
  await notificationStore.syncUnreadCount()
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
  taskListLoading.value = true
  const response = await getAllRepairList(pagination.currentPage, pagination.pageSize)
  if (response.code === 200) {
    // 赋值给 taskListRef 列表
    taskListRef.value = response.data.list
    pagination.total = response.data.total
    console.log(response.data)
  } else {
    ElMessage.error(response.msg)
  }
  taskListLoading.value = false
}

// 处理任务筛选
const handleFilter = async () => {
  const queryParams = buildQueryParams()
  // console.log('筛选条件:', queryParams)
  queryParams.pageNum = pagination.currentPage
  queryParams.pageSize = pagination.pageSize

  taskListLoading.value = true
  const response = await getFilteredRepairList(queryParams)

  if (response.code === 200) {
    taskListRef.value = response.data.list
    pagination.total = response.data.total
    console.log(taskListRef.value)
  } else {
    ElMessage.error(response.msg)
  }
  taskListLoading.value = false
}

// 添加分页改变处理函数
const handlePageChange = (newPage) => {
  pagination.currentPage = newPage
  loadTaskListCondition()
}

const handleSizeChange = (newSize) => {
  pagination.pageSize = newSize
  pagination.currentPage = 1
  loadTaskListCondition()
}

const loadTaskListCondition = async () =>{
  if (filterForm.createTime?.length || filterForm.updateTime?.length || filterForm.status !== '') {
    await handleFilter()
  } else {
    await loadTaskList()
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
  pagination.currentPage = 1
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
    case 7:
    return 'success'   // 已完成评价
  }
}

const getContactTypeLabel = (contactType) => {
  switch (contactType) {
    case 0:
      return '手机'
    case 1:
      return '邮箱'
    case 2:
      return '微信'
    case 3:
      return 'QQ'
    default:
      return '未知联系方式'
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
  remark: '',
  receiverId: ''
})
const rejectFormRef = ref()


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

const approveTask = async (task) => {
  ElMessageBox.confirm(
      `确认要通过该任务吗？`,
      `任务审核确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    const response = await updateRepairFormStatus(task.requestId, 1)
    if (response.code !== 200){
      ElMessage.error(response.msg)
    }else {
      // ElMessage.success(response.msg)
      task.status = 1
      let msgData = {
        taskId: task.requestId,
        receiverId: task.userId,
      }
      const responseMsg = await sendRepairTaskApproveNotification(msgData)
      if (responseMsg.code === 200) {
        ElMessage.success("操作成功")
        await loadTaskListCondition()
      } else {
        ElMessage.error(responseMsg.msg)
      }
      const sendEmailMsg = await sendTaskApproveEmail(task.requestId)
      if (sendEmailMsg.code !== 200){
        ElMessage.error(sendEmailMsg.msg)
      }
    }
  }).catch(() => {
    ElMessage.info('操作已取消');
  });
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
      let msgData = {
        taskId: task.requestId,
        receiverId: task.userId,
        senderId: userInfo.userId
      }
      const msgResponse = await sendRepairTaskAcceptNotification(msgData)
      if (msgResponse.code === 200) {
        ElMessage.success("接收成功")
      } else {
        ElMessage.error(msgResponse.msg)
      }
      await loadTaskListCondition()
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
      loadTaskListCondition()
      // 查找负责人(isLeader=1)的志愿者ID
      const leader = task.repairAssignment?.find(assignment => assignment.isLeader === 1)
      if (!leader) {
        ElMessage.error('未找到任务负责人')
        return
      }
      let notificationData = {
        taskId: task.requestId,
        receiverId: leader.volunteerId,
      }
      console.log('发送通知给负责人:', notificationData)
      const responseMsg = await sendCooperateNotify(notificationData)
      if (responseMsg.code !== 200) {
        ElMessage.error(responseMsg.msg)
      }
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
  rejectForm.receiverId = task.userId
  rejectForm.remark = ''
  rejectDialogVisible.value = true
}

/*完成任务相关功能*/
const completeDialogVisible = ref(false)
const completeForm = reactive({
  taskId: '',
  content: '',     // 维修内容
  duration: '',    // 维修时长
  solution: ''     // 解决方法
})
const completeFormRef = ref()
// 维修图片相关
const maintenanceImages = ref([])
const maxImageCount = 3
const maxImageSize = 1024 * 1024 // 1MB

// 显示完成任务并填写维修日志对话框
const showCompleteDialog = async (task) => {
  completeForm.taskId = task.requestId
  completeForm.content = task.problemDescription
  completeForm.duration = ''
  completeForm.solution = ''
  maintenanceImages.value = []
  // 显示对话框
  completeDialogVisible.value = true
}
// 处理图片上传前的检查
const beforeImageUpload = (file) => {
  // 检查文件大小
  if (file.size > maxImageSize) {
    ElMessage.error('图片大小不能超过1MB!')
    return false
  }
  // 检查文件数量
  if (maintenanceImages.value.length >= maxImageCount) {
    ElMessage.error(`最多只能上传${maxImageCount}张图片!`)
    return false
  }

  // 检查文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  return true
}
// 提交维修日志
const submitMaintenanceLog = async () => {
  try {
    // 先提交维修文本信息
    const maintenanceData = {
      volunteerId: userInfo.volunteerInfo.volunteerId,
      requestId: completeForm.taskId,
      logContent: completeForm.content,
      repairDuration: completeForm.duration,
      solutionSummary: completeForm.solution
    }

    const response = await addTaskLog(maintenanceData)
    if (response.code !== 200) {
      ElMessage.error(response.msg)
      return
    }
    // 然后上传图片（如果有的话）
    if (maintenanceImages.value && maintenanceImages.value.length > 0) {
      await uploadMaintenanceImages(response.data.logId)
    }
    completeDialogVisible.value = false
    ElMessage.success('维修日志提交成功')
  } catch (error) {
    ElMessage.error('提交维修日志失败: ' + error.message)
  }
}

// 上传维修图片函数（模仿RepairFormView.vue的上传风格）
const uploadMaintenanceImages = async (logId) => {
  try {
    // 创建 FormData 对象
    const formData = new FormData()
    formData.append('logId', logId)

    // 添加图片文件
    maintenanceImages.value.forEach(file => {
      formData.append('logImg', file.raw)
    })

    // 调用图片上传 API
    const response = await addTaskLogImg(formData)
    if (response.code !== 200) {
      ElMessage.error('维修图片上传失败: ' + response.msg)
    }
    return response
  } catch (error) {
    ElMessage.error('维修图片上传异常: ' + error.message)
  }
}

// 超出图片数量限制的处理
const handleExceed = (files, uploadFiles) => {
  ElMessage.warning(`最多只能上传${maxImageCount}张图片`)
}
/**/

// 提交拒绝原因
const submitRejectReason = async () => {
  // 这里需要调用后端API提交拒绝原因
  const response = await sendRepairTaskRejectNotification(rejectForm)
  if (response.code !== 200) {
    ElMessage.error('拒绝原因提交失败: ' + response.msg)
    return
  }else {
    ElMessage.success('提交成功')
  }
  // 然后更新任务状态为6
  const responseUpdate = await updateRepairFormStatus(rejectForm.taskId, 6)
  if (responseUpdate.code !== 200) {
    ElMessage.error('任务状态更新失败: ' + responseUpdate.msg)
    return
  }
  rejectDialogVisible.value = false
  await loadTaskListCondition()
}

//维修日志导入知识库
const addToKnowledgeBase = async (log) => {
  ElMessageBox.confirm(
      `确认要将该日志录入到知识库吗？`,
      `知识库录入确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
  ).then(async () => {
    let data = {
      sourceType: 1,
      sourceId: "log_" + log.logId,
      problem: log.logContent,
      solution: log.solutionSummary,
    }
    const response = await addKnowledge(data)
    if (response.code !== 200) {
      ElMessage.error('录入失败：' + response.msg)
      return
    }
    ElMessage.success('录入成功，知识id：' + response.data)
    await loadTaskListCondition()
  }).catch(()=>{
    ElMessage.info('操作已取消')
  });
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
                <el-menu-item index="2" @click="() => router.push('/taskCenter/myTask')">我的任务</el-menu-item>
              </el-menu>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <div class="component-center">
                <el-avatar class="clickable-avatar" @click="() => router.push('/user/basicInfo')" :fit="'cover'" :src="userInfo.avatar"/>
              </div>
              <div class="component-center">
                <el-badge :is-dot="unreadNotifyCount > 0" class="item">
                  <el-button @click="router.push('/user/messageCenter')" type="default" :icon="Message" circle/>
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
            <el-badge :value="pagination.total" class="records-count" type="primary">
              共 {{ pagination.total }} 条任务
            </el-badge>
          </div>

          <!-- 任务列表 -->
          <el-card class="records-card" v-loading="taskListLoading">
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
                      <span class="record-time">{{ formatTime(task.updateTime) }}</span>
                    </div>
                  </template>

                  <div class="record-detail">
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">报修人:</span>
                          <span class="detail-value">{{ task.realName }}</span>
                        </div>
                      </el-col>
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
                          <span class="detail-label">联系号码:</span>
                          <span class="detail-value">{{ task.contactInfo }}</span>
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
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">备注:</span>
                          <span class="detail-value">{{ task.remarks }}</span>
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

                    <!-- 维修日志 -->
                    <div class="detail-item full-width">
                      <span class="detail-label">维修日志:</span>
                      <div class="detail-value logs-section">
                        <el-empty v-if="!task.repairLog || task.repairLog.length === 0" description="暂无维修日志"/>
                        <div v-else>
                          <div v-for="(log, logIndex) in task.repairLog" :key="logIndex" class="log-item">
                            <p><strong>维修人员:</strong> {{ log.volunteerName }}</p>
                            <p class="log-text"><strong>维修内容:</strong> {{ log.logContent }}</p>
                            <p><strong>维修时长:</strong> {{ log.repairDuration }}</p>
                            <p class="log-text"><strong>解决方案:</strong> {{ log.solutionSummary }}</p>
                            <p><strong>提交时间:</strong> {{ formatTime(log.uploadTime) }}</p>
                            <div class="log-images" v-if="log.logImgUrl && log.logImgUrl.length > 0">
                              <h5>维修过程图片:</h5>
                              <el-image
                                  v-for="(img, imgIdx) in log.logImgUrl"
                                  :key="imgIdx"
                                  :src="img"
                                  :preview-src-list="log.logImgUrl"
                                  :initial-index="imgIdx"
                                  fit="cover"
                                  class="log-image"
                                  lazy
                              />
                            </div>
                            <div style="text-align: right; margin-top: 10px;">
                              <el-button v-if="log.importStatus !== 1" type="primary" @click="addToKnowledgeBase(log)">导入知识库</el-button>
                              <el-text type="success" v-else>已录入知识库</el-text>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- 队员信息 -->
                    <div class="detail-item full-width">
                      <span class="detail-label">任务队员:</span>
                      <div class="detail-value team-section">
                        <el-empty v-if="!task.repairAssignment || task.repairAssignment.length === 0" description="暂无队员信息"/>
                        <div v-else>
                          <div v-for="(member, idx) in task.repairAssignment.filter(assignment => assignment.status !== 5 && assignment.status !== 6)" :key="idx" class="team-member">
                            <el-card class="member-card">
                              <div class="member-info">
                                <el-avatar :src="member.avatar" :size="40" />
                                <div class="member-details">
                                  <div class="member-name-status">
                                    <span class="member-name">{{ member.volunteerName }}</span>
                                    <el-tag :type="member.isLeader === 1 ? 'success' : 'primary'" size="small" class="member-role">
                                      {{ member.isLeader === 1 ? '负责人' : '队员' }}
                                    </el-tag>
                                  </div>
                                  <div class="member-other-info">
                                    <p class="member-id">ID: {{ member.volunteerId }}</p>
                                    <p class="member-class">班级: {{ member.majorClass }}</p>
                                    <p class="member-grade">年级: {{ member.grade }}</p>
                                    <p class="member-grade">联系方式: {{ getContactTypeLabel(member.contactType) }}</p>
                                    <p class="member-grade">联系号码:</p>
                                    <p class="member-grade"> {{ member.contactNumber }} </p>
                                  </div>
                                </div>
                              </div>
                            </el-card>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div class="detail-footer">
                      <div class="time-info">
                        <span>创建时间: {{ formatTime(task.createTime) }}</span>
                        <span>完成时间: {{ formatTime(task.completeTime) }}</span>
                        <span>更新时间: {{ formatTime(task.updateTime) }}</span>
                      </div>

                      <!-- 状态更改按钮 -->
                      <div class="action-buttons" v-if="shouldShowActionButtons(task)">
                        <!-- 状态0：待审核 - 仅管理员可操作 -->
                        <template v-if="task.status === 0 && ['admin', 'super_admin'].includes(userInfo.role)">
                          <el-button
                              type="primary"
                              @click.stop="approveTask(task)">
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
                        <template v-else-if="task.status === 2 && RepairAssignArrayCheck(task)">
                          <!-- 志愿者和管理员角色 -->
                          <template v-if="['volunteer', 'admin', 'super_admin'].includes(userInfo.role)">
                            <!-- 用户未参与此任务 - 显示申请加入 -->
                            <el-button
                                v-if="ThisVolunteerNotAttended(task, userInfo)"
                                type="primary"
                                @click.stop="applyToJoinTask(task)">
                              申请加入
                            </el-button>

                            <!-- 用户已申请加入(状态为5) - 显示等待提示 -->
                            <el-text
                                v-else-if="ThisVolunteerWaitingForApplyResult(task, userInfo)"
                                type="success"
                                size="small">
                              已申请加入，等待回复
                            </el-text>

                            <!-- 用户是任务接收者(状态不为5) - 显示操作按钮 -->
                            <template
                                v-else-if="ThisVolunteerIsAttended(task, userInfo)">
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
          <div class="pagination-container">
            <el-pagination
                v-model:current-page="pagination.currentPage"
                v-model:page-size="pagination.pageSize"
                :page-sizes="[10, 20, 50]"
                :total="pagination.total"
                layout="total, sizes, prev, pager, next, jumper"
                @size-change="handleSizeChange"
                @current-change="handlePageChange"
            />
          </div>
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
                v-model="rejectForm.remark"
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
          width="600px">
        <el-form :model="completeForm" ref="completeFormRef">
          <el-form-item label="任务编号" prop="taskId" required>
            <el-input
                v-model="completeForm.taskId"
                disabled>
            </el-input>
          </el-form-item>

          <el-form-item label="维修内容" prop="content" required>
            <el-input
                v-model="completeForm.content"
                disabled>
            </el-input>
          </el-form-item>

          <el-form-item label="维修时长" prop="duration" required>
            <el-input
                v-model="completeForm.duration"
                placeholder="请输入维修时长（如：2小时）">
            </el-input>
          </el-form-item>

          <el-form-item label="解决方法" prop="solution" required>
            <el-input
                v-model="completeForm.solution"
                type="textarea"
                placeholder="请输入解决方法"
                :rows="3">
            </el-input>
          </el-form-item>

          <el-form-item label="维修图片">
            <el-upload
                v-model:file-list="maintenanceImages"
                list-type="picture-card"
                :auto-upload="false"
                :multiple="true"
                :limit="maxImageCount"
                :on-exceed="handleExceed"
                :before-upload="beforeImageUpload">
              <el-icon><Plus /></el-icon>
            </el-upload>
            <div class="image-hint">最多可上传{{ maxImageCount }}张图片，支持JPG/PNG格式，单张不超过1MB</div>
          </el-form-item>
        </el-form>

        <template #footer>
          <el-button @click="completeDialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              @click="submitMaintenanceLog"
              :disabled="!completeForm.content || !completeForm.duration || !completeForm.solution">
            确定
          </el-button>
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
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px 0;
}
.image-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  margin-left: 10px;
}
.logs-section {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
.log-item {
  padding: 10px;
  margin-bottom: 10px;
  background-color: white;
  border-radius: 4px;
  border-left: 3px solid #409eff;
  width: 300px;
}
.log-text {
  word-wrap: break-word;
  word-break: break-all;
  white-space: normal;
}

.log-images {
  margin-top: 10px;
}

.log-image {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
  margin-right: 5px;
}
.team-section, .join-requests-section {
  margin-top: 15px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.team-member {
  display: inline-block;
  margin-right: 10px;
  margin-bottom: 5px;
}

.member-card {
  position: relative;
  margin-bottom: 10px;
}

.member-info {
  display: flex;
  align-items: center;
}

.member-info .el-avatar {
  margin-right: 15px;
}

.member-details {
  flex: 1;
}

.member-name-status {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.member-name {
  font-weight: bold;
  margin-right: 10px;
}

.member-role {
  margin-right: 8px;
}

.member-other-info {
  font-size: 13px;
  color: #606266;
}

.member-other-info p {
  margin: 3px 0;
  line-height: 1.4;
}

.clickable-avatar {
  cursor: pointer;
}
</style>
