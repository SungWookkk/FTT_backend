/**
 * 방명록 서비스 로직
 */
package ftt_backend.service;

import ftt_backend.model.GuestbookEntry;
import ftt_backend.model.UserInfo;
import ftt_backend.repository.GuestbookEntryRepository;
import ftt_backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GuestbookService {

    @Autowired
    private GuestbookEntryRepository guestbookEntryRepository;

    @Autowired
    private UserRepository userRepository;

    // 방명록 작성 (비밀글 여부 추가)
    public GuestbookEntry createEntry(Long ownerId, Long writerId, String content, boolean secret) {
        UserInfo owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("주인을 찾을 수 없습니다"));
        UserInfo writer = userRepository.findById(writerId)
                .orElseThrow(() -> new RuntimeException("작성자를 찾을 수 없습니다"));

        // 방명록 권한 체크 로직 (팀원/친구 여부, ANYONE, NONE 등)
        if (!checkGuestbookPermission(owner, writer)) {
            throw new RuntimeException("해당 유저는 방명록 작성 권한이 없습니다.");
        }

        GuestbookEntry entry = new GuestbookEntry();
        entry.setOwner(owner);
        entry.setWriter(writer);
        entry.setContent(content);
        entry.setSecret(secret); // 비밀글 여부 설정

        return guestbookEntryRepository.save(entry);
    }

    // 기존 createEntry 메서드 (비밀글이 아닌 경우)
    public GuestbookEntry createEntry(Long ownerId, Long writerId, String content) {
        return createEntry(ownerId, writerId, content, false);
    }

    // 방명록 목록 조회 (viewerId: 요청자)
    public List<GuestbookEntry> getEntries(Long ownerId, Long viewerId) {
        List<GuestbookEntry> entries = guestbookEntryRepository.findByOwner_IdOrderByCreatedAtDesc(ownerId);
        // 각 항목에 대해 secret가 true이면, 요청자가 작성자 또는 프로필 소유자(=ownerId)인지 확인
        for (GuestbookEntry entry : entries) {
            if (entry.isSecret()) {
                boolean isAllowed = false;
                if (viewerId != null) {
                    if (viewerId.equals(ownerId) || viewerId.equals(entry.getWriter().getId())) {
                        isAllowed = true;
                    }
                }
                if (!isAllowed) {
                    entry.setContent("비밀 글입니다.");
                }
            }
        }
        return entries;
    }

    // 방명록 삭제 (요청자(requesterId)가 작성자(writer) 또는 프로필 소유자(owner)여야 삭제 가능)
    public GuestbookEntry deleteEntry(Long entryId, Long requesterId) {
        GuestbookEntry entry = guestbookEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("방명록 항목을 찾을 수 없습니다."));

        // 삭제 권한 체크
        if (!requesterId.equals(entry.getWriter().getId()) && !requesterId.equals(entry.getOwner().getId())) {
            throw new RuntimeException("삭제 권한이 없습니다.");
        }

        guestbookEntryRepository.delete(entry);
        return entry;
    }


    // 기존 getEntries 메서드 (viewerId 미제공 시)
    public List<GuestbookEntry> getEntries(Long ownerId) {
        return getEntries(ownerId, null);
    }

    // 주인 댓글 작성 (1회)
    public GuestbookEntry addHostComment(Long entryId, String comment) {
        GuestbookEntry entry = guestbookEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("항목을 찾을 수 없습니다"));

        // 이미 댓글이 있으면 한 번 더 달 수 없도록 처리
        if (entry.getHostComment() != null) {
            throw new RuntimeException("이미 댓글을 작성했습니다. (1회 제한)");
        }

        entry.setHostComment(comment);
        entry.setHostCommentCreatedAt(java.time.LocalDateTime.now());
        return guestbookEntryRepository.save(entry);
    }

    // 방명록 권한 체크 (임의 로직)
    private boolean checkGuestbookPermission(UserInfo owner, UserInfo writer) {
        String permission = owner.getGuestbookPermission();
        if (permission == null || permission.equals("ANYONE")) {
            return true; // 모두 작성 가능
        } else if (permission.equals("NONE")) {
            return false; // 아무도 작성 불가
        } else if (permission.equals("TEAM_ONLY")) {
            // 팀원/친구 확인 로직 (임의)
            return checkIsTeamMember(owner, writer);
        }
        return false;
    }

    private boolean checkIsTeamMember(UserInfo owner, UserInfo writer) {
        // TODO: 팀 관계 / 친구 관계 확인 로직 (예: friendRepository.existsByUserAndFriend(owner, writer);)
        return false;
    }
}
