package gdufs.yixiu.service;

import gdufs.yixiu.dto.RepairRequestDto;
import gdufs.yixiu.pojo.RepairRequest;
import gdufs.yixiu.pojo.RepairRequestImg;
import gdufs.yixiu.pojo.Users;

import java.util.List;

public interface TaskService {
    String addTask(RepairRequestDto repairRequestDto);
    RepairRequestDto queryTaskById(Integer requestId);
    List<RepairRequestDto> queryTaskByUserId(Integer userId);
    List<RepairRequestDto> queryTaskByStatus(String status);
    RepairRequestDto RepairRequestPojoToDto(Users users, RepairRequest repairRequest);

    List<RepairRequestImg> queryImgByRequestId(Integer requestId);
    void deleteTaskImgByRequestId(Integer requestId);
}
