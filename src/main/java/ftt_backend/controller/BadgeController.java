package ftt_backend.controller;

import ftt_backend.model.Task;
import ftt_backend.model.UserBadge;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.BadgeUserRepository;
import ftt_backend.repository.TaskRepository;
import ftt_backend.repository.UserRepository;
import ftt_backend.service.BadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BadgeController {

    @Autowired
    private BadgeUserRepository badgeUserRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private BadgeService badgeService;

    @GetMapping("/user-badges/{userId}")
    public List<UserBadge> getUserBadges(@PathVariable Long userId) {
        return badgeUserRepository.findByUserId(userId);
    }

    /**
     * 테스트용 엔드포인트:
     * 전달된 completedCount(목표 완료 Task 수)보다 현재 완료 수가 부족하면,
     * Dummy Task를 생성하여 완료 수를 맞추고, BadgeService를 호출해 뱃지 지급 후
     * 최신 사용자 뱃지 목록을 반환
     */
    @PostMapping("/user-badges/test/update-completed-count")
    public List<UserBadge> updateCompletedCount(@RequestBody TestUpdateRequest request) {
        // userId 문자열을 Long으로 변환
        Long id;
        try {
            id = Long.parseLong(request.getUserId());
        } catch (NumberFormatException e) {
            throw new RuntimeException("유저 아이디 찾을수 없음 " + request.getUserId());
        }
        // 사용자 조회
        UserInfo user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없음" + id));

        // 현재 완료된 Task 개수 (status가 "DONE")
        int currentCount = taskRepository.countByUser_UserIdAndStatus(user.getUserId(), "DONE");
        int targetCount = request.getCompletedCount();

        // 현재 완료된 Task 수가 목표보다 부족하면 Dummy Task 생성
        if (currentCount < targetCount) {
            int tasksToCreate = targetCount - currentCount;
            for (int i = 0; i < tasksToCreate; i++) {
                Task dummyTask = new Task();
                dummyTask.setTitle("작업 완료 :" + (currentCount + i + 1));
                dummyTask.setDescription("뱃지 완료를 위한 더미 데이터 생성");
                dummyTask.setStatus("DONE");
                dummyTask.setCreatedAt(LocalDate.now());
                dummyTask.setUser(user);
                // 테스트용으로 시작일과 마감일을 오늘로 설정
                dummyTask.setStartDate(LocalDate.now());
                dummyTask.setDueDate(LocalDate.now());
                taskRepository.save(dummyTask);
            }
        }

        // BadgeService를 통해 뱃지 지급 로직 실행
        badgeService.checkAndGrantBadgeIfEligible(user);

        // 업데이트된 사용자 뱃지 목록 반환
        return badgeUserRepository.findByUserId(user.getId());
    }

    /**
      userBadgeId로 UserBadge를 찾음.
      해당 UserBadge의 사용자 ID로 모든 UserBadge를 불러옴.
      모두 activeBadgeId = null**로 초기화(즉, 전부 비활성).
      지금 선택된 UserBadge만 activeBadgeId = userBadge.getBadge().getId()로 설정(활성).
      saveAll()로 한꺼번에 DB에 반영.
      최종적으로 업데이트된 전체 UserBadge 목록을 반환 → 프론트가 다시 이 목록을 받아 UI를 갱신할 수 있음.
     */
    @PatchMapping("/user-badges/{userBadgeId}/activate")
    public List<UserBadge> activateUserBadge(@PathVariable Long userBadgeId) {
        // 1) userBadgeId로 UserBadge 찾기
        UserBadge userBadge = badgeUserRepository.findById(userBadgeId)
                .orElseThrow(() -> new RuntimeException("UserBadge not found: " + userBadgeId));

        // 2) 이 UserBadge가 속한 사용자(UserInfo)
        UserInfo user = userBadge.getUser();

        // 3) 해당 사용자의 모든 UserBadge를 조회
        List<UserBadge> allUserBadges = badgeUserRepository.findByUserId(user.getId());

        // 4) 모든 UserBadge의 activeBadgeId를 null로 초기화
        for (UserBadge ub : allUserBadges) {
            ub.setActiveBadgeId(null);
        }

        // 5) 현재 선택된 UserBadge만 활성화 (activeBadgeId를 해당 badge의 id로 설정)
        userBadge.setActiveBadgeId(userBadge.getBadge().getId());

        // 6) 일괄 저장
        badgeUserRepository.saveAll(allUserBadges);

        // 7) 업데이트된 목록 반환
        return allUserBadges;
    }

    // 테스트용 DTO
    public static class TestUpdateRequest {
        private String userId;
        private int completedCount;

        public String getUserId() {
            return userId;
        }
        public void setUserId(String userId) {
            this.userId = userId;
        }
        public int getCompletedCount() {
            return completedCount;
        }
        public void setCompletedCount(int completedCount) {
            this.completedCount = completedCount;
        }
    }

}
