import {ElMessage} from "element-plus";

export function jumpToRepairForm(userInfo){
    if(userInfo.realName === '' || userInfo.realName === null){
        ElMessage.warning("请先完善个人信息")
    }

}