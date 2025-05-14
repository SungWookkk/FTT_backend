package ftt_backend.repository;

import ftt_backend.model.TeamApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {
    // 특정 팀에 대한 신청 목록 조회
    List<TeamApplication> findByTeam_Id(Long teamId);
    @Modifying
    @Transactional
    @Query("DELETE FROM TeamApplication a WHERE a.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}
