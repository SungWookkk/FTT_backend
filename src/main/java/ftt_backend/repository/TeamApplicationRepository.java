package ftt_backend.repository;

import ftt_backend.model.TeamApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TeamApplicationRepository extends JpaRepository<TeamApplication, Long> {
    List<TeamApplication> findByTeam_Id(Long teamId);
    // 특정 팀에 대해 해당 사용자가 이미 신청한 것이 있는지 확인
    Optional<TeamApplication> findByTeam_IdAndApplicant_Id(Long teamId, Long applicantId);

}
