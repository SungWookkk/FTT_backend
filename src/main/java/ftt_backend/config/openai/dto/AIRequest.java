package ftt_backend.config.openai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 프론트에서 넘어오는 AI 요청 바디
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIRequest {
    private String prompt;
}
