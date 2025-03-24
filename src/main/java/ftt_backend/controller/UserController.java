package ftt_backend.controller;

import ftt_backend.model.UserInfo;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // 프로필 사진 업로드
    @PostMapping("/{userId}/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file
    ) {
        // 1) 유저 찾기
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            // 2) 파일 저장 (예: 프로젝트 루트의 uploads 폴더를 절대 경로로 지정)
            String originalFilename = file.getOriginalFilename();
            // 현재 작업 디렉토리를 기준으로 uploads 폴더 절대 경로 생성
            String uploadDirPath = System.getProperty("user.dir") + File.separator + "uploads";
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();  // 디렉토리 없으면 생성
            }
            String storePath = uploadDirPath + File.separator + userId + "_" + originalFilename;
            File dest = new File(storePath);
            file.transferTo(dest); // 파일 저장

            // 3) DB에 경로 저장
            user.setProfile_image(storePath);
            userRepository.save(user);

            return ResponseEntity.ok("Profile image uploaded successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("File upload failed");
        }
    }
}

