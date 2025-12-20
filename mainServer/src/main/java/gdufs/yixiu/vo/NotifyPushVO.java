package gdufs.yixiu.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotifyPushVO {
    private String type;    // USER / SYSTEM / BROADCAST
    private Integer unread; // 未读数量
}
