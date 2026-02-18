package app.backend.app.mlg.group.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import app.backend.app.mlg.detail.dto.MlgDetailReqDto;
import app.backend.app.mlg.detail.entity.MlgDetailEntity;
import app.backend.app.mlg.detail.mapstruct.MlgDetailMapStruct;
import app.backend.app.mlg.detail.repository.MlgDetailRepository;
import app.backend.app.mlg.group.dto.MlgGroupReqDto;
import app.backend.app.mlg.group.dto.MlgGroupRespDto;
import app.backend.app.mlg.group.dto.MlgGroupSearchCond;
import app.backend.app.mlg.group.dto.MlgPagingRespDto;
import app.backend.app.mlg.group.entity.MlgGroupEntity;
import app.backend.app.mlg.group.mapstruct.MlgGroupMapStruct;
import app.backend.app.mlg.group.repository.MlgGroupRepository;
import app.backend.app.mlg.group.service.MlgGroupService;
import app.backend.core.base.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MlgGroupServiceImpl implements MlgGroupService {

    private final MlgGroupRepository mlgGroupRepository;
    private final MlgDetailRepository mlgDetailRepository;
    private final MlgGroupMapStruct mlgGroupMapStruct;
    private final MlgDetailMapStruct mlgDetailMapStruct;

    @Override
    public Page<MlgPagingRespDto> paging(MlgGroupSearchCond cond, Pageable pageable) {
        return mlgGroupRepository.paging(cond, cond.isPagingYn(), pageable);
    }

    @Override
    public MlgGroupRespDto getGroup(String mlgCodeVal) {
        MlgGroupEntity entity = findGroupOrThrow(mlgCodeVal);
        return mlgGroupMapStruct.toDto(entity);
    }

    @Override
    @Transactional
    public MlgGroupRespDto createGroup(MlgGroupReqDto.Create reqDto) {
        String mlgCodeVal = generateMlgCode();

        MlgGroupEntity entity = mlgGroupMapStruct.toEntity(reqDto, mlgCodeVal);
        MlgGroupEntity saved = mlgGroupRepository.save(entity);

        return mlgGroupMapStruct.toDto(saved);
    }

    @Override
    @Transactional
    public MlgGroupRespDto updateGroup(String mlgCodeVal, MlgGroupReqDto.Update reqDto) {
        MlgGroupEntity entity = findGroupOrThrow(mlgCodeVal);

        entity.update(reqDto.isUseYn(), reqDto.getRemarkContent());

        // 기존 상세 목록을 비우고 새로 추가
        entity.getDetails().clear();

        for (MlgDetailReqDto detailReq : reqDto.getDetails()) {
            MlgDetailEntity detailEntity = mlgDetailMapStruct.toEntity(detailReq);
            detailEntity.setMlgGroup(entity);
            entity.getDetails().add(detailEntity);
        }

        return mlgGroupMapStruct.toDto(entity);
    }

    @Override
    @Transactional
    public void deleteGroup(String mlgCodeVal) {
        MlgGroupEntity entity = findGroupOrThrow(mlgCodeVal);
        mlgGroupRepository.delete(entity);
    }

    @Override
    public Map<String, String> getBundle(String langDivVal) {
        List<MlgDetailEntity> details =
                mlgDetailRepository.findByLangDivValAndMlgGroupUseYnOrderByMlgGroupMlgCodeVal(
                        langDivVal, true);

        Map<String, String> bundle = new LinkedHashMap<>();
        for (MlgDetailEntity detail : details) {
            bundle.put(detail.getMlgGroup().getMlgCodeVal(), detail.getLangContent());
        }
        return bundle;
    }

    private MlgGroupEntity findGroupOrThrow(String mlgCodeVal) {
        return mlgGroupRepository
                .findById(mlgCodeVal)
                .orElseThrow(
                        () ->
                                new BizException(
                                        HttpStatus.NOT_FOUND,
                                        "NOT_FOUND",
                                        "다국어 그룹을 찾을 수 없습니다: " + mlgCodeVal));
    }

    private String generateMlgCode() {
        String lastCode =
                mlgGroupRepository
                        .findTopByOrderByMlgCodeValDesc()
                        .map(MlgGroupEntity::getMlgCodeVal)
                        .orElse("MLG0000000");

        long next = Long.parseLong(lastCode.replace("MLG", "")) + 1;
        return String.format("MLG%07d", next);
    }
}
