package gdufs.yixiu.dto.community.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestCommentDto {
    private Integer userId;
    private Integer postId;
    private String content;
    private Integer status;
}
