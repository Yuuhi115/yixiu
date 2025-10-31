package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairRequestImg {
    private Integer imgId;
    private Integer requestId;
    private String imgUrl;
    private Timestamp createTime;
}
