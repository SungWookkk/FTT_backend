package ftt_backend.service;

import ftt_backend.model.Team;
import ftt_backend.model.TeamApplication;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class TeamApplicationService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamApplicationRepository teamApplicationRepository;

    @Autowired
    private TeamReadingListRepository readingListRepo;

    @Autowired
    private TeamTaskRepository taskRepo;

    @Autowired
    private TeamChannelRepository teamChannelRepository;

    // 팀 신청 생성 (신청자의 ID는 헤더 등을 통해 전달받은 값 사용)
    @Transactional
    public TeamApplication createTeamApplication(Long teamId, Long applicantId, String reason, String goal) {
        // 1) 팀 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다: " + teamId));

        // 2) 신청자 조회
        UserInfo applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + applicantId));

        // 3) 이미 가입한 사용자이면 예외
        if (team.getMembers() != null && team.getMembers().contains(applicant)) {
            // 여기서 예외 혹은 커스텀 메시지로 처리
            throw new RuntimeException("이미 가입되어 있는 팀입니다!");
        }

        // 4) 새 신청 엔티티 생성
        TeamApplication application = new TeamApplication();
        application.setTeam(team);
        application.setApplicant(applicant);
        application.setReason(reason);
        application.setGoal(goal);
        application.setStatus("PENDING");
        application.setAppliedAt(LocalDate.now());

        // 5) DB 저장 후 반환
        return teamApplicationRepository.save(application);
    }

    // 특정 팀의 신청 목록 조회
    public List<TeamApplication> getApplicationsByTeam(Long teamId) {
        return teamApplicationRepository.findByTeam_Id(teamId);
    }

    // 신청 승인: 승인 시 해당 신청자의 정보를 팀의 멤버 목록에 추가
    @Transactional
    public TeamApplication approveApplication(Long applicationId) {
        TeamApplication application = teamApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("신청을 찾을 수 없습니다: " + applicationId));

        application.setStatus("APPROVED");

        Team team = application.getTeam();
        UserInfo applicant = application.getApplicant();
        if (!team.getMembers().contains(applicant)) {
            team.getMembers().add(applicant);
            // 팀 업데이트 필요 시 teamRepository.save(team) 호출
        }

        return teamApplicationRepository.save(application);
    }

    // 신청 반려: 신청 상태를 REJECTED로 변경
    @Transactional
    public TeamApplication rejectApplication(Long applicationId) {
        TeamApplication application = teamApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("신청을 찾을 수 없습니다: " + applicationId));
        application.setStatus("REJECTED");
        return teamApplicationRepository.save(application);
    }
    /**
     * 멤버 추방
     */
    @Transactional
    public void removeMember(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다: " + teamId));
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        if (!team.getMembers().remove(user)) {
            throw new RuntimeException("해당 사용자가 팀 멤버가 아닙니다: " + userId);
        }
        teamRepository.save(team);
    }

    /**
     * 멤버 등급 상승 (USER → ADMIN)
     */
    @Transactional
    public void promoteMember(Long teamId, Long userId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다: " + teamId));
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

        if (!team.getMembers().contains(user)) {
            throw new RuntimeException("해당 사용자가 팀 멤버가 아닙니다: " + userId);
        }

        user.setRole("ADMIN");
        userRepository.save(user);
    }

    /**
     * 팀 탈퇴 (현재 사용자)
     */
    @Transactional
    public void leaveTeam(Long teamId, Long userId) {
        removeMember(teamId, userId);
    }

    /**
     * 팀 해체
     */
    @Transactional
    public void disbandTeam(Long teamId) {
        if (!teamRepository.existsById(teamId))
            throw new RuntimeException("해당 팀이 존재하지 않습니다: " + teamId);

        // 1) 팀 신청
        teamApplicationRepository.deleteByTeamId(teamId);
        // 2) 채널
        teamChannelRepository.deleteByTeamId(teamId);
        // 3) 읽기 자료
        readingListRepo.deleteByTeamId(teamId);
        // 4) 할 일
        taskRepo.deleteByTeamId(teamId);
        // 5) 멤버십
        teamMemberRepository.deleteByTeamId(teamId);
        // 6) 팀 삭제
        teamRepository.deleteById(teamId);
    }
}
