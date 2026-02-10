package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiChatSession {
    private Integer conversationId;
    private Integer userId;
    private String headline;
    private Integer status;
    private Timestamp createTime;
}
