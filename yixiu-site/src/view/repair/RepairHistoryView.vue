<script setup>
import { Message } from "@element-plus/icons-vue";
import { onMounted, reactive, ref } from "vue";
import Cookie from "js-cookie";
import {getRepairFormByUserId, getUserInfo} from "../../api/userApi.js";
import { ElMessage } from "element-plus";
import router from "../../router/index.js";

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

const repairRecords = reactive([
    {
      requestId: "",
      contactType: "",
      deviceType: "",
      deviceModel: "",
      problemDescription: "",
      campus: "",
      repairLocation: "",
      appointmentTime: "",
      status: "",
      createTime: "",
      completeTime: "",
      updateTime: "",
      imgUrl: []
    }
])

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
]

// 下拉菜单相关
const activeTab = ref('all')

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
  // 这里应该调用API获取维修历史记录
  // 暂时使用模拟数据
  const response = await getRepairFormByUserId()
  if (response.code === 200) {
    // 赋值给 repairRecords 列表
    Object.assign(repairRecords, response.data)
    console.log(repairRecords)
  } else {
    ElMessage.error(response.msg)
  }
}

// 处理筛选
const handleFilter = () => {
  // 实际应用中应调用API并传递筛选参数
  console.log('筛选条件:', filterForm)
  ElMessage.info('筛选功能已触发，请连接实际API')
}

// 重置筛选
const resetFilter = () => {
  filterForm.createTime = []
  filterForm.updateTime = []
  filterForm.status = ''
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
    case 1: return 'primary'  // 审核通过
    case 2: return 'primary'  // 已接收
    case 3: return 'success'   // 已完成
    case 4: return 'danger'    // 已取消
    case 5: return 'success'  // 用户自行解决
    case 6: return 'danger' //已被拒绝
  }
}

// 切换视图
const switchView = (tab) => {
  activeTab.value = tab
  // 根据标签过滤数据
  console.log('切换到标签:', tab)
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
            <el-dropdown @command="switchView">
              <el-button type="primary">
                查看方式<i class="el-icon-arrow-down el-icon--right"></i>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="all">全部记录</el-dropdown-item>
                  <el-dropdown-item command="recent">最近记录</el-dropdown-item>
                  <el-dropdown-item command="pending">待处理</el-dropdown-item>
                  <el-dropdown-item command="processing">处理中</el-dropdown-item>
                  <el-dropdown-item command="completed">已完成</el-dropdown-item>
                  <el-dropdown-item command="cancelled">已取消</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>

            <el-badge :value="repairRecords.length" class="records-count" type="primary">
              共 {{ repairRecords.length }} 条记录
            </el-badge>
          </div>

          <!-- 维修记录列表 -->
          <el-card class="records-card">
            <template #header>
              <div class="card-header">
                <span>维修记录详情</span>
              </div>
            </template>

            <el-empty v-if="repairRecords.length === 0" description="暂无维修记录" />

            <div class="records-container">
              <el-collapse v-model="activeNames" accordion>
                <el-collapse-item
                    v-for="(record, index) in repairRecords"
                    :key="record.id"
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

                    <div class="detail-footer">
                      <div class="time-info">
                        <span>创建时间: {{ record.createTime }}</span>
                        <span>完成时间: {{ record.completeTime }}</span>
                        <span>更新时间: {{ record.updateTime }}</span>
                      </div>
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
          </el-card>
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
</style>
