package ftt_backend.repository;

import ftt_backend.model.TaskFile;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * - TaskFile 엔티티에 대한 CRUD 작업을 지원
 */
public interface TaskFileUpdateRepository extends JpaRepository<TaskFile, Long> {

}
