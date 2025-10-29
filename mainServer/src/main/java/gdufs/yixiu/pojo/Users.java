package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String openid;
    private String unionid;
    private String wxNickname;
    private String wxAvatar;
    private String role;
    private String status;
    private String createTime;
    private String updateTime;
    private String lastLogin;
}
