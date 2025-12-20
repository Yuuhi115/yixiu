package gdufs.yixiu.dao;

import gdufs.yixiu.pojo.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    int addNotification(Notification notification);
    int addBroadcastIsRead(@Param("notifyId") Integer notifyId, @Param("userId") Integer userId);
    List<Notification> queryByReceiverId(Integer receiverId);
    Integer checkIsReadBroadcast(@Param("notifyId") Integer notifyId, @Param("userId") Integer userId);
    int changeToRead(Integer notifyId);
    int changeToReadBroadcast(@Param("notifyId") Integer notifyId, @Param("userId") Integer userId);
    int unreadCount(Integer receiverId);
    int unreadBroadcastCount(Integer receiverId);
    String queryTypeByNotifyId(Integer notifyId);
}
