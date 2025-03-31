/**
 * 방명록 서비스 로직
 * */
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

    // 방명록 작성
    public GuestbookEntry createEntry(Long ownerId, Long writerId, String content) {
        UserInfo owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("주인을 찾을수 없음 "));
        UserInfo writer = userRepository.findById(writerId)
                .orElseThrow(() -> new RuntimeException("작성자를 찾을수 없음 "));

        // 방명록 권한 체크 로직 (팀원/친구 여부, ANYONE, NONE 등)
        if (!checkGuestbookPermission(owner, writer)) {
            throw new RuntimeException("해당 유저는 방명록 작성 권한이 없습니다.");
        }

        GuestbookEntry entry = new GuestbookEntry();
        entry.setOwner(owner);
        entry.setWriter(writer);
        entry.setContent(content);

        return guestbookEntryRepository.save(entry);
    }

    // 방명록 목록 조회
    public List<GuestbookEntry> getEntries(Long ownerId) {
        return guestbookEntryRepository.findByOwner_IdOrderByCreatedAtDesc(ownerId);
    }

    // 주인이 댓글 달기 (1회)
    public GuestbookEntry addHostComment(Long entryId, String comment) {
        GuestbookEntry entry = guestbookEntryRepository.findById(entryId)
                .orElseThrow(() -> new RuntimeException("항목을 찾을수 없음 "));

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
            // 예: owner와 writer가 같은 팀에 속하는지, Friend 테이블에서 확인 등
            return checkIsTeamMember(owner, writer);
        }
        return false;
    }

    private boolean checkIsTeamMember(UserInfo owner, UserInfo writer) {
        // TODO: 팀 관계 / 친구 관계 확인 로직
        // 예: friendRepository.existsByUserAndFriend(owner, writer);
        return false;
    }
}
