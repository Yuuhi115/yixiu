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
export function getKnowledgeList(params){
    return request.get("/ai/getKnowledgePage", {params: params, headers: {Authorization: Cookie.get("Authorization")}})
}
export function updateKnowledge(data){
    return request.put("/ai/updateKnowledge", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
export function deleteKnowledge(knowledgeId){
    return request.delete("/ai/deleteKnowledge", {params: {knowledgeId: knowledgeId}, headers: {Authorization: Cookie.get("Authorization")}})
}
export function rebuildKnowledge(){
    return request.post("/ai/rebuildKnowledge", {}, {headers: {Authorization: Cookie.get("Authorization")}})
}
export function addKnowledge(data){
    return request.post("/ai/knowledge", data, {headers: {Authorization: Cookie.get("Authorization")}})
}