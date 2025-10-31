package gdufs.yixiu.service.impl;

import gdufs.yixiu.dao.TaskMapper;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dto.RepairRequestDto;
import gdufs.yixiu.pojo.RepairRequest;
import gdufs.yixiu.pojo.RepairRequestImg;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class TaskServiceImpl implements TaskService {
    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private UsersMapper usersMapper;

    @Override
    public String addTask(RepairRequestDto repairRequestDto) {
        RepairRequest repairRequest = new RepairRequest();

        repairRequest.setUserId(repairRequestDto.getUserId());
        repairRequest.setContactType(repairRequestDto.getContactType());
        repairRequest.setContactInfo(repairRequestDto.getContactInfo());
        repairRequest.setDeviceType(repairRequestDto.getDeviceType());
        repairRequest.setDeviceSystem(repairRequestDto.getDeviceSystem());
        repairRequest.setDeviceModel(repairRequestDto.getDeviceModel());
        repairRequest.setProblemDescription(repairRequestDto.getProblemDescription());
        repairRequest.setCampus(repairRequestDto.getCampus());
        repairRequest.setRepairLocation(repairRequestDto.getRepairLocation());
        repairRequest.setAppointmentTime(repairRequestDto.getAppointmentTime());
        repairRequest.setRemarks(repairRequestDto.getRemarks());

        int rows = taskMapper.addTask(repairRequest);
        Integer requestId = repairRequest.getRequestId();
        log.info("用户No.{} 添加报修单成功，报修单id为：{}",repairRequestDto.getUserId(), requestId);
        return rows > 0 ? requestId.toString() : null;
    }

    @Override
    public RepairRequestDto queryTaskById(Integer requestId) {
        RepairRequest repairRequest = taskMapper.findTaskById(requestId);
        if (repairRequest == null) {
            return null;
        }
        Integer userId = repairRequest.getUserId();
        Users users = usersMapper.findUserById(userId);
        return RepairRequestPojoToDto(users, repairRequest);
    }

    @Override
    public List<RepairRequestDto> queryTaskByUserId(Integer userId) {
        List<RepairRequest> repairRequests = taskMapper.findTaskByUserId(userId);
        Users user = usersMapper.findUserById(userId);
        if (repairRequests == null) {
            return null;
        }
        List<RepairRequestDto> repairRequestDtos = new ArrayList<>();
        for (RepairRequest repairRequest : repairRequests) {
            RepairRequestDto repairRequestDto = RepairRequestPojoToDto(user, repairRequest);
            repairRequestDtos.add(repairRequestDto);
        }
        return repairRequestDtos;
    }

    @Override
    public List<RepairRequestDto> queryTaskByStatus(String status) {
        List<RepairRequest> repairRequests = taskMapper.findTaskByStatus(status);
        if (repairRequests == null) {
            return null;
        }
        List<RepairRequestDto> repairRequestDtos = new ArrayList<>();
        for (RepairRequest repairRequest : repairRequests) {
            Users user = usersMapper.findUserById(repairRequest.getUserId());
            RepairRequestDto repairRequestDto = RepairRequestPojoToDto(user, repairRequest);
            repairRequestDtos.add(repairRequestDto);
        }
        return repairRequestDtos;
    }

    @Override
    public RepairRequestDto RepairRequestPojoToDto(Users users, RepairRequest repairRequest) {
        RepairRequestDto repairRequestDto = new RepairRequestDto();
        repairRequestDto.setUserId(users.getUserId());
        repairRequestDto.setUsername(users.getUsername());
        repairRequestDto.setRealName(users.getRealName());

        repairRequestDto.setRequestId(repairRequest.getRequestId());
        repairRequestDto.setContactType(repairRequest.getContactType());
        repairRequestDto.setContactInfo(repairRequest.getContactInfo());
        repairRequestDto.setDeviceType(repairRequest.getDeviceType());
        repairRequestDto.setDeviceSystem(repairRequest.getDeviceSystem());
        repairRequestDto.setDeviceModel(repairRequest.getDeviceModel());
        repairRequestDto.setProblemDescription(repairRequest.getProblemDescription());
        repairRequestDto.setCampus(repairRequest.getCampus());
        repairRequestDto.setRepairLocation(repairRequest.getRepairLocation());
        repairRequestDto.setAppointmentTime(repairRequest.getAppointmentTime());
        repairRequestDto.setRemarks(repairRequest.getRemarks());
        repairRequestDto.setStatus(repairRequest.getStatus());
        return repairRequestDto;
    }

    @Override
    public List<RepairRequestImg> queryImgByRequestId(Integer requestId) {
        return taskMapper.findRequestImgByRequestId(requestId);
    }

    @Override
    public void deleteTaskImgByRequestId(Integer requestId) {
        taskMapper.deleteTaskImgByRequestId(requestId);
    }
}
