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
import java.util.Optional;
import java.util.UUID;

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
    public UserInfo saveUser(UserInfo user) {
        // 비밀번호가 null 이 아닌 경우에만 인코딩
        if (user.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        user.setRole("USER");
        return userRepository.save(user);
    }
    /** 휴대폰 번호 중복 체크용 래퍼 */
    public boolean existsByPhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }
    /**
     * 휴대전화 번호로 사용자 조회
     * 추가: DataLoader 등에서 중복 체크 후 로드용
     */
    public UserInfo findByPhoneNumber(String phoneNumber) {
        return userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + phoneNumber));
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
    /**
     * OAuth2 로그인 사용자 정보 처리
     * @param email      구글에서 제공된 이메일
     * @param name       구글에서 제공된 사용자 이름
     * @param pictureUrl 구글 프로필 이미지 URL
     * @return 저장되거나 업데이트된 UserInfo
     */
    public UserInfo processOAuth2User(String email, String name, String pictureUrl) {
        // 1) 이메일로 기존 사용자 조회 (없으면 신규 생성)
        Optional<UserInfo> existing = userRepository.findByEmail(email);
        UserInfo user = existing.orElseGet(() -> {
            UserInfo newUser = new UserInfo();
            newUser.setUserId(email);
            newUser.setUsername(name);
            newUser.setEmail(email);
            //소셜 계정엔 phoneNumber 정보가 없으므로 빈 문자열로 설정 (nullable=false 방지)
            newUser.setPhoneNumber("");
            //소셜 계정엔 birthDate 정보가 없으므로 빈 문자열로 설정 (nullable=false 방지)
            newUser.setBirthDate("");
            // OAuth 전용 랜덤 패스워드
            newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            newUser.setRole("USER");
            newUser.setProfile_image(pictureUrl);
            newUser.setSmsOptIn(false);
            // 기본 뱃지 부여 로직 포함
            return createUser(newUser);
        });
        // 2) 기존 사용자라면 프로필 정보만 업데이트
        user.setUsername(name);
        user.setProfile_image(pictureUrl);
        return userRepository.save(user);
    }
}
