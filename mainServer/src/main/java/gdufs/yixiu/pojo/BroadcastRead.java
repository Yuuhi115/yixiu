package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BroadcastRead {
    private Integer checkId;
    private Integer userId;
    private Integer notifyId;
    private Integer isRead;
    private Timestamp createTime;
    private Timestamp updateTime;
}
