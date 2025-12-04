<script setup>
import { reactive, ref, computed, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Iphone, Message, Key, Unlock } from '@element-plus/icons-vue'
import { sendLREmailVerificationCode,
  registerByEmailVerification,
} from '../api/userApi.js'
import { volunteerRegisterByEmailVerification } from '../api/volunteerApi.js';
import router from "../router/index.js";
import Cookie from "js-cookie";

const formRef = ref()
const registerMethod = ref('email')
const form = reactive({
  role: 'student',
  phone: '',
  email: '',
  captcha: '',
  inviteCode: ''
})

const rules = computed(() => ({
  role: [{ required: true, message: '请选择身份', trigger: 'change' }],
  phone: [
    {
      required: registerMethod.value === 'phone',
      message: '请输入手机号',
      trigger: 'blur'
    },
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '手机号格式不正确',
      trigger: 'blur'
    }
  ],
  email: [
    {
      required: registerMethod.value === 'email',
      message: '请输入邮箱',
      trigger: 'blur'
    },
    {
      pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
      message: '邮箱格式不正确',
      trigger: 'blur'
    }
  ],
  captcha: [
    {
      required: true,
      message: '请输入验证码',
      trigger: 'blur'
    }
  ],
  inviteCode: [
    {
      required: form.role === 'volunteer',
      message: '请输入邀请码',
      trigger: 'blur'
    }
  ]
}))

const countdown = ref(0)
let timer = null

const sendCaptcha = async () => {
  if (registerMethod.value === 'phone' && !form.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (registerMethod.value === 'email' && !form.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  if (countdown.value > 0) return

  if (registerMethod.value === 'email') {
    let result = await sendLREmailVerificationCode(form.email);
    startCountdown()
    if (result.code === 200) {
      ElMessage.success('验证码已发送')
    } else {
      ElMessage.error(result.msg)
    }
  }
  console.log('发送验证码到:', registerMethod.value === 'phone' ? form.phone : form.email)
}

const startCountdown = () => {
  countdown.value = 60
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})

const handleRegister = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      console.log('提交注册数据:', form)
      let registerResult;
      if (form.role === 'volunteer'){
        registerResult = await volunteerRegisterByEmailVerification(form);
      }
      else{
        registerResult = await registerByEmailVerification(form);
      }
      if (registerResult.code !== 200) {
        ElMessage.error(registerResult.msg)
        return
      }
      let token = registerResult.data;
      Cookie.set('Authorization', token)
      ElMessage.success('注册成功！')
      await router.push('/')
    } else {
      ElMessage.error('请检查输入内容')
    }
  })
}
</script>

<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 class="title">用户注册</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">

        <el-form-item label="注册账户">
          <el-radio-group v-model="registerMethod">
            <el-radio label="phone">手机号</el-radio>
            <el-radio label="email">邮箱</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="手机号" prop="phone" v-if="registerMethod === 'phone'">
          <el-input v-model="form.phone" placeholder="请输入手机号">
            <template #prefix>
              <el-icon><Iphone /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="邮箱" prop="email" v-if="registerMethod === 'email'">
          <el-input v-model="form.email" placeholder="请输入邮箱">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="验证码" prop="captcha">
          <el-input v-model="form.captcha" placeholder="请输入验证码">
            <template #prefix>
              <el-icon><Key /></el-icon>
            </template>
            <template #append>
              <el-button
                  :type="countdown > 0 ? '' : 'primary'"
                  :disabled="countdown > 0"
                  @click="sendCaptcha"
              >
                {{ countdown > 0 ? `${countdown}s后重新发送` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="身份" prop="role">
          <el-select v-model="form.role">
            <el-option label="学生" value="student" />
            <el-option label="志愿者" value="volunteer" />
          </el-select>
        </el-form-item>

        <el-form-item
            label="邀请码"
            prop="inviteCode"
            v-if="form.role === 'volunteer'"
        >
          <el-input v-model="form.inviteCode" placeholder="请输入邀请码">
            <template #prefix>
              <el-icon><Unlock /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <div class="bottom_button_div">
          <el-link type="primary" @click="() => router.push('/login')" style="margin-left: 30px">已有账号？去登录</el-link>
          <el-form-item class="el-form-item-special">
            <el-button type="primary" class="normal_button" @click="handleRegister">注册</el-button>
          </el-form-item>
        </div>

      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-image: url('../assets/login_backgroud.png');
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
}
.register-card {
  width: 450px;
  padding: 20px 30px;
}
.title {
  text-align: center;
  margin-bottom: 20px;
}
.bottom_button_div{
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.normal_button {
  width: 150px;
  height: 40px;
  font-size: 16px;
}
.el-form-item-special {
  margin-top: auto;
  margin-bottom: auto;
}
</style>
