package ftt_backend.statistics;

import ftt_backend.repository.TaskRepository;
import ftt_backend.statistics.dto.*;
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
                                "  from Task t " +
                                " where t.status = '완료' and t.user.id = :uid",
                        Double.class
                )
                .setParameter("uid", userId)
                .getSingleResult();
        String avgText = String.format("%.1f일", avgDays);

        // 2) 내가 작성한 과제 총수
        long total = taskRepo.countByUser_Id(userId);

        // 3) 내가 완료한 과제 수
        long done = taskRepo.countByUser_IdAndStatusIn(userId, List.of("완료","DONE"));

        // 4) 내가 실패한 과제 수
        long failed = taskRepo.countByUser_IdAndStatus(userId, "실패");

        // 5) 실제 DB 조회로 내 순위 계산 (완료된 Task 수 기준)
        long myCompleted = done;
        Object rankResult = em.createNativeQuery(
                        "SELECT COUNT(*) + 1 FROM ( " +
                                "    SELECT user_id, COUNT(*) AS cnt " +
                                "      FROM task " +
                                "     WHERE status IN ('완료','DONE') " +
                                "     GROUP BY user_id " +
                                "    HAVING cnt > ? " +
                                ") t"
                )
                .setParameter(1, myCompleted)
                .getSingleResult();
        long rankNum = ((Number) rankResult).longValue();
        String rank = rankNum + "위";

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
     * → 시작일(start_date) 기준으로 변경
     */
    public List<MonthlyDto> getMonthly(Long userId) {
        int year = Year.now().getValue();
        @SuppressWarnings("unchecked")
        List<Tuple> rows = em.createNativeQuery(
                        """
                        select
                          date_format(start_date, '%b') as label,     /* 시작일 기준 */
                          count(*)                      as value
                        from task
                       where year(start_date) = ?
                         and user_id = ?
                    group by month(start_date), date_format(start_date, '%b')
                    order by month(start_date)
                        """, Tuple.class
                )
                .setParameter(1, year)
                .setParameter(2, userId)
                .getResultList();

        return rows.stream()
                .map(t -> new MonthlyDto(
                        (String) t.get("label"),
                        ((Number) t.get("value")).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 전체 작업 통계
     */
    public UserStatsDto getUserStats() {
        long totalTasks = taskRepo.count();
        long activeTasks = em.createQuery(
                        "select count(t) from Task t where t.createdAt >= :since", Long.class)
                .setParameter("since", LocalDate.now().minusMonths(1))
                .getSingleResult();
        long completedTasks = taskRepo.countByStatusIn(List.of("완료", "DONE"));
        double completionRate = totalTasks > 0
                ? completedTasks * 100.0 / totalTasks
                : 0.0;

        return new UserStatsDto(totalTasks, activeTasks, completionRate);
    }

    /**
     * 일별 차트 데이터
     * → 시작일(start_date) 기준으로 변경
     */
    public List<DailyDto> getDaily(Long userId, int year, int month) {
        @SuppressWarnings("unchecked")
        List<Tuple> rows = em.createNativeQuery(
                        """
                        SELECT
                          DAY(start_date) AS label,    /* 시작일 기준 */
                          COUNT(*)         AS value
                        FROM task
                        WHERE YEAR(start_date)=?
                          AND MONTH(start_date)=?
                          AND user_id=?
                        GROUP BY DAY(start_date)
                        ORDER BY DAY(start_date)
                        """, Tuple.class
                )
                .setParameter(1, year)
                .setParameter(2, month)
                .setParameter(3, userId)
                .getResultList();

        return rows.stream()
                .map(t -> new DailyDto(
                        String.valueOf(((Number) t.get("label")).intValue()),
                        ((Number) t.get("value")).longValue()
                ))
                .collect(Collectors.toList());
    }

    /**
     * 일별 상세 데이터 (DailyDetailDto)
     * → 시작일(startDate) 기준으로 변경
     */
    public DailyDetailDto getDailyDetail(Long userId, int year, int month, int day) {
        long total = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id = :uid" +
                                "   and function('year', t.startDate) = :y" +
                                "   and function('month', t.startDate) = :m" +
                                "   and function('day', t.startDate) = :d",
                        Long.class
                )
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .setParameter("d", day)
                .getSingleResult();

        long completed = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id = :uid" +
                                "   and function('year', t.startDate) = :y" +
                                "   and function('month', t.startDate) = :m" +
                                "   and function('day', t.startDate) = :d" +
                                "   and t.status in ('완료','DONE')",
                        Long.class
                )
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .setParameter("d", day)
                .getSingleResult();

        long failed = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id = :uid" +
                                "   and function('year', t.startDate) = :y" +
                                "   and function('month', t.startDate) = :m" +
                                "   and function('day', t.startDate) = :d" +
                                "   and t.status = '실패'",
                        Long.class
                )
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .setParameter("d", day)
                .getSingleResult();

        return new DailyDetailDto(total, completed, failed);
    }

    /**
     * 월별 상세 데이터 (MonthlyDetailDto)
     * → 시작일(startDate) 기준으로 변경
     */
    public MonthlyDetailDto getMonthlyDetail(Long userId, int year, int month) {
        long total = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id = :uid" +
                                "   and function('year', t.startDate) = :y" +
                                "   and function('month', t.startDate) = :m",
                        Long.class
                )
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .getSingleResult();

        long completed = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id = :uid" +
                                "   and function('year', t.startDate) = :y" +
                                "   and function('month', t.startDate) = :m" +
                                "   and t.status in ('완료','DONE')",
                        Long.class
                )
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .getSingleResult();

        long failed = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id = :uid" +
                                "   and function('year', t.startDate) = :y" +
                                "   and function('month', t.startDate) = :m" +
                                "   and t.status = '실패'",
                        Long.class
                )
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .getSingleResult();

        return new MonthlyDetailDto(total, completed, failed);
    }
}
