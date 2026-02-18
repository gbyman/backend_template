package app.backend.app.mlg.group.service;

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import app.backend.app.mlg.group.dto.MlgGroupReqDto;
import app.backend.app.mlg.group.dto.MlgGroupRespDto;
import app.backend.app.mlg.group.dto.MlgGroupSearchCond;
import app.backend.app.mlg.group.dto.MlgPagingRespDto;

public interface MlgGroupService {

    Page<MlgPagingRespDto> paging(MlgGroupSearchCond cond, Pageable pageable);

    MlgGroupRespDto getGroup(String mlgCodeVal);

    MlgGroupRespDto createGroup(MlgGroupReqDto.Create reqDto);

    MlgGroupRespDto updateGroup(String mlgCodeVal, MlgGroupReqDto.Update reqDto);

    void deleteGroup(String mlgCodeVal);

    Map<String, String> getBundle(String langDivVal);
}
