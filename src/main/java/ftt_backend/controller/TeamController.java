package ftt_backend.controller;

import ftt_backend.model.Team;
import ftt_backend.service.TeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired
    private TeamService teamService;

    // 팀 생성 요청 처리
    @PostMapping("/create")
    public ResponseEntity<Team> createTeam(@RequestBody Team team) {
        Team createdTeam = teamService.createTeam(team);
        return new ResponseEntity<>(createdTeam, HttpStatus.CREATED);
    }
    // 사용자(userId)가 속한 팀 목록 조회
    @GetMapping("/user/{userId}")
    public List<Team> getTeamsByUserId(@PathVariable Long userId) {
        return teamService.findTeamsByUserId(userId);
    }
    // 팀 상세 조회
    @GetMapping("/{teamId}")
    public ResponseEntity<Team> getTeamById(@PathVariable Long teamId) {
        Team team = teamService.findTeamById(teamId);
        return ResponseEntity.ok(team);
    }
}
