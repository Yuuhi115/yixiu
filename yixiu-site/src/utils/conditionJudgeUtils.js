export function RepairAssignArrayCheck(task) {
    return task.repairAssignment && Array.isArray(task.repairAssignment);
}

export function RepairLogArrayCheck(task) {
    return task.repairLog && Array.isArray(task.repairLog);
}

export function ThisVolunteerNotAttended(task, userInfo) {
    return !task.repairAssignment.some(assign => assign.volunteerId === userInfo.volunteerInfo.volunteerId);
}

export function ThisVolunteerWaitingForApplyResult(task, userInfo) {
    return task.repairAssignment.some(assign => assign.volunteerId === userInfo.volunteerInfo.volunteerId && assign.status === 5);
}

export function ThisVolunteerIsAttended(task, userInfo) {
    return task.repairAssignment.some(assign => assign.volunteerId === userInfo.volunteerInfo.volunteerId && assign.status !== 5 && assign.status !== 6);
}

export function ThisVolunteerIsRejectedFromApply(task, userInfo) {
    return task.repairAssignment.some(assign => assign.volunteerId === userInfo.volunteerInfo.volunteerId && assign.status === 6);
}

// 检查当前用户是否已提交维修日志
export function ThisVolunteerHasSubmittedLog(task, userInfo) {
    return task.repairLog.some(log => log.volunteerId === userInfo.volunteerInfo.volunteerId);
}

export function HaveApplyForThisTask(task, userInfo) {
    return task.repairAssignment.some(assign => assign.status === 5)
        && task.repairAssignment.some(assign => assign.volunteerId === userInfo.volunteerInfo.volunteerId && assign.isLeader === 1);
}

export function ThisVolunteerNeedFillLog(task, userInfo){
    return [3, 7].includes(task.status) && task.repairAssignment.some(assign =>
        assign.volunteerId === userInfo.volunteerInfo.volunteerId && assign.status === 0)

}

export default {
    RepairAssignArrayCheck,
    ThisVolunteerNotAttended,
    ThisVolunteerWaitingForApplyResult,
    ThisVolunteerIsAttended,
    ThisVolunteerIsRejectedFromApply,
    ThisVolunteerHasSubmittedLog,
    RepairLogArrayCheck,
    HaveApplyForThisTask
}