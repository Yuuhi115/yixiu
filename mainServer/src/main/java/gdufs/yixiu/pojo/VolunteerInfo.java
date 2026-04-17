package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

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
    private List<Integer> expertSkillIds;
    private Integer handlingTaskNum;
    private Integer finishedTaskNum;
    private Timestamp createTime;
    private Timestamp updateTime;
}
