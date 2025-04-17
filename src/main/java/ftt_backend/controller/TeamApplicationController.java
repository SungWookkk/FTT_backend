package ftt_backend.controller;

import ftt_backend.model.TeamApplication;
import ftt_backend.service.TeamApplicationService;
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

    // 팀 신청 생성 API
    @PostMapping
    public ResponseEntity<?> createApplication(@RequestBody TeamApplication teamApplication,
                                               @RequestHeader("X-User-Id") Long userId) {
        // 클라이언트에서 teamApplication 객체에 팀 정보가 포함되어 있다고 가정하고 팀 ID 추출
        if (teamApplication.getTeam() == null || teamApplication.getTeam().getId() == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        Long teamId = teamApplication.getTeam().getId();
        String reason = teamApplication.getReason();
        String goal = teamApplication.getGoal();

        try {
            // 서비스 메서드의 시그니처는 (Long teamId, Long applicantId, String reason, String goal)
            TeamApplication createdApplication = teamApplicationService.createTeamApplication(teamId, userId, reason, goal);
            return new ResponseEntity<>(createdApplication, HttpStatus.CREATED);
        } catch (RuntimeException ex) {
            // 예를 들어, 이미 팀에 가입된 경우 "이미 가입되어 있는 팀입니다!" 라는 메시지가 반환
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }

    // 특정 팀의 신청 목록 조회 API
    @GetMapping("/{teamId}")
    public ResponseEntity<List<TeamApplication>> getApplications(@PathVariable Long teamId) {
        List<TeamApplication> apps = teamApplicationService.getApplicationsByTeam(teamId);
        return ResponseEntity.ok(apps);
    }

    // 신청 승인 API
    @PatchMapping("/{applicationId}/approve")
    public ResponseEntity<TeamApplication> approveApplication(@PathVariable Long applicationId) {
        TeamApplication updated = teamApplicationService.approveApplication(applicationId);
        return ResponseEntity.ok(updated);
    }

    // 신청 반려 API
    @PatchMapping("/{applicationId}/reject")
    public ResponseEntity<TeamApplication> rejectApplication(@PathVariable Long applicationId) {
        TeamApplication updated = teamApplicationService.rejectApplication(applicationId);
        return ResponseEntity.ok(updated);
    }
    /** 멤버 추방 */
    @DeleteMapping("/{teamId}/members/{userId}")
    public ResponseEntity<?> kickMember(
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        teamApplicationService.removeMember(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    /** 멤버 등급 상승 */
    @PatchMapping("/{teamId}/members/{userId}/promote")
    public ResponseEntity<?> promoteMember(
            @PathVariable Long teamId,
            @PathVariable Long userId) {
        teamApplicationService.promoteMember(teamId, userId);
        return ResponseEntity.ok().build();
    }

    /** 팀 탈퇴 (현재 사용자) */
    @DeleteMapping("/{teamId}/members/me")
    public ResponseEntity<?> leaveTeam(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long teamId) {
        teamApplicationService.leaveTeam(teamId, userId);
        return ResponseEntity.noContent().build();
    }

    /** 팀 해체 */
    @DeleteMapping("/{teamId}")
    public ResponseEntity<?> disbandTeam(
            @PathVariable Long teamId) {
        teamApplicationService.disbandTeam(teamId);
        return ResponseEntity.noContent().build();
    }
}
