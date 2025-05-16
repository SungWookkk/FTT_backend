package ftt_backend.config.openai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI가 생성한 JSON 스펙을 매핑할 DTO
 */
@Data
@NoArgsConstructor
public class TaskSpecDTO {
    private String title;
    private String description;
    private String priority;

    @JsonProperty("startDate")
    private String startDate;

    @JsonProperty("dueDate")
    private String dueDate;

    private String assignee;
    private String memo;
}
