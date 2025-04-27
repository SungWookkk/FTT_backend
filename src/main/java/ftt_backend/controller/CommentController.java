// ftt_backend.controller.CommentController.java
package ftt_backend.controller;

import ftt_backend.model.Comment;
import ftt_backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/community/posts/{postId}/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    /** 댓글 목록 조회 */
    @GetMapping
    public ResponseEntity<List<Comment>> listComments(@PathVariable Long postId) {
        List<Comment> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 작성
     * - RequestBody로 받은 Comment 인스턴스에서는 `content`만 사용
     */
    @PostMapping
    public ResponseEntity<Comment> createComment(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long authorId,
            @RequestBody Comment incoming
    ) {
        Comment created = commentService.createComment(
                postId,
                authorId,
                incoming.getContent()  // entity의 content 필드만 활용
        );
        return ResponseEntity
                .created(URI.create("/api/community/posts/" + postId + "/comments/" + created.getId()))
                .body(created);
    }
}
