import {ElMessage} from "element-plus";
import router from "../router/index.js";

export function JumpToRepairForm(userInfo){
    if(userInfo.realName === '' || userInfo.realName === null){
        ElMessage.warning("请先完善个人信息")
    }else {
        router.push('/repair/form')
    }
}

export function JumpToTaskList(userInfo) {
    if (userInfo.realName === '' || userInfo.realName === null) {
        ElMessage.warning("请先完善个人信息")
    } else {
        router.push('/taskCenter/list')
    }
}