package ftt_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.CascadeType.ALL;
import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(
        name = "comment",
        indexes = {
                @Index(name = "idx_comment_post", columnList = "post_id"),
                @Index(name = "idx_comment_author", columnList = "author_id"),
                @Index(name = "idx_comment_created_at", columnList = "created_at")
        }
)
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 어느 게시글에 달린 댓글인지 매핑 */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post; // CommunityPost와 다대일 매핑

    /** 작성자 (UserInfo.id) */
    @Column(name = "author_id", nullable = false)
    private Long authorId; // 추가

    /** 댓글 내용 */
    @Lob
    @Column(name = "content", nullable = false)
    private String content; // 추가

    /** 생성/수정 시각 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ────────────────────────────────
    /** 상위 댓글 (대댓글이 아니라면 null) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    @JsonBackReference          // 순환 참조 방지
    private Comment parent;

    /** 이 댓글의 하위 대댓글들 */
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference       // replies 직렬화 허용
    private List<Comment> replies = new ArrayList<>();

    // ────────────────────────────────

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() { // 추가
        this.updatedAt = LocalDateTime.now();
    }

    // DB 컬럼이 아닌 필드
    @Transient
    private String authorName;

    @Transient
    private String authorProfileImage;
}
