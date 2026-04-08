package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillInfo {
    private Integer skillId;
    private String skillName;
    private Timestamp createTime;
    private Timestamp updateTime;
}
