package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCommentListDto {
    private Integer userId;
    private Integer commentId;
    private Integer postId;
    private String content;
    private Integer status;
    private Timestamp createTime;
    private Integer likeCount;
    private Integer replyCount;
}
