package gdufs.yixiu.dto;

import gdufs.yixiu.pojo.RepairAssignment;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RepairRequestDto {
    private Integer userId;
    private String username;
    private String realName;
    private String contactType;
    private String contactInfo;

    private Integer requestId;
    private String deviceType;
    private String deviceSystem;
    private String deviceModel;
    private String problemDescription;
    private String campus;
    private String repairLocation;
    private String appointmentTime;
    private String remarks;
    private Integer status;
    private List<String> imgUrl;
    private List<RepairAssignment> repairAssignment;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp completeTime;
}
