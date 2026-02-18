package app.backend.app.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import app.backend.app.user.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * username으로 사용자 조회
     *
     * @param username 사용자 아이디
     * @return UserEntity
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * username 존재 여부 확인
     *
     * @param username 사용자 아이디
     * @return 존재 여부
     */
    boolean existsByUsername(String username);

    /**
     * email 존재 여부 확인
     *
     * @param email 이메일
     * @return 존재 여부
     */
    boolean existsByEmail(String email);
}
