package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.AdminLoginToken;
import gdufs.yixiu.annotation.PassToken;
import gdufs.yixiu.annotation.SuperAdminLoginToken;
import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.dto.UsersRegisterDto;
import gdufs.yixiu.dto.VolunteerModifyDto;
import gdufs.yixiu.service.AdminService;
import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.util.EmailUtils;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
public class AdminController {
    @Autowired
    private AdminService adminService;
    @Autowired
    private UsersService usersService;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private EmailUtils emailUtils;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @PassToken
    @PostMapping("loginByEmail")
    public Result loginByEmail(@RequestBody UsersRegisterDto userDto) {
        String token = adminService.loginByEmail(userDto);
        if (token == null) {
            return Result.fail("管理员信息不存在");
        }else {
            return Result.success(token);
        }
    }
    @AdminLoginToken
    @GetMapping("inviteCode")
    public Result sendRegisterInviteCode(@RequestParam String email) {
        String code = adminService.sendInviteCode(email);
        return Result.success(code);
    }
    @SuperAdminLoginToken
    @PutMapping("volunteerInfo")
    public Result modifyVolunteerInfo(@RequestBody VolunteerModifyDto volunteerModifyDto,
                                     HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("管理员 (userId:{}) 正在修改志愿者 (userId:{}) 的信息", userId, volunteerModifyDto.getUserId());
        Integer result = adminService.modifyVolunteerInfo(volunteerModifyDto);
        return result == 1 ? Result.success("更新成功") : Result.fail("更新失败");
    }
    @AdminLoginToken
    @PostMapping("sendTaskApproveEmail")
    public Result sendTaskApproveEmail(Integer taskId, HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        String value = redisTemplate.opsForValue().get("config:taskApproveEmail");
        if (value == null) {
            log.warn("Redis中不存在配置: config:taskApproveEmail，使用默认值0");
            value = "0";
        }
        int result = Integer.parseInt(value);
        if (result == 1) {
            log.info("管理员(user_id:{})正在发送任务 (taskId:{}) 审批通过邮件",userId, taskId);
            emailUtils.sendTaskApproveNotify(userId, taskId);
            return Result.success("发送成功");
        }else {
            log.info("管理员(user_id:{})正在发送任务 (taskId:{}) 审批通过邮件，但是未开启邮件发送功能",userId, taskId);
            return Result.success("未开启邮件发送功能");
        }
    }
}
