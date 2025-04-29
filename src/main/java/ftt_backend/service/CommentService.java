package ftt_backend.service;

import ftt_backend.model.Comment;
import ftt_backend.model.CommunityPost;
import ftt_backend.repository.BadgeUserRepository;
import ftt_backend.repository.CommentRepository;
import ftt_backend.repository.CommunityPostRepository;
import ftt_backend.repository.UserRepository;
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

    // UserRepository 주입
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeUserRepository badgeUserRepository;
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



    /** 트리 형태로 댓글 + 대댓글 전체 조회 */
    @Transactional(readOnly = true)
    public List<Comment> getCommentsTree(Long postId) {
        List<Comment> roots = commentRepository.findRootsWithReplies(postId);
        roots.forEach(this::populateCommentAuthorInfo);
        return roots;
    }
    /** 재귀적으로 댓글과 대댓글에 사용자명·이미지 채우기 */
    private void populateCommentAuthorInfo(Comment c) {
        userRepository.findById(c.getAuthorId()).ifPresent(u -> {
            c.setAuthorName(u.getUsername());
            c.setAuthorProfileImage(u.getProfile_image());
            // 활성 뱃지 조회해서 URL 설정
            badgeUserRepository.findActiveByUserId(u.getId())
                    .ifPresent(ub -> c.setAuthorBadgeUrl( ub.getBadge().getIconPath() ));
        });
        if (c.getReplies() != null) {
            c.getReplies().forEach(this::populateCommentAuthorInfo);
        }
    }

    /**
     * 댓글/대댓글 생성
     *
     * @param postId   게시글 ID
     * @param authorId 작성자 ID
     * @param content  댓글 본문
     * @param parentId 최상위(null) 또는 상위 댓글 ID
     */
    @Transactional
    public Comment addComment(Long postId, Long authorId, String content, Long parentId) {
        CommunityPost post = communityPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글이 없습니다: " + postId));

        Comment c = new Comment();
        c.setPost(post);
        c.setAuthorId(authorId);
        c.setContent(content);

        if (parentId != null) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("상위 댓글이 없습니다: " + parentId));
            c.setParent(parent);
        }
        return commentRepository.save(c);
    }

}
