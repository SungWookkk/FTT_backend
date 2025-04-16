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
import java.time.LocalDate;
import java.util.List;

@Service
public class TeamApplicationService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamApplicationRepository teamApplicationRepository;

    // 팀 신청 생성 (신청자의 ID는 헤더 등을 통해 전달받은 값 사용)
    @Transactional
    public TeamApplication createTeamApplication(Long teamId, Long applicantId, String reason, String goal) {
        // 전달받은 teamId로 팀 객체를 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("팀을 찾을 수 없습니다: " + teamId));

        // 신청자의 정보를 조회
        UserInfo applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + applicantId));

        // 새로운 TeamApplication 객체 생성 및 데이터 설정
        TeamApplication application = new TeamApplication();
        application.setTeam(team);
        application.setApplicant(applicant);
        application.setReason(reason);
        application.setGoal(goal);
        application.setStatus("PENDING");
        application.setAppliedAt(LocalDate.now());

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
}
