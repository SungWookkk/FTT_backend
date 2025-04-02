package ftt_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "team_task")
@Data
@Getter
@Setter
public class TeamTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팀 작업 제목: 최대 100자, null 불가
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    // 팀 작업 설명 (HTML 등 긴 내용도 가능하도록 TEXT 타입)
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 시작일
    @Column(name = "start_date")
    private LocalDate startDate;

    // 마감일
    @Column(name = "due_date")
    private LocalDate dueDate;

    // 작업 생성 시각 (날짜만 기록)
    @Column(name = "created_at")
    private LocalDate createdAt;

    // 우선순위 (예: "낮음", "보통", "중요")
    @Column(name = "priority", length = 20)
    private String priority;

    // 작업 상태 (예: "진행중", "완료", "대기")
    @Column(name = "status", length = 20)
    private String status;

    // 메모 (추가 설명 등)
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;

    /**
     * 팀 작업 파일들 (예: 첨부 파일 정보)
     * 하나의 TeamTask가 여러 TeamTaskFile을 가질 수 있음.
     */
    @OneToMany(mappedBy = "teamTask", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"teamTask"})
    private List<TeamTaskFile> files = new ArrayList<>();

    /**
     * 해당 작업이 속한 팀
     * 하나의 팀(Team)은 여러 개의 팀 작업을 가질 수 있음.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
}
