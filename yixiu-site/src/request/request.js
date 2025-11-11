import axios from "axios"
import { ElMessage } from "element-plus"
import router from "../router/index.js"

const baseURL = "/api"

const instance= axios.create({baseURL})

instance.interceptors.response.use(result => {
    // console.log(result.data)
    if (result.status === 200) {  // 请求成功
        return result.data
    }
    else {
        ElMessage.error(result.data.msg ? result.data.msg : "Service Error.")
        return Promise.reject(result)
    }
}, async error =>{
    if (error.status === 500) {
        ElMessage.warning("Please log on first!")
        await router.replace("/")
    }
    else {
        ElMessage.error("Server Error.")
    }
    return Promise.reject(error)  // 异步状态转换为失败状态
})
export default instance