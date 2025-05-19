package ftt_backend.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor  // Jackson용 기본 생성자
@AllArgsConstructor // 필요 시 편하게 인스턴스 생성용
public class SignupRequest {

    @NotBlank
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "영문자와 숫자만 허용됩니다.")
    private String userId;

    @NotBlank
    @Size(min = 3, max = 20)
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "영문자와 숫자만 허용됩니다.")
    private String username;

    @NotBlank
    @Email(message = "올바른 이메일 형식이어야 합니다.")
    private String email;

    @NotBlank
    @Pattern(regexp = "^\\d{10,11}$", message = "- 없이 10~11자리 숫자만 입력하세요.")
    private String phoneNumber;

    @NotNull
    @Past(message = "과거 날짜여야 합니다.")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    @NotBlank
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Pattern(
            regexp = ".*[!@#$%^&*(),.?\":{}|<>].*",
            message = "특수문자를 최소 1개 포함해야 합니다."
    )
    private String password;

    // SMS 수신 동의는 반드시 체크하지 않아도 되므로 NotNull 필요없음
    private Boolean smsOptIn = false;
}
