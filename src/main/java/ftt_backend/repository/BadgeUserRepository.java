package ftt_backend.repository;


import ftt_backend.model.UserBadge;
import ftt_backend.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadgeUserRepository extends JpaRepository<UserBadge, Long> {

    // 1) 특정 유저(UserInfo 객체)로 뱃지 조회
    List<UserBadge> findByUser(UserInfo user);

    // 2) 특정 유저 ID(PK)로 뱃지 조회
    //    (UserBadge의 user 필드가 UserInfo와 N:1 관계이므로, user의 id로도 검색 가능)
    List<UserBadge> findByUserId(Long userId);

}