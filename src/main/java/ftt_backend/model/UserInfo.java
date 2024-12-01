/*
* 사용자 정보를 저장하기 위한 엔티티 클래스
* */
package ftt_backend.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "user_info")
@Data
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    private String role;
}
