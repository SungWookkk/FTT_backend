/*
 * 사용자 정보를 저장하기 위한 엔티티 클래스
 */
package ftt_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_info")
@Data
@Getter
@Setter
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // 기본 키
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false, length = 50)
    private String userId; // 로그인에 사용하는 사용자 ID

    @Column(name = "username", nullable = false, length = 50)
    private String username; // 사용자 닉네임

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email; // 이메일 주소

    @Column(name = "phone_number", unique = true, nullable = false, length = 15)
    private String phoneNumber; // 핸드폰 번호

    @Column(name = "birth_date", nullable = false, length = 10)
    private String birthDate; // 생년월일 (형식: yyyy-MM-dd)

    @Column(name = "password", nullable = false)
    private String password; // 비밀번호 (암호화된 값 저장)

    @Column(name = "role", nullable = false, length = 20)
    private String role; // 사용자의 권한 (예: USER, ADMIN)

    //프사
    @Column(name = "profile_image", length = 200)
    private String profile_image;

    //한 줄 자기소개
    @Column(name = "introduction", length = 200)
    private String introduction;

    //소개
    @Column(name = "description", length = 500)
    private String description;

}
