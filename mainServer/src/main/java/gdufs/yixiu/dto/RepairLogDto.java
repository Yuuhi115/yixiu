package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepairLogDto {
    private Integer requestId;
    private Integer volunteerId;
    private String logContent;
    private String repairDuration;
    private String solutionSummary;
}
