/**
*  내가 쓴 게시글 / 댓글 / 좋아요를 나타내는 컨트롤러
* */
package ftt_backend.controller;

import ftt_backend.model.Comment;
import ftt_backend.model.CommunityPost;
import ftt_backend.service.CommentService;
import ftt_backend.service.CommunityPostService;
import ftt_backend.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/community/users/{userId}")
public class UserContentController {

    @Autowired
    private CommunityPostService postService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private LikeService likeService;

    /** 내가 쓴 게시글 */
    @GetMapping("/posts")
    public ResponseEntity<List<CommunityPost>> myPosts(
            @PathVariable Long userId
    ) {
        List<CommunityPost> posts = postService.getPostsByAuthor(userId);
        return ResponseEntity.ok(posts);
    }

    /** 내가 쓴 댓글 (flat list) */
    @GetMapping("/comments")
    public ResponseEntity<List<Comment>> myComments(
            @PathVariable Long userId
    ) {
        List<Comment> comments = commentService.getCommentsByAuthor(userId);
        return ResponseEntity.ok(comments);
    }

    /** 내가 좋아요 누른 게시글 */
    @GetMapping("/likes")
    public ResponseEntity<List<CommunityPost>> myLikedPosts(
            @PathVariable Long userId
    ) {
        List<CommunityPost> liked = likeService.getLikedPostsByUser(userId);
        return ResponseEntity.ok(liked);
    }
}
