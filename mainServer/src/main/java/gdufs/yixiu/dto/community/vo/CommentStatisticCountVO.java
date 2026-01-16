package gdufs.yixiu.dto.community.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentStatisticCountVO {
    private Integer commentId;
    private Integer count;
}
