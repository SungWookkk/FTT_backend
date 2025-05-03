package ftt_backend.config.batch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Getter
@Setter
public class ReminderMessage {
    private Long userId;
    private String phoneNumber;
    private Long taskId;
    private String taskTitle;
    private LocalDate dueDate;
}
