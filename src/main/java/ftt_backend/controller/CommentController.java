package ftt_backend.controller;

import ftt_backend.model.Comment;
import ftt_backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/community/posts/{postId}/comments")
public class CommentController {
    @Autowired
    private CommentService commentService;

    /**
     * 1) 댓글 트리 전체 조회
     *    - 최상위 댓글(parent == null)과, 그 하위 replies 필드를 함께 리턴
     */
    @GetMapping
    public ResponseEntity<List<Comment>> listComments(@PathVariable Long postId) {
        List<Comment> tree = commentService.getCommentsTree(postId);
        return ResponseEntity.ok(tree);
    }

    /**
     * 2) 댓글 / 대댓글 작성
     *
     * @param postId   게시글 ID
     * @param authorId X-User-Id 헤더로 전달된 작성자 ID
     * @param parentId (선택) parentId 파라미터를 주면 대댓글, 없으면 최상위 댓글
     */
    @PostMapping
    public ResponseEntity<Comment> createComment(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long authorId,
            @RequestParam(required = false) Long parentId,
            @RequestBody Map<String, String> body
    ) {
        String content = body.get("content");
        Comment created = commentService.addComment(postId, authorId, content, parentId);
        // 응답에는 트리 구조보다는 작성된 단일 Comment 만 보내도 충분합니다.
        return ResponseEntity
                .created(URI.create("/api/community/posts/" + postId + "/comments/" + created.getId()))
                .body(created);
    }
}
