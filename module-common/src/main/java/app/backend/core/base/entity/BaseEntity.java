package app.backend.core.base.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.Comment;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedBy
    @Comment("등록자_ID")
    @Column(name = "REG_USER_ID", updatable = false, length = 50)
    private String regUserId;

    @CreatedDate
    @Comment("등록_일시")
    @Column(name = "REG_DATETIME", updatable = false)
    private LocalDateTime regDatetime;

    @LastModifiedBy
    @Comment("수정자_ID")
    @Column(name = "UPDATER_ID", length = 50)
    private String updaterId;

    @LastModifiedDate
    @Comment("수정_일시")
    @Column(name = "UPDATE_DATETIME")
    private LocalDateTime updateDatetime;
}
