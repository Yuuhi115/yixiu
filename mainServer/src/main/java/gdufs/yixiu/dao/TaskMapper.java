package gdufs.yixiu.dao;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.TaskFilterDto;
import gdufs.yixiu.dto.VolunteerStatisticsDto;
import gdufs.yixiu.pojo.*;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TaskMapper {
    int addTask(RepairRequest repairRequest);
    int addRequestImg(RepairRequestImg repairRequestImg);
    int updateTask(RepairRequest repairRequest);
    int findIsExist(Integer requestId);
    RepairRequest findTaskById(Integer requestId);
    List<RepairRequest> findAllTask();
    List<RepairRequest> findTaskByUserId(Integer userId);
    List<RepairRequest> findTaskByStatus(String status);
    List<RepairRequest> findTaskByFilterDto(TaskFilterDto taskFilterDto);
    List<Integer> findMyTaskIdsByFilterDto(TaskFilterDto taskFilterDto);
    List<RepairRequestImg> findRequestImgByRequestId(Integer requestId);
    List<String> findRequestImgUrlByRequestId(Integer requestId);
    int deleteTaskImgByRequestId(Integer requestId);

    int addTaskAssignment(RepairAssignment repairAssignment);
    int addTaskApplyAssignment(RepairAssignment repairAssignment);
    int findIsExistAssignment(Integer requestId);
    List<RepairAssignment> findTaskAssignmentByRequestId(Integer requestId);
    List<Integer> findMyTaskAssignmentIdsByVolunteerId(Integer volunteerId);
    int updateTaskAssignment(RepairAssignment repairAssignment);
    int updateTaskAssignmentByRequestIdAndVolunteerId(RepairAssignment repairAssignment);

    int addTaskLog(RepairLog repairLog);
    int addTaskLogImg(RepairLogImg repairLogImg);
    List<RepairLogImg> findTaskLogImgByLogId(Integer logId);
    List<String> findTaskLogImgUrlByLogId(Integer logId);
    List<RepairLog> findTaskLogByRequestId(Integer requestId);
    int deleteTaskLogImgByLogId(Integer logId);

    int addTaskEvaluate(RepairEvaluate repairEvaluate);
    RepairEvaluate findTaskEvaluateByRequestId(Integer requestId);
    List<RepairLog> findRepairLogsWithDate(@Param("volunteerId") Integer volunteerId,
                                           @Param("startDate") String startDate,
                                           @Param("endDate") String endDate);

}
