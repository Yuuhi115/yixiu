import request from "../request/request.js";
import Cookie from "js-cookie";

export function sendChatMessage(data){
    return request.post("/ai/ask", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
export function getChatSession(params){
    return request.get("/ai/chatSession", {params: params, headers: {Authorization: Cookie.get("Authorization")}})
}
export function getChatHistory(params){
    return request.get("/ai/chatMessage", {params: params, headers: {Authorization: Cookie.get("Authorization")}})
}