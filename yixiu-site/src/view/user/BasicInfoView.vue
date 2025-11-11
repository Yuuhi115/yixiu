<script setup>
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import { Message } from "@element-plus/icons-vue";
import { onMounted, reactive, ref } from "vue";
import Cookie from "js-cookie";
import {getUserInfo, updateAvatar} from "../../api/userApi.js";
import { ElMessage } from "element-plus";
import { Edit, Camera, Plus } from '@element-plus/icons-vue'

/*头像剪裁*/
const cropperRef = ref()
const croppedAvatarUrl = ref('')

const userInfoRef = ref()
const fileInput = ref()
const uploadRef = ref()
const avatarDialogVisible = ref(false)
const tempAvatarUrl = ref('')
const tempAvatarFile = ref(null)

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

onMounted(async () => {
  await queryUserInfo()
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

const editProfile = () => {
  ElMessage.info('编辑功能待实现')
}

// 点击头像更换
const changeAvatar = () => {
  avatarDialogVisible.value = true
}

// 处理头像文件选择
const handleAvatarChange = (file) => {
  const rawFile = file.raw
  if (rawFile) {
    // 验证文件类型
    if (!rawFile.type.includes('image')) {
      ElMessage.error('只能上传图片文件!')
      return
    }
    // 验证文件大小
    if (rawFile.size > 2 * 1024 * 1024) {
      ElMessage.error('图片大小不能超过 2MB!')
      return
    }

    tempAvatarFile.value = rawFile
    tempAvatarUrl.value = URL.createObjectURL(rawFile)
  }
}

// 取消剪裁，重新选择
const cancelCrop = () => {
  tempAvatarUrl.value = ''
  croppedAvatarUrl.value = ''
  tempAvatarFile.value = null
}

// 确认剪裁
const confirmCrop = () => {
  cropperRef.value.getCropData(data => {
    croppedAvatarUrl.value = data
  })
}

// 转换 Data URL 为 Blob 对象
const dataURLtoBlob = (dataurl) => {
  let arr = dataurl.split(',')
  let mime = arr[0].match(/:(.*?);/)[1]
  let bstr = atob(arr[1])
  let n = bstr.length
  let u8arr = new Uint8Array(n)
  while (n--) {
    u8arr[n] = bstr.charCodeAt(n)
  }
  return new Blob([u8arr], { type: mime })
}

// 完成头像更换
const finishAvatarChange = async () => {
  if (!croppedAvatarUrl.value) {
    ElMessage.warning('请先剪裁图片!')
    return
  }
  // 将 base64 转换为 Blob
  const blob = dataURLtoBlob(croppedAvatarUrl.value)
  const file = new File([blob], "avatar.jpg", { type: "image/jpg" })

  let result = await uploadAvatar(file)
  if (!result){
    ElMessage.error('头像更换失败!')
  }else {
    // 更新用户头像
    ElMessage.success('头像更换成功!')
    userInfo.avatar = croppedAvatarUrl.value
  }
  avatarDialogVisible.value = false
  // 重置状态
  tempAvatarUrl.value = ''
  croppedAvatarUrl.value = ''
  tempAvatarFile.value = null
}

// 上传头像到服务器的函数（需要实现）
const uploadAvatar = async (file) => {
  // 实现头像上传逻辑
  const formData = new FormData()
  formData.append('avatar', file)
  const response = await updateAvatar(formData)
  if (response.code === 200) {
    // userInfo.avatar = response.data.avatarUrl
    ElMessage.success('头像上传成功!')
    return true
  } else {
    ElMessage.error('头像上传失败!')
    return false
  }
}
</script>


<template>
  <div class="common-layout">
    <el-container style="height: 100%">
      <el-header class="header_container">
        <!-- 头部内容保持不变 -->
        <el-row :gutter="20">
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <h3 class="mb-3" style="margin-right: 100px">Light义修帮</h3>
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
                <el-menu-item index="1">基本信息</el-menu-item>
                <el-menu-item index="2">申请历史</el-menu-item>
                <el-menu-item index="3">我的收藏</el-menu-item>
                <el-menu-item index="4">消息中心</el-menu-item>
              </el-menu>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <div class="component-center">
                <el-avatar :size="50" :src="userInfo.avatar" />
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
          <el-row :gutter="20" class="profile-container">
            <el-col :span="8">
              <div class="avatar-section">
                <!-- 主头像区域，添加点击事件 -->
                <div class="avatar-container" @click="changeAvatar">
                  <el-avatar :size="120" :src="userInfo.avatar" />
                  <div class="camera-icon">
                    <el-icon><Edit /></el-icon>
                  </div>
                </div>
                <p>{{ userInfo.username }}</p>
              </div>

              <!-- 其他内容保持不变 -->
              <el-card class="info-card">
                <template #header>
                  <div class="card-header">
                    <span>账户状态</span>
                  </div>
                </template>
                <div class="status-info">
                  <p>状态:
                    <el-tag :type="userInfo.status === 'active' ? 'success' : 'danger'">
                      {{ userInfo.status === 'active' ? '在线' : '离线' }}
                    </el-tag>
                  </p>
                  <p>身份: {{userInfo.role === 'volunteer' ? '志愿者' : '普通用户'}}</p>
                  <p>最后登录: {{ userInfo.lastLogin }}</p>
                </div>
              </el-card>
            </el-col>

            <el-col :span="16">
              <el-card class="info-card">
                <template #header>
                  <div class="card-header">
                    <span>基本信息</span>
                    <el-button type="primary" @click="editProfile">编辑信息</el-button>
                  </div>
                </template>

                <el-descriptions :column="1" border>
                  <el-descriptions-item label="用户ID">{{ userInfo.userId }}</el-descriptions-item>
                  <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
                  <el-descriptions-item label="真实姓名">{{ userInfo.realName }}</el-descriptions-item>
                  <el-descriptions-item label="手机号">{{ userInfo.phone }}</el-descriptions-item>
                  <el-descriptions-item label="邮箱">{{ userInfo.email }}</el-descriptions-item>
                  <el-descriptions-item label="角色">
                    {{ userInfo.role === 'volunteer' ? '志愿者' : '普通用户' }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>
            </el-col>
          </el-row>
        </div>
      </el-main>
    </el-container>

    <el-dialog v-model="avatarDialogVisible" title="更换头像" width="600px" center>
      <div class="avatar-upload-area">
        <!-- 文件选择区域 -->
        <div v-if="!tempAvatarUrl" class="upload-select-area">
          <el-upload
              ref="uploadRef"
              class="avatar-uploader"
              :auto-upload="false"
              :show-file-list="false"
              :on-change="handleAvatarChange"
              accept="image/*"
          >
            <el-icon class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div class="upload-tip">
            <p>支持JPG、PNG格式，文件小于2MB</p>
          </div>
        </div>

        <!-- 图片剪裁区域 -->
        <div v-else class="cropper-area">
          <vue-cropper
              ref="cropperRef"
              :img="tempAvatarUrl"
              :output-size="1"
              :output-type="'png'"
              :info="true"
              :full="true"
              :can-move="true"
              :can-move-box="true"
              :fixed-box="false"
              :original="false"
              :auto-crop="true"
              :auto-crop-width="200"
              :auto-crop-height="200"
              :center-box="true"
              :high="true"
              :crop-switch="true"
              :enlarge="1"
              :mode="'contain'"
              :limit-minSize="[100, 100]"
          ></vue-cropper>

          <div class="cropper-actions">
            <el-button @click="cancelCrop">重新选择</el-button>
            <el-button type="primary" @click="confirmCrop">确认剪裁</el-button>
          </div>
        </div>
      </div>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="avatarDialogVisible = false">取消</el-button>
          <el-button
              type="primary"
              @click="finishAvatarChange"
              :disabled="!croppedAvatarUrl"
          >
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>/* 保留原有样式 */
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
  box-shadow: silver 0 0 10px;
  background: white;
}

.profile-container {
  width: 100%;
  margin-top: 20px;
}

.avatar-section {
  text-align: center;
  margin-bottom: 30px;
  position: relative;
}

/* 新增头像样式 */
.avatar-container {
  position: relative;
  display: inline-block;
  cursor: pointer;
}

.avatar-container:hover .camera-icon {
  opacity: 1;
}

.camera-icon {
  position: absolute;
  bottom: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  width: 30px;
  height: 30px;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  color: white;
  opacity: 0;
  transition: opacity 0.3s;
}

.avatar-wrapper:hover .avatar-overlay {
  opacity: 1;
}

.info-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-info p {
  margin: 10px 0;
}



/* 新增头像剪裁相关样式 */
.cropper-area {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.cropper-area .vue-cropper {
  width: 400px;
  height: 300px;
  margin: 0 auto;
}

.cropper-actions {
  margin-top: 20px;
  display: flex;
  gap: 10px;
}

.upload-select-area {
  text-align: center;
}
/* 头像上传区域样式 */
.avatar-upload-area {
  text-align: center;
}

.avatar-uploader .avatar-preview {
  width: 178px;
  height: 178px;
  display: block;
  border-radius: 6px;
}

.avatar-uploader .avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader .avatar-uploader-icon:hover {
  border-color: #409eff;
}

.upload-tip {
  margin-top: 10px;
  color: #909399;
  font-size: 12px;
}
</style>