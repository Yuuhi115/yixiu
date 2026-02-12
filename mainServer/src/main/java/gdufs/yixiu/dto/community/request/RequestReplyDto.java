package gdufs.yixiu.dto.community.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestReplyDto {
    private Integer replyId;
    private Integer fromUserId;
    private Integer toUserId;
    private Integer parentReplyId;
    private Integer commentId;
    private String content;
    private Integer status;
}
