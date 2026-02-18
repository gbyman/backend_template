package app.backend.app.mlg.group.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import app.backend.app.mlg.group.entity.MlgGroupEntity;
import app.backend.support.BaseRepositoryTest;
import app.backend.support.TestFixtures;

@DisplayName("MlgGroupRepository 테스트")
class MlgGroupRepositoryTest extends BaseRepositoryTest {

    @Autowired private MlgGroupRepository mlgGroupRepository;

    @Test
    @DisplayName("findTopByOrderByMlgCodeValDesc - 마지막 코드 조회")
    void findTopByOrderByMlgCodeValDesc() {
        // given
        entityManager.persist(TestFixtures.createMlgGroupOnly("MLG0000001"));
        entityManager.persist(TestFixtures.createMlgGroupOnly("MLG0000002"));
        entityManager.persist(TestFixtures.createMlgGroupOnly("MLG0000003"));
        flushAndClear();

        // when
        Optional<MlgGroupEntity> result = mlgGroupRepository.findTopByOrderByMlgCodeValDesc();

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getMlgCodeVal()).isEqualTo("MLG0000003");
    }

    @Test
    @DisplayName("findTopByOrderByMlgCodeValDesc - 데이터 없으면 Optional.empty")
    void findTopByOrderByMlgCodeValDesc_empty() {
        // when
        Optional<MlgGroupEntity> result = mlgGroupRepository.findTopByOrderByMlgCodeValDesc();

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("그룹 + 상세 Cascade 저장")
    void saveGroupWithDetails() {
        // given
        MlgGroupEntity group = TestFixtures.createMlgGroup("MLG0000001");

        // when
        mlgGroupRepository.save(group);
        flushAndClear();

        // then
        MlgGroupEntity found = entityManager.find(MlgGroupEntity.class, "MLG0000001");
        assertThat(found).isNotNull();
        assertThat(found.getDetails()).hasSize(2);
        assertThat(found.getDetails())
                .extracting("langDivVal")
                .containsExactlyInAnyOrder("ko", "en");
    }

    @Test
    @DisplayName("그룹 삭제 시 상세도 함께 삭제 (Cascade)")
    void deleteGroupCascadesDetails() {
        // given
        MlgGroupEntity group = TestFixtures.createMlgGroup("MLG0000001");
        entityManager.persist(group);
        flushAndClear();

        // when
        MlgGroupEntity found = entityManager.find(MlgGroupEntity.class, "MLG0000001");
        mlgGroupRepository.delete(found);
        flushAndClear();

        // then
        assertThat(entityManager.find(MlgGroupEntity.class, "MLG0000001")).isNull();
    }
}
