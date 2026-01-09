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
                await startNotifyPoll((message) => {
                    if (message && message.unread !== undefined) {
                        this.unreadCount = message.unread
                    }
                })
                this.isPollingStarted = true
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