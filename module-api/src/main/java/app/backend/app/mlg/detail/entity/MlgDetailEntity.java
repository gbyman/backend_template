package app.backend.app.mlg.detail.entity;

import org.hibernate.annotations.Comment;

import app.backend.app.mlg.group.entity.MlgGroupEntity;
import app.backend.core.base.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "TB_MLG_DETAIL",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "UK_MLG_DETAIL",
                        columnNames = {"MLG_CODE_VAL", "LANG_DIV_VAL"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Comment("다국어 상세")
public class MlgDetailEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("다국어 상세 ID")
    @Column(name = "MLG_DETAIL_ID")
    private Long mlgDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MLG_CODE_VAL", nullable = false)
    private MlgGroupEntity mlgGroup;

    @Comment("언어 구분값")
    @Column(name = "LANG_DIV_VAL", length = 10, nullable = false)
    private String langDivVal;

    @Comment("언어 내용")
    @Column(name = "LANG_CONTENT", length = 500, nullable = false)
    private String langContent;

    @Comment("비고")
    @Column(name = "REMARK_CONTENT", length = 500)
    private String remarkContent;

    public void update(String langContent, String remarkContent) {
        this.langContent = langContent;
        this.remarkContent = remarkContent;
    }
}
