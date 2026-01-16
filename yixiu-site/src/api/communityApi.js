import request from "../request/request.js";
import Cookie from "js-cookie";

/*
* data:
* title: string
* content: string
* tagIdList: [int]
* */
export function uploadPost(data){
    return request.post("/community/post/create", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
/*
* data:
* img: [file]
* postId: int
* */
export function uploadPostImage(data){
    return request.post("/community/post/uploadPostImg", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
/*
* params:
* pageNum: int
* pageSize: int
* */
export function getPostList(params){
    return request.get("/community/post/list", {params: params, headers: {Authorization: Cookie.get("Authorization")}})
}
export function addPostLike(postId){
    return request.post("/community/post/modifyLike", {postId: postId} ,{headers: {Authorization: Cookie.get("Authorization")}})
}
export function addPostFavorite(postId){
    return request.post("/community/post/modifyFavorite", {postId: postId} ,{headers: {Authorization: Cookie.get("Authorization")}})
}
/*
* 获取数据库中的所有标签
* */
export function getAllPostTags(){
    return request.get("/community/post/allTags", {headers: {Authorization: Cookie.get("Authorization")}})
}
/*
* data:
* postId: int
* content: string
* */
export function addComment(data){
    return request.post("/community/comment/add", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
export function addCommentLike(commentId){
    return request.post("/community/comment/modifyCommentLike", {commentId: commentId} ,{headers: {Authorization: Cookie.get("Authorization")}})
}
export function deleteComment(commentId){
    return request.put("/community/comment/delete", {commentId: commentId}, {headers: {Authorization: Cookie.get("Authorization")}})
}
/*
* params:
* postId: int
* pageNum: int
* pageSize: int
* 默认获取20条评论，包括回复
* */
export function getCommentListByPostId(params){
    return request.get("/community/comment/listByPostId", {params: params, headers: {Authorization: Cookie.get("Authorization")}})
}
/*
* data:
* commentId: int
* content: string
* toUserId: int
* parentReplyId: int
*
* 参数含义
* commentId: 回复哪一条一级评论？
* toUserId: 回复的一级评论的用户，如果该二级评论是回复另一条二级评论，则该字段填（另一条二级评论）的fromUserId
* parentReplyId: 如果该二级评论是回复另一条二级评论（都是关于这条一级评论的）,则需要填（另一条二级评论）的replyId，如果只是回复一级评论则填null
* */
export function addReply(data){
    return request.post("/community/comment/addReply", data, {headers: {Authorization: Cookie.get("Authorization")}})
}
export function addReplyLike(replyId){
    return request.post("/community/comment/modifyCommentLike", {replyId: replyId} ,{headers: {Authorization: Cookie.get("Authorization")}})
}
export function deleteReply(replyId){
    return request.put("/community/comment/delete", {replyId: replyId}, {headers: {Authorization: Cookie.get("Authorization")}})
}
/*
* params:
* commentId: int
* pageNum: int
* pageSize: int
* 获取该一级评论的回复
* 注意: 该接口仅用于获取从第二页开始的回复，默认是20条一页
* */
export function getReplyPages(params){
    return request.get("/community/comment/repliesPageByCommentId", {params: params, headers: {Authorization: Cookie.get("Authorization")}})
}

