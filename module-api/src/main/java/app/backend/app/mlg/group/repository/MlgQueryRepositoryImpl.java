package app.backend.app.mlg.group.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;

import app.backend.app.mlg.detail.entity.QMlgDetailEntity;
import app.backend.app.mlg.group.dto.MlgGroupSearchCond;
import app.backend.app.mlg.group.dto.MlgPagingRespDto;
import app.backend.app.mlg.group.entity.MlgGroupEntity;
import app.backend.app.mlg.group.entity.QMlgGroupEntity;
import app.backend.core.jpa.querydsl.Querydsl4RepositorySupport;
import jakarta.persistence.EntityManager;

@Repository
public class MlgQueryRepositoryImpl extends Querydsl4RepositorySupport
        implements MlgQueryRepository {

    private final QMlgGroupEntity group = QMlgGroupEntity.mlgGroupEntity;
    private final QMlgDetailEntity detail = QMlgDetailEntity.mlgDetailEntity;

    public MlgQueryRepositoryImpl(EntityManager em) {
        super(MlgGroupEntity.class, em);
    }

    @Override
    public Page<MlgPagingRespDto> paging(
            MlgGroupSearchCond cond, boolean pagingYn, Pageable pageable) {
        return applyPagination(
                pageable,
                pagingYn,
                queryFactory ->
                        queryFactory
                                .select(
                                        Projections.fields(
                                                MlgPagingRespDto.class,
                                                detail.mlgDetailId,
                                                detail.mlgGroup.mlgCodeVal.as("mlgCodeVal"),
                                                detail.langDivVal,
                                                detail.langContent,
                                                group.useYn,
                                                group.remarkContent.as("groupRemarkContent"),
                                                detail.remarkContent.as("detailRemarkContent"),
                                                detail.regUserId,
                                                detail.regDatetime,
                                                detail.updaterId,
                                                detail.updateDatetime))
                                .from(group)
                                .innerJoin(group.details, detail)
                                .where(buildCondition(cond))
                                .orderBy(group.mlgCodeVal.asc(), detail.langDivVal.asc()),
                queryFactory ->
                        queryFactory
                                .select(detail.count())
                                .from(group)
                                .innerJoin(group.details, detail)
                                .where(buildCondition(cond)));
    }

    private BooleanBuilder buildCondition(MlgGroupSearchCond cond) {
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.isNotBlank(cond.getMlgCodeVal())) {
            builder.and(group.mlgCodeVal.eq(cond.getMlgCodeVal()));
        }
        if (StringUtils.isNotBlank(cond.getLangDivVal())) {
            builder.and(detail.langDivVal.eq(cond.getLangDivVal()));
        }
        if (StringUtils.isNotBlank(cond.getLangContent())) {
            builder.and(detail.langContent.containsIgnoreCase(cond.getLangContent()));
        }
        if (cond.getUseYn() != null) {
            builder.and(group.useYn.eq(cond.getUseYn()));
        }

        return builder;
    }
}
