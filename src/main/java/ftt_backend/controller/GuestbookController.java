/**
 * 방명록 로직을 위한 컨트롤러 매핑
 * */
package ftt_backend.controller;

import ftt_backend.model.GuestbookEntry;
import ftt_backend.service.GuestbookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guestbook")
public class GuestbookController {

    @Autowired
    private GuestbookService guestbookService;

    // 방명록 작성 (비밀글 여부 추가)
    @PostMapping("/{ownerId}")
    public GuestbookEntry createEntry(
            @PathVariable Long ownerId,
            @RequestParam Long writerId,
            @RequestParam String content,
            @RequestParam(required = false, defaultValue = "false") boolean secret
    ) {
        return guestbookService.createEntry(ownerId, writerId, content, secret);
    }
    // 방명록 목록 조회
    @GetMapping("/{ownerId}")
    public List<GuestbookEntry> getEntries(@PathVariable Long ownerId) {
        return guestbookService.getEntries(ownerId);
    }

    // 주인 댓글 작성
    @PatchMapping("/comment/{entryId}")
    public GuestbookEntry addHostComment(
            @PathVariable Long entryId,
            @RequestParam String comment
    ) {
        return guestbookService.addHostComment(entryId, comment);
    }

    // 방명록 삭제 (요청자(requesterId) 전달 - 삭제 권한: 작성자 또는 프로필 소유자만 가능)
    @DeleteMapping("/{entryId}")
    public GuestbookEntry deleteEntry(
            @PathVariable Long entryId,
            @RequestParam Long requesterId
    ) {
        return guestbookService.deleteEntry(entryId, requesterId);
    }


    @GetMapping("/test")
    public List<GuestbookEntry> testEntries() {
        // 테스트용: ownerId가 1번인 사용자의 방명록을 반환
        return guestbookService.getEntries(1L);
    }

}
