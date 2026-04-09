import router from "../router/index.js";

export function AcceptSuperAdmin(userInfo){
    return userInfo.role === 'super_admin'
}
export function AcceptAdmin(userInfo){
    return userInfo.role === 'admin' || userInfo.role === 'super_admin'
}

export function AcceptVolunteer(userInfo){
    return userInfo.role === 'volunteer' || userInfo.role === 'admin' || userInfo.role === 'super_admin'
}

export function AcceptUserOnly(userInfo){
    return userInfo.role === 'user'
}

export function RoleCheckVolunteer(){
    const role = localStorage.getItem("role")
    if (role !== 'volunteer' && role !== 'admin' && role !== 'super_admin') {
        router.push('/')
        return false
    }
    return true
}

export function RoleCheckAdmin(){
    const role = localStorage.getItem("role")
    if (role !== 'admin' && role !== 'super_admin') {
        router.push('/')
        return false
    }
    return true
}

export function RoleCheckSuperAdmin(){
    const role = localStorage.getItem("role")
    if (role !== 'super_admin') {
        router.push('/')
        return false
    }
    return true
}