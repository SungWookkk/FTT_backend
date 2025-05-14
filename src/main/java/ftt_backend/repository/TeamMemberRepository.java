package ftt_backend.repository;
import ftt_backend.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember,Long> {

    Optional<TeamMember> findByTeam_IdAndUser_Id(Long teamId, Long userId);
    /** 주어진 teamId 로 연관된 모든 TeamMember(=team_memberships) 삭제 */
    @Modifying
    @Transactional
    @Query("delete from TeamMember tm where tm.team.id = :teamId")
    void deleteByTeamId(Long teamId);
}
