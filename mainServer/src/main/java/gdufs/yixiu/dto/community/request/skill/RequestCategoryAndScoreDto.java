package gdufs.yixiu.dto.community.request.skill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestCategoryAndScoreDto {
    private Integer requestId;
    private Integer skillId;
    private Integer score;
}
