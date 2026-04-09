package gdufs.yixiu.dto.community.request.skill;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VolunteerScoreDto {
    private Integer volunteerId;
    private List<RequestCategoryAndScoreDto> requestCategoryAndScoreList;
}
