/*
* 본인 task 전용
* */
package ftt_backend.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MyTaskStatsDto {
    private long totalTasks;          // 전체 과제 수
    private long activeSinceMonth;    // 한 달간 작성 과제 수
    private double completionRate;    // % 완료율
}
