package gdufs.yixiu.dto.community.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillCategoryResponseDto {
    private String categoryName;
    private Integer code;
    private String msg;
    // 类别id
    private Integer data;
}
