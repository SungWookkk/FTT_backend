package ftt_backend.controller;

import ftt_backend.model.TeamTask;
import ftt_backend.service.TeamTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team/{teamId}/tasks")
public class TeamTaskController {

    @Autowired
    private TeamTaskService teamTaskService;

    // 팀의 모든 작업 조회
    @GetMapping
    public ResponseEntity<List<TeamTask>> getTeamTasks(@PathVariable Long teamId) {
        List<TeamTask> tasks = teamTaskService.getTasksByTeamId(teamId);
        return ResponseEntity.ok(tasks);
    }

    // 팀 작업 생성
    @PostMapping
    public ResponseEntity<TeamTask> createTeamTask(@PathVariable Long teamId, @RequestBody TeamTask teamTask) {
        TeamTask createdTask = teamTaskService.createTeamTask(teamId, teamTask);
        return new ResponseEntity<>(createdTask, HttpStatus.CREATED);
    }
}
