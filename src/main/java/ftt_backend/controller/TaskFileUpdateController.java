package ftt_backend.controller;

import ftt_backend.model.TaskFile;
import ftt_backend.service.TaskFileUpdateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * - 파일 업로드와 다운로드 관련 엔드포인트를 제공
 * - 파일은 Multipart 형식으로 업로드 서버 로컬 저장 / 메타데이터는 DB에 저장
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskFileUpdateController {

    @Autowired
    private TaskFileUpdateService taskFileUpdateService;

    /**
     * 파일 업로드 엔드포인트
     * @param taskId 파일을 첨부할 Task의 ID
     * @param file 업로드할 MultipartFile
     * @return 저장된 TaskFile 엔티티 반환
     */
    @PostMapping("/{taskId}/files")
    public ResponseEntity<TaskFile> uploadFile(
            @PathVariable Long taskId,
            @RequestParam("file") MultipartFile file
    ) throws IOException {
        TaskFile savedFile = taskFileUpdateService.uploadFile(taskId, file);
        return ResponseEntity.ok(savedFile);
    }

    /**
     * 파일 다운로드 엔드포인트
     * DB에서 해당 파일의 경로를 조회 후 파일을 읽어 HTTP 응답 스트림으로 전송
     * Content-Disposition 헤더에 원본 파일명을 포함시켜 다운로드 시 사용자가 원본 파일명으로 받을 수 있게함
     *
     * @param taskId Task의 ID
     * @param fileId 다운로드할 파일의 ID
     * @return 파일 스트림과 함께 HTTP 응답 반환
     */
    @GetMapping("/{taskId}/files/{fileId}")
    public ResponseEntity<InputStreamResource> downloadFile(
            @PathVariable Long taskId,
            @PathVariable Long fileId
    ) throws IOException {
        Path filePath = taskFileUpdateService.getFilePath(fileId);
        String mimeType = Files.probeContentType(filePath);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }

        TaskFile taskFile = taskFileUpdateService.getTaskFile(fileId);
        InputStreamResource resource = new InputStreamResource(Files.newInputStream(filePath));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + taskFile.getOriginalFilename() + "\"")
                .contentType(MediaType.parseMediaType(mimeType))
                .body(resource);
    }
}
