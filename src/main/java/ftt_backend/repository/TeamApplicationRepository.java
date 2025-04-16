package ftt_backend.repository;

import ftt_backend.model.TeamApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {
    // 특정 팀에 대한 신청 목록 조회
    List<TeamApplication> findByTeam_Id(Long teamId);
}
