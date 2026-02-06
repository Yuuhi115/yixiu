package gdufs.yixiu.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiKnowledgeRequestDto {
    private String problem;
    private String solution;
    @JsonProperty("source_type")
    private String sourceType;
    @JsonProperty("source_id")
    private String sourceId; // log_id
}
