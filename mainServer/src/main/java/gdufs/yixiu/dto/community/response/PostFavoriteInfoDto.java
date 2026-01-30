package gdufs.yixiu.dto.community.response;

import gdufs.yixiu.dto.community.vo.TagVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostFavoriteInfoDto {
    private Integer postId;
    private Integer postUserId;
    private String postUserAvatar;
    private String title;
    private List<TagVO> tags;
    private Timestamp createTime;
}
