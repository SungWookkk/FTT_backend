package ftt_backend.model;

import ftt_backend.model.Badge;
import ftt_backend.model.UserInfo;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "user_badge")
@Data
public class UserBadge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_info 테이블과 N:1 관계
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserInfo user; // UserInfo 엔티티

    // badge 테이블과 N:1 관계
    @ManyToOne
    @JoinColumn(name = "badge_id", nullable = false)
    private Badge badge;

    // 언제 획득했는지 기록
    @Column(name = "acquired_date")
    private LocalDate acquiredDate;

    // 뱃지 활성/비활성 여부, 등등 필요한 컬럼 추가 가능
    // @Column(name = "active")
    // private boolean active;
}
