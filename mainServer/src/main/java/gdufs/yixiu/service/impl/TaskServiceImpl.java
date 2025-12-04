package gdufs.yixiu.service.impl;

import gdufs.yixiu.dao.TaskMapper;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dto.RepairAssignmentDto;
import gdufs.yixiu.dto.RepairRequestDto;
import gdufs.yixiu.dto.TaskFilterDto;
import gdufs.yixiu.pojo.RepairAssignment;
import gdufs.yixiu.pojo.RepairRequest;
import gdufs.yixiu.pojo.RepairRequestImg;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.TaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private String serviceRequestUrl;
    @Value("${resources-path.service-request-url}")
    private void setServiceRequestUrl(String serviceRequestUrl) {
        this.serviceRequestUrl = serviceRequestUrl;
    }
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
    public String addTaskAssignment(RepairAssignmentDto repairAssignmentDto) {
        RepairAssignment repairAssignment = new RepairAssignment();
        RepairRequest repairRequest = new RepairRequest();
        repairRequest.setRequestId(repairAssignmentDto.getRequestId());
        repairRequest.setStatus(2);
        int assignNum = taskMapper.findIsExistAssignment(repairAssignmentDto.getRequestId());
        if (assignNum > 0) {
            repairAssignment.setIsLeader(0);
        }else {
            repairAssignment.setIsLeader(1);
        }
        repairAssignment.setRequestId(repairAssignmentDto.getRequestId());
        repairAssignment.setVolunteerId(repairAssignmentDto.getVolunteerId());
        repairAssignment.setRemarks(repairAssignmentDto.getRemarks());
        int rows = taskMapper.addTaskAssignment(repairAssignment);
        int rows2 = taskMapper.updateTask(repairRequest);
        log.info("志愿者No.{} 添加报修分配单成功，分配id为：{}，任务id为：{}",repairAssignmentDto.getVolunteerId(), repairAssignment.getAssignId(), repairAssignmentDto.getRequestId());
        return rows > 0 && rows2 > 0 ? "success" : null;
    }

    @Override
    public boolean updateTask(RepairRequestDto repairRequestDto) {
        RepairRequest repairRequest = new RepairRequest();
        repairRequest.setRequestId(repairRequestDto.getRequestId());
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
        repairRequest.setStatus(repairRequestDto.getStatus());
        int rows = taskMapper.updateTask(repairRequest);
        return rows > 0;
    }

    @Override
    public RepairRequestDto queryTaskById(Integer requestId) {
        RepairRequest repairRequest = taskMapper.findTaskById(requestId);
        if (repairRequest == null) {
            return null;
        }
        Integer userId = repairRequest.getUserId();
        Users users = usersMapper.findUserById(userId);
        return repairRequestPojoToDto(users, repairRequest);
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
            RepairRequestDto repairRequestDto = repairRequestPojoToDto(user, repairRequest);
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
            RepairRequestDto repairRequestDto = repairRequestPojoToDto(user, repairRequest);
            repairRequestDtos.add(repairRequestDto);
        }
        return repairRequestDtos;
    }

    @Override
    public List<RepairRequestDto> queryAllTask() {
        List<RepairRequest> repairRequests = taskMapper.findAllTask();
        if (repairRequests == null) {
            return null;
        }
        List<RepairRequestDto> repairRequestDtos = new ArrayList<>();
        for (RepairRequest repairRequest : repairRequests) {
            Users user = usersMapper.findUserById(repairRequest.getUserId());
            RepairRequestDto repairRequestDto = repairRequestPojoToDto(user, repairRequest);
            repairRequestDtos.add(repairRequestDto);
        }
        return repairRequestDtos;
    }

    @Override
    public List<RepairRequestDto> queryTaskByFilter(TaskFilterDto taskFilterDto) {
        TaskFilterDto newTaskFilterDto = transformEndTime(taskFilterDto);
        List<RepairRequest> repairRequests = taskMapper.findTaskByFilterDto(newTaskFilterDto);
        if (repairRequests == null) {
            return null;
        }
        List<RepairRequestDto> repairRequestDtos = new ArrayList<>();
        for (RepairRequest repairRequest : repairRequests) {
            Users user = usersMapper.findUserById(repairRequest.getUserId());
            RepairRequestDto repairRequestDto = repairRequestPojoToDto(user, repairRequest);
            repairRequestDtos.add(repairRequestDto);
        }
        return repairRequestDtos;
    }

    @Override
    public RepairRequestDto repairRequestPojoToDto(Users users, RepairRequest repairRequest) {
        RepairRequestDto repairRequestDto = new RepairRequestDto();
        List<String> imgUrls = taskMapper.findRequestImgUrlByRequestId(repairRequest.getRequestId());
        List<String> modifyImgUrls = new ArrayList<>();
        for (String url : imgUrls){
            url = serviceRequestUrl + url;
            modifyImgUrls.add(url);
        }
        List<RepairAssignment> repairAssignments = taskMapper.findTaskAssignmentByRequestId(repairRequest.getRequestId());
        repairRequestDto.setRepairAssignment(repairAssignments);
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
        repairRequestDto.setImgUrl(modifyImgUrls);
        repairRequestDto.setCreateTime(repairRequest.getCreateTime());
        repairRequestDto.setUpdateTime(repairRequest.getUpdateTime());
        repairRequestDto.setCompleteTime(repairRequest.getCompleteTime());
        return repairRequestDto;
    }

    @Override
    public TaskFilterDto transformEndTime(TaskFilterDto taskFilterDto) {
        if (taskFilterDto.getUpdateEndTime() != null){
            taskFilterDto.setUpdateEndTime(taskFilterDto.getUpdateEndTime() + " 23:59:59");
        }
        if (taskFilterDto.getCreateEndTime() != null){
            taskFilterDto.setCreateEndTime(taskFilterDto.getCreateEndTime() + " 23:59:59");
        }
        return taskFilterDto;
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
