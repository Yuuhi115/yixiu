package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostFavorite {
    private Integer favoriteId;
    private Integer postId;
    private Integer userId;
    private Timestamp createTime;
}
