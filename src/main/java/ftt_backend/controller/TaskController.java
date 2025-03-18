/*
 * TaskController: 클라이언트 요청을 받아서 TaskService를 통해 처리
 * /api/tasks 경로에서 GET, POST, PUT 등을 제공
 */
package ftt_backend.controller;

import ftt_backend.model.Task;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.TaskRepository;
import ftt_backend.repository.UserRepository;
import ftt_backend.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    // 모든 Task 조회 (관리자용 등)
    @GetMapping("")
    public ResponseEntity<?> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    // 새 Task 생성 (현재 로그인한 사용자의 Task로 설정)
    @PostMapping("")
    public ResponseEntity<?> createTask(@RequestBody Task task, Principal principal) {
        // 인증이 안된 상태라면 에러 반환
        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("인증이 필요합니다.");
        }

        // principal.getName()은 JWT 생성 시 subject로 설정한 값이어야 합니다.
        String userId = principal.getName();

        // 만약 taskService.createTask 메서드가 userId도 받아서 작업에 할당하도록 구현되어 있다면:
        Task createdTask = taskService.createTask(task, userId);

        return ResponseEntity.ok(createdTask);
    }

    // 단일 Task 조회
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("해당 Task가 존재하지 않습니다."));
        return ResponseEntity.ok(task);
    }

    // "내 작업" 조회: 로그인한 사용자의 작업만 조회
    @GetMapping("/my-tasks")
    public ResponseEntity<?> getMyTasks(Principal principal) {
        UserInfo user = userRepository.findByUserId(principal.getName())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        List<Task> myTasks = taskRepository.findByUser_UserId(user.getUserId());
        return ResponseEntity.ok(myTasks);
    }

    // 수정 Task
    @PutMapping("/{taskId}")
    public ResponseEntity<Task> updateTask(@PathVariable Long taskId, @RequestBody Task updatedTask) {
        Task task = taskService.updateTask(taskId, updatedTask);
        return ResponseEntity.ok(task);
    }

    // 단일 Task 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok("작업 " + id + " 삭제.");
    }

    // 다중 Task 삭제
    @DeleteMapping("")
    public ResponseEntity<?> deleteTasks(@RequestBody List<Long> ids) {
        for (Long id : ids) {
            taskService.deleteTask(id);
        }
        return ResponseEntity.ok("작업들 삭제 : " + ids);
    }
}