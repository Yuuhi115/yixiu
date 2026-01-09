package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VolunteerInfo {
    private Integer volunteerId;
    private Integer userId;
    private String studentNumber;
    private String majorClass;
    private String grade;
    private Integer status;
    private Integer contactType; // 联系方式 0: 手机号 1: 邮箱号 2: 微信号 3: QQ号
    private String contactNumber;
    private Timestamp createTime;
    private Timestamp updateTime;
}
