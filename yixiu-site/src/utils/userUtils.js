
import Cookie from "js-cookie";
import router from "../router/index.js";

export function checkVolunteerPermission (role) {
    if (!['volunteer', 'admin', 'super_admin'].includes(role)) {
        router.push('/')
    }
}