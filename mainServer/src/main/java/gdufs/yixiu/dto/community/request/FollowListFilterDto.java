package gdufs.yixiu.dto.community.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.core.annotation.MergedAnnotations;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowListFilterDto {
    private Integer userId;
    private Integer status;
    private String keyword;
}
