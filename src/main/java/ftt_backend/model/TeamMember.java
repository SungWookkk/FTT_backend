package ftt_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="team_memberships")
@Getter
@Setter
@NoArgsConstructor
public class TeamMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="team_id", nullable=false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_info_id", nullable=false)
    private UserInfo user;

    @Enumerated(EnumType.STRING)
    @Column(length=20, nullable=false)
    private TeamRole role;
}