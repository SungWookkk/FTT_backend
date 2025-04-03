package ftt_backend.repository;

import ftt_backend.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    // members가 ManyToMany로 연결되어 있으므로, members의 id가 userId인 Team 목록을 조회
    @Query("SELECT t FROM Team t JOIN t.members m WHERE m.id = :userId")
    List<Team> findByMembers_Id(@Param("userId") Long userId);
}