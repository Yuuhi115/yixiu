package gdufs.yixiu.dto.filter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserListFilter {
    private Integer status;
    private Integer sortBy;
    private Integer sortOrder;
    private String role;
    private String searchName;
}
