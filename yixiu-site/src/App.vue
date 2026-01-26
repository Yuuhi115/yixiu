<script setup>
import { onMounted } from 'vue'
import { useNotificationStore } from './stores/notificationInit.js'
import { useRoute } from 'vue-router'

const notificationStore = useNotificationStore()
const route = useRoute()

onMounted(async () => {
  // 检查当前路由是否为登录或注册页面
  if (route.path === '/login' || route.path === '/register') {
    console.log('当前为登录/注册页面，跳过通知轮询初始化')
    return
  }
  // 应用启动时初始化通知轮询
  console.log('App mounted, initializing polling...')
  await notificationStore.initPolling()
})

</script>

<template>
  <router-view></router-view>
</template>

<style scoped>

</style>
