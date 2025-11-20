package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairRequest {
    private Integer requestId;
    private Integer userId;
    private String contactType;
    private String contactInfo;
    private String deviceType;
    private String deviceSystem;
    private String deviceModel;
    private String problemDescription;
    private String campus;
    private String repairLocation;
    private String appointmentTime;
    private String remarks;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp completeTime;
}
