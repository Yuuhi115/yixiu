package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairRequest {
    private Integer requestId;
    private Integer userId;
    private String deviceType;
    private String problemDescription;
    private String requestTime;
}
