package ftt_backend.statistics;

import ftt_backend.model.UserInfo;
import ftt_backend.service.UserService;
import ftt_backend.statistics.dto.*;
import ftt_backend.config.JwtUtils;          // ← JWT 유틸 임포트
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired private StatisticsService statsService;
    @Autowired private UserService userService;
    @Autowired private JwtUtils jwtUtils;   // ← JWT 유틸 주입

    /**
     * HTTP 헤더에서 Bearer 토큰을 꺼내서,
     * jwtUtils.validateToken 으로 검증 후,
     * jwtUtils.getAuthentication(token) 으로 Authentication 얻고,
     * 그 이름(username)을 UserService.findByUserId 에 넘겨서 DB PK 조회
     */
    private Long resolveCurrentUserDbId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization 헤더가 없거나 형식이 잘못되었습니다.");
        }
        String token = authHeader.substring(7);

        if (!jwtUtils.validateToken(token)) {
            throw new RuntimeException("토큰 검증 실패");
        }

        Authentication authentication = jwtUtils.getAuthentication(token);
        String userId = authentication.getName();
        UserInfo me = userService.findByUserId(userId);
        return me.getId();
    }


    @GetMapping("/overview")
    public List<OverviewDto> getOverview(
            @RequestHeader("Authorization") String authHeader
    ) {
        Long meId = resolveCurrentUserDbId(authHeader);
        return statsService.getOverview(meId);
    }

    @GetMapping("/monthly")
    public List<MonthlyDto> getMonthly(
            @RequestHeader("Authorization") String authHeader
    ) {
        Long meId = resolveCurrentUserDbId(authHeader);
        return statsService.getMonthly(meId);
    }

    /** 전체 작업 통계 */
    @GetMapping("/users")
    public UserStatsDto getAllTaskStats() {
        return statsService.getUserStats();
    }
    /**
     * 일별 통계 (year, month 쿼리 파라미터)
     */
    @GetMapping("/daily")
    public List<DailyDto> getDaily(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("year") int year,
            @RequestParam("month") int month
    ) {
        Long meId = resolveCurrentUserDbId(authHeader);
        return statsService.getDaily(meId, year, month);
    }
    @GetMapping("/daily/detail")
    public DailyDetailDto getDailyDetail(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day
    ) {
        Long meId = resolveCurrentUserDbId(authHeader);
        return statsService.getDailyDetail(meId, year, month, day);
    }
    @GetMapping("/monthly/detail")
    public MonthlyDetailDto getMonthlyDetail(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam int year,
            @RequestParam int month
    ) {
        Long meId = resolveCurrentUserDbId(authHeader);
        return statsService.getMonthlyDetail(meId, year, month);
    }
}
