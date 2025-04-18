package ftt_backend.service;

import ftt_backend.model.Team;
import ftt_backend.model.TeamMember;
import ftt_backend.model.TeamRole;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TeamMemberRepository;
import ftt_backend.repository.TeamRepository;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class TeamMemberService {

    @Autowired
    private TeamMemberRepository tmRepo;

    @Autowired
    private TeamRepository teamRepo;

    @Autowired
    private UserRepository userRepo;

    /**
     * 팀에 새 멤버를 추가
     * 1) team_members (ManyToMany) 테이블에 반영
     * 2) team_memberships (역할 테이블) 에 기본 MEMBER 역할로 저장
     */
    @Transactional
    public void addMemberToTeam(Long teamId, Long userId) {
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다: " + teamId));
        UserInfo user = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        // 1) team_members (ManyToMany)에도 추가
        team.getMembers().add(user);

        // 2) team_memberships 테이블에도 기록
        TeamMember tm = new TeamMember();
        tm.setTeam(team);
        tm.setUser(user);
        tm.setRole(TeamRole.MEMBER);  // 기본 역할
        tmRepo.save(tm);
    }

    /**
     * 기존 멤버의 역할을 변경
     */
    @Transactional
    public void changeRole(Long teamId, Long userId, TeamRole newRole) {
        TeamMember tm = tmRepo.findByTeam_IdAndUser_Id(teamId, userId)
                .orElseThrow(() ->
                        new RuntimeException("해당 팀(" + teamId + ")의 멤버(" + userId + ")를 찾을 수 없습니다.")
                );
        tm.setRole(newRole);
        tmRepo.save(tm);
    }

    /**
     * 팀에서 멤버를 제거 (team_memberships 테이블에서 삭제)
     */
    @Transactional
    public void removeMember(Long teamId, Long userId) {
        TeamMember tm = tmRepo.findByTeam_IdAndUser_Id(teamId, userId)
                .orElseThrow(() ->
                        new RuntimeException("해당 팀(" + teamId + ")의 멤버(" + userId + ")를 찾을 수 없습니다.")
                );
        tmRepo.delete(tm);
    }

    /**
     * 특정 팀의 모든 TeamMember 엔티티를 조회
     */
    @Transactional
    public List<TeamMember> listMembers(Long teamId) {
        Team team = teamRepo.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다: " + teamId));

        List<TeamMember> memberships = new ArrayList<>();
        for (UserInfo user : team.getMembers()) {
            TeamMember tm = tmRepo.findByTeam_IdAndUser_Id(teamId, user.getId())
                    .orElseGet(() -> {
                        TeamRole defaultRole = user.getId().equals(Long.valueOf(team.getTeamLeader()))
                                ? TeamRole.TEAM_LEADER
                                : TeamRole.MEMBER;
                        TeamMember created = new TeamMember();
                        created.setTeam(team);
                        created.setUser(user);
                        created.setRole(defaultRole);
                        return tmRepo.save(created);
                    });
            memberships.add(tm);
        }
        return memberships;
    }
}
