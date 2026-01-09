import { pollNotification } from "../api/notificationApi.js"
import { ElMessage, ElNotification } from "element-plus"

let polling = false
let pollTimer = null  // 添加定时器引用

export function startNotifyPoll(callback) {
    if (polling) return
    polling = true

    const poll = async () => {
        if (!polling) return
        try {
            const res = await pollNotification()
            // res 就是 NotifyPushVO
            if (res && res.type === "NONE") {
                console.log("null notify poll", res)
            }
            if (res && (res.type === "BROADCAST") || (res.type === "SYSTEM")) {
                console.log("broadcast notify poll", res)
                ElNotification ({
                    title: "新通知",
                    message: "你有一条新的系统通知",
                    type: "primary",
                    duration: 5000
                })
            }
            if (res && res.type === "USER") {
                console.log("user notify poll", res)
                ElNotification ({
                    title: "新通知",
                    message: "你有一条新的私信",
                    type: "primary",
                    duration: 5000
                })
            }
            if (callback && typeof callback === 'function') {
                callback(res); // res 是从后端接收到的数据
            }
            localStorage.setItem("unreadNotify", res.unread)
        } catch (e) {
            console.error("notify poll error", e)
        } finally {
            if (polling) {
                setTimeout(poll, 500)
            }
        }
    }
    poll()
}

export function stopNotifyPoll() {
    if (polling) {
        console.log("停止通知轮询")
        polling = false
        if (pollTimer) {
            clearTimeout(pollTimer)
            pollTimer = null
        }
    }
}

export function isPolling() {
    return polling
}