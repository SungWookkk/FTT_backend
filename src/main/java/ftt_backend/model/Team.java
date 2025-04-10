package ftt_backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "team")
@Data
@Getter
@Setter
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // 기본 키
    private Long id;

    @Column(name = "team_name", nullable = false, length = 100)
    private String teamName; // 팀 이름

    @Column(name = "description", length = 500)
    private String description; // 팀 설명

    @Column(name = "announcement", length = 500)
    private String announcement; // 팀 공지사항

    @Column(name = "category", nullable = false, length = 50)
    private String category; // 팀의 카테고리 주제 (예: 코딩, 취업 등)

    // 팀 생성한 사람의 userId를 저장하여 팀장 권한 부여
    @Column(name = "team_leader", nullable = false, length = 50)
    private String teamLeader;

    /**
     * 팀원들: UserInfo와 다대다 관계
     * team_members 조인 테이블을 통해 연결
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "team_members",
            joinColumns = @JoinColumn(name = "team_id"),
            inverseJoinColumns = @JoinColumn(name = "user_info_id")
    )
    private List<UserInfo> members;

    /**
     * 팀의 할 일 (TodoList)
     * 팀 하나는 여러 개의 할 일을 가질 수 있음
     */

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference  // 순환 참조 끊기: team을 직렬화할 때 tasks를 포함하지만, tasks 내 team은 직렬화되지 않음.
    private List<TeamTask> tasks = new ArrayList<>();
}
