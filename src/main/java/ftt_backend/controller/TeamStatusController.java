package ftt_backend.controller;

import ftt_backend.model.Team;
import ftt_backend.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 팀 상태 메시지 관련 API (GET, PUT)
 * 여기서는 Team 엔티티의 announcement 필드를 팀 상태 메시지로 활용
 */
@RestController
@RequestMapping("/api/teams/{teamId}/statusMessage")
public class TeamStatusController {

    @Autowired
    private TeamService teamService;

    // 팀 상태 메시지 조회: GET /api/teams/{teamId}/statusMessage
    @GetMapping
    public ResponseEntity<String> getStatusMessage(@PathVariable Long teamId) {
        Team team = teamService.findTeamById(teamId);
        String statusMessage = team.getAnnouncement(); // announcement 필드를 상태 메시지로 사용
        if (statusMessage == null || statusMessage.trim().isEmpty()) {
            statusMessage = "팀 상태메시지가 없습니다. 팀 작업을 생성하여 팀 작업에 기여해봐요!";
        }
        return ResponseEntity.ok(statusMessage);
    }

    // 팀 상태 메시지 수정: PUT /api/teams/{teamId}/statusMessage
    @PutMapping
    public ResponseEntity<Team> updateStatusMessage(@PathVariable Long teamId, @RequestBody String statusMessage) {
        Team team = teamService.findTeamById(teamId);
        team.setAnnouncement(statusMessage);
        Team updatedTeam = teamService.updateTeam(team);
        return ResponseEntity.ok(updatedTeam);
    }
}
