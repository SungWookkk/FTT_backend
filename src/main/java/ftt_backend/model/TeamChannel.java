package ftt_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "team_channel")
@Data
@Getter
@Setter
public class TeamChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채널 이름 (필수)
    @Column(name = "channel_name", nullable = false, length = 100)
    private String channelName;

    // 채널 설명 (옵션)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 채널 생성 시각
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // 마지막 수정 시각
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 해당 채널이 속한 팀 (Team 엔티티와 ManyToOne 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // 채널 생성자(작성자) - UserInfo 엔티티와 ManyToOne 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private UserInfo createdBy;
}
