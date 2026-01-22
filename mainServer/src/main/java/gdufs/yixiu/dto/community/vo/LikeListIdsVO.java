package gdufs.yixiu.dto.community.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeListIdsVO {
    private List<Integer> postIds;
    private List<Integer> commentIds;
    private List<Integer> replyIds;
}
