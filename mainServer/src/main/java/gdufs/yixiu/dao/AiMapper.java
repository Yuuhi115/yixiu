package gdufs.yixiu.dao;

import gdufs.yixiu.pojo.AiChatMessage;
import gdufs.yixiu.pojo.AiChatSession;
import gdufs.yixiu.pojo.AiKnowledge;
import gdufs.yixiu.pojo.AiQuestionLog;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AiMapper {
    List<AiKnowledge> getAllKnowledge();
    int addChatSession(AiChatSession aiChatSession);
    int addChatMessage(AiChatMessage aiChatMessage);
    int addQuestionLog(AiQuestionLog aiQuestionLog);
    List<AiChatSession> getChatSessionByUserId(Integer userId);
    List<AiChatMessage> getChatMessageByConversationId(Integer conversationId);
    AiChatSession getChatSessionById(Integer conversationId);
    List<AiKnowledge> getKnowledgeList(String sortBy, String sortOrder);
    int updateKnowledge(AiKnowledge aiKnowledge);
    int deleteKnowledge(Integer knowledgeId);
}
