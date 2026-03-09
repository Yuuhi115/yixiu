package gdufs.yixiu.dto;

import gdufs.yixiu.dto.community.response.CommunityStatisticDto;
import gdufs.yixiu.pojo.Users;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListDto {
    private Integer userId;
    private String username;
    private String avatar;
    private String realName;
    private String email;
    private String userSignature;
    private Integer status;
    private String role;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Timestamp lastLogin;
    private Integer getLikeCount;
    private Integer postCount;
    private Integer commentCount;
    private Integer replyCount;
}
