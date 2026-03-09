package gdufs.yixiu.dto.filter;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserCommentListFilter {
    @NotNull
    private Integer userId;
    private Integer status;
    private Integer sortBy;
    private Integer sortOrder;
}
