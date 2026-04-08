package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VolunteerSkill {
    private Integer skillRelId;
    private Integer volunteerId;
    private Integer skillId;
    private Integer taskCount;
    private Double averageScore;
    private Double bayesianScore;
}
