import request from "../request/request.js";
import Cookie from "js-cookie";

export function inviteVolunteer(email) {
    return request.get("/admin/inviteCode", {headers: {Authorization: Cookie.get("Authorization")}, params: {email: email}})
}