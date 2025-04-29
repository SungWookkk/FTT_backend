package ftt_backend.controller;

import ftt_backend.model.CommunityPost;
import ftt_backend.service.CommunityPostService;
import ftt_backend.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community/posts")
public class CommunityPostController {

    @Autowired
    private CommunityPostService postService;

    @Autowired
    private LikeService likeService;
    /** 전체 게시글 조회 */
    @GetMapping
    public ResponseEntity<List<CommunityPost>> listPosts(
            @RequestParam(required = false) String category
    ) {
        List<CommunityPost> all = postService.getAllPosts(category);
        return ResponseEntity.ok(all);
    }

    /** 단일 게시글 조회 (조회수 1 증가) */
    @GetMapping("/{id}")
    public ResponseEntity<CommunityPost> getPost(@PathVariable Long id) {
        return postService.getPostById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 새 게시글 생성
     * - 요청 헤더에 X-User-Id: 작성자 userId
     * - 요청 바디에 CommunityPost(JSON)
     */
    @PostMapping
    public ResponseEntity<CommunityPost> createPost(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CommunityPost post
    ) {
        CommunityPost created = postService.createPost(post, userId);
        return ResponseEntity
                .created(URI.create("/api/community/posts/" + created.getId()))
                .body(created);
    }

    /** 게시글 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<CommunityPost> updatePost(
            @PathVariable Long id,
            @RequestBody CommunityPost post
    ) {
        CommunityPost updated = postService.updatePost(id, post);
        return ResponseEntity.ok(updated);
    }

    /** 게시글 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }
    /** 좋아요 토글 */
    @PostMapping("/{id}/like")
    public ResponseEntity<?> toggleLike(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        LikeService.ToggleResult result = likeService.toggleLike(id, userId);
        return ResponseEntity.ok(Map.of(
                "likesCount", result.likesCount,
                "liked", result.liked
        ));
    }

}
