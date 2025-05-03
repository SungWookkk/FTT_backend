package ftt_backend.repository;

import ftt_backend.model.Task;
import ftt_backend.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// Task 엔티티에 대한 CRUD 작업을 지원하는 Repository 인터페이스
public interface TaskRepository extends JpaRepository<Task, Long> {
    // 기본적인 CRUD 메서드 외 추가적인 메서드는 필요시 선언
    List<Task> findByUser_UserId(String userId);

    //특정 userId, status=’DONE’인 Task 개수”를 세는 메서드가 필요
    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.userId = :userId AND t.status = :status")
    int countByUser_UserIdAndStatus(@Param("userId") String userId, @Param("status") String status);

    /**
     * 오늘 자정부터 내일 자정, 혹은 현재 시각부터 2시간 후 사이에 due_date가 속한 Task를 모두 조회
     */
    @Query("""
      SELECT t
        FROM Task t
       WHERE t.dueDate BETWEEN :today AND :tomorrow
          OR t.dueDate = :today
    """)
    List<Task> findImminentTasks(
            @Param("today") LocalDate today,
            @Param("tomorrow") LocalDate tomorrow
    );
}