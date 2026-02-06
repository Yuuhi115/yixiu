package gdufs.yixiu.service;

import gdufs.yixiu.dto.ai.AiAskResponseDto;
import gdufs.yixiu.dto.ai.AiKnowledgeRequestDto;
import gdufs.yixiu.dto.ai.AiKnowledgeResponseDto;

public interface AiService {
    AiAskResponseDto ask(String question);
    AiKnowledgeResponseDto addKnowledge(AiKnowledgeRequestDto aiKnowledgeDto);
}
