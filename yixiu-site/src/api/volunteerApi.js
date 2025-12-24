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

export function getAllRepairList(pageNum, pageSize){
    return request.get("/task/getAll", {params: {pageNum: pageNum, pageSize: pageSize},
        headers: {Authorization: Cookie.get("Authorization")}})
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
// 申请加入维修任务
export function applyToJoin(data) {
    return request.post("/task/applyToJoin", data, {headers: {Authorization: Cookie.get("Authorization")}})
}

// 添加维修日志
export function addTaskLog(data) {
    return request.post("/task/addLog", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
// 添加维修日志图片
export function addTaskLogImg(data){
    return request.post("/task/uploadLogImg", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
// 获取志愿者信息列表(除了自己)
export function getVolunteerInfoListExcludeMyself(pageNum, pageSize) {
    return request.get("/volunteer/infoListExcludeUserId", {params: {pageNum: pageNum, pageSize: pageSize},
        headers: {Authorization: Cookie.get("Authorization")}})
}
export function getMyTaskList(pageNum, pageSize){
    return request.get("/task/getMyTaskByVolunteerId", {params: {pageNum: pageNum, pageSize: pageSize},
        headers: {Authorization: Cookie.get("Authorization")}})
}

export function handleJoinApplication(data, action){
    if (action === "approve"){
        return request.put("/task/approveTaskApply", data, {headers: {Authorization: Cookie.get("Authorization")}})
    }
    return request.put("/task/rejectTaskApply", data, {headers: {Authorization: Cookie.get("Authorization")}})
}

export function getMyTaskListFiltered(queryFilter){
    return request.get("/task/getMyTaskByFilter",
        {
            params: queryFilter,
            headers: {Authorization: Cookie.get("Authorization")}
        })
}
