package app.backend.support;

import java.util.ArrayList;
import java.util.List;

import app.backend.app.mlg.detail.dto.MlgDetailReqDto;
import app.backend.app.mlg.detail.dto.MlgDetailRespDto;
import app.backend.app.mlg.detail.entity.MlgDetailEntity;
import app.backend.app.mlg.group.dto.MlgGroupReqDto;
import app.backend.app.mlg.group.dto.MlgGroupRespDto;
import app.backend.app.mlg.group.entity.MlgGroupEntity;

/** 테스트 데이터 팩토리 */
public final class TestFixtures {

    private TestFixtures() {}

    /** MlgGroupEntity 생성 (details 포함) */
    public static MlgGroupEntity createMlgGroup(String mlgCodeVal) {
        MlgGroupEntity group =
                MlgGroupEntity.builder()
                        .mlgCodeVal(mlgCodeVal)
                        .useYn(true)
                        .remarkContent("테스트 그룹")
                        .details(new ArrayList<>())
                        .build();

        MlgDetailEntity koDetail =
                MlgDetailEntity.builder()
                        .langDivVal("ko")
                        .langContent("저장")
                        .remarkContent("한국어")
                        .mlgGroup(group)
                        .build();

        MlgDetailEntity enDetail =
                MlgDetailEntity.builder()
                        .langDivVal("en")
                        .langContent("Save")
                        .remarkContent("English")
                        .mlgGroup(group)
                        .build();

        group.getDetails().add(koDetail);
        group.getDetails().add(enDetail);

        return group;
    }

    /** MlgGroupEntity 생성 (details 없이) */
    public static MlgGroupEntity createMlgGroupOnly(String mlgCodeVal) {
        return MlgGroupEntity.builder()
                .mlgCodeVal(mlgCodeVal)
                .useYn(true)
                .remarkContent("테스트 그룹")
                .details(new ArrayList<>())
                .build();
    }

    /** MlgGroupReqDto.Create 생성 */
    public static MlgGroupReqDto.Create createMlgGroupReqCreate() {
        MlgGroupReqDto.Create create = new MlgGroupReqDto.Create();
        create.setUseYn(true);
        create.setRemarkContent("테스트 비고");

        MlgDetailReqDto koDetail = new MlgDetailReqDto();
        koDetail.setLangDivVal("ko");
        koDetail.setLangContent("저장");
        koDetail.setRemarkContent("한국어");

        MlgDetailReqDto enDetail = new MlgDetailReqDto();
        enDetail.setLangDivVal("en");
        enDetail.setLangContent("Save");
        enDetail.setRemarkContent("English");

        create.setDetails(List.of(koDetail, enDetail));

        return create;
    }

    /** MlgGroupReqDto.Update 생성 */
    public static MlgGroupReqDto.Update createMlgGroupReqUpdate() {
        MlgGroupReqDto.Update update = new MlgGroupReqDto.Update();
        update.setUseYn(true);
        update.setRemarkContent("수정된 비고");

        MlgDetailReqDto koDetail = new MlgDetailReqDto();
        koDetail.setLangDivVal("ko");
        koDetail.setLangContent("수정됨");
        koDetail.setRemarkContent("한국어 수정");

        MlgDetailReqDto enDetail = new MlgDetailReqDto();
        enDetail.setLangDivVal("en");
        enDetail.setLangContent("Updated");
        enDetail.setRemarkContent("English updated");

        update.setDetails(List.of(koDetail, enDetail));

        return update;
    }

    /** MlgGroupRespDto 생성 */
    public static MlgGroupRespDto createMlgGroupRespDto(String mlgCodeVal) {
        MlgDetailRespDto koDetail =
                MlgDetailRespDto.builder()
                        .mlgDetailId(1L)
                        .mlgCodeVal(mlgCodeVal)
                        .langDivVal("ko")
                        .langContent("저장")
                        .build();

        MlgDetailRespDto enDetail =
                MlgDetailRespDto.builder()
                        .mlgDetailId(2L)
                        .mlgCodeVal(mlgCodeVal)
                        .langDivVal("en")
                        .langContent("Save")
                        .build();

        return MlgGroupRespDto.builder()
                .mlgCodeVal(mlgCodeVal)
                .useYn(true)
                .remarkContent("테스트 그룹")
                .details(List.of(koDetail, enDetail))
                .build();
    }
}
