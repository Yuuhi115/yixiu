package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostLike {
    private Integer likeId;
    private Integer postId;
    private Integer userId;
    private Timestamp createTime;
}
