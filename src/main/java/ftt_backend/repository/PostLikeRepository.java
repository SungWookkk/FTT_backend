package ftt_backend.repository;

import ftt_backend.model.CommunityPost;
import ftt_backend.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);
    long countByPostId(Long postId);
    void deleteByPostIdAndUserId(Long postId, Long userId);

    // 수정: PostLike 가 아니라 CommunityPost 리스트 반환
    @Query("SELECT pl.post FROM PostLike pl WHERE pl.userId = :userId")
    List<CommunityPost> findLikedPostsByUserId(@Param("userId") Long userId);
}
