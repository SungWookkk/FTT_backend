package ftt_backend.service;

import ftt_backend.model.Team;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TeamRepository;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Team createTeam(Team team) {
        // team.getTeamLeader()가 "2" (문자열)이라면 숫자로 변환
        Long leaderId = Long.valueOf(team.getTeamLeader());

        UserInfo leaderUser = userRepository.findById(leaderId)
                .orElseThrow(() -> new RuntimeException("사용자 찾을수 없음" + leaderId));

        // 팀 멤버에 leaderUser 추가
        if (team.getMembers() == null) {
            team.setMembers(new ArrayList<>());
        }
        team.getMembers().add(leaderUser);

        return teamRepository.save(team);
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
}
