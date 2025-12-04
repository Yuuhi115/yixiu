import request from "../request/request.js";
import Cookie from "js-cookie";

export function volunteerRegisterByEmailVerification(registerForm){
    let data = {
        email : registerForm.email,
        role : registerForm.role,
        inviteCode : registerForm.inviteCode,
        verificationCode : registerForm.captcha
    }
    console.log(data)
    return request.post("/volunteer/registerByEmail", data)
}

export function loginByEmailVerification(loginForm) {
}

export function updateVolunteerInfo(volunteerInfo) {
    let data = {
        userId: volunteerInfo.userId,
        studentNumber: volunteerInfo.studentNumber,
        majorClass: volunteerInfo.majorClass,
        grade: volunteerInfo.grade,
    }
    console.log(data)
    return request.put("/volunteer/info", data, {headers: {Authorization: Cookie.get("Authorization")}})
}

export function getAllRepairList(){
    return request.get("/task/getAll", {headers: {Authorization: Cookie.get("Authorization")}})
}

export function getFilteredRepairList(queryFilter){
    return request.get("/task/getByFilter",
        {
            params: queryFilter,
            headers: {Authorization: Cookie.get("Authorization")}
        })
}
// 添加分配单
export function addTaskAssign(data){
    return request.post("/task/addAssign", data, {headers: {Authorization: Cookie.get("Authorization")}})
}