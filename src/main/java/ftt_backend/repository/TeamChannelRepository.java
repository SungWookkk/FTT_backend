package ftt_backend.repository;

import ftt_backend.model.TeamChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamChannelRepository extends JpaRepository<TeamChannel, Long> {
    // 특정 팀에 속한 채널 목록 조회
    List<TeamChannel> findByTeamId(Long teamId);
}
