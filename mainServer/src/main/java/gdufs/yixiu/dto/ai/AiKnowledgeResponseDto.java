package gdufs.yixiu.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiKnowledgeResponseDto {
    private Integer code;
    private String msg;
    private String data;
}
