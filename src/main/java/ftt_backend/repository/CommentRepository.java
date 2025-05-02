package ftt_backend.repository;

import ftt_backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // post_id 기준으로 정렬하여 댓글 목록 조회
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    // 게시글별 최상위 댓글만 가져오기 (자식은 fetch join 으로 같이)
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.replies WHERE c.post.id = :postId AND c.parent IS NULL ORDER BY c.createdAt ASC")
    List<Comment> findRootsWithReplies(Long postId);
    // 내가 쓴 댓글 조회 (최신순)
    List<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
