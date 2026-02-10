package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.annotation.VolunteerLoginToken;
import gdufs.yixiu.dto.ai.AiAskRequestDto;
import gdufs.yixiu.dto.ai.AiAskResponseDto;
import gdufs.yixiu.dto.ai.AiKnowledgeRequestDto;
import gdufs.yixiu.dto.ai.AiKnowledgeResponseDto;
import gdufs.yixiu.pojo.AiQuestionLog;
import gdufs.yixiu.service.AiService;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    @Autowired
    private JWTUtils jwtUtils;
    @Autowired
    private AiService aiService;
    @UserLoginToken
    @PostMapping("/ask")
    public Result ask(@RequestBody AiAskRequestDto aiAskRequestDto,
                      HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("用户{}提问了:{}", userId, aiAskRequestDto.getQuestion());
        AiQuestionLog aiQuestionLog = new AiQuestionLog();
        aiQuestionLog.setQuestion(aiAskRequestDto.getQuestion());
        aiQuestionLog.setUserId(userId);
        log.info("aiAskRequestDto:{}", aiAskRequestDto);
//        不是新对话就需要传会话id
        AiAskResponseDto response = aiService.ask(aiAskRequestDto);
        if (aiAskRequestDto.getConversationId() != null) {
            aiService.addChatMessage(aiAskRequestDto.getConversationId(), "user", aiAskRequestDto.getQuestion());
        }
        Map<String, String> map = new HashMap<>();

//        只有新对话才会有response.getHeadline() != null
        if (response.getHeadline() != null){
            aiAskRequestDto.setConversationId(aiService.addChatSession(userId, response.getHeadline()));
            int row = aiService.addChatMessage(aiAskRequestDto.getConversationId(), "user", aiAskRequestDto.getQuestion());
            if (row != 1) {
                return Result.fail("添加会话失败");
            }
        }
        aiService.addChatMessage(aiAskRequestDto.getConversationId(), "assistant", response.getAnswer());
        if (response.getCode() != 200) {
            int row = aiService.addQuestionLog(aiQuestionLog);
            if (row != 1) {
                return Result.fail("添加问题日志失败");
            }
            return Result.fail(response.getAnswer());
        }
        map.put("answer", response.getAnswer());
        map.put("type", response.getType());
        map.put("conversationId", aiAskRequestDto.getConversationId().toString());
        if (response.getSimilarity() != null)
            map.put("similarity", response.getSimilarity().toString());
        if (response.getHitKnowledgeId() != null)
            map.put("hit_knowledge_id", response.getHitKnowledgeId().toString());
        if (response.getHeadline() != null){
            map.put("headline", response.getHeadline());
        }
        if (response.getSimilarity() != null && response.getSimilarity() > 0.5){
            aiQuestionLog.setMatchedKnowledgeId(response.getHitKnowledgeId());
            aiQuestionLog.setSimilarity(response.getSimilarity());
        }
        int row = aiService.addQuestionLog(aiQuestionLog);
        if (row != 1) {
            return Result.fail("添加问题日志失败");
        }
        return Result.success(map);
    }
    @VolunteerLoginToken
    @PostMapping("/knowledge")
    public Result addKnowledge(@RequestBody AiKnowledgeRequestDto aiKnowledgeDto) {
        AiKnowledgeResponseDto result = aiService.addKnowledge(aiKnowledgeDto);
        if (result.getCode() != 200){
            return Result.fail(result.getCode(), result.getMsg());
        }
        log.info("添加知识成功, knowledge_id:{}", result.getData());
        return Result.success(result.getData());
    }
    @UserLoginToken
    @GetMapping("/allKnowledge")
    public Result getAllKnowledge() {
        return Result.success(aiService.queryAllKnowledge());
    }
    @UserLoginToken
    @GetMapping("/chatMessage")
    public Result getChatMessage(@RequestParam("conversationId") Integer conversationId,
                                 @RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                 @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                 HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("用户{}查询了会话{}的聊天记录", userId, conversationId);
        return Result.success(aiService.queryChatMessage(conversationId, pageNum, pageSize));
    }
    @UserLoginToken
    @GetMapping("/chatSession")
    public Result getChatSession(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                 @RequestParam(name = "pageSize", defaultValue = "20") Integer pageSize,
                                 HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        return Result.success(aiService.queryChatSession(userId, pageNum, pageSize));
    }
}
