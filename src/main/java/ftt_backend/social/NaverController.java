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

@Controller
public class NaverController {

    @Value("${spring.security.oauth2.client.registration.naver.client-id}")
    private String clientId;
    @Value("${spring.security.oauth2.client.registration.naver.client-secret}")
    private String clientSecret;
    @Value("${spring.security.oauth2.client.registration.naver.redirect-uri}")
    private String redirectUri;

    private final JwtUtils    jwtUtils;
    private final UserService userService;
    private final RestTemplate restTemplate = new RestTemplate();

    public NaverController(JwtUtils jwtUtils, UserService userService) {
        this.jwtUtils    = jwtUtils;
        this.userService = userService;
    }

    @GetMapping("/oauth2/authorize/naver")
    public RedirectView authorizeNaver() {
        String authUrl = "https://nid.naver.com/oauth2.0/authorize"
                + "?client_id="    + clientId
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code";
        return new RedirectView(authUrl);
    }

    @GetMapping("/login/oauth2/code/naver")
    public RedirectView handleNaverCallback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            HttpServletResponse response
    ) throws Exception {
        MultiValueMap<String, String> tokenReq = new LinkedMultiValueMap<>();
        tokenReq.add("grant_type",    "authorization_code");
        tokenReq.add("client_id",     clientId);
        tokenReq.add("client_secret", clientSecret);
        tokenReq.add("code",          code);
        tokenReq.add("state",         state);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> tokenEntity =
                new HttpEntity<>(tokenReq, headers);

        Map<String, Object> tokenRes = restTemplate.postForObject(
                "https://nid.naver.com/oauth2.0/token", tokenEntity, Map.class
        );
        String accessToken = (String) tokenRes.get("access_token");

        // 2) Profile 조회
        HttpHeaders profileHeaders = new HttpHeaders();
        profileHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> profileEntity = new HttpEntity<>(profileHeaders);
        Map<String, Object> profileResponse = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                profileEntity,
                Map.class
        ).getBody();

        @SuppressWarnings("unchecked")
        Map<String, Object> resp = (Map<String, Object>) profileResponse.get("response");
        if (resp == null) {
            throw new IllegalStateException("네이버 프로필 응답에 response 필드가 없습니다: " + profileResponse);
        }
        String email = (String) resp.get("email");
        String name  = (String) resp.get("name");
        String pic   = (String) resp.get("profile_image");
        String naverId   = resp.get("id").toString();   // 네이버 user-id

        UserInfo user = userService.processOAuth2User(
                email,
                name,
                pic,
                "naver",       // provider
                naverId        // providerId
        );
        // 4) JWT 발급
        String jwt = jwtUtils.generateToken(user.getUserId());

        // 5) 활성 뱃지 결정
        List<UserBadge> badges = userService.getUserBadges(user.getId());
        String activeBadgeId = badges.isEmpty()
                ? ""
                : badges.get(0).getBadge().getId().toString();

        // 6) 리디렉트
        String redirectUrl = "http://localhost:8080/oauth2/redirect"
                + "?token="        + URLEncoder.encode(jwt,    StandardCharsets.UTF_8)
                + "&userName="     + URLEncoder.encode(user.getUsername(),  StandardCharsets.UTF_8)
                + "&userId="       + URLEncoder.encode(user.getId().toString(),StandardCharsets.UTF_8)
                + "&userRole="     + URLEncoder.encode(user.getRole(),      StandardCharsets.UTF_8)
                + "&profileImage=" + URLEncoder.encode(user.getProfile_image(), StandardCharsets.UTF_8)
                + "&activeBadge="  + URLEncoder.encode(activeBadgeId,        StandardCharsets.UTF_8);

        return new RedirectView(redirectUrl);
    }
}
