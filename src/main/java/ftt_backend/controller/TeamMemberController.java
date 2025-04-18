package ftt_backend.controller;

import ftt_backend.model.TeamRole;
import ftt_backend.service.TeamMemberService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/teams/{teamId}/members")
@RequiredArgsConstructor
public class TeamMemberController {

    private final TeamMemberService memberService;

    @Getter @Setter
    public static class MemberDto {
        private Long id;
        private String username;
        private String role;
        public MemberDto(Long id, String username, String role) {
            this.id = id;
            this.username = username;
            this.role = role;
        }
    }

    @Getter @Setter
    public static class RoleBody {
        private String role;
    }

    /** 1) 멤버 목록 조회 — 이 한 개만*/
    @GetMapping
    public List<MemberDto> listMembers(@PathVariable Long teamId) {
        return memberService.listMembers(teamId)
                .stream()
                .map(tm -> new MemberDto(
                        tm.getUser().getId(),
                        tm.getUser().getUsername(),
                        tm.getRole().name()
                ))
                .collect(Collectors.toList());
    }



    /** 2) 팀에 새 멤버 추가 (addMemberToTeam 호출) */
    @PostMapping("/{userId}")
    public ResponseEntity<Void> addMember(
            @PathVariable Long teamId,
            @PathVariable Long userId
    ) {
        memberService.addMemberToTeam(teamId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /** 3) 멤버 역할 변경 */
    @PatchMapping("/{userId}/role")
    public ResponseEntity<Void> changeRole(
            @PathVariable Long teamId,
            @PathVariable Long userId,
            @RequestBody RoleBody body
    ) {
        memberService.changeRole(teamId, userId, TeamRole.valueOf(body.getRole()));
        return ResponseEntity.ok().build();
    }

    /** 4) 멤버 추방 */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long teamId,
            @PathVariable Long userId
    ) {
        memberService.removeMember(teamId, userId);
        return ResponseEntity.noContent().build();
    }
}
