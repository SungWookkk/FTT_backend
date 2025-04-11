// TeamApplicationController.java
package ftt_backend.controller;

import ftt_backend.model.TeamApplication;
import ftt_backend.service.TeamApplicationService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-applications")
public class TeamApplicationController {

    @Autowired
    private TeamApplicationService teamApplicationService;

    /**
     * 팀 신청 생성
     * 헤더 X-User-Id와 함께, 신청할 팀의 teamId, reason, goal을 담은 JSON을 전송받습니다.
     */
    @PostMapping
    public ResponseEntity<TeamApplication> createApplication(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody ApplicationRequestDto requestDto
    ) {
        TeamApplication saved = teamApplicationService.createApplication(
                userId,
                requestDto.getTeamId(),
                requestDto.getReason(),
                requestDto.getGoal()
        );
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    /**
     * 특정 팀에 대한 신청 목록 조회
     */
    @GetMapping("/team/{teamId}")
    public ResponseEntity<List<TeamApplication>> getApplicationsByTeamId(@PathVariable Long teamId) {
        List<TeamApplication> apps = teamApplicationService.getApplicationsByTeamId(teamId);
        return ResponseEntity.ok(apps);
    }

    /**
     * 팀 신청 승인 처리
     */
    @PutMapping("/{applicationId}/approve")
    public ResponseEntity<TeamApplication> approveApplication(@PathVariable Long applicationId) {
        TeamApplication app = teamApplicationService.approveApplication(applicationId);
        return ResponseEntity.ok(app);
    }

    /**
     * 팀 신청 거절 처리
     */
    @PutMapping("/{applicationId}/reject")
    public ResponseEntity<TeamApplication> rejectApplication(@PathVariable Long applicationId) {
        TeamApplication app = teamApplicationService.rejectApplication(applicationId);
        return ResponseEntity.ok(app);
    }
}

/**
 * 팀 신청 생성 시 사용할 요청 DTO
 */
@Getter
@Setter
class ApplicationRequestDto {
    private Long teamId;
    private String reason;
    private String goal;

}
