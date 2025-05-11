package ftt_backend.statistics;

import ftt_backend.repository.TaskRepository;
import ftt_backend.repository.UserRepository;
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
     * 전체 작업 통계
     */
    public UserStatsDto getUserStats() {
        // 1) 전체 과제 수
        long totalTasks = taskRepo.count();

        // 2) 한 달 내에 생성된 과제 수
        long activeTasks = em.createQuery(
                        "select count(t) from Task t where t.createdAt >= :since", Long.class)
                .setParameter("since", LocalDate.now().minusMonths(1))
                .getSingleResult();

        // 3) 전체 완료된 과제 수
        long completedTasks = taskRepo.countByStatusIn(List.of("완료", "DONE"));

        // 4) 전체 완료율 계산
        double completionRate = totalTasks > 0
                ? completedTasks * 100.0 / totalTasks
                : 0.0;

        return new UserStatsDto(
                totalTasks,
                activeTasks,
                completionRate
        );
    }
    /**
     * 주어진 년·월, 사용자에 대해
     * 일(day)별 작업 시작 수를 반환
     */
    public List<DailyDto> getDaily(Long userId, int year, int month) {
        @SuppressWarnings("unchecked")
        List<Tuple> rows = em.createNativeQuery(
                        """
                        SELECT
                          DAY(created_at) AS label,
                          COUNT(*)         AS value
                        FROM task
                        WHERE YEAR(created_at)=?
                          AND MONTH(created_at)=?
                          AND user_id=?
                        GROUP BY DAY(created_at)
                        ORDER BY DAY(created_at)
                        """, Tuple.class)
                .setParameter(1, year)
                .setParameter(2, month)
                .setParameter(3, userId)
                .getResultList();

        return rows.stream()
                .map(t -> new DailyDto(
                        String.valueOf(((Number)t.get("label")).intValue()),
                        ((Number)t.get("value")).longValue()
                ))
                .collect(Collectors.toList());
    }
    public DailyDetailDto getDailyDetail(Long userId, int year, int month, int day) {
        // JPQL 함수 day/month/year 사용
        long total = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id=:uid " +
                                "   and function('year', t.createdAt)=:y " +
                                "   and function('month', t.createdAt)=:m " +
                                "   and function('day', t.createdAt)=:d"
                        , Long.class)
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .setParameter("d", day)
                .getSingleResult();

        long completed = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id=:uid " +
                                "   and function('year', t.createdAt)=:y " +
                                "   and function('month', t.createdAt)=:m " +
                                "   and function('day', t.createdAt)=:d " +
                                "   and t.status in ('완료','DONE')"
                        , Long.class)
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .setParameter("d", day)
                .getSingleResult();

        long failed = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id=:uid " +
                                "   and function('year', t.createdAt)=:y " +
                                "   and function('month', t.createdAt)=:m " +
                                "   and function('day', t.createdAt)=:d " +
                                "   and t.status = '실패'"
                        , Long.class)
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .setParameter("d", day)
                .getSingleResult();

        return new DailyDetailDto(total, completed, failed);
    }

    // 달의 생성,완료,실패 task 반환
    public MonthlyDetailDto getMonthlyDetail(Long userId, int year, int month) {
        // 전체 생성 개수
        long total = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id=:uid " +
                                "   and function('year', t.createdAt)=:y " +
                                "   and function('month', t.createdAt)=:m"
                        , Long.class)
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .getSingleResult();

        // 완료 개수
        long completed = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id=:uid " +
                                "   and function('year', t.createdAt)=:y " +
                                "   and function('month', t.createdAt)=:m " +
                                "   and t.status in ('완료','DONE')"
                        , Long.class)
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .getSingleResult();

        // 실패 개수
        long failed = em.createQuery(
                        "select count(t) from Task t " +
                                " where t.user.id=:uid " +
                                "   and function('year', t.createdAt)=:y " +
                                "   and function('month', t.createdAt)=:m " +
                                "   and t.status = '실패'"
                        , Long.class)
                .setParameter("uid", userId)
                .setParameter("y", year)
                .setParameter("m", month)
                .getSingleResult();

        return new MonthlyDetailDto(total, completed, failed);
    }
}
