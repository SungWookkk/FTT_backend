package ftt_backend.service;

import ftt_backend.model.Badge;
import ftt_backend.model.UserBadge;
import ftt_backend.model.UserInfo;
import ftt_backend.config.JwtUtils;
import ftt_backend.model.dto.SignupRequest;
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

    // ----------------------------------------------------
    // SignupRequest 받아서 검증 후 회원가입 처리
    // ----------------------------------------------------
    public UserInfo registerUser(SignupRequest req) {
        // 1) userId 중복 검사 (String 그대로 비교)
        if (userRepository.existsByUserId(req.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }

        // 2) 전화번호 중복 검사
        if (userRepository.existsByPhoneNumber(req.getPhoneNumber())) {
            throw new IllegalArgumentException("이미 등록된 휴대폰 번호입니다.");
        }

        // 3) 생년월일 만 7세 이상 체크
        if (req.getBirthDate().plusYears(7).isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("만 7세 미만은 가입할 수 없습니다.");
        }

        // 4) DTO → 엔티티 매핑
        UserInfo u = new UserInfo();
        u.setUserId(req.getUserId());
        u.setUsername(req.getUsername());
        u.setEmail(req.getEmail());
        u.setPhoneNumber(req.getPhoneNumber());
        u.setBirthDate(req.getBirthDate().toString());
        u.setPassword(req.getPassword());      // 암호화는 createUser()에서
        u.setSmsOptIn(req.getSmsOptIn());
        u.setProvider("LOCAL");
        u.setProviderId(req.getUserId());

        // 5) 암호화 + 기본 ROLE, 기본 뱃지 부여
        return createUser(u);
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
     * (provider + providerId 로 유니크하게 구분)
     *
     * @param email       제공된 이메일 (optional)
     * @param name        사용자 이름
     * @param pictureUrl  프로필 이미지 URL
     * @param provider    "google", "naver", "kakao" 등
     * @param providerId  소셜 프로바이더가 준 고유 ID
     */
    public UserInfo processOAuth2User(
            String email,
            String name,
            String pictureUrl,
            String provider,
            String providerId
    ) {
        // 1) provider+providerId 로 기존 사용자 조회
        Optional<UserInfo> existing =
                userRepository.findByProviderAndProviderId(provider, providerId);

        UserInfo user = existing.orElseGet(() -> {
            // 신규 사용자 생성
            UserInfo newUser = new UserInfo();
            newUser.setUserId(provider + "_" + providerId);  // 예: "kakao_123456789"
            newUser.setProvider(provider);
            newUser.setProviderId(providerId);

            // 이메일 동의 안 하면 null 허용
            newUser.setEmail(email);

            newUser.setUsername(name);
            newUser.setProfile_image(pictureUrl);
            newUser.setPhoneNumber(null);
            newUser.setBirthDate("");
            newUser.setPassword(
                    passwordEncoder.encode(UUID.randomUUID().toString())
            );
            newUser.setRole("USER");
            newUser.setSmsOptIn(false);

            // 기본 뱃지 부여 포함(createUser 내부 로직 재사용)
            return createUser(newUser);
        });

        // 2) 기존 사용자라면 프로필 정보만 업데이트
        user.setUsername(name);
        user.setProfile_image(pictureUrl);
        return userRepository.save(user);
    }
}
