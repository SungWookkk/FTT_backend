package ftt_backend.config.batch.dto;

import lombok.*;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReminderMessage {
    private Long userId;
    private String phoneNumber;
    private Long taskId;
    private String taskTitle;
    private LocalDate dueDate;
}
