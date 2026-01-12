package gdufs.yixiu.service;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.*;
import gdufs.yixiu.pojo.*;

import java.util.List;

public interface TaskService {
    String addTask(RepairRequestDto repairRequestDto);
    String addTaskAssignment(RepairAssignmentDto repairAssignmentDto);
    String applyToJoinTask(RepairAssignmentDto repairAssignmentDto);
    boolean updateTask(RepairRequestDto repairRequestDto);
    RepairRequestDto queryTaskById(Integer requestId);
    PageInfo<RepairRequestDto> queryTaskByUserId(Integer userId, Integer pageNum, Integer pageSize);
    List<RepairRequestDto> queryTaskByStatus(String status);
    PageInfo<RepairRequestDto> queryAllTask(Integer pageNum, Integer pageSize);
    PageInfo<RepairRequestDto> queryTaskByFilter(TaskFilterDto taskFilterDto, Integer pageNum, Integer pageSize);
    RepairRequestDto repairRequestPojoToDto(Users users, RepairRequest repairRequest);
    TaskFilterDto transformEndTime(TaskFilterDto taskFilterDto);
    RepairAssignmentDto repairAssignmentPojoToDto(RepairAssignment repairAssignment);

    List<RepairRequestImg> queryImgByRequestId(Integer requestId);
    void deleteTaskImgByRequestId(Integer requestId);
    Integer addTaskLog(RepairLogDto repairLogDto);
    List<RepairLogImg> queryTaskLogImgByLogId(Integer logId);
    void deleteTaskLogImgByLogId(Integer logId);

    PageInfo<RepairRequestDto> queryMyTaskByVolunteerId(Integer volunteerId, Integer pageNum, Integer pageSize);
    PageInfo<RepairRequestDto> queryMyTaskByFilter(TaskFilterDto taskFilterDto, Integer pageNum, Integer pageSize);
    boolean updateAssignmentStatus(Integer assignId, Integer status, String reason);
    boolean updateAssignmentStatusByRequestIdAndVolunteerId(Integer requestId, Integer volunteerId, Integer status);

    Integer addTaskEvaluate(RepairEvaluate repairEvaluate);

    List<VolunteerStatisticsDto> exportRepairLogsWithDate(Integer volunteerId, String startDate, String endDate);

}
