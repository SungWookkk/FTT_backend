package ftt_backend.service;

import ftt_backend.model.CommunityPost;
import ftt_backend.model.PostLike;
import ftt_backend.repository.CommunityPostRepository;
import ftt_backend.repository.PostLikeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class LikeService {
    @Autowired private PostLikeRepository likeRepo;
    @Autowired private CommunityPostRepository postRepo;

    /**
     * 토글: 없으면 좋아요, 있으면 취소
     * @return 배열 [새로운 좋아요 수, 현재 좋아요 상태(true=좋아요됨)]
     */
    @Transactional
    public ToggleResult toggleLike(Long postId, Long userId) {
        CommunityPost post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다: "+postId));

        boolean nowLiked;
        if (likeRepo.findByPostIdAndUserId(postId, userId).isPresent()) {
            likeRepo.deleteByPostIdAndUserId(postId, userId);
            post.setLikesCount(post.getLikesCount() - 1);
            nowLiked = false;
        } else {
            PostLike pl = new PostLike();
            pl.setPost(post);
            pl.setUserId(userId);
            likeRepo.save(pl);
            post.setLikesCount(post.getLikesCount() + 1);
            nowLiked = true;
        }
        postRepo.save(post);
        return new ToggleResult(post.getLikesCount(), nowLiked);
    }

    public static class ToggleResult {
        public final int likesCount;
        public final boolean liked;
        public ToggleResult(int likesCount, boolean liked) {
            this.likesCount = likesCount;
            this.liked = liked;
        }
    }
    @Transactional(readOnly = true)
    public List<CommunityPost> getLikedPostsByUser(Long userId) {
        return likeRepo.findLikedPostsByUserId(userId);
    }
}
