package gdufs.yixiu.dto.community.request.skill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillScoreCalculateDto {
    private List<VolunteerScoreDto> volunteerScoreDtoList;
    private Integer requestId;
    private Integer score;
    private Integer skillId;
}
