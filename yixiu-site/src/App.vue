<script setup>
import { onMounted } from 'vue'
import { useNotificationStore } from './stores/notificationInit.js'

const notificationStore = useNotificationStore()

onMounted(async () => {
  // 应用启动时初始化通知轮询
  console.log('App mounted, initializing polling...')
  await notificationStore.initPolling()

  setInterval(async () => {
    if (!notificationStore.isPollingStarted) {
      console.log("检测到轮询未运行，正在重启...")
      await notificationStore.restartPolling()
    }
  }, 60000)
})

</script>

<template>
  <router-view></router-view>
</template>

<style scoped>

</style>
