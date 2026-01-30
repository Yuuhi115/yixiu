import {ElMessage} from "element-plus";
import router from "../router/index.js";
import {addProfileView} from "../api/userApi.js";

export function JumpToRepairForm(userInfo){
    if(userInfo.realName === '' || userInfo.realName === null){
        ElMessage.warning("请先完善个人信息")
    }else {
        router.push('/repair/form')
    }
}

export function JumpToTaskList(userInfo, path) {
    if (userInfo.realName === '' ||
        userInfo.realName === null ||
        userInfo.volunteerInfo.contactType === '' ||
        userInfo.volunteerInfo.contactType === null ||
        userInfo.volunteerInfo.contactNumber === '' ||
        userInfo.volunteerInfo.contactNumber === null
    ) {
        ElMessage.warning("请先完善个人信息")
    } else {
        router.push(path)
    }
}

export async function JumpToUserProfile(userId) {
    await router.push('/community/profile/' + userId)
    const response = await addProfileView(userId)
    if (response.code !== 200) {
        ElMessage.error(response.msg)
    }
}