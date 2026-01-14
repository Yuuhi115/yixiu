package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostTag {
    private Integer tagId;
    private String tagName;
    private Integer status; // 状态：0正常 1停用
    private Timestamp createTime;
}
