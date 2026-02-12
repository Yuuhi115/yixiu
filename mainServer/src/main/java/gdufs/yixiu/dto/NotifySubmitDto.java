package gdufs.yixiu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotifySubmitDto {
    private Integer taskId;
    private String remark;
    private Integer senderId;
    private Integer receiverId;
    private String title;
    private String content;
    private String type;
    private String link;

    private Integer postId;
    private Integer commentId;
    private Integer replyId;
    private Integer parentReplyId;
    private String commentContent;
    private String replyContent;
}
