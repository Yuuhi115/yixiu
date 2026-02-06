package gdufs.yixiu.dto.ai;

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
}
