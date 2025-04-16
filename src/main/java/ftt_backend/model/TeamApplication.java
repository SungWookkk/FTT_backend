package ftt_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "team_application")
@Data
@Getter
@Setter
public class TeamApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신청한 팀 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;

    // 신청한 사용자 (applicant)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private UserInfo applicant;

    // 신청 사유
    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    // 목표
    @Column(name = "goal", columnDefinition = "TEXT")
    private String goal;

    // 신청 상태: "PENDING", "APPROVED", "REJECTED"
    @Column(name = "status", length = 20)
    private String status;

    // 신청 일자
    @Column(name = "applied_at")
    private LocalDate appliedAt;


}
