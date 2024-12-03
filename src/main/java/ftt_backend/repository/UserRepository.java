/*
* 데이터베이스에서 사용자 정보를 검색하는 리포지토리
* */

package ftt_backend.repository;

import ftt_backend.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserInfo, Long> {
    Optional<UserInfo> findByUserId(String username);
}
