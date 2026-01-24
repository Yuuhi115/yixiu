package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFollow {
    private Integer followId;
    private Integer followerId;
    private Integer followeeId;
    private Integer status;
    private Integer isUpdate;
    private Timestamp createTime;
    private Timestamp updateTime;
}
