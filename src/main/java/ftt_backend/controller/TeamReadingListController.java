package ftt_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/teams/{teamId}/readingList")
public class TeamReadingListController {

    // 임시 더미 데이터를 반환
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getReadingList(@PathVariable Long teamId) {
        List<Map<String, Object>> readingList = Arrays.asList(
                Map.of("category", "AWS",
                        "items", Arrays.asList(
                                Map.of("title", "EC2 Spring Boot 배포", "link", "https://example.com/ec2-spring"),
                                Map.of("title", "EC2 MySQL 설정", "link", "https://example.com/ec2-mysql")
                        )),
                Map.of("category", "BackEnd",
                        "items", Arrays.asList(
                                Map.of("title", "Node.js", "link", "https://nodejs.org"),
                                Map.of("title", "HTTP", "link", "https://developer.mozilla.org/docs/Web/HTTP"),
                                Map.of("title", "Docker", "link", "https://www.docker.com/"),
                                Map.of("title", "Web Architecture", "link", "https://example.com/web-architecture")
                        ))
        );
        return ResponseEntity.ok(readingList);
    }
}
