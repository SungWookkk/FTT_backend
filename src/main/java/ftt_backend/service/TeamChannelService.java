package ftt_backend.service;

import ftt_backend.model.Team;
import ftt_backend.model.TeamChannel;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TeamChannelRepository;
import ftt_backend.repository.TeamRepository;
import ftt_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class TeamChannelService {

    @Autowired
    private TeamChannelRepository teamChannelRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(TeamChannelService.class);

    /**
     * 팀에 속한 모든 채널 조회
     *  - 팀이 없으면 RuntimeException 발생 (기존 흐름 유지)
     *  - 있으면 TeamChannel 엔티티 리스트 반환
     */
    public List<TeamChannel> getChannelsByTeamId(Long teamId) {
        logger.debug("getChannelsByTeamId 호출: teamId={}", teamId);

        // 1) 팀 존재 여부 확인
        teamRepository.findById(teamId)
                .orElseThrow(() -> {
                    logger.error("팀을 찾을 수 없습니다: teamId={}", teamId);
                    return new RuntimeException("팀 ID를 찾을수없음 " + teamId);
                });

        // 2) 채널 조회
        List<TeamChannel> channels = teamChannelRepository.findByTeamId(teamId);

        // 3) 디버깅용 채널 정보 로깅
        logger.debug("조회된 채널 개수: {} for teamId={}", channels.size(), teamId);
        for (TeamChannel ch : channels) {
            logger.debug("  ↳ Channel[id={}, name={}]", ch.getId(), ch.getChannelName());
        }

        return channels;
    }

    // 채널 생성
    @Transactional
    public TeamChannel createTeamChannel(Long teamId, TeamChannel channel) {
        // 팀 존재 여부 확인
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀 ID를 찾을수없음" + teamId));
        channel.setTeam(team);
        channel.setCreatedAt(LocalDateTime.now());
        channel.setUpdatedAt(LocalDateTime.now());

        if (channel.getCreatedBy() == null || channel.getCreatedBy().getId() == null) {
            throw new RuntimeException("CreatedBy 정보가 누락되었습니다.");
        }
        UserInfo createdBy = userRepository.findById(channel.getCreatedBy().getId())
                .orElseThrow(() -> new RuntimeException("사용자 ID를 찾을수없음" + channel.getCreatedBy().getId()));
        channel.setCreatedBy(createdBy);

        return teamChannelRepository.save(channel);
    }

    // 채널 수정
    @Transactional
    public TeamChannel updateTeamChannel(Long teamId, Long channelId, Map<String, Object> updates) {
        TeamChannel channel = teamChannelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("채널 ID를 찾을수없음" + channelId));
        if (!channel.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("해당 채널은 이 팀에 속하지 않습니다.");
        }
        if (updates.containsKey("channelName")) {
            channel.setChannelName((String) updates.get("channelName"));
        }
        if (updates.containsKey("description")) {
            channel.setDescription((String) updates.get("description"));
        }
        channel.setUpdatedAt(LocalDateTime.now());
        return teamChannelRepository.save(channel);
    }

    // 채널 삭제
    @Transactional
    public void deleteTeamChannel(Long teamId, Long channelId) {
        TeamChannel channel = teamChannelRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("채널 ID를 찾을수없음" + channelId));
        if (!channel.getTeam().getId().equals(teamId)) {
            throw new RuntimeException("해당 채널은 이 팀에 속하지 않습니다.");
        }
        teamChannelRepository.delete(channel);
    }
}
