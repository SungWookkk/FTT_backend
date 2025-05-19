package ftt_backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 로그인 혹은 데모 로그인 응답으로
 * 클라이언트에 돌려줄 사용자 정보 + JWT 토큰
 */
@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String userName;
    private String userId;
    private String userRole;
    private String profileImage;
    private Object activeBadge;
}
