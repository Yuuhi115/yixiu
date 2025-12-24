import request from "../request/request.js";
import Cookie from "js-cookie";

export function pollNotification() {
    return request.get("/notify/poll", {headers: {Authorization: Cookie.get("Authorization")}})
}
export function getNotifyList() {
    return request.get("/notify/list", {headers: {Authorization: Cookie.get("Authorization")}})
}

export function getUnreadNotifyCount() {
    return request.get("/notify/getUnreadCount", {headers: {Authorization: Cookie.get("Authorization")}})
}

export function changeToRead(notifyId){
    return request.put("/notify/changeToRead", null, {params: {notifyId: notifyId}, headers: {Authorization: Cookie.get("Authorization")}})
}
export function sendSystemNoticeToUser(data){
    return request.post("/notify/systemToUser", data, {headers: {Authorization: Cookie.get("Authorization")}})
}

export function sendUserNoticeToUser(data){
    return request.post("/notify/userToUser", data, {headers: {Authorization: Cookie.get("Authorization")}})
}