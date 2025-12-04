
import Cookie from "js-cookie";
export function checkToken (){
    const token = Cookie.get("Authorization");
    return !!token;
}

export default {
    checkToken
}