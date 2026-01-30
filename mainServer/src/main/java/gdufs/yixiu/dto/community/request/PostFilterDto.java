package gdufs.yixiu.dto.community.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostFilterDto {
    private Integer postUserId; // 帖子用户id
    private Integer status; // 0. 正常 1. 隐藏 2. 删除, 3. 置顶  默认0
    private Integer tagId;
    private String orderType; // 排序方式 1. update_time 2. like_num 3. comment_num 4. favorite_num
    private String order; // desc asc, 默认为desc
    private String keyword; // 搜索关键字
    private Integer postId;
}
