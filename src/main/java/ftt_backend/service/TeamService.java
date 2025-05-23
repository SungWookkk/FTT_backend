package ftt_backend.service;

import ftt_backend.model.Team;
import ftt_backend.model.TeamChannel;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TeamChannelRepository;
import ftt_backend.repository.TeamRepository;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamChannelRepository teamChannelRepository;

    // 헤더에서 전달된 userId(팀 생성자 정보)를 사용하여 팀 생성
    @Transactional
    public Team createTeam(Team team, Long userId) {
        // 팀 생성자의 정보를 DB에서 조회
        UserInfo leaderUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        // 팀 리더 필드를 사용자 ID(또는 필요에 따라 전체 사용자 정보)를 설정
        team.setTeamLeader(String.valueOf(userId));

        // 팀 멤버 리스트가 null이면 초기화하고, 팀 생성자를 멤버 목록에 추가
        if (team.getMembers() == null) {
            team.setMembers(new ArrayList<>());
        }
        team.getMembers().add(leaderUser);

        // status 필드가 비어있거나 null인 경우 기본값 설정
        if (team.getStatus() == null || team.getStatus().trim().isEmpty()) {
            team.setStatus("Active");
        }

        // 팀 저장
        Team savedTeam = teamRepository.save(team);

        // 기본 채널 생성 (팀 생성 시 자동 추가)
        // 기본 채널 목록: "General", "공지사항", "자유 채널"
        String[] defaultChannels = {"General", "공지사항", "자유 채널"};
        for (String channelName : defaultChannels) {
            TeamChannel channel = new TeamChannel();
            channel.setChannelName(channelName);
            channel.setDescription(""); // 채널 설명 (필요시 수정)
            channel.setCreatedAt(LocalDateTime.now());
            channel.setUpdatedAt(LocalDateTime.now());
            channel.setTeam(savedTeam);
            channel.setCreatedBy(leaderUser);
            teamChannelRepository.save(channel);
        }

        return savedTeam;
    }


    // 특정 userId가 속한 팀 목록 조회
    public List<Team> findTeamsByUserId(Long userId) {
        return teamRepository.findByMembers_Id(userId);
    }

    // 팀 상세 조회
    public Team findTeamById(Long teamId) {
        return teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다: " + teamId));
    }

    @Transactional
    public Team updateTeam(Team team) {
        return teamRepository.save(team);
    }

    // 전체 팀 목록 조회 메서드
    public List<Team> findAllTeams() {
        return teamRepository.findAll();
    }

    /**
     * 지정된 사용자를 팀에서 탈퇴
     * @param teamId 팀 식별자
     * @param userId 사용자 식별자
     */
    @Transactional
    public void leaveTeam(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다: " + teamId));

        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        boolean removed = team.getMembers().removeIf(u -> u.getId().equals(userId));
        if (!removed) {
            throw new RuntimeException("해당 사용자는 이 팀의 멤버가 아닙니다.");
        }

        // 팀 리더가 탈퇴하는 경우 추가 비즈니스 로직 필요할 수 있음
        teamRepository.save(team);
    }
}


