package ftt_backend.statistics;

import ftt_backend.repository.TaskRepository;
import ftt_backend.repository.UserRepository;
import ftt_backend.statistics.dto.MonthlyDto;
import ftt_backend.statistics.dto.OverviewDto;
import ftt_backend.statistics.dto.UserStatsDto;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StatisticsService {

    @Autowired
    private TaskRepository taskRepo;
    @Autowired
    private EntityManager em;

    /**
     * 카드형 요약 데이터
     */
    public List<OverviewDto> getOverview(Long userId) {
        // 1) 평균 처리 시간: 완료된 내 task만
        Double avgDays = em.createQuery(
                        "select avg(datediff(t.dueDate, t.createdAt)) " +
                                "from Task t " +
                                "where t.status = '완료' and t.user.id = :uid", Double.class)
                .setParameter("uid", userId)
                .getSingleResult();
        String avgText = String.format("%.1f일", avgDays);

        // 2) 내가 작성한 과제 총수
        long total = taskRepo.countByUser_Id(userId);

        // 3) 내가 완료한 과제 수
        long done   = taskRepo.countByUser_IdAndStatusIn(userId, List.of("완료", "DONE"));

        // 4) 내가 실패한 과제 수
        long failed = taskRepo.countByUser_IdAndStatus(userId, "실패");

        // 5) (예시)
        String rank = "857위";

        return List.of(
                new OverviewDto("작업 처리 평균 시간", avgText, null),
                new OverviewDto("작성한 작업", total + "개", "상위 32%"),
                new OverviewDto("완료한 작업", done + "개", "상위 15%"),
                new OverviewDto("실패한 작업", failed + "개", "상위 15%"),
                new OverviewDto("작업에 따른 사용자 순위", rank, null)
        );
    }
    /**
     * 월별 차트 데이터
     */
    public List<MonthlyDto> getMonthly(Long userId) {
        int year = Year.now().getValue();
        @SuppressWarnings("unchecked")
        List<Tuple> rows = em.createNativeQuery(
                        """
                        select
                          date_format(created_at, '%b') as label,
                          count(*)                      as value
                        from task
                       where year(created_at) = ?
                         and user_id = ?
                    group by month(created_at), date_format(created_at, '%b')
                    order by month(created_at)
                        """, Tuple.class)
                .setParameter(1, year)
                .setParameter(2, userId)
                .getResultList();

        return rows.stream()
                .map(t -> new MonthlyDto(
                        (String)t.get("label"),
                        ((Number)t.get("value")).longValue()))
                .collect(Collectors.toList());
    }

    /**
     * 전체 사용자 통계
     */
    public UserStatsDto getUserStats(Long userId) {
        long totalTasks   = taskRepo.countByUser_Id(userId);
        long completed  = taskRepo.countByUser_IdAndStatusIn(userId, List.of("완료", "DONE"));
        double rate       = totalTasks > 0 ? completed * 100.0 / totalTasks : 0.0;

        // (optional) 한 달간 활동 여부를 세고 싶다면
        long activeSinceMonth = em.createQuery(
                        "select count(t) from Task t where t.user.id = :uid and t.createdAt >= :since", Long.class)
                .setParameter("uid", userId)
                .setParameter("since", LocalDate.now().minusMonths(1))
                .getSingleResult();

        return new UserStatsDto(
                totalTasks,         // 전체 과제 수
                activeSinceMonth,   // 한 달간 작성 과제 수
                rate
        );
    }
}
