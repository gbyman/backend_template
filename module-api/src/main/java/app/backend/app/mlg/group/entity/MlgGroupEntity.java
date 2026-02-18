package app.backend.app.mlg.group.entity;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.Comment;

import app.backend.app.mlg.detail.entity.MlgDetailEntity;
import app.backend.core.base.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TB_MLG_GROUP")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Comment("다국어 그룹")
public class MlgGroupEntity extends BaseEntity {

    @Id
    @Comment("다국어 코드값")
    @Column(name = "MLG_CODE_VAL", length = 10)
    private String mlgCodeVal;

    @Comment("사용 여부")
    @Column(name = "USE_YN", nullable = false)
    @Builder.Default
    private boolean useYn = true;

    @Comment("비고")
    @Column(name = "REMARK_CONTENT", length = 500)
    private String remarkContent;

    @OneToMany(mappedBy = "mlgGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MlgDetailEntity> details = new ArrayList<>();

    public void update(boolean useYn, String remarkContent) {
        this.useYn = useYn;
        this.remarkContent = remarkContent;
    }
}
