package gdufs.yixiu.service;

import gdufs.yixiu.dto.NotificationDto;
import gdufs.yixiu.pojo.Notification;

import java.util.List;

public interface NotificationService {
    void sendUserNotify(Notification notification);
    void sendBroadcast(Notification notification);
    void sendSystemNotify(Notification notification);
    void saveAndPush(Notification notification);
    int findUnreadCount(Integer receiverId);
    List<NotificationDto> queryByReceiverId(Integer receiverId);
    int updateIsRead(Integer notifyId, Integer receiverId);
}
