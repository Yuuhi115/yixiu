package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    private Integer notifyId;
    private Integer senderId;
    private Integer receiverId;
    private String title;
    private String content;
    private String type;
    private Integer isRead;
    private String link;
    private Timestamp createTime;
}
