package gdufs.yixiu.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiAskResponseDto {
    private Integer code;
    // AI / MANUAL
    private String type;
    private String answer;
    private Double similarity;
    @JsonProperty("hit_knowledge_id")
    private Integer hitKnowledgeId;
    private String headline;
}
