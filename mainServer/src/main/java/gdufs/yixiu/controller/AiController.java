package gdufs.yixiu.controller;

import gdufs.yixiu.dto.ai.AiAskRequestDto;
import gdufs.yixiu.dto.ai.AiAskResponseDto;
import gdufs.yixiu.dto.ai.AiKnowledgeRequestDto;
import gdufs.yixiu.dto.ai.AiKnowledgeResponseDto;
import gdufs.yixiu.service.AiService;
import gdufs.yixiu.util.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
public class AiController {
    @Autowired
    private AiService aiService;
    @PostMapping("/ask")
    public Result ask(@RequestBody AiAskRequestDto aiAskRequestDto) {
        AiAskResponseDto response = aiService.ask(aiAskRequestDto.getQuestion());
        Map<String, String> map = new HashMap<>();
        if (response.getCode() != 200) {
            return Result.fail(response.getAnswer());
        }
        map.put("answer", response.getAnswer());
        map.put("type", response.getType());
        if (response.getSimilarity() != null)
            map.put("similarity", response.getSimilarity().toString());
        return Result.success(map);
    }
    @PostMapping("/knowledge")
    public Result addKnowledge(@RequestBody AiKnowledgeRequestDto aiKnowledgeDto) {
        AiKnowledgeResponseDto result = aiService.addKnowledge(aiKnowledgeDto);
        if (result.getCode() != 200){
            return Result.fail(result.getCode(), result.getMsg());
        }
        log.info("添加知识成功, knowledge_id:{}", result.getData());
        return Result.success(result.getData());
    }
}
