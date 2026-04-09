import request from "../request/request.js";
import Cookie from "js-cookie";

export function inviteVolunteer(email) {
    return request.get("/admin/inviteCode", {headers: {Authorization: Cookie.get("Authorization")}, params: {email: email}})
}

export function modifyVolunteer(data) {
    return request.put("/admin/volunteerInfo", data, {headers: {Authorization: Cookie.get("Authorization")}})
}

export function modifyTaskApproveEmailNotifyStatus(isOpen){
    let data = new FormData()
    data.append('isOpen', isOpen)
    return request.put("/config/isSendEmailAboutTaskApprove", data, {headers: {Authorization: Cookie.get("Authorization")}})
}

export function queryTaskApproveEmailNotifyStatus(){
    return request.get("/config/isSendEmailAboutTaskApprove", {headers: {Authorization: Cookie.get("Authorization")}})
}
export function sendTaskApproveEmail(taskId){
    let data = new FormData()
    data.append('taskId', taskId)
    return request.post("/admin/sendTaskApproveEmail", data, {headers: {Authorization: Cookie.get("Authorization")}})
}