package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/*
* 帖子浏览表
* */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostView {
    private Integer viewId;
    private Integer postId;
    private Integer userId;
    private String ipAddress;
    private Timestamp createTime;
}
