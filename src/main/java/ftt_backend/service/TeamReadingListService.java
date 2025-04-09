package ftt_backend.service;

import ftt_backend.model.Team;
import ftt_backend.model.TeamReadingListItem;
import ftt_backend.repository.TeamReadingListRepository;
import ftt_backend.repository.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamReadingListService {

    @Autowired
    private TeamReadingListRepository readingListRepository;

    @Autowired
    private TeamRepository teamRepository;

    // 팀에 속한 읽기 자료 항목 조회
    public List<TeamReadingListItem> getReadingListItems(Long teamId) {
        return readingListRepository.findByTeamId(teamId);
    }

    // 새 읽기 자료 항목 생성 및 저장
    @Transactional
    public TeamReadingListItem createReadingListItem(Long teamId, TeamReadingListItem item) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team not found with id: " + teamId));
        item.setTeam(team);
        return readingListRepository.save(item);
    }
}
