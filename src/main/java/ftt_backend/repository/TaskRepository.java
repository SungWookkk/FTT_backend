package ftt_backend.repository;

import ftt_backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

// Task 엔티티에 대한 CRUD 작업을 지원하는 Repository 인터페이스
public interface TaskRepository extends JpaRepository<Task, Long> {
    // 기본적인 CRUD 메서드 외 추가적인 메서드는 필요시 선언
}
