package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.AdminLoginToken;
import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.dto.NotificationDto;
import gdufs.yixiu.pojo.Notification;
import gdufs.yixiu.service.NotificationService;
import gdufs.yixiu.service.impl.NotificationServiceImpl;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.LongPullingNotifier;
import gdufs.yixiu.util.Result;
import gdufs.yixiu.vo.NotifyPushVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/api/v1/notify")
public class LongPollingController {
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private NotificationService notificationService;

    /**
     * 通知长轮询接口
     * 前端登录后循环调用
     */
    @UserLoginToken
    @GetMapping("/poll")
    public DeferredResult<NotifyPushVO> poll(
            HttpServletRequest request
    ) {
        // 从请求中解析当前用户 ID（来自 token）
        Integer userId = jwtUtils.getInfoFromToken(request.getHeader("Authorization")).getId();

        // 创建 DeferredResult，超时时间 30 秒
        DeferredResult<NotifyPushVO> result =
                new DeferredResult<>(30_000L);

        // 注册到 LongPollingNotifier
        LongPullingNotifier.add(userId, result);

        // 超时返回（30 秒内没新通知）
        result.onTimeout(() -> {
            result.setResult(new NotifyPushVO("NONE", notificationService.findUnreadCount(userId)));
        });

        // 客户端中断 / 网络异常清理
        result.onCompletion(() -> {
            LongPullingNotifier.remove(userId, result);
        });

        return result;
    }
    @UserLoginToken
    @PostMapping("/userToUser")
    public Result userToUser(@RequestBody NotificationDto notificationDto, HttpServletRequest request){
        Notification notification = new Notification();
        notification.setSenderId(jwtUtils.getInfoFromToken(request.getHeader("Authorization")).getId());
        notification.setReceiverId(notificationDto.getReceiverId());
        notification.setContent(notificationDto.getContent());
        notification.setTitle(notificationDto.getTitle());
        notification.setLink(notificationDto.getLink());
        notificationService.sendUserNotify(notification);
        return Result.success("发送成功");
    }
    @AdminLoginToken
    @PostMapping("/broadcast")
    public Result broadcast(@RequestBody NotificationDto notificationDto){
        Notification notification = new Notification();
        notification.setSenderId(notificationDto.getSenderId());
        notification.setContent(notificationDto.getContent());
        notification.setTitle(notificationDto.getTitle());
        notification.setLink(notificationDto.getLink());
        notificationService.sendBroadcast(notification);
        return Result.success("发送成功");
    }
    @AdminLoginToken
    @PostMapping("/systemToUser")
    public Result systemToUser(@RequestBody NotificationDto notificationDto){
        Notification notification = new Notification();
        notification.setSenderId(notificationDto.getSenderId());
        notification.setReceiverId(notificationDto.getReceiverId());
        notification.setContent(notificationDto.getContent());
        notification.setTitle(notificationDto.getTitle());
        notification.setLink(notificationDto.getLink());
        notificationService.sendSystemNotify(notification);
        return Result.success("发送成功");
    }
    @UserLoginToken
    @GetMapping("/list")
    public Result getByReceiverId(HttpServletRequest request){
        Integer userId = jwtUtils.getInfoFromToken(request.getHeader("Authorization")).getId();
        return Result.success(notificationService.queryByReceiverId(userId));
    }
    @UserLoginToken
    @PutMapping("/changeToRead")
    public Result changeToRead(@RequestParam("notifyId") Integer notifyId, HttpServletRequest request){
        Integer userId = jwtUtils.getInfoFromToken(request.getHeader("Authorization")).getId();
        int result = notificationService.updateIsRead(notifyId, userId);
        return result == 1 ? Result.success("修改成功") : Result.fail("修改失败");
    }
    @UserLoginToken
    @GetMapping("/getUnreadCount")
    public Result getUnreadCount(HttpServletRequest request){
        Integer userId = jwtUtils.getInfoFromToken(request.getHeader("Authorization")).getId();
        return Result.success(notificationService.findUnreadCount(userId));
    }
}
