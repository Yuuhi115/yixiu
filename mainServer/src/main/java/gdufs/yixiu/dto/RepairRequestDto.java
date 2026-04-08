package gdufs.yixiu.dto;

import gdufs.yixiu.pojo.RepairAssignment;
import gdufs.yixiu.pojo.RepairEvaluate;
import gdufs.yixiu.pojo.RepairLog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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
    @NotEmpty(message = "联系方式不能为空")
    private String contactType;
    @NotEmpty(message = "联系号码不能为空")
    private String contactInfo;
    private Integer requestId;
    @NotEmpty(message = "设备类型不能为空")
    private String deviceType;
    @NotEmpty(message = "设备系统不能为空")
    private String deviceSystem;
    @NotEmpty(message = "设备型号不能为空")
    private String deviceModel;
    @NotEmpty(message = "问题描述不能为空")
    private String problemDescription;
    @NotEmpty(message = "校区不能为空")
    private String campus;
    @NotEmpty(message = "地点不能为空")
    private String repairLocation;
    @NotEmpty(message = "预约时间不能为空")
    private String appointmentTime;
//    问题类别id(软件/网络/硬件)
    private Integer skillId;
    private String remarks;
    private Integer status;
    private List<String> imgUrl;
    private List<RepairAssignmentDto> repairAssignment;
    private List<RepairLog> repairLog;
    private RepairEvaluate repairEvaluate;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp completeTime;
}
