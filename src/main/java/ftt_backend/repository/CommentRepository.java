package ftt_backend.repository;

import ftt_backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // post_id 기준으로 정렬하여 댓글 목록 조회
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
}
