package ftt_backend.service;

import ftt_backend.model.Badge;
import ftt_backend.model.UserBadge;
import ftt_backend.model.UserInfo;
import ftt_backend.model.dto.BadgeProgressDTO;
import ftt_backend.repository.BadgeRepository;
import ftt_backend.repository.BadgeUserRepository;
import ftt_backend.repository.TaskRepository;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
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

    @Autowired
    private UserRepository userRepository;
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
    /**
     * 해당 사용자의 현재 배지 → 다음 배지까지의 진행률 계산
     */
    public BadgeProgressDTO getProgress(String loginUserId) {
        // 0) 로그인 ID 로 UserInfo 조회
        UserInfo user = userRepository.findByUserId(loginUserId)
                .orElseThrow(() -> new RuntimeException("해당 사용자를 찾을 수 없습니다: " + loginUserId));

        // 1) 완료된 Task 개수
        int completed = taskRepository.countByUser_UserIdAndStatus(loginUserId, "DONE");

        // 2) 뱃지 기준값 오름차순 정렬
        List<Badge> badges = badgeRepository.findAll()
                .stream()
                .sorted(Comparator.comparingInt(Badge::getCompletionThreshold))
                .toList();

        // 3) 현재 사용자가 가진 뱃지 중 가장 높은 기준값
        int lower = badgeUserRepository.findByUser(user).stream()
                .map(ub -> ub.getBadge().getCompletionThreshold())
                .max(Integer::compareTo)
                .orElse(0);

        // 4) 다음 뱃지 (기준값 > lower) 혹은 마지막 뱃지
        Badge next = badges.stream()
                .filter(b -> b.getCompletionThreshold() > lower)
                .findFirst()
                .orElse(badges.get(badges.size() - 1));
        int upper = next.getCompletionThreshold();

        // 5) 진행률 계산 (분모 0 이면 100%)
        double rate;
        int diff = upper - lower;
        if (diff <= 0) {
            rate = 1.0;
        } else {
            rate = (double)(completed - lower) / diff;
            if (rate < 0) rate = 0;
            if (rate > 1) rate = 1;
        }

        // 6) 현재 뱃지 이름
        String currentName = badges.stream()
                .filter(b -> b.getCompletionThreshold() == lower)
                .map(Badge::getBadgeName)
                .findFirst()
                .orElse("없음");

        return new BadgeProgressDTO(
                currentName,
                next.getBadgeName(),
                completed,
                lower,
                upper,
                rate
        );
    }
}
