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