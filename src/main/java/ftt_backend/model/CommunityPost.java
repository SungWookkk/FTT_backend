package ftt_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "community_post",
        indexes = {
                @Index(name = "idx_cp_author", columnList = "author_id"),
                @Index(name = "idx_cp_created_at", columnList = "created_at")
        }
)
@Data
@Getter
@Setter
public class CommunityPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** 작성자 (UserInfo.id 와 매핑) */
    @Column(name = "author_id", nullable = false)
    private Long authorId;

    /** 게시글 제목 */
    @Column(name = "title", nullable = false, length = 255)
    private String title;

    /** 게시글 본문 */
    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    /** 게시 상태 (예: ACTIVE, DELETED) */
    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE";

    /** 조회 수 */
    @Column(name = "views_count", nullable = false)
    private Integer viewsCount = 0;

    /** 좋아요 수 */
    @Column(name = "likes_count", nullable = false)
    private Integer likesCount = 0;

    /** 댓글 수 */
    @Column(name = "comments_count", nullable = false)
    private Integer commentsCount = 0;

    /** 생성 일시 */
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /** 수정 일시 */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 아래 두 필드는 DB 컬럼이 아니므로 @Transient
    @Transient
    private String authorName;

    @Transient
    private String authorProfileImage;
}
