package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepairAssignmentDto {
    private Integer assignId;
    private Integer requestId;
    private Integer volunteerId;
    private Integer isLeader;
    private Timestamp assignedTime;
    private Integer status;
    private String remarks;
    private Timestamp updateTime;
}
