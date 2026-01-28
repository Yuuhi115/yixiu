package gdufs.yixiu.dto.community.response;

import gdufs.yixiu.dto.community.vo.UserInfoVO;
import gdufs.yixiu.dto.community.vo.VolunteerDataVO;
import gdufs.yixiu.pojo.VolunteerInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto {
    private UserInfoVO userInfoVO;
    private CommunityStatisticDto communityStatisticDto;
    private VolunteerDataVO volunteerDataVO;
    private String role;
    private Boolean isFollow;
    private Integer visitedNum;
    private Date lastLoginTime;
}
