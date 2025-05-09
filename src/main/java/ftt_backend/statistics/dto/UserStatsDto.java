/*
* 전체 사용자 전용
* */
package ftt_backend.statistics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatsDto {
    private long totalTasks;     // 전체 과제 수
    private long activeTasks;    // 한 달간 작성된 과제 수
    private double completionRate; // 0~100 퍼센트
}
