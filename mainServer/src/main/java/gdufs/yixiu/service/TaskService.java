package gdufs.yixiu.service;

import gdufs.yixiu.dto.RepairAssignmentDto;
import gdufs.yixiu.dto.RepairRequestDto;
import gdufs.yixiu.dto.TaskFilterDto;
import gdufs.yixiu.pojo.RepairRequest;
import gdufs.yixiu.pojo.RepairRequestImg;
import gdufs.yixiu.pojo.Users;

import java.util.List;

public interface TaskService {
    String addTask(RepairRequestDto repairRequestDto);
    String addTaskAssignment(RepairAssignmentDto repairAssignmentDto);
    boolean updateTask(RepairRequestDto repairRequestDto);
    RepairRequestDto queryTaskById(Integer requestId);
    List<RepairRequestDto> queryTaskByUserId(Integer userId);
    List<RepairRequestDto> queryTaskByStatus(String status);
    List<RepairRequestDto> queryAllTask();
    List<RepairRequestDto> queryTaskByFilter(TaskFilterDto taskFilterDto);
    RepairRequestDto repairRequestPojoToDto(Users users, RepairRequest repairRequest);
    TaskFilterDto transformEndTime(TaskFilterDto taskFilterDto);

    List<RepairRequestImg> queryImgByRequestId(Integer requestId);
    void deleteTaskImgByRequestId(Integer requestId);
}
