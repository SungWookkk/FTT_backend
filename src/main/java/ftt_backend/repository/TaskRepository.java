package ftt_backend.repository;

import ftt_backend.model.Task;
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
            Pageable pageable                // ← org.springframework.data.domain.Pageable
    );

}
