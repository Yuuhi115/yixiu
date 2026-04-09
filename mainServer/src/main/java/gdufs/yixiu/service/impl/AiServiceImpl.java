package gdufs.yixiu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dao.AiMapper;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dto.ai.*;
import gdufs.yixiu.dto.community.request.skill.SkillScoreCalculateDto;
import gdufs.yixiu.dto.community.response.skill.SkillCategoryResponseDto;
import gdufs.yixiu.pojo.AiChatMessage;
import gdufs.yixiu.pojo.AiChatSession;
import gdufs.yixiu.pojo.AiKnowledge;
import gdufs.yixiu.pojo.AiQuestionLog;
import gdufs.yixiu.service.AiService;
import gdufs.yixiu.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class AiServiceImpl implements AiService {
    @Autowired
    private AiMapper aiMapper;
    @Autowired
    private UsersMapper usersMapper;

    private String askPath;
    @Value("${ai.service.ask-path}")
    private void setAskPath(String askPath) {
        this.askPath = askPath;
    }
    private String addKnowledgePath;
    @Value("${ai.service.add-knowledge-path}")
    private void setAddKnowledgePath(String addKnowledgePath) {
        this.addKnowledgePath = addKnowledgePath;
    }
    private String baseUrl;
    @Value("${ai.service.base-url}")
    private void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    private final RestTemplate restTemplate;
    public AiServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public SkillCategoryResponseDto getTaskSkillId(String problemDescription) {
        String url = baseUrl + askPath + "/getTaskSkillId";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, String> data = new HashMap<>();
        data.put("problemDescription", problemDescription);
        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(data, headers);
        try {
            ResponseEntity<SkillCategoryResponseDto> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            SkillCategoryResponseDto.class
                    );
            log.info("获取问题描述类别id服务调用成功");
            return response.getBody();
        } catch (Exception e) {
            log.error("调用 AI Flask 服务失败: {}", url, e);
            SkillCategoryResponseDto fallback = new SkillCategoryResponseDto();
            fallback.setCode(500);
            fallback.setMsg("问题描述分类服务不可用，请提交人工维修申请。");
            fallback.setData(null);
            return fallback;
        }
    }

    @Override
    public Result updateVolunteerSkillScore(SkillScoreCalculateDto skillScoreCalculateDto) {
        String url = baseUrl + askPath + "/updateAndGetExperts";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Result skillScoreUpdateRequest = Result.success(skillScoreCalculateDto);
        HttpEntity<Result> entity =
                new HttpEntity<>(skillScoreUpdateRequest, headers);
        try {
            ResponseEntity<Result> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            Result.class
                    );
            log.info("更新技能分数服务调用成功");
            return response.getBody();
        }catch (Exception e){
            log.error("调用 更新技能分数 Flask 服务失败: {}", url, e);
            return Result.fail("更新技能分数服务不可用，请联系系统管理员。");
        }
    }

    @Override
    public AiAskResponseDto ask(AiAskRequestDto aiAskRequestDto) {
        String url = baseUrl + askPath;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AiAskRequestDto> entity =
                new HttpEntity<>(aiAskRequestDto, headers);
        try {
            ResponseEntity<AiAskResponseDto> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            AiAskResponseDto.class
                    );
            log.info("AI 服务调用成功");
            return response.getBody();
        } catch (Exception e) {
            log.error("调用 AI Flask 服务失败: {}", url, e);
            AiAskResponseDto fallback = new AiAskResponseDto();
            fallback.setType("MANUAL");
            fallback.setAnswer("AI 服务暂时不可用，请提交人工维修申请。");
            fallback.setCode(500);
            return fallback;
        }
    }

    @Override
    public AiKnowledgeResponseDto addKnowledge(AiKnowledgeRequestDto aiKnowledgeDto) {
        String url = baseUrl + addKnowledgePath;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AiKnowledgeRequestDto> entity =
                new HttpEntity<>(aiKnowledgeDto, headers);

        try {
            ResponseEntity<AiKnowledgeResponseDto> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            AiKnowledgeResponseDto.class
                    );

            return response.getBody();

        } catch (Exception e) {
            log.error("AI 知识入库失败", e);
            AiKnowledgeResponseDto fail = new AiKnowledgeResponseDto();
            fail.setCode(500);
            fail.setMsg("AI 知识服务不可用，入库失败");
            return fail;
        }
    }

    @Override
    public AiKnowledgeResponseDto rebuildKnowledge() {
        String url = baseUrl + addKnowledgePath + "/rebuild";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AiKnowledgeResponseDto> entity =
                new HttpEntity<>(headers);

        try {
            ResponseEntity<AiKnowledgeResponseDto> response =
                    restTemplate.postForEntity(
                            url,
                            entity,
                            AiKnowledgeResponseDto.class
                    );
            return response.getBody();
        } catch (Exception e) {
            log.error("AI 知识库重建失败", e);
            AiKnowledgeResponseDto fail = new AiKnowledgeResponseDto();
            fail.setCode(500);
            fail.setMsg("AI 知识服务不可用，重建失败");
            return fail;
        }
    }

    @Override
    public List<AiKnowledge> queryAllKnowledge() {
        return aiMapper.getAllKnowledge();
    }

    @Override
    public PageInfo<AiKnowledge> queryKnowledgePage(Integer pageNum, Integer pageSize,
                                                    String sortBy, String sortOrder) {
        PageHelper.startPage(pageNum, pageSize);
        List<AiKnowledge> aiKnowledges = aiMapper.getKnowledgeList(sortBy, sortOrder);
        return new PageInfo<>(aiKnowledges);
    }

    @Override
    public int addChatSession(Integer userId, String headline) {
        AiChatSession aiChatSession = new AiChatSession();
        aiChatSession.setUserId(userId);
        aiChatSession.setHeadline(headline);
        int row = aiMapper.addChatSession(aiChatSession);
        if (row == 1){
            return aiChatSession.getConversationId();
        }
        return 0;
    }

    @Override
    public int addChatMessage(Integer conversationId, String role, String content) {
        AiChatMessage aiChatMessage = new AiChatMessage();
        aiChatMessage.setConversationId(conversationId);
        aiChatMessage.setRole(role);
        aiChatMessage.setContent(content);
        return aiMapper.addChatMessage(aiChatMessage);
    }

    @Override
    public int addQuestionLog(AiQuestionLog aiQuestionLog) {
        return aiMapper.addQuestionLog(aiQuestionLog);
    }

    @Override
    public PageInfo<AiChatMessage> queryChatMessage(Integer conversationId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AiChatMessage> aiChatMessages = aiMapper.getChatMessageByConversationId(conversationId);
        PageInfo<AiChatMessage> pageInfo = new PageInfo<>(aiChatMessages);
        Collections.reverse(pageInfo.getList());
        return pageInfo;
    }

    @Override
    public PageInfo<AiChatSession> queryChatSession(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<AiChatSession> aiChatSessions = aiMapper.getChatSessionByUserId(userId);
        return new PageInfo<>(aiChatSessions);
    }

    @Override
    public int modifyKnowledge(KnowledgeModifyDto knowledgeModifyDto) {
        AiKnowledge aiKnowledge = new AiKnowledge();
        aiKnowledge.setKnowledgeId(knowledgeModifyDto.getKnowledgeId());
        if (knowledgeModifyDto.getProblem() != null) {
            aiKnowledge.setProblem(knowledgeModifyDto.getProblem());
        }
        if (knowledgeModifyDto.getSolution() != null) {
            aiKnowledge.setSolution(knowledgeModifyDto.getSolution());
        }
        if (knowledgeModifyDto.getStatus() != null) {
            aiKnowledge.setStatus(knowledgeModifyDto.getStatus());
        }
        return aiMapper.updateKnowledge(aiKnowledge);
    }

    @Override
    public int deleteKnowledge(Integer knowledgeId) {
        return aiMapper.deleteKnowledge(knowledgeId);
    }
}
