package gdufs.yixiu.service;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.NotificationDto;
import gdufs.yixiu.dto.NotificationFilterDto;
import gdufs.yixiu.pojo.Notification;

import java.util.List;

public interface NotificationService {
    void sendUserNotify(Notification notification);
    void sendBroadcast(Notification notification);
    void sendSystemNotify(Notification notification);
    void saveAndPush(Notification notification);
    int findUnreadCount(Integer receiverId);
    PageInfo<NotificationDto> queryByReceiverId(Integer receiverId, Integer pageNum, Integer pageSize);
    PageInfo<NotificationDto> queryByReceiverIdFilter(NotificationFilterDto notificationFilterDto, Integer pageNum, Integer pageSize);
    int updateIsRead(Integer notifyId, Integer receiverId);
    List<NotificationDto> notificationsToNotificationDtos(List<Notification> notifications, Integer receiverId);
}
