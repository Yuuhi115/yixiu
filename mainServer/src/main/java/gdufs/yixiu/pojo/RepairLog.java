package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairLog {
    private Integer logId;
    private Integer requestId;
    private Integer volunteerId;
    private String volunteerName;
    private String logContent;
    private String repairDuration;
    private String solutionSummary;
    private Timestamp uploadTime;
    private Integer importStatus;
    private List<String> logImgUrl;
}
