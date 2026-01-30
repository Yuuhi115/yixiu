package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.AdminLoginToken;
import gdufs.yixiu.annotation.SuperAdminLoginToken;
import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.annotation.VolunteerLoginToken;
import gdufs.yixiu.dao.TaskMapper;
import gdufs.yixiu.dto.NotifySubmitDto;
import gdufs.yixiu.dto.RoleChangeDto;
import gdufs.yixiu.dto.UserBasicInfoDto;
import gdufs.yixiu.dto.UserModifyDto;
import gdufs.yixiu.dto.community.vo.LikeListIdsVO;
import gdufs.yixiu.pojo.Notification;
import gdufs.yixiu.pojo.RepairRequest;
import gdufs.yixiu.service.NotificationService;

import gdufs.yixiu.service.TaskService;
import gdufs.yixiu.service.UsersService;
import gdufs.yixiu.util.EmailUtils;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Autowired
    private TaskService taskService;
    @Autowired
    private TaskMapper taskMapper;
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
    @AdminLoginToken
    @PostMapping("/taskApprove")
    public Result taskApprove(@RequestBody NotifySubmitDto notifySubmitDto){
        if (notifySubmitDto.getTaskId() == null || notifySubmitDto.getReceiverId() == null){
            return Result.fail("参数错误");
        }
        Notification notification = new Notification();
        notification.setTitle("报修审核通过通知");
        notification.setContent(
                "您的电脑维修申请(申请编号: " + notifySubmitDto.getTaskId() + ")已通过审核，请继续等待义修志愿者接收任务");
        notification.setReceiverId(notifySubmitDto.getReceiverId());
        notification.setType("SYSTEM");
        notification.setLink("/repair/history");
        notificationService.saveAndPush(notification);
        log.info("用户(id:{})的报修申请(id:{})已通过审核", notifySubmitDto.getReceiverId(), notifySubmitDto.getTaskId());
        return Result.success("审核成功");
    }
    @AdminLoginToken
    @PostMapping("/taskReject")
    public Result taskReject(@RequestBody NotifySubmitDto notifySubmitDto){
        if (notifySubmitDto.getTaskId() == null ||
                notifySubmitDto.getReceiverId() == null ||
                notifySubmitDto.getRemark() == null
        ){
            return Result.fail("参数错误");
        }
        Notification notification = new Notification();
        notification.setTitle("报修审核未通过通知");
        notification.setContent(
                "您的电脑维修申请(申请编号: " + notifySubmitDto.getTaskId() + ")未通过审核，请重新填写并提交申请。"
                + "拒绝原因: " + notifySubmitDto.getRemark() + "。如有疑问，请联系义修队队长");
        notification.setReceiverId(notifySubmitDto.getReceiverId());
        notification.setType("SYSTEM");
        notification.setLink("/repair/history");
        notificationService.saveAndPush(notification);
        log.info("用户(id:{})的报修申请(id:{})未通过审核", notifySubmitDto.getReceiverId(), notifySubmitDto.getTaskId());
        return Result.success("审核成功");
    }
    @VolunteerLoginToken
    @PostMapping("/taskAccept")
    public Result taskAccept(@RequestBody NotifySubmitDto notifySubmitDto, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        if (
                notifySubmitDto.getTaskId() == null ||
                notifySubmitDto.getReceiverId() == null
        ){
            return Result.fail("参数错误");
        }
        Notification notification = new Notification();
        notification.setTitle("报修申请接收通知");
        notification.setContent(
                "您的电脑维修申请(申请编号: " + notifySubmitDto.getTaskId() + ")已被志愿者接收，请留意志愿者的短信或来电");
        notification.setReceiverId(notifySubmitDto.getReceiverId());
        notification.setSenderId(userId);
        notification.setType("USER");
        notification.setLink("/repair/history");
        notificationService.saveAndPush(notification);
        log.info("用户(id:{})的报修申请(id:{})已被义修志愿者(id:{})接收", notifySubmitDto.getReceiverId(), notifySubmitDto.getTaskId(), userId);
        return Result.success("接收成功");
    }
    @VolunteerLoginToken
    @PostMapping("/taskComplete")
    public Result taskComplete(@RequestBody NotifySubmitDto notifySubmitDto, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        if (
                notifySubmitDto.getTaskId() == null
        ){
            return Result.fail("参数错误");
        }
        Notification toUserNotification = new Notification();
        toUserNotification.setTitle("报修任务完成通知");
        toUserNotification.setContent(
                "您的电脑维修申请(申请编号: " + notifySubmitDto.getTaskId() + ")已完成维修，请确认并填写评价");
        Integer customerId = taskMapper.findTaskById(notifySubmitDto.getTaskId()).getUserId();
        toUserNotification.setReceiverId(customerId);
        toUserNotification.setSenderId(userId);
        toUserNotification.setType("USER");
        toUserNotification.setLink("/repair/history");
        notificationService.saveAndPush(toUserNotification);

        String name = usersService.queryUserById(userId).getRealName();
        List<Integer> notifyUserIds = taskService.findTaskMemberIds(notifySubmitDto.getTaskId());
        Notification toTaskMemberNotification = new Notification();
        toTaskMemberNotification.setTitle("维修任务日志填写通知");
        toTaskMemberNotification.setContent(
                "队员: " + name + " 已完成电脑维修任务(申请编号: " + notifySubmitDto.getTaskId() + ")，请尽快填写维修任务日志");
        toTaskMemberNotification.setSenderId(userId);
        toTaskMemberNotification.setType("USER");
        toTaskMemberNotification.setLink("/taskCenter/myTask");
        for (Integer notifyUserId : notifyUserIds) {
            if (notifyUserId.equals(userId)){
                continue;
            }
            toTaskMemberNotification.setReceiverId(notifyUserId);
            notificationService.saveAndPush(toTaskMemberNotification);
            log.info("用户(id:{})的报修任务(id:{})已完成，已发送维修日志填写通知给队员(id:{})", userId, notifySubmitDto.getTaskId(), toTaskMemberNotification.getReceiverId());
        }
        return Result.success("操作成功");
    }
    @UserLoginToken
    @PostMapping("/taskEvaluateComplete")
    public Result taskEvaluateComplete(@RequestBody NotifySubmitDto notifySubmitDto, HttpServletRequest request){
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        if (
                notifySubmitDto.getTaskId() == null
        ){
            return Result.fail("参数错误");
        }
        Notification notification = new Notification();
        notification.setTitle("报修任务评价完成通知");
        notification.setContent(
                "您接收的电脑维修任务(申请编号: " + notifySubmitDto.getTaskId() + ")已由用户(id: " + userId + ")完成评价，请确认评价结果");
        notification.setSenderId(userId);
        notification.setType("USER");
        notification.setLink("/taskCenter/myTask");
        List<Integer> notifyUserIds = taskService.findTaskMemberIds(notifySubmitDto.getTaskId());
        for (Integer notifyUserId : notifyUserIds) {
            notification.setReceiverId(notifyUserId);
            notificationService.saveAndPush(notification);
            log.info("用户(id:{})的电脑报修申请(申请单号:{})已完成评价，已发送评价完成通知给队员(id:{})", userId, notifySubmitDto.getTaskId(), notification.getReceiverId());
        }
        return Result.success("操作成功");
    }
}
