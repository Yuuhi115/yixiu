package gdufs.yixiu.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import gdufs.yixiu.dao.AiMapper;
import gdufs.yixiu.dao.UsersMapper;
import gdufs.yixiu.dto.ai.AiAskRequestDto;
import gdufs.yixiu.dto.ai.AiAskResponseDto;
import gdufs.yixiu.dto.ai.AiKnowledgeRequestDto;
import gdufs.yixiu.dto.ai.AiKnowledgeResponseDto;
import gdufs.yixiu.dto.community.vo.UserInfoVO;
import gdufs.yixiu.pojo.AiChatMessage;
import gdufs.yixiu.pojo.AiChatSession;
import gdufs.yixiu.pojo.AiKnowledge;
import gdufs.yixiu.pojo.AiQuestionLog;
import gdufs.yixiu.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Collections;
import java.util.List;


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
    public List<AiKnowledge> queryAllKnowledge() {
        return aiMapper.getAllKnowledge();
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
}
