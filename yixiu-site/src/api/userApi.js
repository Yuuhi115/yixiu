import request from "../request/request.js";
import Cookie from "js-cookie";
import qs from 'qs';
import {ElMessage} from "element-plus";
import router from "../router/index.js";
import {stopNotifyPoll} from "../utils/notificationUtils.js";

export async function checkToken() {
    const isAuth = await request.get("/users/checkAuth", {headers: {Authorization: Cookie.get("Authorization")}})
    if (isAuth.code !== 200) {
        ElMessage.error("登录信息过期，将于3秒后跳转至登录页面")
        await stopNotifyPoll()
        setTimeout(() => {
            Cookie.remove('Authorization')
            router.push('/login')
        }, 3000)
    }
}
/*邮箱验证码登录*/
export function loginByEmailVerification(loginForm) {
    let data = {
        email : loginForm.email,
        role : loginForm.role,
        verificationCode : loginForm.captcha
    }
    console.log(data)
    let result
    if (data.role === "admin") {
        result = request.post("/admin/loginByEmail", data)
    }
    else if (data.role === "volunteer") {
        result = request.post("/volunteer/loginByEmail", data)
    }else {
        result = request.post("/users/loginByEmail", data)
    }
    return result
}
/*邮箱注册*/
export function registerByEmailVerification(registerForm) {
    let data = {
        email : registerForm.email,
        role : registerForm.role,
        verificationCode : registerForm.captcha
    }
    console.log(data)
    return request.post("/users/registerByEmail", data)
}

export function sendLREmailVerificationCode(email) {
    return request.get("/send/emailVerification", {params: {email: email}})
}
/*获取用户信息*/
export function getUserInfo(token) {
    return request.get("/users/userInfo", {headers: {Authorization: token}})
}
/*更改头像*/
export function updateAvatar(data) {
    return request.put("/users/avatar", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
/*更改用户基本信息*/
export function updateUserInfo(data) {
    return request.put("/users/userInfo", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
/*提交报修表单*/
export function submitRepairForm(data) {
    return request.post("/task/add", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
/*提交报修图片*/
export function submitRepairFormImg(data) {
    return request.post("/task/uploadRequestImg", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
/*获取报修单By用户id*/
export function getRepairFormByUserId(pageNum, pageSize) {
    return request.get("/task/getByUserId", {params: {pageNum: pageNum, pageSize: pageSize}, headers: {Authorization: Cookie.get("Authorization")}})
}
/*更新报修单状态*/
export function updateRepairFormStatus(requestId, status) {
    return request.put("/task/updateStatus",
        { requestId: requestId, status: status },
        { headers: { Authorization: Cookie.get("Authorization") } }
    )
}
/*获取报修单By筛选器限定用户*/
export function getRepairFormByFilterLimitUser(queryFilter) {
    return request.get("/task/getByFilterLimitUser",
        { params: queryFilter, headers: { Authorization: Cookie.get("Authorization") } }
    )
}
/*添加维修评价*/
export function addEvaluation(data){
    return request.post("/task/addEvaluation", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
