<script setup>
import { Message } from "@element-plus/icons-vue"
import {computed, onMounted, reactive, ref} from "vue"
import Cookie from "js-cookie"
import {addEvaluation, getRepairFormByFilterLimitUser, getRepairFormByUserId, getUserInfo} from "../../api/userApi.js"
import { ElMessage } from "element-plus"
import router from "../../router/index.js"
import { ElRate, ElDialog } from 'element-plus'


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

// 维修记录数据
const repairRecordsRef = ref([])
const evaluationFormRef = ref()

const pagination = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 筛选条件
const filterForm = reactive({
  createTime: [],
  updateTime: [],
  status: ''
})

// 状态选项
const statusOptions = [
  { label: '已提交待审核', value: 0 },
  { label: '审核通过', value: 1 },
  { label: '已接收', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
  { label: '用户自行解决', value: 5 },
  { label: '已被拒绝', value: 6 },
  { label: '已完成评价', value: 7 },
]

onMounted(async () => {
  await queryUserInfo()
  await loadRepairHistory()
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

// 加载维修历史记录
const activeNames = ref([])

const loadRepairHistory = async () => {
  const response = await getRepairFormByUserId(pagination.currentPage, pagination.pageSize)
  if (response.code === 200) {
    // 赋值给 repairRecordsRef 列表
    repairRecordsRef.value = response.data.list
    pagination.total = response.data.total
    console.log(repairRecordsRef.value)
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

// 处理任务筛选
const handleFilter = async () => {
  const queryParams = buildQueryParams()
  // console.log('筛选条件:', queryParams)
  queryParams.pageNum = pagination.currentPage
  queryParams.pageSize = pagination.pageSize
  queryParams.userId = userInfo.userId

  const response = await getRepairFormByFilterLimitUser(queryParams)

  if (response.code === 200) {
    repairRecordsRef.value = response.data.list
    pagination.total = response.data.total
    console.log(repairRecordsRef.value)
  } else {
    ElMessage.error(response.msg)
  }
}

// 重置筛选
const resetFilter = () => {
  filterForm.createTime = []
  filterForm.updateTime = []
  filterForm.status = ''
  pagination.currentPage = 1
  loadRepairHistory()
}

// 获取状态标签
const getStatusLabel = (status) => {
  const option = statusOptions.find(item => item.value === status)
  return option ? option.label : '未知状态'
}

// 获取状态标签类型
const getStatusType = (status) => {
  switch(status) {
    case 0: return 'warning'     // 已提交待审核
    case 1: return 'primary'     // 审核通过
    case 2: return 'primary'     // 已接收
    case 3: return 'success'     // 已完成
    case 4: return 'danger'      // 已取消
    case 5: return 'success'     // 用户自行解决
    case 6: return 'danger'      // 已被拒绝
  }
}

// 分页改变处理函数
const handlePageChange = (newPage) => {
  pagination.currentPage = newPage
  conditionRepairHistory()
}

const handleSizeChange = (newSize) => {
  pagination.pageSize = newSize
  pagination.currentPage = 1
  conditionRepairHistory()
}

const conditionRepairHistory = () => {
  if (filterForm.createTime?.length || filterForm.updateTime?.length || filterForm.status !== '') {
    handleFilter()
  } else {
    loadRepairHistory()
  }
}

// 评价相关数据
const evaluationDialogVisible = ref(false)
let currentRecord = ref(null)

const evaluationForm = reactive({
  satisfaction: 0,
  content: ''
})

// 评价表单验证规则
const evaluationRules = {
  satisfaction: [
    { required: true, message: '请选择满意度', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 5, max: 500, message: '评价内容长度应在5到500个字符之间', trigger: 'blur' }
  ]
}

// 打开评价弹窗
const openEvaluationDialog = (record) => {
  currentRecord = record
  evaluationForm.satisfaction = 0
  evaluationForm.content = ''
  evaluationDialogVisible.value = true
}

// 关闭评价弹窗
const closeEvaluationDialog = () => {
  evaluationDialogVisible.value = false
  // 重置表单
  Object.assign(evaluationForm, {
    satisfaction: 0,
    content: ''
  })
}

/*评价部分*/
// 提交评价
const submitEvaluation = async () => {
  evaluationFormRef.value.validate(async (valid) => {
    if (valid) {
      let data = {
        requestId: currentRecord.requestId,
        score: evaluationForm.satisfaction,
        content: evaluationForm.content
      }

      const response = await addEvaluation(data)

      if (response.code !== 200){
        ElMessage.error(response.msg)
      }

      console.log('提交评价:', {
        recordId: currentRecord.requestId,
        satisfaction: evaluationForm.satisfaction,
        content: evaluationForm.content
      })

      ElMessage.success('评价提交成功！')
      evaluationDialogVisible.value = false
      await conditionRepairHistory()
    } else {
      ElMessage.error('请完善必填信息')
    }
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
              <h3 class="clickable-title" style="margin-right: 100px" @click="() => router.push('/')">Light义修帮</h3>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="grid-content ep-bg-purple">
              <el-menu
                  default-active="3"
                  class="el-menu-demo"
                  mode="horizontal"
                  style="border-bottom: silver solid 1px;background-color: snow"
                  :ellipsis="false"
              >
                <el-menu-item index="1" @click="() => router.push('/repair/form')">填写预约问卷</el-menu-item>
                <el-menu-item index="2" @click="() => router.push('/repair/template')">预约信息模板管理</el-menu-item>
                <el-menu-item index="3">预约历史</el-menu-item>
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
            <el-badge :value="pagination.total" class="records-count" type="primary">
              共 {{ pagination.total }} 条记录
            </el-badge>
          </div>

          <!-- 维修记录列表 -->
          <el-card class="records-card">
            <template #header>
              <div class="card-header">
                <span>维修记录详情</span>
              </div>
            </template>

            <el-empty v-if="repairRecordsRef.length === 0" description="暂无维修记录" />

            <div class="records-container">
              <el-collapse v-model="activeNames" accordion>
                <el-collapse-item
                    v-for="(record, index) in repairRecordsRef"
                    :key="record.requestId"
                    :name="index">
                  <template #title>
                    <div class="collapse-header">
                      <span class="record-id">申请编号: {{ record.requestId }}</span>
                      <el-tag :type="getStatusType(record.status)" size="small">
                        {{ getStatusLabel(record.status) }}
                      </el-tag>
                      <span class="record-time">{{ record.createTime }}</span>
                    </div>
                  </template>

                  <div class="record-detail">
                    <el-row :gutter="20">
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">设备类型:</span>
                          <span class="detail-value">{{ record.deviceType }}</span>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">设备型号:</span>
                          <span class="detail-value">{{ record.deviceModel }}</span>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">联系方式:</span>
                          <span class="detail-value">{{ record.contactType }}</span>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">预约时间:</span>
                          <span class="detail-value">{{ record.appointmentTime }}</span>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">维修地点:</span>
                          <span class="detail-value">{{ record.repairLocation }}</span>
                        </div>
                      </el-col>
                      <el-col :span="12">
                        <div class="detail-item">
                          <span class="detail-label">校区:</span>
                          <span class="detail-value">{{ record.campus === "0" ? '大学城校区' : '白云山校区' }}</span>
                        </div>
                      </el-col>
                    </el-row>

                    <div class="detail-item full-width">
                      <span class="detail-label">问题描述:</span>
                      <span class="detail-value">{{ record.problemDescription }}</span>
                    </div>

                    <div class="detail-item full-width" v-if="record.imgUrl && record.imgUrl.length > 0">
                      <span class="detail-label">故障图片:</span>
                      <div class="detail-value image-gallery">
                        <el-image
                            v-for="(image, imgIndex) in record.imgUrl"
                            :key="imgIndex"
                            :src="image"
                            :preview-src-list="record.imgUrl"
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

                    <!-- 队员信息 -->
                    <div class="detail-item full-width">
                      <span class="detail-label">任务队员:</span>
                      <div class="detail-value team-section">
                        <el-empty v-if="!record.repairAssignment || record.repairAssignment.length === 0" description="暂无队员信息"/>
                        <div v-else>
                          <div v-for="(member, idx) in record.repairAssignment.filter(assignment => assignment.status !== 5 && assignment.status !== 6)" :key="idx" class="team-member">
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
                                    <p class="member-class">班级: {{ member.majorClass }}</p>
                                    <p class="member-grade">年级: {{ member.grade }}</p>
                                    <p class="member-contactType">联系方式: {{
                                        member.contactType === 0 ? '手机号' :
                                            member.contactType === 1 ? '邮箱号' :
                                                member.contactType === 2 ? '微信号' :
                                                    member.contactType === 3 ? 'QQ号' : '未知' }}</p>
                                    <p class="member-contactNumber">联系号码: {{ member.contactNumber }}</p>
                                  </div>
                                </div>
                              </div>
                            </el-card>
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- 任务评价 -->
                    <div class="detail-item full-width" v-if="record.status === 7 && record.repairEvaluate">
                      <span class="detail-label">任务评价:</span>
                      <div class="detail-value evaluation-section">
                        <el-descriptions :column="1" size="small">
                          <el-descriptions-item label="评分" :label-style="{ fontWeight: 'bold', color: '#409EFF' }">
                            <el-rate
                                v-model="record.repairEvaluate.score"
                                disabled
                                :max="5"
                                show-text
                                :texts="['非常差', '差', '一般', '好', '非常好']"
                            />
                          </el-descriptions-item>
                          <el-descriptions-item label="评价内容" :label-style="{ fontWeight: 'bold', color: '#409EFF' }">
                            <span>{{ record.repairEvaluate.content }}</span>
                          </el-descriptions-item>
                          <el-descriptions-item label="评价时间" :label-style="{ fontWeight: 'bold', color: '#409EFF' }">
                            <span>{{ record.repairEvaluate.createTime }}</span>
                          </el-descriptions-item>
                        </el-descriptions>
                      </div>
                    </div>

                    <!-- 如果记录没有评价但状态为7，显示暂无评价 -->
                    <div class="detail-item full-width" v-else-if="record.status === 3">
                      <span class="detail-label">任务评价:</span>
                      <span class="detail-value">暂无评价信息</span>
                    </div>

                    <div class="detail-footer">
                      <div class="time-info">
                        <span>创建时间: {{ record.createTime }}</span>
                        <span>完成时间: {{ record.completeTime }}</span>
                        <span>更新时间: {{ record.updateTime }}</span>
                      </div>
                      <!-- 评价按钮 -->
                      <div class="action-buttons" v-if="record.status === 3">
                        <el-button
                            type="primary"
                            @click="openEvaluationDialog(record)"
                        >
                          填写评价
                        </el-button>
                      </div>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </el-card>

          <!-- 分页组件 -->
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
    </el-container>
  </div>

  <!-- 评价弹窗 -->
  <el-dialog
      v-model="evaluationDialogVisible"
      title="填写维修评价"
      width="500px"
      @close="closeEvaluationDialog"
  >
    <el-form :model="evaluationForm" :rules="evaluationRules" ref="evaluationFormRef">
      <el-form-item label="满意度" prop="satisfaction" label-width="100px">
        <el-rate
            v-model="evaluationForm.satisfaction"
            :max="5"
            show-text
            :texts="['非常差', '差', '一般', '好', '非常好']"
        />
      </el-form-item>

      <el-form-item label="评价内容" prop="content" label-width="100px">
        <el-input
            v-model="evaluationForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入您的评价内容..."
            maxlength="500"
            show-word-limit
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeEvaluationDialog">取消</el-button>
        <el-button type="primary" @click="submitEvaluation">提交评价</el-button>
      </span>
    </template>
  </el-dialog>
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

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding: 20px 0;
}

.team-section {
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
.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.evaluation-section .el-descriptions {
  border: none;
  background: #f0f9ff;
  border-radius: 8px;
  padding: 15px;
}

.evaluation-section .el-descriptions .el-descriptions-item__label {
  font-weight: bold;
  color: #409eff;
  padding-right: 10px;
}

.evaluation-section .el-descriptions .el-descriptions-item__content {
  padding: 2px 0;
}

.dialog-footer {
  display: flex;
  justify-content: space-between;
  width: 100%;
}
</style>
