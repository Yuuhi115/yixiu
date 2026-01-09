package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.SuperAdminLoginToken;
import gdufs.yixiu.dto.RoleChangeDto;
import gdufs.yixiu.dto.UserBasicInfoDto;
import gdufs.yixiu.dto.UserModifyDto;
import gdufs.yixiu.pojo.Notification;
import gdufs.yixiu.service.NotificationService;

import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.util.EmailUtils;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/notify")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private EmailUtils emailUtils;
    @Autowired
    private UsersService usersService;
    @PostMapping("/roleChange")
    @SuperAdminLoginToken
    public Result roleChange(@RequestBody RoleChangeDto roleChangeDto,
                             HttpServletRequest request){
        String token = request.getHeader("Authorization");

        Notification notification = new Notification();
        notification.setSenderId(jwtUtils.getInfoFromToken(token).getId());
        notification.setReceiverId(roleChangeDto.getUserId());
        notification.setTitle("账户身份变更通知");
        notification.setType("USER");
        new UserBasicInfoDto();
        UserBasicInfoDto userBasicInfoDto;
        if (roleChangeDto.getRole().equals("admin") && roleChangeDto.getOriginalRole().equals("volunteer")) {
            notification.setContent("您的账户身份已由【志愿者】更改为【管理员】");
            userBasicInfoDto = usersService.queryUserById(roleChangeDto.getUserId());
            if (userBasicInfoDto.getEmail() != null) {
                emailUtils.sendCustomVerifyCode(userBasicInfoDto.getEmail(), "【广外义修通】 账户身份变更通知", "您的账户身份已被更改为【管理员】，请重新以新身份登录平台，如有疑问，请联系义修队队长");
            }
        }
        if (roleChangeDto.getOriginalRole().equals("admin") && roleChangeDto.getRole().equals("volunteer")) {
            notification.setContent("您的账户身份已由【管理员】更改为【志愿者】");
            userBasicInfoDto = usersService.queryUserById(roleChangeDto.getUserId());
            if (userBasicInfoDto.getEmail() != null) {
                emailUtils.sendCustomVerifyCode(userBasicInfoDto.getEmail(), "【广外义修通】 账户身份变更通知", "您的账户身份已被更改为【志愿者】，请重新以新身份登录平台，如有疑问，请联系义修队队长");
            }
        }
        notificationService.saveAndPush(notification);
        log.info("用户(id:{})的身份已被管理员(id:{})更改为 {}", roleChangeDto.getUserId(), notification.getSenderId(), roleChangeDto.getRole());
        redisTemplate.delete("token:" + roleChangeDto.getUserId());
        return Result.success("更改成功");
    }
}
