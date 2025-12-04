package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VolunteerModifyDto {
    private Integer userId;
    private String studentNumber;
    private String majorClass;
    private String grade;
}
