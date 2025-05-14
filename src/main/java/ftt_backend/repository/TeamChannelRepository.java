package ftt_backend.repository;

import ftt_backend.model.TeamChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface TeamChannelRepository extends JpaRepository<TeamChannel, Long> {
    // 특정 팀에 속한 채널 목록 조회
    List<TeamChannel> findByTeamId(Long teamId);

    @Modifying
    @Transactional
    @Query("DELETE FROM TeamChannel c WHERE c.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}
