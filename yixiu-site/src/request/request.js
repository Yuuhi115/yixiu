import axios from "axios"
import { ElMessage } from "element-plus"
import router from "../router/index.js"
import Cookie from "js-cookie";

const baseURL = "/api"

const instance= axios.create({baseURL})

instance.interceptors.response.use(result => {
    // console.log(result.data)
    if (result.status === 200) {
        if (result.config.responseType === 'blob' ||
            result.config.responseType === 'arraybuffer') {
            // 判断是否是 JSON 错误
            const contentType = result.headers['content-type'] || '';
            if (contentType.includes('application/json')) {
                return result.data.text().then(text => {
                    return Promise.reject(JSON.parse(text));
                });
            }
            return result;
        }
        if (result.data.code === 401 || result.data.msg === "this user's token had been replaced") {
            ElMessage.warning('您的账号已在其他设备登录，请重新登录')
            localStorage.removeItem('role')
            Cookie.remove('Authorization')
            setTimeout(() => {
                router.replace('/login')
            }, 2000)
            return Promise.reject(result.data)
        }
        // 请求成功
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