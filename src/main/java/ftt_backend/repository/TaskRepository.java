package ftt_backend.repository;

import ftt_backend.model.Task;
import ftt_backend.model.UserInfo;
import org.springframework.data.domain.Page;              // ← 스프링 데이터 Page
import org.springframework.data.domain.Pageable;          // ← 스프링 데이터 Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser_UserId(String userId);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.user.userId = :userId AND t.status = :status")
    int countByUser_UserIdAndStatus(
            @Param("userId") String userId,
            @Param("status") String status
    );

    @SuppressWarnings("unused")
    @Query("""
      SELECT t
        FROM Task t
       WHERE t.dueDate BETWEEN :today AND :tomorrow
    """)
    Page<Task> findImminentTasks(
            @Param("today") LocalDate today,
            @Param("tomorrow") LocalDate tomorrow,
            Pageable pageable
    );
    boolean existsByUserAndTitleAndDueDate(UserInfo user, String title, LocalDate dueDate);


    // 특정 유저가 작성한 Task 총 개수
    long countByUser_Id(Long userId);
    long countByStatusIn(List<String> statuses);
    // 특정 유저가 작성한 Task 중 상태(status)에 따른 개수
    long countByUser_IdAndStatus(Long userId, String status);
    long countByUser_IdAndStatusIn(Long userId, List<String> statuses);


}
