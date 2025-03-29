package ftt_backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "badge")
@Data
public class Badge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_name", nullable = false, length = 50)
    private String badgeName;

    @Column(name = "description", length = 200)
    private String description;

    @Column(name = "icon_path", length = 200)
    private String iconPath;

    // 완료 횟수 조건
    @Column(name = "completion_threshold")
    private Integer completionThreshold;
}
