package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TaskStatusDto {
    private Integer requestId;
    private Integer status;
}
