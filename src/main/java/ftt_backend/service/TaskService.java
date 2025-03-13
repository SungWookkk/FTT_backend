/*
 * TaskService: Task 엔티티 관련 비즈니스 로직 담당
 * create, update, findAll 등의 메서드를 통해 Controller와 소통
 */
package ftt_backend.service;

import ftt_backend.model.Task;
import ftt_backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    // 모든 Task 목록 조회
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // 새 Task 생성
    public Task createTask(Task task) {
        // 필요시 task의 생성일시나 기본 상태값 설정 가능
        return taskRepository.save(task);
    }

    // 특정 Task 수정
    public Task updateTask(Long id, Task updatedTask) {
        // 기존 Task를 DB에서 조회
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        // 변경할 필드들만 업데이트
        existingTask.setTitle(updatedTask.getTitle());
        existingTask.setDescription(updatedTask.getDescription());
        existingTask.setDueDate(updatedTask.getDueDate());
        existingTask.setPriority(updatedTask.getPriority());
        existingTask.setStatus(updatedTask.getStatus());
        existingTask.setAssignee(updatedTask.getAssignee());
        existingTask.setMemo(updatedTask.getMemo());
        // user 관계(작성자)가 있다면 수정 로직 추가 가능

        // 변경된 Task를 DB에 저장 후 반환
        return taskRepository.save(existingTask);
    }
}
