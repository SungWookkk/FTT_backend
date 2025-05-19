/*
 * 회원가입 및 로그인 요청을 처리하는 컨트롤러
 */
package ftt_backend.controller;

import ftt_backend.config.JwtUtils;
import ftt_backend.model.UserInfo;
import ftt_backend.model.dto.AuthResponse;
import ftt_backend.repository.UserRepository;
import ftt_backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserInfo userInfo) {
        userService.createUser(userInfo);
        return ResponseEntity.ok("회원가입 성공!");
    }


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody UserInfo loginRequest) {
        String token = userService.authenticate(loginRequest.getUserId(), loginRequest.getPassword());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid credentials");
        }

        log.info("Authorization: Bearer {}", token);

        // 여기서 user 정보도 함께 반환
        UserInfo user = userService.findByUserId(loginRequest.getUserId());
        return ResponseEntity.ok(Map.of(
                "token", token,
                "userName", user.getUsername(),
                "userId", user.getId(),
                "userRole", user.getRole()
        ));
    }

    // 로그아웃 엔드포인트 추가
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> logoutRequest) {
        // 엔드포인트는 클라이언트에서 로그아웃 이벤트 발생 시 호출.
        // 측에서 로그아웃 요청이 발생했음을 모니터링하고 디버깅하기 위함.
        System.out.println("로그아웃 요청: 사용자 이름 = "
                + logoutRequest.get("userName") + ", 사용자 ID = "
                + logoutRequest.get("userId"));
        return ResponseEntity.ok("로그아웃 성공!");
    }

    /** demo/demo 계정으로 바로 로그인 */
    @GetMapping("/demo")
    public ResponseEntity<?> demoLogin() {
        UserInfo demo = userRepository.findByUserId("demo")
                .orElseThrow(() -> new RuntimeException("demo 계정이 없습니다"));
        String token = jwtUtils.generateToken(demo.getUserId());
        return ResponseEntity.ok(new AuthResponse(
                token,
                demo.getUsername(),
                String.valueOf(demo.getId()),
                demo.getRole(),
                demo.getProfile_image(),
                null
        ));
    }
}
