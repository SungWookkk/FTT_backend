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
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{userId}")
    public ResponseEntity<UserInfo> getUserInfo(@PathVariable Long userId) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다"));
        return ResponseEntity.ok(user);
    }

    // 프로필 사진 업로드
    @PostMapping("/{userId}/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @PathVariable Long userId,
            @RequestParam("file") MultipartFile file
    ) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다"));

        try {
            String originalFilename = file.getOriginalFilename();

            // 절대 경로(실제 파일 저장 위치)
            String uploadDirPath = System.getProperty("user.dir") + File.separator + "uploads";
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // 실제 파일 저장
            String absoluteFilePath = uploadDirPath + File.separator + userId + "_" + originalFilename;
            file.transferTo(new File(absoluteFilePath));

            // **DB**에는 "상대 경로"만 저장 → "/uploads/2_git.png" 형태
            String relativePath = "/uploads/" + userId + "_" + originalFilename;
            user.setProfile_image(relativePath);
            userRepository.save(user);

            return ResponseEntity.ok(user);
            // 업로드 후 user 객체(또는 profile_image만) 반환
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("파일 업로드 실패");
        }
    }
    @PatchMapping("/{userId}/profile")
    public ResponseEntity<?> updateProfile(
            @PathVariable Long userId,
            @RequestBody Map<String, String> updates
    ) {
        UserInfo user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 본인만 수정 가능하도록 검증 로직(토큰에서 userId 비교) 등 추가 가능

        // 필드가 존재하면 업데이트
        if (updates.containsKey("introduction")) {
            user.setIntroduction(updates.get("introduction"));
        }
        if (updates.containsKey("description")) {
            user.setDescription(updates.get("description"));
        }

        userRepository.save(user);
        return ResponseEntity.ok(user); // 갱신된 user 정보 반환
    }
    // 새로 추가: username으로 사용자 조회
    @GetMapping("/by-username/{username}")
    public ResponseEntity<UserInfo> getUserByUsername(@PathVariable String username) {
        UserInfo user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("해당 username을 가진 사용자를 찾을 수 없습니다."));
        return ResponseEntity.ok(user);
    }
}
