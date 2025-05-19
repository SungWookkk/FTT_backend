package ftt_backend.controller;

import ftt_backend.config.JwtUtils;
import ftt_backend.model.Badge;
import ftt_backend.model.Task;
import ftt_backend.model.UserBadge;
import ftt_backend.model.UserInfo;
import ftt_backend.model.dto.BadgeProgressDTO;
import ftt_backend.repository.BadgeRepository;
import ftt_backend.repository.BadgeUserRepository;
import ftt_backend.repository.TaskRepository;
import ftt_backend.repository.UserRepository;
import ftt_backend.service.BadgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @GetMapping("/badges")
    public List<Badge> getAllBadges() {
        return badgeRepository.findAll();
    }

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
            throw new RuntimeException("유저 아이디 찾을 수 없음 " + request.getUserId());
        }
        // 사용자 조회
        UserInfo user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없음 " + id));

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

        // 업데이트된 사용자 뱃지 목록 조회
        List<UserBadge> updatedUserBadges = badgeUserRepository.findByUserId(user.getId());

        // 지급된 뱃지 중 가장 높은 등급(혹은 최근 취득한 뱃지)로 정렬하여 선택
        updatedUserBadges.sort((ub1, ub2) -> {
            // 예시: completionThreshold 기준 내림차순 (높을수록 좋은 뱃지)
            Integer t1 = ub1.getBadge().getCompletionThreshold();
            Integer t2 = ub2.getBadge().getCompletionThreshold();
            return t2.compareTo(t1);
            // 만약 취득일자가 있다면 그 기준으로 정렬할 수도 있음.
        });

        // 모든 뱃지의 activeBadgeId를 초기화한 후,
        // 정렬된 목록의 첫 번째 뱃지를 자동 활성화
        for (UserBadge ub : updatedUserBadges) {
            ub.setActiveBadgeId(null);
        }
        if (!updatedUserBadges.isEmpty()) {
            UserBadge highestBadge = updatedUserBadges.get(0);
            highestBadge.setActiveBadgeId(highestBadge.getBadge().getId());
        }
        badgeUserRepository.saveAll(updatedUserBadges);

        return updatedUserBadges;
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
    @GetMapping("/badges/progress")
    public ResponseEntity<?> getProgress(
            @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authorization 헤더 필요");
            }
            String token = authHeader.substring(7);
            if (!jwtUtils.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 토큰");
            }
            String loginUserId = jwtUtils.getAuthentication(token).getName();
            BadgeProgressDTO dto = badgeService.getProgress(loginUserId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("progress조회 중 오류: " + e.getMessage());
        }
    }
}
