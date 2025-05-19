package ftt_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BadgeProgressDTO {
    private String currentBadgeName;   // "뚜벅뚜벅 초심자"
    private String nextBadgeName;      // "목표를 위한 노력가!"
    private int currentCount;          // 사용자 완료 작업 수
    private int lowerThreshold;        // 현재 단계 최소 기준 (Badge_01 → 0)
    private int upperThreshold;        // 다음 단계 기준 (Badge_02 → 30)
    private double progressRate;       // 0.0 ~ 1.0
}
