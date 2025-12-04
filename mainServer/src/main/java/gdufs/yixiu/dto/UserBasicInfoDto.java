package gdufs.yixiu.dto;

import gdufs.yixiu.pojo.VolunteerInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserBasicInfoDto {
    private Integer userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String role;
    private String status;
    private Date lastLogin;
    private VolunteerInfo volunteerInfo;
}

