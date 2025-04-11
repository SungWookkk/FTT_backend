package ftt_backend.service;

import ftt_backend.model.Team;
import ftt_backend.model.TeamApplication;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TeamApplicationRepository;
import ftt_backend.repository.TeamRepository;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TeamApplicationService {

    @Autowired
    private TeamApplicationRepository teamApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    /**
     * 팀 신청 생성
     * @param userId   현재 로그인한 사용자의 id (UserInfo의 primary key)
     * @param teamId   신청할 팀의 id
     * @param reason   신청하는 이유
     * @param goal     목표
     * @return 생성된 TeamApplication 엔티티
     */
    @Transactional
    public TeamApplication createApplication(Long userId, Long teamId, String reason, String goal) {
        // 신청한 사용자(UserInfo) 조회
        UserInfo applicant = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다. userId=" + userId));

        // 신청할 팀(Team) 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("해당 팀을 찾을 수 없습니다. teamId=" + teamId));

        // 중복 신청 여부 확인 (같은 팀에 대해 같은 사용자가 이미 신청한 경우)
        Optional<TeamApplication> existingApplication = teamApplicationRepository.findByTeam_IdAndApplicant_Id(teamId, userId);
        if (existingApplication.isPresent()) {
            throw new RuntimeException("이미 이 팀에 신청하셨습니다.");
        }

        // 신청 엔티티 생성
        TeamApplication application = new TeamApplication();
        application.setApplicant(applicant);
        // 실제 UserInfo의 username 값을 별도 컬럼에 저장
        application.setApplicantUsername(applicant.getUsername());
        application.setTeam(team);
        application.setReason(reason);
        application.setGoal(goal);
        // 생성자에서 기본값("PENDING")과 createdAt이 설정됨

        return teamApplicationRepository.save(application);
    }

    /**
     * 특정 팀에 대한 전체 신청 목록 조회
     * @param teamId 팀 id
     * @return 신청 목록
     */
    public List<TeamApplication> getApplicationsByTeamId(Long teamId) {
        return teamApplicationRepository.findByTeam_Id(teamId);
    }

    /**
     * 단건 신청 조회
     * @param applicationId 신청 id
     * @return 해당 신청 엔티티
     */
    public TeamApplication getApplicationById(Long applicationId) {
        return teamApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("신청 정보를 찾을 수 없습니다. applicationId=" + applicationId));
    }

    /**
     * 신청 승인 처리
     * 승인 시, 신청 사용자를 해당 팀의 멤버로 추가함.
     * @param applicationId 신청 id
     * @return 승인된 신청 엔티티
     */
    @Transactional
    public TeamApplication approveApplication(Long applicationId) {
        TeamApplication application = getApplicationById(applicationId);
        application.setStatus("APPROVED");

        // 승인 시, 신청한 사용자를 팀 멤버 리스트에 추가
        Team team = application.getTeam();
        team.getMembers().add(application.getApplicant());
        teamRepository.save(team);

        return teamApplicationRepository.save(application);
    }

    /**
     * 신청 거절 처리
     * @param applicationId 신청 id
     * @return 변경된 신청 엔티티
     */
    @Transactional
    public TeamApplication rejectApplication(Long applicationId) {
        TeamApplication application = getApplicationById(applicationId);
        application.setStatus("REJECTED");
        return teamApplicationRepository.save(application);
    }
}
