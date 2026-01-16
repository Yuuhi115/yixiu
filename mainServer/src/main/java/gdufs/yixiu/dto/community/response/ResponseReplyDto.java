package gdufs.yixiu.dto.community.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseReplyDto {
    private Integer replyId;
    private Integer commentId;
    private Integer fromUserId;
    private String fromUserName;
    private String fromUserAvatar;
    private Integer toUserId;
    private String toUserName;
    private Integer parentReplyId;
    private String content;
    private Integer likeNum;
    private Integer isLike;
    private Timestamp createTime;
}
