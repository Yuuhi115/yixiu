package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RepairLogImg {
    private Integer imgId;
    private Integer logId;
    private String imgUrl;
    private Timestamp uploadTime;
}
