<script setup>
import 'vue-cropper/dist/index.css'
import { VueCropper } from 'vue-cropper'
import { Message } from "@element-plus/icons-vue";
import {onMounted, reactive, ref, computed, onUnmounted} from "vue";
import Cookie from "js-cookie";
import {getUserInfo, updateAvatar, updateUserInfo} from "../../api/userApi.js";
import {updateVolunteerInfo} from "../../api/volunteerApi.js";
import { ElMessage } from "element-plus";
import { Edit, Camera, Plus } from '@element-plus/icons-vue'
import router from "../../router/index.js";
import {getUnreadNotifyCount} from "../../api/notificationApi.js";

/*头像剪裁*/
const cropperRef = ref()
const croppedAvatarUrl = ref('')

const userInfoRef = ref()
let unreadNotifyCount = ref(0)
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
  volunteerInfo: {
    volunteerId: "",
    studentNumber: "",
    majorClass: "",
    grade: "",
    contactType: "",
    contactNumber: "",
  }
})

onMounted(async () => {
  await queryUserInfo()
  await getUnreadNotify()
})

// 计算属性获取角色信息
const userRole = computed(() => {
  return localStorage.getItem('role')
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

// 未读通知数
const getUnreadNotify = async () => {
  const response = await getUnreadNotifyCount()
  if (response.code !== 200) {
    ElMessage.error(response.msg)
    return
  }
  unreadNotifyCount.value = response.data
}

// 基本信息修改用户名和名字
const editProfileDialogVisible = ref(false)
const editProfileForm = reactive({
  username: '',
  realName: ''
})
const editProfileRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ]
}
const editProfileFormRef = ref()

const editProfile = () => {
  // 初始化表单数据
  editProfileForm.username = userInfo.username
  editProfileForm.realName = userInfo.realName
  // 显示对话框
  editProfileDialogVisible.value = true
}

const saveProfileEdit = async () => {
  editProfileFormRef.value.validate(async (valid) => {
    if (valid) {
      // 调用更新用户信息的API
      const response = await updateUserInfo(editProfileForm)
      if (response.code === 200) {
        // 更新成功后更新本地数据
        userInfo.username = editProfileForm.username
        userInfo.realName = editProfileForm.realName
        ElMessage.success('修改成功')
      } else {
        ElMessage.error(response.msg)
      }
      editProfileDialogVisible.value = false
    }
  })
}

// 志愿者信息编辑相关
const editVolunteerDialogVisible = ref(false)
const editVolunteerForm = reactive({
  volunteerId: '',
  studentNumber: '',
  majorClass: '',
  grade: '',
  contactType: "",
  contactNumber: "",
})
const editVolunteerFormRef = ref()

const getContactTypeText = (contactType) => {
  switch (String(contactType)) {
    case '0':
      return '手机号'
    case '1':
      return '邮箱号'
    case '2':
      return '微信号'
    case '3':
      return 'QQ号'
    default:
      return '未知'
  }
}

const editVolunteerInfo = () => {
  // 初始化表单数据（示例数据）
  editVolunteerForm.userId = userInfo.userId
  editVolunteerForm.volunteerId = userInfo.volunteerInfo.volunteerId
  editVolunteerForm.studentNumber = userInfo.volunteerInfo.studentNumber
  editVolunteerForm.majorClass = userInfo.volunteerInfo.majorClass
  editVolunteerForm.grade = userInfo.volunteerInfo.grade
  editVolunteerForm.contactType = userInfo.volunteerInfo.contactType
  editVolunteerForm.contactNumber = userInfo.volunteerInfo.contactNumber
  // 显示对话框
  editVolunteerDialogVisible.value = true
}

const saveVolunteerEdit = async () => {
  editVolunteerFormRef.value.validate?.(async (valid) => {
    if (valid) {
      const response = await updateVolunteerInfo(editVolunteerForm)
      if (response.code !== 200) {
        ElMessage.error(response.msg)
        return
      }
      await queryUserInfo()
      ElMessage.success('志愿者信息修改成功')
      editVolunteerDialogVisible.value = false
    }
  })
}


/*电话和邮箱更新*/
const editDialogVisible = ref(false)
const editFieldType = ref('') // 'phone' 或 'email'
const editForm = reactive({
  newValue: '',
  captcha: ''
})

// 打开编辑对话框
const openEditDialog = (fieldType) => {
  editFieldType.value = fieldType
  editForm.newValue = ''
  editForm.captcha = ''
  editDialogVisible.value = true
}

// 发送验证码
const sendCaptcha = async () => {
  if (!editForm.newValue) {
    ElMessage.warning('请输入新值')
    return
  }

  // 验证手机号或邮箱格式
  if (editFieldType.value === 'phone' && !/^1[3-9]\d{9}$/.test(editForm.newValue)) {
    ElMessage.error('手机号格式不正确')
    return
  }

  if (editFieldType.value === 'email' && !/^\w+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/.test(editForm.newValue)) {
    ElMessage.error('邮箱格式不正确')
    return
  }

  // 调用发送验证码API
  // await sendLREmailVerificationCode(editForm.newValue)
  ElMessage.success('验证码已发送')
}

// 保存修改
const saveEdit = async () => {
  if (!editForm.newValue || !editForm.captcha) {
    ElMessage.warning('请填写完整信息')
    return
  }

  // 调用更新API
  // const token = Cookie.get('Authorization')
  // const response = await updateUserInfo(token, {
  //   [editFieldType.value]: editForm.newValue,
  //   captcha: editForm.captcha
  // })

  // if (response.code === 200) {
  //   userInfo[editFieldType.value] = editForm.newValue
  //   editDialogVisible.value = false
  //   ElMessage.success('修改成功')
  // } else {
  //   ElMessage.error(response.msg)
  // }

  userInfo[editFieldType.value] = editForm.newValue
  editDialogVisible.value = false
  ElMessage.success('修改成功')
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
                <el-menu-item index="1">基本信息</el-menu-item>
                <el-menu-item index="2">我的收藏</el-menu-item>
                <el-menu-item index="3" @click="() => router.push('/user/messageCenter')">消息中心</el-menu-item>
              </el-menu>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="grid-content ep-bg-purple">
              <div class="component-center">
                <el-avatar :fit="'cover'" :src="userInfo.avatar" />
              </div>
              <div class="component-center">
                <el-badge :is-dot="unreadNotifyCount > 0" class="item">
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
                    <el-tag :type="userInfo.status === '1' ? 'success' : 'danger'">
                      {{ userInfo.status === '1' ? '在线' : '离线' }}
                    </el-tag>
                  </p>
                  <p>
                    {{
                      userInfo.role === 'super_admin' ? '超级管理员' :
                      userInfo.role === 'admin' ? '管理员' :
                      userInfo.role === 'volunteer' ? '志愿者' : '普通用户'
                    }}
                  </p>
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

                <el-descriptions :column="1" border class="uniform-descriptions">
                  <el-descriptions-item label="用户ID">{{ userInfo.userId }}</el-descriptions-item>
                  <el-descriptions-item label="用户名">{{ userInfo.username }}</el-descriptions-item>
                  <el-descriptions-item label="真实姓名">{{ userInfo.realName }}</el-descriptions-item>
                  <el-descriptions-item label="手机号">
                    {{ userInfo.phone }}
                    <el-link :underline="false" type="primary" class="right-aligned-text" @click="openEditDialog('phone')">
                      修改
                    </el-link>
                  </el-descriptions-item>
                  <el-descriptions-item label="邮箱">
                    {{ userInfo.email }}
                    <el-link :underline="false" type="primary" class="right-aligned-text" @click="openEditDialog('email')">
                      修改
                    </el-link>
                  </el-descriptions-item>
                  <el-descriptions-item label="角色">
                    {{
                      userInfo.role === 'super_admin' ? '超级管理员' :
                      userInfo.role === 'admin' ? '管理员' :
                      userInfo.role === 'volunteer' ? '志愿者' : '普通用户'
                    }}
                  </el-descriptions-item>
                </el-descriptions>
              </el-card>

              <!-- 志愿者信息卡片 -->
              <el-card class="info-card" v-if="userRole !== 'student'">
                <template #header>
                  <div class="card-header">
                    <span>志愿者信息</span>
                    <el-button type="primary" @click="editVolunteerInfo">编辑信息</el-button>
                  </div>
                </template>

                <el-descriptions :column="1" border class="uniform-descriptions">
                  <el-descriptions-item label="志愿者ID">{{ userInfo.volunteerInfo.volunteerId }}</el-descriptions-item>
                  <el-descriptions-item label="学号">{{ userInfo.volunteerInfo.studentNumber }}</el-descriptions-item>
                  <el-descriptions-item label="专业班级">{{ userInfo.volunteerInfo.majorClass }}</el-descriptions-item>
                  <el-descriptions-item label="年级">{{ userInfo.volunteerInfo.grade }}</el-descriptions-item>
                  <el-descriptions-item label="联系方式">{{ getContactTypeText(userInfo.volunteerInfo.contactType) }}</el-descriptions-item>
                  <el-descriptions-item label="联系号码">{{ userInfo.volunteerInfo.contactNumber }}</el-descriptions-item>
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

    <!--修改手机号和邮箱dialog-->
    <el-dialog v-model="editDialogVisible" :title="editFieldType === 'phone' ? '修改手机号' : '修改邮箱'" width="500px" center>
      <el-form :model="editForm" label-width="80px">
        <el-form-item :label="editFieldType === 'phone' ? '新手机号' : '新邮箱'">
          <el-input v-model="editForm.newValue" :placeholder="editFieldType === 'phone' ? '请输入新手机号' : '请输入新邮箱'" />
        </el-form-item>
        <el-form-item label="验证码">
          <el-row :gutter="10">
            <el-col :span="16">
              <el-input v-model="editForm.captcha" placeholder="请输入验证码" />
            </el-col>
            <el-col :span="8">
              <el-button @click="sendCaptcha">发送</el-button>
            </el-col>
          </el-row>
        </el-form-item>
      </el-form>
      <template #footer>
    <span class="dialog-footer">
      <el-button @click="editDialogVisible = false">取消</el-button>
      <el-button type="primary" @click="saveEdit">确定</el-button>
    </span>
      </template>
    </el-dialog>

    <!-- 修改用户名和名字dialog -->
    <el-dialog v-model="editProfileDialogVisible" title="编辑基本信息" width="500px" center>
      <el-form
          ref="editProfileFormRef"
          :model="editProfileForm"
          :rules="editProfileRules"
          label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="editProfileForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="editProfileForm.realName" placeholder="若不方便透露，可填写(姓氏+同学)，例如张同学" />
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="editProfileDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveProfileEdit">确定</el-button>
      </span>
      </template>
    </el-dialog>

    <!-- 志愿者信息编辑对话框 -->
    <el-dialog v-model="editVolunteerDialogVisible" title="编辑志愿者信息" width="500px" center>
      <el-form
          ref="editVolunteerFormRef"
          :model="editVolunteerForm"
          label-width="80px"
      >
        <el-form-item label="志愿者ID">
          <el-input v-model="editVolunteerForm.volunteerId" disabled />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model="editVolunteerForm.studentNumber" />
        </el-form-item>
        <el-form-item label="专业班级">
          <el-input v-model="editVolunteerForm.majorClass" />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="editVolunteerForm.grade" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-radio-group v-model="editVolunteerForm.contactType">
            <el-radio label="0">手机号</el-radio>
            <el-radio label="1">邮箱号</el-radio>
            <el-radio label="2">微信号</el-radio>
            <el-radio label="3">QQ号</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="联系号码">
          <el-input v-model="editVolunteerForm.contactNumber" />
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="editVolunteerDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveVolunteerEdit">确定</el-button>
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
  border-radius: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
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

.uniform-descriptions :deep(.el-descriptions__label) {
  width: 120px; /* 信息展示表格固定标签宽度 */
}
</style>