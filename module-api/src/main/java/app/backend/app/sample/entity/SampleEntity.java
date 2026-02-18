package app.backend.app.sample.entity;

import org.hibernate.annotations.Comment;

import app.backend.core.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "TB_SAMPLE",
        indexes = {
            @Index(name = "IDX_SAMPLE_TITLE", columnList = "TITLE"),
            @Index(name = "IDX_SAMPLE_USE_YN", columnList = "USE_YN")
        })
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Comment("샘플 테이블")
public class SampleEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("샘플_ID")
    @Column(name = "SAMPLE_ID")
    private Long id;

    @Comment("제목")
    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Comment("내용")
    @Column(name = "CONTENT", columnDefinition = "TEXT")
    private String content;

    @Comment("사용_여부")
    @Column(name = "USE_YN", nullable = false, length = 1)
    @Builder.Default
    private String useYn = "Y";

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
