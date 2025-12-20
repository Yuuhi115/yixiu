// websocket.js
// import {ElMessage} from "element-plus";
// export function initWebSocket() {
//     const token = localStorage.getItem("token")
//     if (!token) return
//
//     const ws = new WebSocket(`ws://localhost:8080/ws/notify?token=${token}`)
//
//     ws.onopen = () => {
//         console.log("WebSocket 连接成功")
//     }
//
//     ws.onmessage = (msg) => {
//         const data = msg.data
//         console.log("收到通知：", data)
//         ElMessage.info("通知：" + data)  // 可使用 Element Plus 消息提示
//     }
//
//     ws.onerror = () => {
//         console.log("WebSocket 连接错误")
//     }
//
//     ws.onclose = () => {
//         console.log("WebSocket 已关闭")
//     }
//
//     window.$ws = ws
// }