package app.backend.app.mlg.detail.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import app.backend.app.mlg.detail.entity.MlgDetailEntity;

public interface MlgDetailRepository extends JpaRepository<MlgDetailEntity, Long> {

    @EntityGraph(attributePaths = {"mlgGroup"})
    List<MlgDetailEntity> findByLangDivValAndMlgGroupUseYnOrderByMlgGroupMlgCodeVal(
            String langDivVal, boolean useYn);
}
