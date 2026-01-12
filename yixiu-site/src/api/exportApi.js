import request from "../request/request.js";
import Cookie from "js-cookie";

export function exportVolunteerRepairStatistics(data) {
    return request.get("/export/volunteerStatistics",
        {headers: {Authorization: Cookie.get("Authorization")}, params: data, responseType: "blob"})
}