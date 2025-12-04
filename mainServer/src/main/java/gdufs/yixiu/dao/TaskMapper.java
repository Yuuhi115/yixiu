package gdufs.yixiu.dao;

import gdufs.yixiu.dto.TaskFilterDto;
import gdufs.yixiu.pojo.RepairAssignment;
import gdufs.yixiu.pojo.RepairRequest;
import gdufs.yixiu.pojo.RepairRequestImg;
import org.apache.ibatis.annotations.Mapper;

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
    List<RepairRequestImg> findRequestImgByRequestId(Integer requestId);
    List<String> findRequestImgUrlByRequestId(Integer requestId);
    int deleteTaskImgByRequestId(Integer requestId);

    int addTaskAssignment(RepairAssignment repairAssignment);
    int findIsExistAssignment(Integer requestId);
    List<RepairAssignment> findTaskAssignmentByRequestId(Integer requestId);

}
