package ftt_backend.service;

import ftt_backend.model.Badge;
import ftt_backend.model.UserBadge;
import ftt_backend.model.UserInfo;
import ftt_backend.config.JwtUtils;
import ftt_backend.repository.BadgeRepository;
import ftt_backend.repository.BadgeUserRepository;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeRepository badgeRepository;

    @Autowired
    private BadgeUserRepository badgeUserRepository;

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

    // 계정 생성시 뱃지 기본 단계 적용
    public UserInfo createUser(UserInfo userInfo) {
        // 비밀번호 암호화 & 기본 Role 설정
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        userInfo.setRole("USER");

        // 1) 사용자 정보 저장
        UserInfo savedUser = userRepository.save(userInfo);

        // 2) 기본 뱃지 (Badge_01) 찾기
        Badge defaultBadge = badgeRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("기본 뱃지를 찾을 수 없습니다."));

        // 3) UserBadge 엔티티 생성 & 저장
        UserBadge userBadge = new UserBadge();
        userBadge.setUser(savedUser);
        userBadge.setBadge(defaultBadge);
        userBadge.setAcquiredDate(LocalDate.now());
        badgeUserRepository.save(userBadge);

        return savedUser;
    }

    public List<UserBadge> getUserBadges(Long userId) {
        // 유저 ID로 해당 사용자의 모든 뱃지 조회
        return badgeUserRepository.findByUserId(userId);
    }
    public UserInfo findByUserId(String userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

}
