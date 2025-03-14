package ftt_backend.service;

import ftt_backend.model.Task;
import ftt_backend.model.TaskFile;
import ftt_backend.repository.TaskFileUpdateRepository;
import ftt_backend.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * - 파일 업로드 및 다운로드 관련 비즈니스 로직 처리
 * - 파일은 서버의 로컬 디렉토리 (/uploads/tasks/)에 저장 파일  DB에 저장
 */
@Service
public class TaskFileUpdateService {

    // 파일이 저장될 로컬 디렉토리
    private static final String UPLOAD_DIR = "uploads/tasks/";

    @Autowired
    private TaskFileUpdateRepository taskFileUpdateRepository;

    @Autowired
    private TaskRepository taskRepository;

    /**
     * 파일 업로드 처리 메서드
     * @param taskId 파일이 첨부될 Task의 ID
     * @param file 업로드할 MultipartFile
     * @return 저장된 TaskFile 엔티티
     * @throws IOException 파일 저장 중 오류 발생 시 예외 발생
     */
    public TaskFile uploadFile(Long taskId, MultipartFile file) throws IOException {
        // Task 조회: 해당 taskId가 없으면 예외 발생
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task를 찾을수 없습니다"));

        // 업로드 디렉토리가 존재하지 않으면 생성
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 원본 파일명
        String originalFilename = file.getOriginalFilename();

        // 충돌 방지를 위해 UUID를 사용하여 저장할 파일명 생성
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String storedFilename = UUID.randomUUID().toString() + fileExtension;

        // 파일 저장 경로 설정
        Path filePath = uploadPath.resolve(storedFilename);

        // 파일 저장 (파일의 바이트 배열 기록)
        Files.write(filePath, file.getBytes(), StandardOpenOption.CREATE);

        // TaskFile 엔티티 생성 및 저장
        TaskFile taskFile = new TaskFile();
        taskFile.setOriginalFilename(originalFilename);
        taskFile.setStoredFilename(storedFilename);
        taskFile.setFilePath(filePath.toString());
        taskFile.setFileSize(file.getSize());
        taskFile.setUploadTime(LocalDateTime.now());
        taskFile.setTask(task);

        return taskFileUpdateRepository.save(taskFile);
    }

    /**
     * 파일 경로 조회 메서드
     * @param fileId 다운로드할 파일의 ID
     * @return 파일 경로(Path)
     */
    public Path getFilePath(Long fileId) throws IOException {
        TaskFile taskFile = getTaskFile(fileId);
        Path path = Paths.get(taskFile.getFilePath());
        if (!Files.exists(path)) {
            throw new IOException("File not found on disk");
        }
        return path;
    }

    /**
     * 파일 ID로 TaskFile 엔티티를 조회하는 메서드
     * @param fileId 조회할 파일의 ID
     * @return TaskFile 엔티티
     */
    public TaskFile getTaskFile(Long fileId) {
        return taskFileUpdateRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));
    }
}
