package gdufs.yixiu.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KnowledgeModifyDto {
    private Integer knowledgeId;
    private String problem;
    private String solution;
    private Integer status;
}
