package gdufs.yixiu.service;

import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dto.ai.*;
import gdufs.yixiu.dto.community.request.skill.SkillScoreCalculateDto;
import gdufs.yixiu.dto.community.response.skill.SkillCategoryResponseDto;
import gdufs.yixiu.pojo.AiChatMessage;
import gdufs.yixiu.pojo.AiChatSession;
import gdufs.yixiu.pojo.AiKnowledge;
import gdufs.yixiu.pojo.AiQuestionLog;
import gdufs.yixiu.util.Result;

import java.util.List;

public interface AiService {
    AiAskResponseDto ask(AiAskRequestDto aiAskRequestDto);
    AiKnowledgeResponseDto addKnowledge(AiKnowledgeRequestDto aiKnowledgeDto);
    AiKnowledgeResponseDto rebuildKnowledge();
    List<AiKnowledge> queryAllKnowledge();
    PageInfo<AiKnowledge> queryKnowledgePage(Integer pageNum, Integer pageSize, String sortBy, String sortOrder);
    int addChatSession(Integer userId, String headline);
    int addChatMessage(Integer conversationId, String role, String content);
    int addQuestionLog(AiQuestionLog aiQuestionLog);
    PageInfo<AiChatMessage> queryChatMessage(Integer conversationId, Integer pageNum, Integer pageSize);
    PageInfo<AiChatSession> queryChatSession(Integer userId, Integer pageNum, Integer pageSize);
    int modifyKnowledge(KnowledgeModifyDto knowledgeModifyDto);
    int deleteKnowledge(Integer knowledgeId);
    SkillCategoryResponseDto getTaskSkillId(String problemDescription);
    Result updateVolunteerSkillScore(SkillScoreCalculateDto skillScoreCalculateDto);
}
