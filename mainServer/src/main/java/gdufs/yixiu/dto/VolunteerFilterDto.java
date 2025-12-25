package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VolunteerFilterDto {
    private Integer excludeUserId;
    private Integer status;
    private String majorClass;
    private String grade;
}
