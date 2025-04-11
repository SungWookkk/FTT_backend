package ftt_backend.controller;

import ftt_backend.model.TeamReadingListItem;
import ftt_backend.service.TeamReadingListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team/{teamId}/readingList")
public class TeamReadingListController {

    @Autowired
    private TeamReadingListService readingListService;

    // 읽기 자료 항목 조회
    @GetMapping
    public ResponseEntity<List<TeamReadingListItem>> getReadingList(@PathVariable Long teamId) {
        List<TeamReadingListItem> items = readingListService.getReadingListItems(teamId);
        return ResponseEntity.ok(items);
    }

    // 새 읽기 자료 항목 생성
    @PostMapping
    public ResponseEntity<TeamReadingListItem> createReadingListItem(
            @PathVariable Long teamId,
            @RequestBody TeamReadingListItem item) {
        TeamReadingListItem createdItem = readingListService.createReadingListItem(teamId, item);
        return new ResponseEntity<>(createdItem, HttpStatus.CREATED);
    }
}
