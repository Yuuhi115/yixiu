package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostCommentReply {
    private Integer replyId;
    private Integer commentId;
    private Integer fromUserId;
    private Integer toUserId;
    private Integer parentReplyId;
    private String content;
    private Integer status; // 状态：0正常 1删除
    private Timestamp createTime;
}
