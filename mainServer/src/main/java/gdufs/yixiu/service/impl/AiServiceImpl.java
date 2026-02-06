package gdufs.yixiu.service.impl;

import gdufs.yixiu.dto.ai.AiAskRequestDto;
import gdufs.yixiu.dto.ai.AiAskResponseDto;
import gdufs.yixiu.dto.ai.AiKnowledgeRequestDto;
import gdufs.yixiu.dto.ai.AiKnowledgeResponseDto;
import gdufs.yixiu.service.AiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.Objects;


@Service
@Slf4j
public class AiServiceImpl implements AiService {

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
    public AiAskResponseDto ask(String question) {
        String url = baseUrl + askPath;

        AiAskRequestDto request = new AiAskRequestDto();
        request.setQuestion(question);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<AiAskRequestDto> entity =
                new HttpEntity<>(request, headers);

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
}
