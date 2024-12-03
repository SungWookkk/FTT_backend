/*
 * 회원가입 및 로그인을 처리하는 서비스 클래스
 */
package ftt_backend.service;

import ftt_backend.model.UserInfo;
import ftt_backend.config.JwtUtils;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // 회원가입
    public void saveUser(UserInfo user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER"); // 기본 역할은 USER로 설정
        userRepository.save(user);
    }

    // 로그인
    public String authenticate(String userId, String password) {
        UserInfo user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));
        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtUtils.generateToken(user.getUserId());
        } else {
            throw new RuntimeException("Invalid credentials");
        }
    }
}
