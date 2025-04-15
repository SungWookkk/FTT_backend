package ftt_backend.controller;

import ftt_backend.model.TeamChannel;
import ftt_backend.service.TeamChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/team/{teamId}/channels")
public class TeamChannelController {

    @Autowired
    private TeamChannelService teamChannelService;

    // 팀 채널 전체 조회
    @GetMapping
    public ResponseEntity<List<TeamChannel>> getTeamChannels(@PathVariable Long teamId) {
        List<TeamChannel> channels = teamChannelService.getChannelsByTeamId(teamId);
        return ResponseEntity.ok(channels);
    }

    // 채널 생성
    @PostMapping
    public ResponseEntity<TeamChannel> createTeamChannel(@PathVariable Long teamId, @RequestBody TeamChannel channel) {
        TeamChannel createdChannel = teamChannelService.createTeamChannel(teamId, channel);
        return new ResponseEntity<>(createdChannel, HttpStatus.CREATED);
    }

    // 채널 수정 (부분 업데이트)
    @PatchMapping("/{channelId}")
    public ResponseEntity<TeamChannel> updateTeamChannel(
            @PathVariable Long teamId,
            @PathVariable Long channelId,
            @RequestBody Map<String, Object> updates) {
        TeamChannel updatedChannel = teamChannelService.updateTeamChannel(teamId, channelId, updates);
        return ResponseEntity.ok(updatedChannel);
    }

    // 채널 삭제
    @DeleteMapping("/{channelId}")
    public ResponseEntity<Void> deleteTeamChannel(@PathVariable Long teamId, @PathVariable Long channelId) {
        teamChannelService.deleteTeamChannel(teamId, channelId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
