package ftt_backend.repository;

import ftt_backend.model.TeamReadingListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamReadingListRepository extends JpaRepository<TeamReadingListItem, Long> {

    // 특정 팀의 읽기 자료 항목들을 조회
    List<TeamReadingListItem> findByTeamId(Long teamId);
}
