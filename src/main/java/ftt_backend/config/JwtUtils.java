package ftt_backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

    private final SecretKey SECRET_KEY;

    // .env 파일에서 jwt.secret 값을 로드하고 SecretKey 객체로 변환
    public JwtUtils() {
        Dotenv dotenv = Dotenv.configure().load();
        String secret = dotenv.get("JWT_SECRET");

        if (secret == null || secret.length() < 32) {
            throw new IllegalArgumentException("Secret key must be at least 32 characters.");
        }

        // Secret Key를 생성
        this.SECRET_KEY = Keys.hmacShaKeyFor(secret.getBytes());
    }

    // JWT 토큰 생성
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 1일 유효
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    // JWT 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 인증 정보 추출
    public Authentication getAuthentication(String token) {
        String username = Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
        return new UsernamePasswordAuthenticationToken(username, null, List.of());
    }
}
