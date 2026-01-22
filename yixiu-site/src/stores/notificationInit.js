import { defineStore } from 'pinia'
import { startNotifyPoll } from "../utils/notificationUtils.js"
import {getUnreadNotifyCount} from "../api/notificationApi.js";

export const useNotificationStore = defineStore('notification', {
    // 状态
    state: () => ({
        unreadCount: 0,
        isPollingStarted: false
    }),

    // 计算属性
    getters: {
        hasUnread: (state) => state.unreadCount > 0
    },

    // 动作
    actions: {
        // 初始化轮询
        async initPolling() {
            if (!this.isPollingStarted) {
                try {
                    await startNotifyPoll((message) => {
                        if (message && message.unread !== undefined) {
                            this.unreadCount = message.unread
                        }
                    })
                    this.isPollingStarted = true
                    console.log("通知轮询已启动")

                    // 启动监控任务，确保轮询持续运行
                    this.startPollingMonitor()
                } catch (error) {
                    console.error('启动通知轮询失败:', error)
                    // 尝试重启
                    setTimeout(() => {
                        this.restartPolling()
                    }, 5000) // 5秒后重试
                }
            }else {
                console.log("通知轮询异常，尝试重新启动")
                this.restartPolling()
            }
        },
        
        startPollingMonitor() {
            // 定期检查轮询状态，如有问题自动重启
            setInterval(() => {
                if (!this.isPollingStarted) {
                    this.restartPolling()
                }
            }, 30000) // 每30秒检查一次
        },

        async restartPolling() {
            console.log("尝试重启通知轮询...")
            this.isPollingStarted = false
            try {
                await this.initPolling()
            } catch (error) {
                console.error("重启轮询失败，将在10秒后再次尝试:", error)
                setTimeout(() => {
                    this.restartPolling()
                }, 10000) // 10秒后再次重试
            }
        },

        // 更新未读数量
        updateUnreadCount(count) {
            this.unreadCount = count
        },
        // 重置未读数量
        resetUnreadCount() {
            this.unreadCount = 0
        },

        async syncUnreadCount() {
            try {
                const response = await getUnreadNotifyCount()
                if (response.code === 200) {
                    this.updateUnreadCount(response.data)
                    console.log("同步未读数量成功", response.data)
                }
            } catch (error) {
                console.error('同步未读数量失败:', error)
            }
        }
    }
})