package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostComment {
    private Integer commentId;
    private Integer postId;
    private Integer userId;
    private String content;
    private Integer status; //状态：0正常 1删除
    private Timestamp createTime;
}
