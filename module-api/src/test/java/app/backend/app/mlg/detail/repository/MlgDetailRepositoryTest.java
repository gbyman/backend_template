package app.backend.app.mlg.detail.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import app.backend.app.mlg.detail.entity.MlgDetailEntity;
import app.backend.app.mlg.group.entity.MlgGroupEntity;
import app.backend.support.BaseRepositoryTest;
import app.backend.support.TestFixtures;

@DisplayName("MlgDetailRepository 테스트")
class MlgDetailRepositoryTest extends BaseRepositoryTest {

    @Autowired private MlgDetailRepository mlgDetailRepository;

    @BeforeEach
    void setUp() {
        // 활성 그룹 2개 + 비활성 그룹 1개
        MlgGroupEntity group1 = TestFixtures.createMlgGroup("MLG0000001");
        MlgGroupEntity group2 = TestFixtures.createMlgGroup("MLG0000002");

        MlgGroupEntity inactiveGroup =
                MlgGroupEntity.builder()
                        .mlgCodeVal("MLG0000003")
                        .useYn(false)
                        .remarkContent("비활성 그룹")
                        .build();
        MlgDetailEntity inactiveDetail =
                MlgDetailEntity.builder()
                        .langDivVal("ko")
                        .langContent("비활성")
                        .mlgGroup(inactiveGroup)
                        .build();
        inactiveGroup.getDetails().add(inactiveDetail);

        entityManager.persist(group1);
        entityManager.persist(group2);
        entityManager.persist(inactiveGroup);
        flushAndClear();
    }

    @Test
    @DisplayName("언어별 번들 조회 - 정렬 확인")
    void findBundle_sortedByMlgCodeVal() {
        // when
        List<MlgDetailEntity> results =
                mlgDetailRepository.findByLangDivValAndMlgGroupUseYnOrderByMlgGroupMlgCodeVal(
                        "ko", true);

        // then
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getMlgGroup().getMlgCodeVal()).isEqualTo("MLG0000001");
        assertThat(results.get(1).getMlgGroup().getMlgCodeVal()).isEqualTo("MLG0000002");
    }

    @Test
    @DisplayName("비활성 그룹은 번들에서 제외")
    void findBundle_excludesInactiveGroups() {
        // when
        List<MlgDetailEntity> results =
                mlgDetailRepository.findByLangDivValAndMlgGroupUseYnOrderByMlgGroupMlgCodeVal(
                        "ko", true);

        // then
        assertThat(results)
                .extracting(d -> d.getMlgGroup().getMlgCodeVal())
                .doesNotContain("MLG0000003");
    }

    @Test
    @DisplayName("영어 번들 조회")
    void findBundle_english() {
        // when
        List<MlgDetailEntity> results =
                mlgDetailRepository.findByLangDivValAndMlgGroupUseYnOrderByMlgGroupMlgCodeVal(
                        "en", true);

        // then
        assertThat(results).hasSize(2);
        assertThat(results).allSatisfy(d -> assertThat(d.getLangDivVal()).isEqualTo("en"));
    }
}
