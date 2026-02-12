import request from "../request/request.js";
import Cookie from "js-cookie";

export function pollNotification() {
    return request.get("/notify/poll", {headers: {Authorization: Cookie.get("Authorization")}})
}
export function getNotifyList(pageNum, pageSize) {
    return request.get("/notify/list", {headers: {Authorization: Cookie.get("Authorization")}, params: {pageNum: pageNum, pageSize: pageSize}})
}

export function getNotifyByFilter(queryFilter) {
    return request.get("/notify/listByFilter",
        {
            params: queryFilter,
            headers: {Authorization: Cookie.get("Authorization")}
        })
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

export function sendRoleChangeNotification(data){
    return request.post("/notify/roleChange", data, {headers: {Authorization: Cookie.get("Authorization")}})
}

export function sendRepairTaskApproveNotification(data){
    return request.post("/notify/taskApprove", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
export function sendRepairTaskRejectNotification(data){
    return request.post("/notify/taskReject", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
export function sendRepairTaskAcceptNotification(data){
    return request.post("/notify/taskAccept", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
export function sendRepairCompleteNotification(data){
    return request.post("/notify/taskComplete", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
export function sendRepairEvaluateCompleteNotification(data){
    return request.post("/notify/taskEvaluateComplete", data, {headers: {Authorization: Cookie.get("Authorization")}})
}

/*社区通知*/
export function sendCommentNotify(data){
    return request.post("/notify/comment", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
export function sendReplyNotify(data){
    if (data.parentReplyId){
        return request.post("/notify/replyToReply", data, {headers: {Authorization: Cookie.get("Authorization")}})
    }else {
        return request.post("/notify/replyToComment", data, {headers: {Authorization: Cookie.get("Authorization")}})
    }
}