package ftt_backend.service;

import ftt_backend.model.Badge;
import ftt_backend.model.UserBadge;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.BadgeRepository;
import ftt_backend.repository.BadgeUserRepository;
import ftt_backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/**
 * 뱃지 부여 조건을 체크하고, 해당 조건을 달성한 사용자가 아직 받지 않은 뱃지를 자동 지급하는 서비스
 */
@Service
public class BadgeService {

    @Autowired
    private BadgeRepository badgeRepository;
    @Autowired
    private BadgeUserRepository badgeUserRepository;
    @Autowired
    private TaskRepository taskRepository;

    /**
     * 사용자가 Task를 완료할 때마다 호출.
     *  1) 완료된 Task 개수 확인
     *  2) DB에서 모든 뱃지 조회
     *  3) 해당 뱃지의 completionThreshold 이하가 되면 지급
     */
    public void checkAndGrantBadgeIfEligible(UserInfo user) {
        // 1) 해당 사용자의 "완료된 작업" 개수
        int completedCount = taskRepository.countByUser_UserIdAndStatus(user.getUserId(), "DONE");

        // 2) DB에서 모든 뱃지 조회 (completionThreshold 포함)
        List<Badge> allBadges = badgeRepository.findAll();

        for (Badge badge : allBadges) {
            // 뱃지에 completionThreshold가 없거나 0 이하이면 조건 체크 생략 가능
            if (badge.getCompletionThreshold() == null) {
                continue;
            }

            // 3) 조건 충족? (예: completedCount >= 80)
            if (completedCount >= badge.getCompletionThreshold()) {
                // 이미 이 뱃지를 가지고 있는지 확인
                boolean alreadyOwned = badgeUserRepository.findByUserId(user.getId()).stream()
                        .anyMatch(ub -> ub.getBadge().getId().equals(badge.getId()));

                if (!alreadyOwned) {
                    // 아직 소유하지 않았다면 지급
                    UserBadge newUserBadge = new UserBadge();
                    newUserBadge.setUser(user);
                    newUserBadge.setBadge(badge);
                    newUserBadge.setAcquiredDate(LocalDate.now());
                    badgeUserRepository.save(newUserBadge);
                }
            }
        }
    }

    /**
     * 간단한 DTO-like 내부 클래스: 뱃지 이름 + 필요한 완료 횟수
     */
    private static class BadgeRequirement {
        String badgeName;
        int threshold;
        public BadgeRequirement(String badgeName, int threshold) {
            this.badgeName = badgeName;
            this.threshold = threshold;
        }
    }
}
