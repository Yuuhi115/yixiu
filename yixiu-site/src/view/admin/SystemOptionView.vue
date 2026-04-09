<script setup>

import {Message, Bell} from "@element-plus/icons-vue";
import {onMounted, reactive, ref} from "vue";
import Cookie from "js-cookie";
import {checkToken, getUserInfo} from "../../api/userApi.js";
import {ElMessage} from "element-plus";
import router from "../../router/index.js";
import {AcceptSuperAdmin, RoleCheckAdmin, RoleCheckSuperAdmin, RoleCheckVolunteer} from "../../utils/roleCheckUtils.js";
import {
  modifyTaskApproveEmailNotifyStatus,
  queryTaskApproveEmailNotifyStatus
} from "../../api/adminApi.js";


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

const emailNotifyEnabled = ref(false)
const loading = ref(false)

onMounted(async () => {
  await queryUserInfo()
  await loadEmailNotifyStatus()
  RoleCheckSuperAdmin();
})



const queryUserInfo = async () => {
  const token = Cookie.get('Authorization')
  const response = await getUserInfo(token)
  if (response.code !== 200) {
    ElMessage.error(response.msg)
    return
  }
  Object.assign(userInfo, response.data)
  console.log(userInfo)
}

const loadEmailNotifyStatus = async () => {
  loading.value = true
  try {
    const response = await queryTaskApproveEmailNotifyStatus()
    if (response.code === 200) {
      emailNotifyEnabled.value = response.data === 1
    } else {
      ElMessage.error(response.msg || '加载配置失败')
    }
  } catch (error) {
    ElMessage.error('加载配置异常: ' + error.message)
  } finally {
    loading.value = false
  }
}

const handleEmailNotifyChange = async (value) => {
  const isOpen = value ? 1 : 0
  loading.value = true
  try {
    const response = await modifyTaskApproveEmailNotifyStatus(isOpen)
    if (response.code === 200) {
      ElMessage.success(value ? '已开启邮件通知' : '已关闭邮件通知')
    } else {
      ElMessage.error(response.msg || '修改失败')
      emailNotifyEnabled.value = !value
    }
  } catch (error) {
    ElMessage.error('修改异常: ' + error.message)
    emailNotifyEnabled.value = !value
  } finally {
    loading.value = false
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
                <el-menu-item index="1">设置</el-menu-item>
              </el-menu>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <div class="component-center">
                <el-avatar class="clickable-avatar" @click="() => router.push('/user/basicInfo')" :fit="'cover'" :src="userInfo.avatar"/>
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

      <el-main>
        <div class="main-content">
          <el-card class="settings-card" v-loading="loading">
            <template #header>
              <div class="card-header">
                <span class="card-title">系统设置</span>
              </div>
            </template>

            <div class="settings-container">
              <el-divider content-position="left">
                <el-icon><Bell /></el-icon>
                <span style="margin-left: 8px;">通知设置</span>
              </el-divider>

              <div class="setting-item">
                <div class="setting-info">
                  <div class="setting-label">
                    <span class="label-text">新任务邮件通知</span>
                    <el-tag type="info" size="small" effect="plain">管理员审核通过后触发</el-tag>
                  </div>
                  <div class="setting-description">
                    开启后，当管理员审核通过维修申请时，系统将自动向所有活跃志愿者发送邮件通知
                  </div>
                </div>
                <div class="setting-control">
                  <el-switch
                      v-model="emailNotifyEnabled"
                      @change="handleEmailNotifyChange"
                      :active-value="true"
                      :inactive-value="false"
                      active-text="开启"
                      inactive-text="关闭"
                      inline-prompt
                      style="--el-switch-on-color: #13ce66; --el-switch-off-color: #ff4949"
                  />
                </div>
              </div>

              <el-alert
                  title="提示"
                  type="info"
                  description="邮件发送频率已优化，每位志愿者之间会有一定间隔，避免频繁发送。"
                  show-icon
                  :closable="false"
                  class="setting-tip"
              />
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
  height: 100vh;
  width: 800px;
  margin-right: auto;
  margin-left: auto;

  display: flex;
  flex-direction: row-reverse;
  justify-content: flex-start;

  border: snow 8px solid;
  border-radius: 30px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  --el-box-shadow: ;
  background: white;
  padding: 20px;
}
.clickable-avatar {
  cursor: pointer;
}

.settings-card {
  width: 100%;
  border-radius: 12px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.settings-container {
  padding: 10px 0;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #ebeef5;
}

.setting-item:last-of-type {
  border-bottom: none;
}

.setting-info {
  flex: 1;
  margin-right: 20px;
}

.setting-label {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.label-text {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.setting-description {
  font-size: 14px;
  color: #909399;
  line-height: 1.6;
}

.setting-control {
  flex-shrink: 0;
}

.setting-tip {
  margin-top: 20px;
}

:deep(.el-divider__text) {
  display: flex;
  align-items: center;
  font-weight: 500;
  color: #606266;
}
</style>
