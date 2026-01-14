package gdufs.yixiu.dto.community.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPostDto {
    private Integer postId;
    private Integer userId;
    private String title;
    private String content;
    private Integer status;
}
