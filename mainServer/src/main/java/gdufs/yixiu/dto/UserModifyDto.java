package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/* 普通用户更新基本信息 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserModifyDto {
    private Integer userId;
    private String username;
    private String realName;
    private String role;
    private String userSignature;
}
