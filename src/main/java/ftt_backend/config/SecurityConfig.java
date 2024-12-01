/*
* Security 관련 설정 세팅
* */

package ftt_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/**") // 보안 적용 경로 설정
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // 인증 없이 접근 허용
                        .anyRequest().authenticated() // 그 외 요청은 인증 필요
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/auth/**")) // 특정 경로에 대해 CSRF 비활성화
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives("default-src 'self'")) // CSP 설정
                        .frameOptions(frame -> frame.sameOrigin()) // 동일 출처에서 iframe 허용
                );
        return http.build();
    }
}
