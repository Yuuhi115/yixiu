package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VolunteerModifyDto {
    private Integer userId;
    private String realName;
    private String studentNumber;
    private String majorClass;
    private String grade;
    private Integer status;
    private Integer contactType; // 联系方式 0: 手机号 1: 邮箱号 2: 微信号 3: QQ号
    private String contactNumber;
    private String role; //管理员 or 志愿者
}
