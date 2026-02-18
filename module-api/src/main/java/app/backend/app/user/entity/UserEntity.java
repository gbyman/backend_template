package app.backend.app.user.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;

import app.backend.core.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TB_USER")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Comment("사용자 테이블")
public class UserEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("사용자_ID")
    @Column(name = "USER_ID")
    private Long id;

    @Comment("사용자 아이디")
    @Column(name = "USERNAME", nullable = false, unique = true, length = 50)
    private String username;

    @Comment("비밀번호")
    @Column(name = "PASSWORD", nullable = false, length = 100)
    private String password;

    @Comment("사용자 이름")
    @Column(name = "USER_NAME", length = 100)
    private String userName;

    @Comment("이메일")
    @Column(name = "EMAIL", length = 100)
    private String email;

    @Comment("권한 (ROLE_USER, ROLE_ADMIN)")
    @Column(name = "ROLE", nullable = false, length = 20)
    @Builder.Default
    private String role = "ROLE_USER";

    @Comment("계정 활성화 여부")
    @Column(name = "ENABLED", nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Comment("계정 잠금 여부")
    @Column(name = "ACCOUNT_NON_LOCKED", nullable = false)
    @Builder.Default
    private Boolean accountNonLocked = true;

    @Comment("최종 로그인 일시")
    @Column(name = "LAST_LOGIN_AT")
    private LocalDateTime lastLoginAt;

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updatePassword(String password) {
        this.password = password;
    }

    public void updateProfile(String userName, String email) {
        this.userName = userName;
        this.email = email;
    }
}
