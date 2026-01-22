package gdufs.yixiu.dto.community.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommunityStatisticDto {
    private Integer userId;
    private Integer postNum;
    private Integer followNum;
    private Integer fansNum;
    private Integer getLikeNum;
}
