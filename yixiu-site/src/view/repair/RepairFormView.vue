<script setup>

import {Message, Plus} from "@element-plus/icons-vue";
import {onMounted, reactive, ref} from "vue";
import Cookie from "js-cookie";
import {getUserInfo, submitRepairForm, submitRepairFormImg} from "../../api/userApi.js";
import {ElMessage} from "element-plus";
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

/*报修单部分*/

// 表单引用
const repairFormRef = ref()

const repairForm = reactive({
  contactType: '',
  contactInfo: '',
  deviceType: '',
  deviceSystem: '',
  deviceModel: '',
  problemDescription: '',
  campus: 0,
  repairLocation: '',
  appointmentTime: '',
  remarks: '',
  status: 0 // 默认状态为待处理
})

// 表单验证规则
const rules = {
  contactType: [
    { required: true, message: '请选择联系方式', trigger: 'change' }
  ],
  contactInfo: [
    { required: true, message: '请输入联系号码', trigger: 'blur' }
  ],
  deviceType: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  deviceSystem: [
    { required: true, message: '请选择设备系统', trigger: 'change' }
  ],
  deviceModel: [
    { required: true, message: '请输入设备型号', trigger: 'blur' }
  ],
  problemDescription: [
    { required: true, message: '请描述设备遇到的具体问题', trigger: 'blur' }
  ],
  repairLocation: [
    { required: true, message: '请输入具体维修地点', trigger: 'blur' }
  ],
  appointmentTime: [
    { required: true, message: '请输入预约时间', trigger: 'blur' }
  ]
}

// 设备类型与系统的映射关系
const deviceSystemOptions = {
  laptop: [
    { label: "Windows", value: "windows" },
    { label: "macOS", value: "macos" },
    { label: "Linux", value: "linux" }
  ],
  desktop: [
    { label: "Windows", value: "windows" },
    { label: "macOS", value: "macos" },
    { label: "Linux", value: "linux" }
  ],
  mobile: [
    { label: "Android", value: "android" },
    { label: "iOS", value: "ios" }
  ],
  tablet: [
    { label: "Android", value: "android" },
    { label: "iOS", value: "ios" }
  ],
  other: [
    { label: "Windows", value: "windows" },
    { label: "macOS", value: "macos" },
    { label: "Linux", value: "linux" },
    { label: "Android", value: "android" },
    { label: "iOS", value: "ios" }
  ]
}

const imageFileList = ref([])

// 超出图片数量限制的处理
const handleExceed = (files, uploadFiles) => {
  ElMessage.warning('最多只能上传3张图片')
}

// 图片预览处理
const handlePictureCardPreview = (uploadFile) => {
  // 可以在这里实现图片预览逻辑
  console.log(uploadFile)
}

const submitForm = async () => {
  repairFormRef.value.validate(async (valid) => {
    if (valid) {
      // 提交表单逻辑
      console.log('提交表单:', repairForm)
      const response = await submitRepairForm(repairForm)
      if (response.code !== 200) {
        ElMessage.error(response.msg)
      }
      else {
        const requestId = response.data.requestId
        // 上传图片
        if (imageFileList.value && imageFileList.value.length > 0) {
          await uploadImages(requestId)
        }
        ElMessage.success('报修申请已提交')
        await router.push('/repair/history')
      }
    } else {
      ElMessage.error('请完善必填信息')
    }
  })
}

// 上传图片函数
const uploadImages = async (requestId) => {
  try {
    // 创建 FormData 对象
    const formData = new FormData()
    formData.append('requestId', requestId)

    // 添加图片文件
    imageFileList.value.forEach(file => {
      formData.append('img', file.raw)
    })

    // 调用图片上传 API
    const response = await submitRepairFormImg(formData)
    if (response.code !== 200) {
      ElMessage.error('图片上传失败: ' + response.msg)
    }
    return response
  } catch (error) {
    ElMessage.error('图片上传异常: ' + error.message)
  }
}

const resetForm = () => {
  // 重置表单
  Object.keys(repairForm).forEach(key => {
    if (key !== 'campus' && key !== 'status') {
      repairForm[key] = ''
    }
  })
  repairForm.campus = 0
  repairForm.status = 0
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
                <el-menu-item index="1">填写预约问卷</el-menu-item>
                <el-menu-item index="2" @click="() => router.push('/repair/template')">预约信息模板管理</el-menu-item>
                <el-menu-item index="3" @click="() => router.push('/repair/history')">预约历史</el-menu-item>
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

      <!--主界面-->
      <el-main>
        <div class="main-content">
          <el-form
              ref="repairFormRef"
              :model="repairForm"
              :rules="rules"
              label-width="120px"
              class="repair-form">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item label="联系方式" prop="contactType">
                  <el-select v-model="repairForm.contactType" placeholder="请选择联系方式">
                    <el-option label="微信" value="wechat"></el-option>
                    <el-option label="电话" value="phone"></el-option>
                    <el-option label="邮箱" value="email"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="联系号码" prop="contactInfo">
                  <el-input v-model="repairForm.contactInfo" placeholder="请输入联系号码"></el-input>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="设备类型" prop="deviceType">
                  <el-select v-model="repairForm.deviceType" placeholder="请选择设备类型">
                    <el-option label="笔记本电脑" value="laptop"></el-option>
                    <el-option label="台式机" value="desktop"></el-option>
                    <el-option label="手机" value="mobile"></el-option>
                    <el-option label="平板" value="tablet"></el-option>
                    <el-option label="其他" value="other"></el-option>
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="设备系统" prop="deviceSystem">
                  <el-select
                      v-model="repairForm.deviceSystem"
                      placeholder="请选择设备系统"
                      :disabled="!repairForm.deviceType"
                  >
                    <el-option
                        v-for="option in deviceSystemOptions[repairForm.deviceType] || []"
                        :key="option.value"
                        :label="option.label"
                        :value="option.value"
                    />
                  </el-select>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="设备型号" prop="deviceModel">
                  <el-input v-model="repairForm.deviceModel" placeholder="请输入设备型号"></el-input>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="问题描述" prop="problemDescription">
                  <el-input
                      v-model="repairForm.problemDescription"
                      type="textarea"
                      placeholder="请详细描述设备遇到的问题"
                      :rows="3">
                  </el-input>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="故障图片">
                  <el-upload
                      v-model:file-list="imageFileList"
                      list-type="picture-card"
                      :auto-upload="false"
                      :multiple="true"
                      :limit="3"
                      :on-exceed="handleExceed"
                      :on-preview="handlePictureCardPreview">
                    <el-icon><Plus /></el-icon>
                  </el-upload>

                  <div class="image-hint">最多可上传3张图片，支持JPG/PNG格式</div>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="校区" prop="campus">
                  <el-radio-group v-model="repairForm.campus">
                    <el-radio :label="0">大学城校区</el-radio>
                    <el-radio :label="1">白云山校区</el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="维修地点" prop="repairLocation">
                  <el-input v-model="repairForm.repairLocation" placeholder="请输入具体维修地点"></el-input>
                  <div class="time-hint">示例：南苑13栋304、一饭一等</div>
                </el-form-item>
              </el-col>

              <el-col :span="12">
                <el-form-item label="预约时间" prop="appointmentTime">
<!--                  <el-date-picker-->
<!--                      v-model="repairForm.appointment_time"-->
<!--                      type="datetime"-->
<!--                      placeholder="请选择预约时间"          style="width: 100%">-->
<!--                  </el-date-picker>-->
                  <el-input
                      v-model="repairForm.appointmentTime"
                      placeholder="请输入预约时间"        style="width: 100%">
                  </el-input>
                  <div class="time-hint">示例：周三下午、周四全天、下周一上午等</div>
                </el-form-item>
              </el-col>

              <el-col :span="24">
                <el-form-item label="备注" prop="remarks">
                  <el-input
                      v-model="repairForm.remarks"
                      type="textarea"
                      placeholder="如有其他需要说明的情况，请在此填写"
                      :rows="2">
                  </el-input>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item>
              <el-button type="primary" @click="submitForm">提交申请</el-button>
              <el-button @click="resetForm">重置</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-main>
      <!--      <el-footer>Footer</el-footer>-->
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
  padding-bottom: 30px;
}

.repair-form {
  padding: 20px;
  background: white;
  border-radius: 8px;
  margin: 20px;
}

.repair-form .el-form-item {
  margin-bottom: 20px;
}
.time-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
.image-hint {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  margin-left: 10px;
}
.clickable-avatar {
  cursor: pointer;
}
</style>