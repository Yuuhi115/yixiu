package gdufs.yixiu.dto.community.request;

import gdufs.yixiu.pojo.PostTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequestPostDto {
    private Integer postId;
    private Integer userId;
    private String title;
    private String content;
    private Integer status;
    private List<Integer> tagIdList;
}
