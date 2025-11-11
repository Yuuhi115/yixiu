import request from "../request/request.js";
import Cookie from "js-cookie";
/*邮箱验证码登录*/
export function loginByEmailVerification(loginForm) {
    let data = {
        email : loginForm.email,
        role : loginForm.role,
        verificationCode : loginForm.captcha
    }
    console.log(data)
    return request.post("/users/loginByEmail", data)
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