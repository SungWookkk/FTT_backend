package ftt_backend.config.openai.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TaskResponseDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate dueDate;
    private LocalDate createdAt;
    private String priority;
    private String status;
    private String assignee;
    private String memo;
}
