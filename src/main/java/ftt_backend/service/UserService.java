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
    private JwtUtils jwtUtils; // JwtUtils를 사용

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
        System.out.println("userId: " + userId); // 요청된 userId 출력
        UserInfo user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을수 없습니다."));
        System.out.println("User found: " + user); // 검색된 사용자 정보 출력

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // JwtUtils를 사용하여 토큰 생성
        return jwtUtils.generateToken(user.getUserId());
    }
}
