package ftt_backend.repository;

import ftt_backend.model.TeamTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface TeamTaskRepository extends JpaRepository<TeamTask, Long> {
    // 특정 팀의 작업들 조회
    List<TeamTask> findByTeamId(Long teamId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TeamTask t WHERE t.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}
