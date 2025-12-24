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
    private Timestamp createTime;
    private Timestamp updateTime;
}
