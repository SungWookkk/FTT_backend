package ftt_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "team_reading_list_item")
@Data
@Getter
@Setter
public class TeamReadingListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 읽기 자료의 카테고리 (예: AWS, BackEnd 등)
    @Column(name = "category", nullable = false, length = 100)
    private String category;

    // 읽기 자료 제목
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // 읽기 자료 링크
    @Column(name = "link", nullable = false, length = 500)
    private String link;

    // 이 읽기 자료가 속한 팀 (Team 엔티티와 다대일 관계)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;
}
