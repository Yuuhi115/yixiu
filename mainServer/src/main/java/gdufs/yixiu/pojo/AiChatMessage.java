package gdufs.yixiu.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiChatMessage {
    private Integer messageId;
    private Integer conversationId;
    private String role;
    private String content;
    private Timestamp createTime;
}
