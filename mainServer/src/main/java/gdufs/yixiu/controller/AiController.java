package gdufs.yixiu.controller;

import gdufs.yixiu.annotation.AdminLoginToken;
import gdufs.yixiu.annotation.UserLoginToken;
import gdufs.yixiu.annotation.VolunteerLoginToken;
import gdufs.yixiu.dto.ai.*;
import gdufs.yixiu.dto.community.vo.LikeListIdsVO;
import gdufs.yixiu.pojo.AiKnowledge;
import gdufs.yixiu.pojo.AiQuestionLog;
import gdufs.yixiu.service.AiService;
import gdufs.yixiu.util.JWTUtils;
import gdufs.yixiu.util.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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
            map.put("hitKnowledgeId", response.getHitKnowledgeId().toString());
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
    public Result addKnowledge(@RequestBody AiKnowledgeRequestDto aiKnowledgeDto,
                               HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        AiKnowledgeResponseDto result = aiService.addKnowledge(aiKnowledgeDto);
        if (result.getCode() != 200){
            return Result.fail(result.getCode(), result.getMsg());
        }
        log.info("志愿者(user_id:{})添加知识(knowledge_id:{})成功", userId, result.getData());
        return Result.success(result.getData());
    }
    @VolunteerLoginToken
    @PostMapping("/rebuildKnowledge")
    public Result rebuildKnowledge() {
        AiKnowledgeResponseDto result = aiService.rebuildKnowledge();
        if (result.getCode() != 200){
            return Result.fail(result.getCode(), result.getMsg());
        }
        log.info("重建知识库成功");
        return Result.success(null);
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
    @UserLoginToken
    @PostMapping("/addChatMessage")
    public Result addChatMessage(@RequestBody AiAskRequestDto aiAskRequestDto){
        if (!aiAskRequestDto.getRole().equals("user") && !aiAskRequestDto.getRole().equals("assistant")){
            return Result.fail("role参数错误");
        }
        if (aiAskRequestDto.getConversationId() == null){
            return Result.fail("conversationId参数错误");
        }
        if (aiAskRequestDto.getQuestion() == null){
            return Result.fail("question参数错误");
        }
        int row = aiService.addChatMessage(aiAskRequestDto.getConversationId(), aiAskRequestDto.getRole(), aiAskRequestDto.getQuestion());
        return row == 1 ? Result.success(null) : Result.fail("添加聊天记录失败");
    }
    @UserLoginToken
    @PostMapping("/addChatSession")
    public Result addChatSession(String headline, HttpServletRequest request){
        if (headline == null){
            return Result.fail("headline参数错误");
        }
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        Map<String, Object> map = new HashMap<>();
        map.put("conversationId", aiService.addChatSession(userId, headline));
        return Result.success(map);
    }
    @VolunteerLoginToken
    @GetMapping("/getKnowledgePage")
    public Result getKnowledgePage(@RequestParam(name = "pageNum", defaultValue = "1") Integer pageNum,
                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                   @RequestParam(name = "sortBy", defaultValue = "createTime") String sortBy,
                                   @RequestParam(name = "sortOrder", defaultValue = "DESC") String sortOrder){
        if(!sortOrder.equals("ASC") && !sortOrder.equals("DESC")){
            return Result.fail("sortOrder参数错误");
        }
        if (!sortBy.equals("createTime") && !sortBy.equals("hitCount")){
            return Result.fail("sortBy参数错误");
        }
        return Result.success(aiService.queryKnowledgePage(pageNum, pageSize, sortBy, sortOrder));
    }
    @AdminLoginToken
    @PutMapping("/updateKnowledge")
    public Result updateKnowledge(@RequestBody KnowledgeModifyDto knowledgeModifyDto,
                                  HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        if (knowledgeModifyDto.getKnowledgeId() == null){
            return Result.fail("knowledgeId参数错误");
        }
        log.info("管理员(user_id:{})修改知识(knowledge_id:{})", userId, knowledgeModifyDto.getKnowledgeId());
        int row = aiService.modifyKnowledge(knowledgeModifyDto);
        return row == 1 ? Result.success(null) : Result.fail("修改知识失败");
    }
    @AdminLoginToken
    @DeleteMapping("/deleteKnowledge")
    public Result deleteKnowledge(@RequestParam("knowledgeId") Integer knowledgeId,
                                  HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        Integer userId = jwtUtils.getInfoFromToken(token).getId();
        log.info("管理员(user_id:{})删除知识(knowledge_id:{})", userId, knowledgeId);
        int row = aiService.deleteKnowledge(knowledgeId);
        return row == 1 ? Result.success(null) : Result.fail("删除知识失败");
    }
}
