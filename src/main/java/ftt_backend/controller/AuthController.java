/*
 * 회원가입 및 로그인 요청을 처리하는 컨트롤러
 */
package ftt_backend.controller;

import ftt_backend.model.UserInfo;
import ftt_backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody UserInfo userInfo) {
        userService.saveUser(userInfo);
        return ResponseEntity.ok("User registered successfully!");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody UserInfo loginRequest) {
        String token = userService.authenticate(loginRequest.getUserId(), loginRequest.getPassword());
        return ResponseEntity.ok(token);
    }
}
