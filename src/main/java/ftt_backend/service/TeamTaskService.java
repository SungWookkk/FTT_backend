package ftt_backend.service;

import ftt_backend.model.Team;
import ftt_backend.model.TeamTask;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TeamRepository;
import ftt_backend.repository.TeamTaskRepository;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TeamTaskService {

    @Autowired
    private TeamTaskRepository teamTaskRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

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

        // user 정보가 누락됐거나 username이 null이면 예외 처리
        if (teamTask.getUser() == null || teamTask.getUser().getUsername() == null) {
            throw new RuntimeException("User 정보가 누락되었습니다.");
        }
        // UserInfo 엔티티를 username으로 조회
        UserInfo user = userRepository.findByUsername(teamTask.getUser().getUsername())
                .orElseThrow(() -> new RuntimeException("User not found with username: " + teamTask.getUser().getUsername()));
        teamTask.setUser(user);

        // (선택) 시작일과 오늘 비교해 상태 결정
        LocalDate now = LocalDate.now();  // ex. 2025-04-13
        LocalDate start = teamTask.getStartDate(); // ex. 2025-04-15
        if (start != null && start.isAfter(now)) {
            teamTask.setStatus("진행 예정");
        } else {
            // start가 오늘이거나 이미 지났으면 "진행중"
            teamTask.setStatus("진행중");
        }


        return teamTaskRepository.save(teamTask);
    }
    // 팀 작업 상태 업데이트 메서드
    @Transactional
    public TeamTask updateTaskStatus(Long teamId, Long taskId, String newStatus) {
        TeamTask task = teamTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("팀 Task id를 찾을수 없음" + taskId));

        // 해당 작업이 지정한 팀에 속하는지 확인 (필요 시)
        if (!task.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("해당 작업은 이 팀에 속하지 않습니다.");
        }

        task.setStatus(newStatus);
        return teamTaskRepository.save(task);
    }
}
