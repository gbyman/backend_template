package app.backend.app.mlg.group.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.backend.app.mlg.detail.entity.MlgDetailEntity;
import app.backend.app.mlg.detail.mapstruct.MlgDetailMapStruct;
import app.backend.app.mlg.detail.repository.MlgDetailRepository;
import app.backend.app.mlg.group.dto.MlgGroupReqDto;
import app.backend.app.mlg.group.dto.MlgGroupRespDto;
import app.backend.app.mlg.group.entity.MlgGroupEntity;
import app.backend.app.mlg.group.mapstruct.MlgGroupMapStruct;
import app.backend.app.mlg.group.repository.MlgGroupRepository;
import app.backend.app.mlg.group.service.impl.MlgGroupServiceImpl;
import app.backend.core.base.exception.BizException;
import app.backend.support.TestFixtures;

@DisplayName("MlgGroupService 단위 테스트")
@ExtendWith(MockitoExtension.class)
class MlgGroupServiceTest {

    @InjectMocks private MlgGroupServiceImpl mlgGroupService;

    @Mock private MlgGroupRepository mlgGroupRepository;
    @Mock private MlgDetailRepository mlgDetailRepository;
    @Mock private MlgGroupMapStruct mlgGroupMapStruct;
    @Mock private MlgDetailMapStruct mlgDetailMapStruct;

    @Nested
    @DisplayName("getGroup")
    class GetGroup {

        @Test
        @DisplayName("존재하는 코드로 조회 시 성공")
        void success() {
            // given
            String mlgCodeVal = "MLG0000001";
            MlgGroupEntity entity = TestFixtures.createMlgGroup(mlgCodeVal);
            MlgGroupRespDto respDto = TestFixtures.createMlgGroupRespDto(mlgCodeVal);

            given(mlgGroupRepository.findById(mlgCodeVal)).willReturn(Optional.of(entity));
            given(mlgGroupMapStruct.toDto(entity)).willReturn(respDto);

            // when
            MlgGroupRespDto result = mlgGroupService.getGroup(mlgCodeVal);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMlgCodeVal()).isEqualTo(mlgCodeVal);
            assertThat(result.getDetails()).hasSize(2);
        }

        @Test
        @DisplayName("존재하지 않는 코드로 조회 시 BizException")
        void notFound() {
            // given
            String mlgCodeVal = "MLG9999999";
            given(mlgGroupRepository.findById(mlgCodeVal)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mlgGroupService.getGroup(mlgCodeVal))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining(mlgCodeVal);
        }
    }

    @Nested
    @DisplayName("createGroup")
    class CreateGroup {

        @Test
        @DisplayName("성공 - MLG 코드 자동 생성")
        void success() {
            // given
            MlgGroupReqDto.Create reqDto = TestFixtures.createMlgGroupReqCreate();
            MlgGroupEntity entity = TestFixtures.createMlgGroup("MLG0000002");
            MlgGroupRespDto respDto = TestFixtures.createMlgGroupRespDto("MLG0000002");

            MlgGroupEntity lastGroup = TestFixtures.createMlgGroupOnly("MLG0000001");
            given(mlgGroupRepository.findTopByOrderByMlgCodeValDesc())
                    .willReturn(Optional.of(lastGroup));
            given(mlgGroupMapStruct.toEntity(eq(reqDto), eq("MLG0000002"))).willReturn(entity);
            given(mlgGroupRepository.save(entity)).willReturn(entity);
            given(mlgGroupMapStruct.toDto(entity)).willReturn(respDto);

            // when
            MlgGroupRespDto result = mlgGroupService.createGroup(reqDto);

            // then
            assertThat(result).isNotNull();
            assertThat(result.getMlgCodeVal()).isEqualTo("MLG0000002");
            then(mlgGroupRepository).should(times(1)).save(any());
        }

        @Test
        @DisplayName("첫 번째 그룹 생성 시 MLG0000001")
        void firstGroup() {
            // given
            MlgGroupReqDto.Create reqDto = TestFixtures.createMlgGroupReqCreate();
            MlgGroupEntity entity = TestFixtures.createMlgGroup("MLG0000001");
            MlgGroupRespDto respDto = TestFixtures.createMlgGroupRespDto("MLG0000001");

            given(mlgGroupRepository.findTopByOrderByMlgCodeValDesc()).willReturn(Optional.empty());
            given(mlgGroupMapStruct.toEntity(eq(reqDto), eq("MLG0000001"))).willReturn(entity);
            given(mlgGroupRepository.save(entity)).willReturn(entity);
            given(mlgGroupMapStruct.toDto(entity)).willReturn(respDto);

            // when
            MlgGroupRespDto result = mlgGroupService.createGroup(reqDto);

            // then
            assertThat(result.getMlgCodeVal()).isEqualTo("MLG0000001");
        }
    }

    @Nested
    @DisplayName("updateGroup")
    class UpdateGroup {

        @Test
        @DisplayName("성공 - 상세 목록 교체")
        void success() {
            // given
            String mlgCodeVal = "MLG0000001";
            MlgGroupEntity entity = TestFixtures.createMlgGroup(mlgCodeVal);
            MlgGroupReqDto.Update reqDto = TestFixtures.createMlgGroupReqUpdate();
            MlgGroupRespDto respDto = TestFixtures.createMlgGroupRespDto(mlgCodeVal);

            given(mlgGroupRepository.findById(mlgCodeVal)).willReturn(Optional.of(entity));
            given(mlgDetailMapStruct.toEntity(any()))
                    .willReturn(
                            MlgDetailEntity.builder().langDivVal("ko").langContent("수정됨").build());
            given(mlgGroupMapStruct.toDto(entity)).willReturn(respDto);

            // when
            MlgGroupRespDto result = mlgGroupService.updateGroup(mlgCodeVal, reqDto);

            // then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 코드로 수정 시 BizException")
        void notFound() {
            // given
            String mlgCodeVal = "MLG9999999";
            MlgGroupReqDto.Update reqDto = TestFixtures.createMlgGroupReqUpdate();
            given(mlgGroupRepository.findById(mlgCodeVal)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mlgGroupService.updateGroup(mlgCodeVal, reqDto))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining(mlgCodeVal);
        }
    }

    @Nested
    @DisplayName("deleteGroup")
    class DeleteGroup {

        @Test
        @DisplayName("성공")
        void success() {
            // given
            String mlgCodeVal = "MLG0000001";
            MlgGroupEntity entity = TestFixtures.createMlgGroup(mlgCodeVal);
            given(mlgGroupRepository.findById(mlgCodeVal)).willReturn(Optional.of(entity));

            // when
            mlgGroupService.deleteGroup(mlgCodeVal);

            // then
            then(mlgGroupRepository).should(times(1)).delete(entity);
        }

        @Test
        @DisplayName("존재하지 않는 코드로 삭제 시 BizException")
        void notFound() {
            // given
            String mlgCodeVal = "MLG9999999";
            given(mlgGroupRepository.findById(mlgCodeVal)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> mlgGroupService.deleteGroup(mlgCodeVal))
                    .isInstanceOf(BizException.class)
                    .hasMessageContaining(mlgCodeVal);
        }
    }

    @Nested
    @DisplayName("getBundle")
    class GetBundle {

        @Test
        @DisplayName("언어별 번들 Map 반환")
        void success() {
            // given
            MlgGroupEntity group1 = TestFixtures.createMlgGroupOnly("MLG0000001");
            MlgGroupEntity group2 = TestFixtures.createMlgGroupOnly("MLG0000002");

            MlgDetailEntity detail1 =
                    MlgDetailEntity.builder()
                            .mlgDetailId(1L)
                            .langDivVal("ko")
                            .langContent("저장")
                            .mlgGroup(group1)
                            .build();

            MlgDetailEntity detail2 =
                    MlgDetailEntity.builder()
                            .mlgDetailId(2L)
                            .langDivVal("ko")
                            .langContent("취소")
                            .mlgGroup(group2)
                            .build();

            given(
                            mlgDetailRepository
                                    .findByLangDivValAndMlgGroupUseYnOrderByMlgGroupMlgCodeVal(
                                            "ko", true))
                    .willReturn(List.of(detail1, detail2));

            // when
            Map<String, String> bundle = mlgGroupService.getBundle("ko");

            // then
            assertThat(bundle).hasSize(2);
            assertThat(bundle.get("MLG0000001")).isEqualTo("저장");
            assertThat(bundle.get("MLG0000002")).isEqualTo("취소");
        }

        @Test
        @DisplayName("해당 언어 데이터 없을 때 빈 Map")
        void emptyBundle() {
            // given
            given(
                            mlgDetailRepository
                                    .findByLangDivValAndMlgGroupUseYnOrderByMlgGroupMlgCodeVal(
                                            "ja", true))
                    .willReturn(new ArrayList<>());

            // when
            Map<String, String> bundle = mlgGroupService.getBundle("ja");

            // then
            assertThat(bundle).isEmpty();
        }
    }
}
