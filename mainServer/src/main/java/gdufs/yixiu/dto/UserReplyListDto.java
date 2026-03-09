package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserReplyListDto {
    private Integer userId;
    private Integer replyId;
    private Integer commentId;
    private Integer postId;
    private Integer status;
    private String content;
    private Timestamp createTime;
    private Integer likeCount;
    private Integer replyCount;
}
