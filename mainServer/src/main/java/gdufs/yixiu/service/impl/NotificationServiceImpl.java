package gdufs.yixiu.service.impl;

import gdufs.yixiu.dao.NotificationMapper;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dto.NotificationDto;
import gdufs.yixiu.pojo.Notification;
import gdufs.yixiu.pojo.Users;
import gdufs.yixiu.service.NotificationService;
import gdufs.yixiu.util.LongPullingNotifier;
import gdufs.yixiu.vo.NotifyPushVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {
    @Autowired
    private NotificationMapper notificationMapper;
    @Autowired
    private UsersMapper usersMapper;

    private String avatarPath;

    @Value("${resources-path.service-avatar-url}")
//    @Value("${resources-path.service-linux-avatar-url}")
    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    /**
     * 用户 -> 用户
     */
    @Override
    public void sendUserNotify(Notification notification) {
        notification.setType("USER");
        saveAndPush(notification);
        log.info("用户{}发送了通知{}给用户{}", notification.getSenderId(), notification.getContent(), notification.getReceiverId());
    }

    @Override
    public void sendBroadcast(Notification notification) {
        // 查所有用户
        notification.setType("BROADCAST");
        notification.setReceiverId(0);
        notification.setSenderId(0);
        saveAndPush(notification);
        log.info("系统发送了广播{}给所有用户", notification.getContent());
    }

    /**
     * 系统 -> 单个用户
     */
    @Override
    public void sendSystemNotify(Notification notification) {
        notification.setType("SYSTEM");
        notification.setSenderId(0);
        saveAndPush(notification);
        log.info("系统发送了系统消息{}给用户{}", notification.getContent(), notification.getReceiverId());
    }

    /**
     * 系统 -> 全体公告
     */
    @Override
    public void saveAndPush(Notification notification) {

        // 通知落库
        notificationMapper.addNotification(notification);

        if (Objects.equals(notification.getType(), "BROADCAST")){
            List<Integer> userIds = usersMapper.findAllUserIds();
            for (Integer userId : userIds) {
                notificationMapper.addBroadcastIsRead(notification.getNotifyId(), userId);
            }
        }

        Integer receiverId = notification.getReceiverId();

        // 统计未读数量
        int totalUnread = findUnreadCount(receiverId);

        NotifyPushVO pushVO = new NotifyPushVO(notification.getType(), totalUnread);

        if (receiverId == 0) {
            LongPullingNotifier.broadcast(pushVO);
        }

        // 唤醒该用户的轮询请求
        LongPullingNotifier.notifyUser(receiverId, pushVO);
    }

    @Override
    public int findUnreadCount(Integer receiverId) {
        int unread = notificationMapper.unreadCount(receiverId);
        int broadcastUnread = notificationMapper.unreadBroadcastCount(receiverId);
        return unread + broadcastUnread;
    }

    @Override
    public List<NotificationDto> queryByReceiverId(Integer receiverId) {
        List<Notification> notifications = notificationMapper.queryByReceiverId(receiverId);
        List<NotificationDto> notificationDtoList = new ArrayList<>();
        for (Notification notification : notifications) {
            NotificationDto notificationDto = new NotificationDto();
            if (Objects.equals(notification.getType(), "BROADCAST")){
                notificationDto.setIsRead(notificationMapper.checkIsReadBroadcast(notification.getNotifyId(), receiverId));
                log.info("用户{}的广播{}未读数量为{}", receiverId, notification.getNotifyId(), notificationDto.getIsRead());
            }else {
                notificationDto.setIsRead(notification.getIsRead());
                Users users = usersMapper.findUserById(notification.getSenderId());
                notificationDto.setSenderUsername(users.getUsername());
                notificationDto.setSenderAvatar(avatarPath + users.getAvatar());
            }
            notificationDto.setNotifyId(notification.getNotifyId());
            notificationDto.setSenderId(notification.getSenderId());
            notificationDto.setReceiverId(notification.getReceiverId());
            notificationDto.setTitle(notification.getTitle());
            notificationDto.setContent(notification.getContent());
            notificationDto.setLink(notification.getLink());
            notificationDto.setType(notification.getType());
            notificationDto.setCreateTime(notification.getCreateTime());
            notificationDtoList.add(notificationDto);
        }
        return notificationDtoList;
    }

    @Override
    public int updateIsRead(Integer notifyId, Integer receiverId) {
        String type = notificationMapper.queryTypeByNotifyId(notifyId);
        int row;
        if (type.equals("BROADCAST")){
            row = notificationMapper.changeToReadBroadcast(notifyId, receiverId);
        }
        else {
            row = notificationMapper.changeToRead(notifyId);
        }
        return row;
    }
}
