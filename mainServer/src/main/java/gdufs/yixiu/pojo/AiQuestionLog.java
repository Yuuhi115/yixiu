package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiQuestionLog {
    private Integer questionId;
    private Integer userId;
    private String question;
    private Integer matchedKnowledgeId;
    private Double similarity;
    private Timestamp createTime;
}
