/*
 * TaskService: Task 엔티티 관련 비즈니스 로직 담당
 * create, update, findAll 등의 메서드를 통해 Controller와 소통
 */
package ftt_backend.service;

import ftt_backend.model.Task;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TaskRepository;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    // 모든 Task 목록 조회
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    // userId를 추가로 받아 해당 유저의 Task를 생성
    public Task createTask(Task task) {
        String userId = task.getUserId();
        if (userId == null || userId.isBlank()) {
            throw new RuntimeException("사용자를 찾을수 없음");
        }
        UserInfo user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없음 " + userId));
        task.setUser(user);
        task.setCreatedAt(LocalDateTime.now());
        return taskRepository.save(task);
    }



    // 특정 Task 수정
    public Task updateTask(Long id, Task updatedTask) {
        // 기존 Task를 DB에서 조회
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("id에 따른 작업을 찾을수 없습니다" + id));

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
    // 특정 Task 삭제
    public void deleteTask(Long id) {
        //  DB에서 해당 Task 조회
        Task existingTask = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task의 id를 찾을수 없어요 " + id));

        //  조회된 Task 삭제
        taskRepository.delete(existingTask);
    }
}
