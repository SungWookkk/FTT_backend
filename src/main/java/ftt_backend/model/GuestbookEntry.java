package ftt_backend.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "guestbook_entry")
@Data
@NoArgsConstructor
@Getter
@Setter
public class GuestbookEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 방명록 주인(프로필 소유자)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private UserInfo owner;

    // 글을 작성한 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private UserInfo writer;

    // 방명록 글 내용
    @Column(name = "content", nullable = false, length = 500)
    private String content;

    // 작성 시간
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // 프로필 주인이 남기는 댓글 (1회 한정)
    @Column(name = "host_comment", length = 500)
    private String hostComment;

    // 주인 댓글 작성 시간
    @Column(name = "host_comment_created_at")
    private LocalDateTime hostCommentCreatedAt;

    // 비밀 글 여부
    @Column(name = "secret", nullable = false)
    private boolean secret = false;
}
