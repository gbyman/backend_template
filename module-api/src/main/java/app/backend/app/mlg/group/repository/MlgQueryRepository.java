package app.backend.app.mlg.group.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import app.backend.app.mlg.group.dto.MlgGroupSearchCond;
import app.backend.app.mlg.group.dto.MlgPagingRespDto;

public interface MlgQueryRepository {

    Page<MlgPagingRespDto> paging(MlgGroupSearchCond cond, boolean pagingYn, Pageable pageable);
}
