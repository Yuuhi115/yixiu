package gdufs.yixiu.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiKnowledgeRequestDto {
    @NotNull(message = "problem不能为空")
    private String problem;
    @NotNull(message = "problem不能为空")
    private String solution;
    @NotNull(message = "sourceType不能为空")
    private String sourceType;
    @Pattern(
            regexp = "^(log_\\d+|post_\\d+)$",
            message = "sourceId 格式错误，应为 log_+logId 或 post_+postId"
    )
    private String sourceId; // log_id
}
