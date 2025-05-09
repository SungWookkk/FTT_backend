package ftt_backend.statistics;

import ftt_backend.model.UserInfo;
import ftt_backend.service.UserService;
import ftt_backend.statistics.dto.MonthlyDto;
import ftt_backend.statistics.dto.OverviewDto;
import ftt_backend.statistics.dto.UserStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired private StatisticsService statsService;
    @Autowired private UserService userService;  // DB 조회용

    /** 카드형 개요 통계 */
    @GetMapping("/overview")
    public List<OverviewDto> getOverview(Principal principal) {
        // 1) 로그인한 사람의 userId(principal.getName()) 로 UserInfo 조회
        UserInfo me = userService.findByUserId(principal.getName());
        // 2) 그 사용자의 DB PK(id)를 서비스로 넘겨서 통계 생성
        return statsService.getOverview(me.getId());
    }

    /** 월별 차트 데이터 */
    @GetMapping("/monthly")
    public List<MonthlyDto> getMonthly(Principal principal) {
        UserInfo me = userService.findByUserId(principal.getName());
        return statsService.getMonthly(me.getId());
    }

    /** 본인 사용자 통계 */
    @GetMapping("/users")
    public UserStatsDto getUserStats(Principal principal) {
        UserInfo me = userService.findByUserId(principal.getName());
        return statsService.getUserStats(me.getId());
    }
}
