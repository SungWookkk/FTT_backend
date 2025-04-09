package ftt_backend.repository;

import ftt_backend.model.TeamTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamTaskRepository extends JpaRepository<TeamTask, Long> {
    // 특정 팀의 작업들 조회
    List<TeamTask> findByTeamId(Long teamId);
}
