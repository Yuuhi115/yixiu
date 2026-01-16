package gdufs.yixiu.dto.community.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCommentDto {
    private Integer postId;
    private Integer commentId;
    private Integer userId;
    private String username;
    private String avatar;
    private String content;
    private Integer likeNum;
    private Integer replyNum;
    private Integer isLike;
    private Timestamp createTime;
    private List<ResponseReplyDto> replyList;
}
