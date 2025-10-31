package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String status;
}
