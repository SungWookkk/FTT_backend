package ftt_backend.service;

import ftt_backend.model.CommunityPost;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.BadgeUserRepository;
import ftt_backend.repository.CommunityPostRepository;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CommunityPostService {

    @Autowired
    private CommunityPostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BadgeUserRepository badgeUserRepository;
    /** 전체 게시글 조회 */
    @Transactional(readOnly = true)
    public List<CommunityPost> getAllPosts(String category) {
        List<CommunityPost> posts = (category == null || category.equals("전체"))
                ? postRepository.findAll()
                : postRepository.findByCategory(category);
        posts.forEach(this::populateAuthorInfo);
        return posts;
    }


    /** 단일 게시글 조회 (조회수 증가 포함) */
    @Transactional
    public Optional<CommunityPost> getPostById(Long id) {
        Optional<CommunityPost> opt = postRepository.findById(id);
        opt.ifPresent(post -> {
            post.setViewsCount(post.getViewsCount() + 1);
            postRepository.save(post);
            populateAuthorInfo(post);
        });
        return opt;
    }
    /** authorName, authorProfileImage채움*/
    private void populateAuthorInfo(CommunityPost post) {
        userRepository.findById(post.getAuthorId()).ifPresent(user -> {
            post.setAuthorName(user.getUsername());
            post.setAuthorProfileImage(user.getProfile_image());
            // 여기서 UserBadgeRepository를 통해 활성 뱃지를 가져와서
            badgeUserRepository.findActiveByUserId(user.getId())
                    .ifPresent(ub -> {
                        // Badge.iconPath를 CommunityPost.authorBadgeImageUrl에 세팅
                        post.setAuthorBadgeImageUrl(ub.getBadge().getIconPath());
                    });
        });
    }
    /** 새 게시글 생성 (헤더 X-User-Id 로 전달된 userId 로 작성자 설정) */
    @Transactional
    public CommunityPost createPost(CommunityPost post, Long authorId) {
        // ① userRepository.findById 로 UserInfo.id 조회
        UserInfo author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("작성자를 찾을 수 없습니다: " + authorId));
        // ② authorId 필드에 실제 PK(id) 설정
        post.setAuthorId(author.getId());
        return postRepository.save(post);
    }


    /** 게시글 수정 */
    @Transactional
    public CommunityPost updatePost(Long id, CommunityPost updated) {
        CommunityPost post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다: " + id));
        post.setTitle(updated.getTitle());
        post.setContent(updated.getContent());
        post.setStatus(updated.getStatus());
        return postRepository.save(post);
    }

    /** 게시글 삭제 */
    @Transactional
    public void deletePost(Long id) {
        // 실제 삭제 대신 상태만 변경하려면 아래처럼:
        // CommunityPost post = postRepository.findById(id)...;
        // post.setStatus("DELETED"); postRepository.save(post);
        postRepository.deleteById(id);
    }
}
