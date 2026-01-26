package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Users {
    private Integer userId;
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String userSignature;
    private String openid;
    private String unionid;
    private String wxNickname;
    private String wxAvatar;
    private String role;
    private String status;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Date lastLogin;
}
