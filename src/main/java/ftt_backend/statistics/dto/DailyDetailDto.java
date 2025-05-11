package ftt_backend.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DailyDetailDto {
    private long total;      // 해당 일자에 생성된 전체 Task 개수
    private long completed;  // 완료된 Task 개수
    private long failed;     // 실패한 Task 개수
}
