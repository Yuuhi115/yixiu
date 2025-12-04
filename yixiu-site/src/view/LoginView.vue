<script setup>import { reactive, ref, computed, onUnmounted, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Iphone, Message, Lock, Key } from '@element-plus/icons-vue'
import { sendLREmailVerificationCode,
  loginByEmailVerification } from '../api/userApi.js'
import Cookie from "js-cookie"
import router from "../router/index.js";
import { ElLoading } from 'element-plus'

const formRef = ref()
const loginMethod = ref('email')
const authMethod = ref('captcha')

onMounted(async () => {
  // 获取用户角色，并设置登录方式
  const token = Cookie.get('Authorization')
  if (token) {
      ElMessage.success('已登录，正在跳转至主页面...')
      await router.replace('/')
    }
  }
)

const form = reactive({
  role: 'student',
  phone: '',
  email: '',
  password: '',
  captcha: ''
})

// 计算属性用于双向绑定
const authValue = computed({
  get() {
    return authMethod.value === 'password' ? form.password : form.captcha
  },
  set(value) {
    if (authMethod.value === 'password') {
      form.password = value
    } else {
      form.captcha = value
    }
  }
})

const rules = computed(() => ({
  role: [{ required: true, message: '请选择身份', trigger: 'change' }],
  phone: [
    {
      required: loginMethod.value === 'phone',
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
      required: loginMethod.value === 'email',
      message: '请输入邮箱',
      trigger: 'blur'
    },
    {
      pattern: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
      message: '邮箱格式不正确',
      trigger: 'blur'
    }
  ],
  password: [
    {
      required: authMethod.value === 'password',
      message: '请输入密码',
      trigger: 'blur'
    }
  ],
  captcha: [
    {
      required: authMethod.value === 'captcha',
      message: '请输入验证码',
      trigger: 'blur'
    }
  ]
}))

const countdown = ref(0) // 验证码发送倒计时变量
let timer = null // 再次发送定时器

/*发送验证码*/
const sendCaptcha = async () => {
  if (loginMethod.value === 'phone' && !form.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  if (loginMethod.value === 'email' && !form.email) {
    ElMessage.warning('请输入邮箱')
    return
  }
  if (countdown.value > 0) return // 如果正在倒计时，则不执行

  if (loginMethod.value === 'email') {
    const loadingInstance = ElLoading.service()
    let loginResult = await sendLREmailVerificationCode(form.email);
    loadingInstance.close()
    startCountdown() // 启动倒计时
    if (loginResult.code === 200) {
      ElMessage.success('验证码已发送')
    } else {
      ElMessage.error(loginResult.msg)
    }
  }
  console.log('发送验证码到:', loginMethod.value === 'phone' ? form.phone : form.email)
}

// 验证码再次发送启动倒计时
const startCountdown = () => {
  countdown.value = 60 // 设置倒计时时间为60秒
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
    }
  }, 1000)
}

// 组件卸载时清理定时器
onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
  }
})
/* 登录逻辑 */
const handleLogin = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      console.log('提交表单数据:', form)
      let loginResult = await loginByEmailVerification(form);
      if (loginResult.code !== 200) {
        ElMessage.error(loginResult.msg)
        return
      }
      let token = loginResult.data;
      Cookie.set('Authorization', token, { expires: 7 })
      localStorage.setItem('role', form.role)
      ElMessage.success('登录成功！')
      await router.replace('/')
    } else {
      ElMessage.error('请检查输入内容')
    }
  })
}


</script>

<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">用户登录</h2>

      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">

        <el-form-item label="登录账户">
          <el-radio-group v-model="loginMethod">
            <el-radio label="phone">手机号</el-radio>
            <el-radio label="email">邮箱</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="手机号" prop="phone" v-if="loginMethod === 'phone'">
          <el-input v-model="form.phone" placeholder="请输入手机号">
            <template #prefix>
              <el-icon><Iphone /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="邮箱" prop="email" v-if="loginMethod === 'email'">
          <el-input v-model="form.email" placeholder="请输入邮箱">
            <template #prefix>
              <el-icon><Message /></el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item label="登录方式">
          <el-radio-group v-model="authMethod">
            <el-radio label="密码" value="password" />
            <el-radio label="验证码" value="captcha" />
          </el-radio-group>
        </el-form-item>

        <el-form-item
            :label="authMethod === 'password' ? '密码' : '验证码'"
            :prop="authMethod === 'password' ? 'password' : 'captcha'"
        >
          <el-input
              v-model="authValue"
              :type="authMethod === 'password' ? 'password' : 'text'"
              :placeholder="authMethod === 'password' ? '请输入密码' : '请输入验证码'"
              :show-password="authMethod === 'password'"
          >
            <template #prefix>
              <el-icon>
                <Lock v-if="authMethod === 'password'" />
                <Key v-if="authMethod === 'captcha'" />
              </el-icon>
            </template>

            <template #append v-if="authMethod === 'captcha'">
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
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>

        <div class="bottom_button_div">
          <el-link type="primary" @click="() => router.push('/register')" style="margin-left: 30px">没有账户？去注册</el-link>
          <el-form-item class="el-form-item-special">
            <el-button type="primary" class="normal_button" @click="handleLogin">登录</el-button>
          </el-form-item>
        </div>


      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-container {
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
.login-card {
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
