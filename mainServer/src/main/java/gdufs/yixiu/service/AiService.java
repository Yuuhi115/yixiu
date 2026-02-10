package gdufs.yixiu.service;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.ai.AiAskRequestDto;
import gdufs.yixiu.dto.ai.AiAskResponseDto;
import gdufs.yixiu.dto.ai.AiKnowledgeRequestDto;
import gdufs.yixiu.dto.ai.AiKnowledgeResponseDto;
import gdufs.yixiu.pojo.AiChatMessage;
import gdufs.yixiu.pojo.AiChatSession;
import gdufs.yixiu.pojo.AiKnowledge;
import gdufs.yixiu.pojo.AiQuestionLog;

import java.util.List;

public interface AiService {
    AiAskResponseDto ask(AiAskRequestDto aiAskRequestDto);
    AiKnowledgeResponseDto addKnowledge(AiKnowledgeRequestDto aiKnowledgeDto);
    List<AiKnowledge> queryAllKnowledge();
    int addChatSession(Integer userId, String headline);
    int addChatMessage(Integer conversationId, String role, String content);
    int addQuestionLog(AiQuestionLog aiQuestionLog);
    PageInfo<AiChatMessage> queryChatMessage(Integer conversationId, Integer pageNum, Integer pageSize);
    PageInfo<AiChatSession> queryChatSession(Integer userId, Integer pageNum, Integer pageSize);
}
