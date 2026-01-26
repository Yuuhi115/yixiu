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
public class ResponsePostDto {
    private Integer postId;
    private Integer userId;
    private String username;
    private String avatar;
    private String userSignature;
    private String title;
    private String content;
    private List<String> imgUrls;
    private List<TagVO> tags;
    private Integer status;
    private Integer likeNum;
    private Integer commentNum;
    private Integer viewNum;
    private Integer favoriteNum;
    private Integer isLiked;
    private Integer isFavorite;
    private Timestamp createTime;
    private Timestamp updateTime;
}
