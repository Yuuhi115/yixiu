package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto {
    private Integer notifyId;
    private Integer senderId;
    private Integer receiverId;
    private String title;
    private String content;
    private String link;
    private String type;
    private Integer isRead;
    private String senderUsername;
    private String senderAvatar;
    private Timestamp createTime;
}
