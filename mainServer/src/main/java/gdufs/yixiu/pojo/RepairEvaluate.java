package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairEvaluate {
    private Integer evaluateId;
    private Integer userId;
    private Integer requestId;
    private String content;
    private Integer score;
    private Timestamp createTime;
}
