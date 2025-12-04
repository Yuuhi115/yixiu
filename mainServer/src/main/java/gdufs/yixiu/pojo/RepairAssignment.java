package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairAssignment {
    private Integer assignId;
    private Integer requestId;
    private Integer volunteerId;
    private Integer isLeader;
    private Timestamp assignedTime;
    private Integer status;
    private String remarks;
    private Timestamp updateTime;
}
