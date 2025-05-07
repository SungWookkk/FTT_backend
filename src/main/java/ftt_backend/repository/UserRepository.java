/*
* 데이터베이스에서 사용자 정보를 검색하는 리포지토리
* */

package ftt_backend.repository;

import ftt_backend.model.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserInfo, Long> {
    // userId 컬럼을 기준으로 UserInfo 엔티티를 검색하는 메서드
    // Optional<UserInfo>를 반환하여, 결과가 없을 경우 빈 값을 처리할 수 있게함
    Optional<UserInfo> findByUserId(String userId);
    Optional<UserInfo> findByUsername(String userName);
    Optional<UserInfo> findByPhoneNumber(String phoneNumber);
    // 추가: 휴대번호로 존재 여부를 체크할 메서드
    boolean existsByPhoneNumber(String phoneNumber);
}