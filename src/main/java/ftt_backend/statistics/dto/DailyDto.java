package ftt_backend.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyDto {
    private String label;  // "1", "2", ..., "31"
    private long value;    // 해당 일자 시작된 작업 수
}
