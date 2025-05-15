package ftt_backend.social;

import ftt_backend.model.UserBadge;
import ftt_backend.model.UserInfo;
import ftt_backend.service.UserService;
import ftt_backend.config.JwtUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * Google OAuth2 전용 컨트롤러
 */
@Controller
public class GoogleController {

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    private final JwtUtils    jwtUtils;
    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    public GoogleController(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils    = jwtUtils;
        this.userService = userService;
    }

    @GetMapping("/oauth2/authorize/google")
    public RedirectView authorizeGoogle() {
        String authUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id="    + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=openid%20profile%20email";
        return new RedirectView(authUrl);
    }

    /**
     * 1) code→access_token
     * 2) access_token→프로필
     * 3) processOAuth2User → UserInfo 저장/조회
     * 4) JWT 발급 → HttpOnly 쿠키로 세팅
     * 5) /dashboard 로 리다이렉트
     */
    @GetMapping("/login/oauth2/code/google")
    public RedirectView handleGoogleCallback(
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws Exception {
        // --- 1) access_token 교환 ---
        MultiValueMap<String, String> tokenReq = new LinkedMultiValueMap<>();
        tokenReq.add("code", code);
        tokenReq.add("client_id", clientId);
        tokenReq.add("client_secret", clientSecret);
        tokenReq.add("redirect_uri", redirectUri);
        tokenReq.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> tokenEntity =
                new HttpEntity<>(tokenReq, headers);

        Map<String, Object> tokenRes = restTemplate
                .postForObject("https://oauth2.googleapis.com/token", tokenEntity, Map.class);
        String accessToken = tokenRes.get("access_token").toString();

        // --- 2) 프로필 조회 ---
        HttpHeaders profileHeaders = new HttpHeaders();
        profileHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> profileEntity = new HttpEntity<>(profileHeaders);
        Map<String, Object> profile = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v3/userinfo",
                HttpMethod.GET,
                profileEntity,
                Map.class
        ).getBody();

        // --- 3) DB 저장/조회 & ---
        String email = (String) profile.get("email");
        String name  = (String) profile.get("name");
        String pic   = (String) profile.get("picture");
        String googleId   = (String) profile.get("sub");  // 구글 user-id

        UserInfo user = userService.processOAuth2User(
                email,
                name,
                pic,
                "google",      // provider
                googleId       // providerId
        );

        // 4) JWT 발급
        String jwt = jwtUtils.generateToken(user.getUserId());

        // 5) 첫 번째 뱃지를 활성 뱃지로 판단
        List<UserBadge> badges = userService.getUserBadges(user.getId());
        String activeBadgeId = badges.isEmpty()
                ? ""
                : badges.get(0).getBadge().getId().toString();

        // 6) 프론트엔드 리디렉트 (토큰, 사용자 정보, 프로필 이미지, 활성 뱃지)
        String redirectUrl = "http://localhost:8080/oauth2/redirect"
                + "?token="        + URLEncoder.encode(jwt, StandardCharsets.UTF_8)
                + "&userName="     + URLEncoder.encode(user.getUsername(), StandardCharsets.UTF_8)
                + "&userId="       + URLEncoder.encode(user.getId().toString(), StandardCharsets.UTF_8)
                + "&userRole="     + URLEncoder.encode(user.getRole(), StandardCharsets.UTF_8)
                + "&profileImage=" + URLEncoder.encode(user.getProfile_image(), StandardCharsets.UTF_8)
                + "&activeBadge="  + URLEncoder.encode(activeBadgeId, StandardCharsets.UTF_8);

        return new RedirectView(redirectUrl);
    }
}
