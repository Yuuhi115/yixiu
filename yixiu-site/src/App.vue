<script setup>
import { onMounted } from 'vue'
import { useNotificationStore } from './stores/notificationInit.js'

const notificationStore = useNotificationStore()

onMounted(async () => {
  // 应用启动时初始化通知轮询
  await notificationStore.initPolling()
})

</script>

<template>
  <router-view></router-view>
</template>

<style scoped>

</style>
