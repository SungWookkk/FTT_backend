// ftt_backend.service.CommentService.java
package ftt_backend.service;

import ftt_backend.model.Comment;
import ftt_backend.model.CommunityPost;
import ftt_backend.repository.CommentRepository;
import ftt_backend.repository.CommunityPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommunityPostRepository communityPostRepository;

    /**
     * 특정 게시글에 달린 댓글 조회
     */
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
    }

    /**
     * 댓글 생성
     * @param postId   댓글을 달 게시글 ID
     * @param authorId 작성자 UserInfo.id
     * @param content  댓글 본문
     */
    @Transactional
    public Comment createComment(Long postId, Long authorId, String content) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + postId));

        Comment comment = new Comment();
        comment.setPost(post);
        comment.setAuthorId(authorId);
        comment.setContent(content);

        return commentRepository.save(comment);
    }
}
