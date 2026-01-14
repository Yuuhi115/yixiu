package gdufs.yixiu.dto.community.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostFilterDto {
    private Integer status;
    private Integer tagId;
}
