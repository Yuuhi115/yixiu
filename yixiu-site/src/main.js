import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// import {initWebSocket} from "./config/websocket.js";


createApp(App).mount('#app')
const app = createApp(App)
app.use(router)
app.use(ElementPlus)
app.mount('#app')
// initWebSocket()
