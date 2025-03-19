/*
 * Task 엔티티 클래스
 * 데이터베이스의 'task' 테이블과 매핑 할 일 정보를 저장하는 역할
 */
package ftt_backend.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "task")
@Data
@Getter
@Setter
public class Task {

    // 기본 키로 id 컬럼을 사용하 자동 증가
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 작업(할 일) 제목. 최대 100자까지 가능하며 null 불가
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    // 작업(할 일) 설명. TEXT 타입으로 길이가 긴 HTML 내용도 저장 가능
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // 작업 마감일 LocalDateTime으로 관리
    @Column(name = "due_date")
    private LocalDate dueDate;

    // 작업 생성 시각
    @Column(name = "created_at")
    private LocalDate createdAt;

    // 작업 우선순위 (예: "낮음", "보통", "중요" 등). 최대 20자
    @Column(name = "priority", length = 20)
    private String priority;

    // 작업 상태
    @Column(name = "status", length = 20)
    private String status;

    // 담당자(assignee) 이름.
    @Column(name = "assignee", length = 50)
    private String assignee;

    // TEXT 타입으로 충분한 길이의 문자열 저장 가능
    @Column(name = "memo", columnDefinition = "TEXT")
    private String memo;


    // TaskFile과 1:N 매핑
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonIgnoreProperties(value = {"task"})
    // ↑ TaskFile에서 'task' 필드는 무시 > 무한 루프 방지
    private List<TaskFile> files = new ArrayList<>();

    // 작업 생성자를 나타내는 사용자와의 다대일 관계
    // 하나의 UserInfo가 여러 Task를 가질 수 있으므로 ManyToOne 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @Getter
    @Setter
    private UserInfo user;

    @Transient               // DB 컬럼이 아님
    private String userId;

}
