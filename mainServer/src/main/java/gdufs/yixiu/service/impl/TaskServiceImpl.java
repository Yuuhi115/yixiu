package gdufs.yixiu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dao.TaskMapper;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dao.VolunteerMapper;
import gdufs.yixiu.dto.*;
import gdufs.yixiu.pojo.*;
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
    @Autowired
    private VolunteerMapper volunteerMapper;
    private String serviceRequestUrl;
    private String serviceRepairLogUrl;
    private String serviceAvatarUrl;
    @Value("${resources-path.service-request-url}")
    private void setServiceRequestUrl(String serviceRequestUrl) {
        this.serviceRequestUrl = serviceRequestUrl;
    }
    @Value("${resources-path.service-repairLog-url}")
    private void setServiceRepairLogUrl(String serviceRepairLogUrl) {
        this.serviceRepairLogUrl = serviceRepairLogUrl;
    }
    @Value("${resources-path.service-avatar-url}")
    private void setServiceAvatarUrl(String serviceAvatarUrl) {
        this.serviceAvatarUrl = serviceAvatarUrl;
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
    public String applyToJoinTask(RepairAssignmentDto repairAssignmentDto) {

        RepairAssignment repairAssignment = new RepairAssignment();
        repairAssignment.setRequestId(repairAssignmentDto.getRequestId());
        repairAssignment.setVolunteerId(repairAssignmentDto.getVolunteerId());
        repairAssignment.setStatus(5);
        int rows = taskMapper.addTaskApplyAssignment(repairAssignment);
        if (rows > 0){
            log.info("志愿者No.{} 申请加入任务，任务id为：{}",repairAssignmentDto.getVolunteerId(), repairAssignmentDto.getRequestId());
        }
        return rows > 0 ? "success" : null;
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
    public PageInfo<RepairRequestDto> queryTaskByUserId(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<RepairRequest> repairRequests = taskMapper.findTaskByUserId(userId);
        Users user = usersMapper.findUserById(userId);
        if (repairRequests == null) {
            return null;
        }
        // 直接创建 PageInfo 对象包装原始查询结果
        PageInfo<RepairRequest> pageInfo = new PageInfo<>(repairRequests);

        List<RepairRequestDto> repairRequestDtos = new ArrayList<>();
        for (RepairRequest repairRequest : repairRequests) {
            RepairRequestDto repairRequestDto = repairRequestPojoToDto(user, repairRequest);
            repairRequestDtos.add(repairRequestDto);
        }

        // 保持分页信息
        PageInfo<RepairRequestDto> resultPageInfo = new PageInfo<>(repairRequestDtos);
        resultPageInfo.setTotal(pageInfo.getTotal());
        resultPageInfo.setPages(pageInfo.getPages());
        resultPageInfo.setPageNum(pageInfo.getPageNum());
        resultPageInfo.setPageSize(pageInfo.getPageSize());

        return resultPageInfo;
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
    public PageInfo<RepairRequestDto> queryAllTask(Integer pageNum, Integer pageSize) {

        PageHelper.startPage(pageNum, pageSize);
        List<RepairRequest> repairRequests = taskMapper.findAllTask();
        if (repairRequests == null) {
            return new PageInfo<>(new ArrayList<>());
        }

        // 直接创建 PageInfo 对象包装原始查询结果
        PageInfo<RepairRequest> pageInfo = new PageInfo<>(repairRequests);

        // 转换数据
        List<RepairRequestDto> repairRequestDtos = new ArrayList<>();
        for (RepairRequest repairRequest : pageInfo.getList()) {
            Users user = usersMapper.findUserById(repairRequest.getUserId());
            RepairRequestDto repairRequestDto = repairRequestPojoToDto(user, repairRequest);
            repairRequestDtos.add(repairRequestDto);
        }

        // 保持分页信息
        PageInfo<RepairRequestDto> resultPageInfo = new PageInfo<>(repairRequestDtos);
        resultPageInfo.setTotal(pageInfo.getTotal());
        resultPageInfo.setPages(pageInfo.getPages());
        resultPageInfo.setPageNum(pageInfo.getPageNum());
        resultPageInfo.setPageSize(pageInfo.getPageSize());

        return resultPageInfo;
    }

    @Override
    public PageInfo<RepairRequestDto> queryTaskByFilter(TaskFilterDto taskFilterDto, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        TaskFilterDto newTaskFilterDto = transformEndTime(taskFilterDto);
        List<RepairRequest> repairRequests = taskMapper.findTaskByFilterDto(newTaskFilterDto);
        if (repairRequests == null) {
            return null;
        }
        // 直接创建 PageInfo 对象包装原始查询结果
        PageInfo<RepairRequest> pageInfo = new PageInfo<>(repairRequests);
        // 转换数据
        List<RepairRequestDto> repairRequestDtos = new ArrayList<>();
        for (RepairRequest repairRequest : repairRequests) {
            Users user = usersMapper.findUserById(repairRequest.getUserId());
            RepairRequestDto repairRequestDto = repairRequestPojoToDto(user, repairRequest);
            repairRequestDtos.add(repairRequestDto);
        }
        // 保持分页信息
        PageInfo<RepairRequestDto> resultPageInfo = new PageInfo<>(repairRequestDtos);
        resultPageInfo.setTotal(pageInfo.getTotal());
        resultPageInfo.setPages(pageInfo.getPages());
        resultPageInfo.setPageNum(pageInfo.getPageNum());
        resultPageInfo.setPageSize(pageInfo.getPageSize());

        return resultPageInfo;
    }
    @Override
    public PageInfo<RepairRequestDto> queryMyTaskByFilter(TaskFilterDto taskFilterDto, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        TaskFilterDto newTaskFilterDto = transformEndTime(taskFilterDto);
        // 获取自己参与的任务(通过查TaskAssignment表)
        List<Integer> requestIds = taskMapper.findMyTaskIdsByFilterDto(newTaskFilterDto);
        if (requestIds == null) {
            return null;
        }
        PageInfo<Integer> pageInfo = new PageInfo<>(requestIds);
        List<RepairRequestDto> repairRequestDtos = new ArrayList<>();
        for (Integer requestId : requestIds) {
            RepairRequestDto repairRequestDto = queryTaskById(requestId);
            repairRequestDtos.add(repairRequestDto);
        }
        PageInfo<RepairRequestDto> resultPageInfo = new PageInfo<>(repairRequestDtos);
        resultPageInfo.setTotal(pageInfo.getTotal());
        resultPageInfo.setPages(pageInfo.getPages());
        resultPageInfo.setPageNum(pageInfo.getPageNum());
        resultPageInfo.setPageSize(pageInfo.getPageSize());
        return resultPageInfo;
    }

    @Override
    public PageInfo<RepairRequestDto> queryMyTaskByVolunteerId(Integer volunteerId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Integer> requestIds = taskMapper.findMyTaskAssignmentIdsByVolunteerId(volunteerId);
        if (requestIds == null) {
            return null;
        }
        PageInfo<Integer> pageInfo = new PageInfo<>(requestIds);
        List<RepairRequestDto> repairRequestDtos = new ArrayList<>();
        for (Integer requestId : requestIds) {
            RepairRequestDto repairRequestDto = queryTaskById(requestId);
            repairRequestDtos.add(repairRequestDto);
        }
        PageInfo<RepairRequestDto> resultPageInfo = new PageInfo<>(repairRequestDtos);
        resultPageInfo.setTotal(pageInfo.getTotal());
        resultPageInfo.setPages(pageInfo.getPages());
        resultPageInfo.setPageNum(pageInfo.getPageNum());
        resultPageInfo.setPageSize(pageInfo.getPageSize());
        return resultPageInfo;
    }


    @Override
    public boolean updateAssignmentStatus(Integer assignId, Integer status, String reason) {
        RepairAssignment repairAssignment = new RepairAssignment();
        repairAssignment.setAssignId(assignId);
        repairAssignment.setStatus(status);
        repairAssignment.setRemarks(reason);
        return taskMapper.updateTaskAssignment(repairAssignment) == 1;
    }

    @Override
    public boolean updateAssignmentStatusByRequestIdAndVolunteerId(Integer requestId, Integer volunteerId, Integer status) {
        RepairAssignment repairAssignment = new RepairAssignment();
        repairAssignment.setRequestId(requestId);
        repairAssignment.setVolunteerId(volunteerId);
        repairAssignment.setStatus(status);
        return taskMapper.updateTaskAssignmentByRequestIdAndVolunteerId(repairAssignment) == 1;
    }

    @Override
    public Integer addTaskEvaluate(RepairEvaluate repairEvaluate) {
        RepairRequestDto repairRequestDto = new RepairRequestDto();
        repairRequestDto.setRequestId(repairEvaluate.getRequestId());
        repairRequestDto.setStatus(7);
        boolean update = updateTask(repairRequestDto);
        if (!update) {
            return null;
        }
        int addRow = taskMapper.addTaskEvaluate(repairEvaluate);
        return addRow == 1 ? repairEvaluate.getEvaluateId() : null;
    }

    @Override
    public List<VolunteerStatisticsDto> exportRepairLogsWithDate(Integer volunteerId, String startDate, String endDate) {
        List<RepairLog> repairLogs = taskMapper.findRepairLogsWithDate(volunteerId, startDate, endDate);
        List<VolunteerStatisticsDto> excelDataList = new ArrayList<>();

        for (RepairLog repairLog : repairLogs) {
            VolunteerStatisticsDto excelDto = new VolunteerStatisticsDto();
            excelDto.setLogId(repairLog.getLogId());
            excelDto.setVolunteerId(repairLog.getVolunteerId());
            excelDto.setRequestId(repairLog.getRequestId());
            excelDto.setLogContent(repairLog.getLogContent());
            excelDto.setRepairDuration(repairLog.getRepairDuration());
            excelDto.setSolutionSummary(repairLog.getSolutionSummary());
            // 处理时间字段
            if (repairLog.getUploadTime() != null) {
                excelDto.setUploadTime(repairLog.getUploadTime().toLocalDateTime());
            }
            String name = volunteerMapper.findVolunteerNameByVolunteerId(repairLog.getVolunteerId());
            excelDto.setVolunteerName(name);
            excelDataList.add(excelDto);
        }

        return excelDataList;
    }
    // 从任务分配表里面获取到参与维修的用户id，排除传入的userId
    @Override
    public List<Integer> findTaskMemberIds(Integer requestId) {
        return taskMapper.findTaskMemberIdsByRequestId(requestId);
    }

    @Override
    public RepairRequestDto repairRequestPojoToDto(Users users, RepairRequest repairRequest) {
        RepairRequestDto repairRequestDto = new RepairRequestDto();

        // 拼接维修申请图片信息
        List<String> imgUrls = taskMapper.findRequestImgUrlByRequestId(repairRequest.getRequestId());
        List<String> modifyImgUrls = new ArrayList<>();
        for (String url : imgUrls){
            url = serviceRequestUrl + url;
            modifyImgUrls.add(url);
        }
        // 拼接任务分配单信息
        List<RepairAssignment> repairAssignments = taskMapper.findTaskAssignmentByRequestId(repairRequest.getRequestId());
        List<RepairAssignmentDto> repairAssignmentDtos = new ArrayList<>();
        for (RepairAssignment repairAssignment : repairAssignments){
            RepairAssignmentDto repairAssignmentDto = repairAssignmentPojoToDto(repairAssignment);
            repairAssignmentDtos.add(repairAssignmentDto);
        }
        repairRequestDto.setRepairAssignment(repairAssignmentDtos);

        // 拼接任务日志信息
        List<RepairLog> repairLogs = taskMapper.findTaskLogByRequestId(repairRequest.getRequestId());
        for (RepairLog repairLog : repairLogs){
            List<String> repairLogImgs = taskMapper.findTaskLogImgUrlByLogId(repairLog.getLogId());
            List<String> modifyRepairLogImgs = new ArrayList<>();
            for (String url : repairLogImgs){
                url = serviceRepairLogUrl + url;
                modifyRepairLogImgs.add(url);
            }
            repairLog.setLogImgUrl(modifyRepairLogImgs);
            repairLog.setVolunteerName(volunteerMapper.findVolunteerNameByVolunteerId(repairLog.getVolunteerId()));
        }

        // 拼接用户评价
        RepairEvaluate repairEvaluate = taskMapper.findTaskEvaluateByRequestId(repairRequest.getRequestId());
        repairRequestDto.setRepairEvaluate(repairEvaluate);

        repairRequestDto.setRepairLog(repairLogs);
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
    public RepairAssignmentDto repairAssignmentPojoToDto(RepairAssignment repairAssignment) {
        VolunteerInfo volunteer = volunteerMapper.findVolunteerInfoByVolunteerId(repairAssignment.getVolunteerId());
        Users users = usersMapper.findUserRealNameAndAvatarById(volunteer.getUserId());
        String realName = users.getRealName();
        String avatar = users.getAvatar();
        RepairAssignmentDto repairAssignmentDto = new RepairAssignmentDto();
        repairAssignmentDto.setAssignId(repairAssignment.getAssignId());
        repairAssignmentDto.setVolunteerName(realName);
        repairAssignmentDto.setAvatar(serviceAvatarUrl + avatar);
        repairAssignmentDto.setMajorClass(volunteer.getMajorClass());
        repairAssignmentDto.setGrade(volunteer.getGrade());
        repairAssignmentDto.setContactType(volunteer.getContactType());
        repairAssignmentDto.setContactNumber(volunteer.getContactNumber());
        repairAssignmentDto.setAssignId(repairAssignment.getAssignId());
        repairAssignmentDto.setRequestId(repairAssignment.getRequestId());
        repairAssignmentDto.setVolunteerId(repairAssignment.getVolunteerId());
        repairAssignmentDto.setIsLeader(repairAssignment.getIsLeader());
        repairAssignmentDto.setAssignedTime(repairAssignment.getAssignedTime());
        repairAssignmentDto.setStatus(repairAssignment.getStatus());
        repairAssignmentDto.setRemarks(repairAssignment.getRemarks());
        repairAssignmentDto.setUpdateTime(repairAssignment.getUpdateTime());
        return repairAssignmentDto;
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

    @Override
    public Integer addTaskLog(RepairLogDto repairLogDto) {
        RepairLog repairLog = new RepairLog();
        repairLog.setVolunteerId(repairLogDto.getVolunteerId());
        repairLog.setRequestId(repairLogDto.getRequestId());
        repairLog.setLogContent(repairLogDto.getLogContent());
        repairLog.setRepairDuration(repairLogDto.getRepairDuration());
        repairLog.setSolutionSummary(repairLogDto.getSolutionSummary());
        int row = taskMapper.addTaskLog(repairLog);
        if (row == 1) {
            RepairRequest repairRequest = new RepairRequest();
            repairRequest.setRequestId(repairLogDto.getRequestId());
            repairRequest.setStatus(3);
            int updateRow = taskMapper.updateTask(repairRequest);
            if (updateRow == 1) {
                return repairLog.getLogId();
            }
            return null;
        }
        return null;
    }

    @Override
    public List<RepairLogImg> queryTaskLogImgByLogId(Integer logId) {
        return taskMapper.findTaskLogImgByLogId(logId);
    }

    @Override
    public void deleteTaskLogImgByLogId(Integer logId) {
        taskMapper.deleteTaskLogImgByLogId(logId);
    }

    @Override
    public int updateTaskLogImportStatus(RepairLogDto repairLogDto) {
        if (repairLogDto.getLogId() == null)
            return 0;
        RepairLog repairLog = new RepairLog();
        repairLog.setLogId(repairLogDto.getLogId());
        if (repairLogDto.getImportStatus() != null)
            repairLog.setImportStatus(repairLogDto.getImportStatus());
        return taskMapper.updateTaskLog(repairLog);
    }
}
