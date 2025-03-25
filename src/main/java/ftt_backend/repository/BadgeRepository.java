package ftt_backend.repository;

import ftt_backend.model.Badge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    // 예) 뱃지 이름으로 조회
    Optional<Badge> findByBadgeName(String badgeName);

}