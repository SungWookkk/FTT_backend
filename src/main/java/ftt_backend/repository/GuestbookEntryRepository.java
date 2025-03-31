package ftt_backend.repository;

import ftt_backend.model.GuestbookEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestbookEntryRepository extends JpaRepository<GuestbookEntry, Long> {

    // 특정 사용자의 프로필(방명록) 글들을 최근순으로 조회
    List<GuestbookEntry> findByOwner_IdOrderByCreatedAtDesc(Long ownerId);
}
