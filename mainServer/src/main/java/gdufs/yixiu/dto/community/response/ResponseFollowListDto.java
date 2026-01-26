package gdufs.yixiu.dto.community.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseFollowListDto {
    private Integer followId;
    private Integer followUserId;
    private String followUsername;
    private String followUserAvatar;
    private String followUserSignature;
    private Integer status;
    private Integer isUpdate;
    private Timestamp createTime;
    private Timestamp updateTime;
}
