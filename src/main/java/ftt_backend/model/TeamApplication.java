// TeamApplication.java
package ftt_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_application")
@Getter
@Setter
public class TeamApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팀에 신청한 사용자 정보 (UserInfo와 ManyToOne 관계)
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo applicant;

    @Column(name = "applicant_username")
    private String applicantUsername;  // 실제 UserInfo의 username 저장

    // 신청한 팀 (Team과 ManyToOne 관계)
    @ManyToOne
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    private String reason;    // 신청하는 이유
    private String goal;      // 목표

    // 신청 상태 (예: PENDING, APPROVED, REJECTED)
    private String status;

    // 신청 일자, 업데이트 일자
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    //  기본 상태 및 생성 일자 설정
    public TeamApplication() {
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }
}
