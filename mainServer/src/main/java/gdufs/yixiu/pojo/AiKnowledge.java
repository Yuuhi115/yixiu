package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiKnowledge {
    private Integer knowledgeId;
    private Integer sourceType;
    private String sourceId;
    private String problem;
    private String solution;
    private Integer status;
    private Timestamp createTime;
    private Timestamp updateTime;
}
