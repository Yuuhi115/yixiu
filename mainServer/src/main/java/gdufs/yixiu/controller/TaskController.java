package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.dto.RepairAssignmentDto;
import gdufs.yixiu.dto.RepairRequestDto;
import gdufs.yixiu.dto.TaskFilterDto;
import gdufs.yixiu.dto.TaskStatusDto;
import gdufs.yixiu.pojo.RepairRequest;
import gdufs.yixiu.service.ImgUploadService;
import gdufs.yixiu.service.TaskService;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/*
* 报修单相关
* */

@Slf4j
@RestController
@RequestMapping("/api/v1/task")
public class TaskController {
    @Autowired
    private TaskService taskService;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private ImgUploadService imgUploadService;

    @UserLoginToken
    @PostMapping("/add")
    public Result addTask(@RequestBody RepairRequestDto repairRequestDto,
                          HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        repairRequestDto.setUserId(userId);
        String requestId = taskService.addTask(repairRequestDto);
        Map<String, String> map = new HashMap<>();
        map.put("requestId", requestId);
        return Result.success(map);
    }

    @UserLoginToken
    @PostMapping("/uploadRequestImg")
    public Result uploadRequestImg(@RequestParam("requestId") Integer requestId,
                                   @RequestParam("img") MultipartFile[] files,
                                   HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        int count = 1;
        List<String> imgUrls = new ArrayList<>();
        log.info("用户No.{} 上传报修单No.{} 的图片数量为 {}", userId, requestId, files.length);
        Integer originalImgNum = taskService.queryImgByRequestId(requestId).size();
        if (originalImgNum != 0) {
            taskService.deleteTaskImgByRequestId(requestId);
        }
        for (MultipartFile file : files) {
            String imgUrl = imgUploadService.uploadRequestImg(file, requestId, count);
            imgUrls.add(imgUrl);
            count++;
        }
        Map<String, List<String>> map = new HashMap<>();
        map.put("imgUrls", imgUrls);
        return Result.success(map);
    }

    @UserLoginToken
    @GetMapping("/getByRid")
    public Result getTaskByRequestId(@RequestParam("requestId") Integer requestId,
                                     HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("用户No.{} 查询报修单No.{}", userId, requestId);
        RepairRequestDto repairRequestDto = taskService.queryTaskById(requestId);
        if (repairRequestDto == null) {
            return Result.fail("没有此报修单");
        }
        return Result.success(repairRequestDto);
    }

//    @UserLoginToken
//    @GetMapping("/getByStatus")
//    public Result getTaskByStatus(@RequestParam("status") String status,
//                                  HttpServletRequest request) {
//        String token = request.getHeader("Authorization");
//        Integer userId = jwtUtils.getInfoFromToken(token).getId();
//        log.info("用户No.{} 查询状态为 {} 的报修单", userId, status);
//        List<RepairRequestDto> repairRequestDtos = taskService.queryTaskByStatus(status);
//        if (repairRequestDtos == null) {
//            return Result.fail("没有此状态的报修单");
//        }
//        return Result.success(repairRequestDtos);
//    }

    @UserLoginToken
    @GetMapping("/getByUserId")
    public Result getTaskByUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("用户No.{} 获取自己的报修单", userId);
        List<RepairRequestDto> repairRequestDtos = taskService.queryTaskByUserId(userId);
        if (repairRequestDtos == null) {
            return Result.fail("没有此用户报修单");
        }
        return Result.success(repairRequestDtos);
    }
    @UserLoginToken
    @GetMapping("/getAll")
    public Result getAllTask(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("用户No.{} 获取所有报修单", userId);
        List<RepairRequestDto> repairRequestDtos = taskService.queryAllTask();
        if (repairRequestDtos == null) {
            return Result.fail("没有报修单");
        }
        return Result.success(repairRequestDtos);
    }
    @UserLoginToken
    @GetMapping("/getByFilter")
    public Result getTaskByFilter(TaskFilterDto taskFilterDto,
                                  HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        String role = jwtUtils.getInfoFromToken(token).getRole();
        if (!Arrays.asList("volunteer", "admin", "super_admin").contains(role)) {
            return Result.fail("权限不足");
        }
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("用户No.{} 获取报修单", userId);
//        log.info("筛选条件为 {}", taskFilterDto);
        List<RepairRequestDto> repairRequestDtos = taskService.queryTaskByFilter(taskFilterDto);
        if (repairRequestDtos == null) {
            return Result.fail("未找到此条件的报修单");
        }
        return Result.success(repairRequestDtos);
    }
    @UserLoginToken
    @GetMapping("/getByFilterLimitUser")
    public Result getTaskByFilterLimitUser(TaskFilterDto taskFilterDto,
                                  HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        if (!taskFilterDto.getUserId().equals(userId)) {
            return Result.fail("身份信息异常");
        }
        log.info("用户No.{} 获取报修单", userId);
        log.info("筛选条件为 {}", taskFilterDto);
        List<RepairRequestDto> repairRequestDtos = taskService.queryTaskByFilter(taskFilterDto);
        if (repairRequestDtos == null) {
            return Result.fail("未找到此条件的报修单");
        }
        return Result.success(repairRequestDtos);
    }
    @UserLoginToken
    @PutMapping("/updateStatus")
    public Result updateTaskStatus(@RequestBody TaskStatusDto taskStatusDto,
                                   HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        String role = jwtUtils.getInfoFromToken(token).getRole();
        if (taskStatusDto.getStatus() == 0){
            return Result.fail("操作异常");
        }
        if (role.equals("student") && Arrays.asList(1, 2, 3, 6).contains(taskStatusDto.getStatus())) {
            return Result.fail("权限不足");
        }
        if (role.equals("volunteer") && Arrays.asList(1, 6).contains(taskStatusDto.getStatus())) {
            return Result.fail("权限不足");
        }
        log.info("{}用户No.{} 更新报修单No.{} 状态为 {}",role, userId, taskStatusDto.getRequestId(), taskStatusDto.getStatus());
        RepairRequestDto repairRequestDto = taskService.queryTaskById(taskStatusDto.getRequestId());
        if (repairRequestDto == null) {
            return Result.fail("没有此报修单");
        }
        repairRequestDto.setStatus(taskStatusDto.getStatus());
        boolean result = taskService.updateTask(repairRequestDto);
        if (!result) {
            return Result.fail("更新异常");
        }
        return Result.success("更新成功");
    }
    @UserLoginToken
    @PostMapping("/addAssign")
    public Result addTaskAssignment(@RequestBody RepairAssignmentDto repairAssignmentDto,
                                    HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        String role = jwtUtils.getInfoFromToken(token).getRole();
        if (!Arrays.asList("volunteer", "admin", "super_admin").contains(role)) {
            return Result.fail("权限不足");
        }
        String assignId = taskService.addTaskAssignment(repairAssignmentDto);
        return assignId == null ? Result.fail("添加异常") : Result.success("添加成功(assignId: " + assignId + ")");
    }
}
