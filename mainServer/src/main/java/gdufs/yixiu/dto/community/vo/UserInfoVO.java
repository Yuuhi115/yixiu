package gdufs.yixiu.dto.community.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {
    private Integer userId;
    private String username;
    private String avatar;
    private String userSignature;
}
