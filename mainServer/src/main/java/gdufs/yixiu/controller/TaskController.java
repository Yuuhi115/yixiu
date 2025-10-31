package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.dto.RepairRequestDto;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* 报修单相关
* */

@Slf4j
@RestController
@RequestMapping("/task")
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
        if (!userId.equals(repairRequestDto.getUserId())){
            return Result.fail("用户信息错误");
        }
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
        if (originalImgNum != 0){
            taskService.deleteTaskImgByRequestId(requestId);
        }
        for (MultipartFile file : files){
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
        if (repairRequestDto == null){
            return Result.fail("没有此报修单");
        }
        return Result.success(repairRequestDto);
    }

    @UserLoginToken
    @GetMapping("/getByStatus")
    public Result getTaskByStatus(@RequestParam("status") String status,
                                  HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("用户No.{} 查询状态为 {} 的报修单", userId, status);
        List<RepairRequestDto> repairRequestDtos = taskService.queryTaskByStatus(status);
        if (repairRequestDtos == null){
            return Result.fail("没有此状态的报修单");
        }
        return Result.success(repairRequestDtos);
    }

}
