package ftt_backend.service;

import ftt_backend.model.Team;
import ftt_backend.model.TeamTask;
import ftt_backend.repository.TeamRepository;
import ftt_backend.repository.TeamTaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamTaskService {

    @Autowired
    private TeamTaskRepository teamTaskRepository;

    @Autowired
    private TeamRepository teamRepository;

    // 팀에 속한 모든 작업 조회
    public List<TeamTask> getTasksByTeamId(Long teamId) {
        // 팀 존재 여부 확인 (없으면 예외 발생)
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        return teamTaskRepository.findByTeamId(teamId);
    }

    // 팀 작업 생성
    @Transactional
    public TeamTask createTeamTask(Long teamId, TeamTask teamTask) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        teamTask.setTeam(team);
        return teamTaskRepository.save(teamTask);
    }
}
