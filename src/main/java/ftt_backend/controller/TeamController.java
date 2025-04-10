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

    // 팀 생성 요청 처리 (헤더에서 팀 생성자의 아이디(X-User-Id) 를 받음)
    @PostMapping("/create")
    public ResponseEntity<Team> createTeam(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody Team team
            // 요청 본문에 팀 객체만 보내도 되고 추가로 헤더에 팀 생성자의 아이디를 보냄
    ) {
        Team createdTeam = teamService.createTeam(team, userId);
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
    // 전체 팀 목록 조회 엔드포인트
    @GetMapping("/all")
    public List<Team> getAllTeams() {
        return teamService.findAllTeams();
    }
}
