package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskFilterDto {
    private String createStartTime;
    private String createEndTime;
    private String updateStartTime;
    private String updateEndTime;
    private Integer status;
    private Integer userId;
}
