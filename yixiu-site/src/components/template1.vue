<script setup>

import {Message} from "@element-plus/icons-vue";
import {onMounted, reactive, ref} from "vue";
import Cookie from "js-cookie";
import {getUserInfo} from "../../api/userApi.js";
import {ElMessage} from "element-plus";

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
</script>

<template>
  <div class="common-layout">
    <el-container style="height: 100%">
      <el-header class="header_container">
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
  box-shadow: silver 0 0 10px;
  --el-box-shadow: ;
  background: white;
}
</style>