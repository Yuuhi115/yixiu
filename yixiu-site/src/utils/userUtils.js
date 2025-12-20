
import Cookie from "js-cookie";
import router from "../router/index.js";
export function checkToken (){
    const token = Cookie.get("Authorization");
    return !!token;
}

export function checkVolunteerPermission (role) {
    if (!['volunteer', 'admin', 'super_admin'].includes(role)) {
        router.push('/')
    }
}