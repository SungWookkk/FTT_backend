package ftt_backend.repository;
import ftt_backend.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<TeamMember,Long> {

    Optional<TeamMember> findByTeam_IdAndUser_Id(Long teamId, Long userId);
}
