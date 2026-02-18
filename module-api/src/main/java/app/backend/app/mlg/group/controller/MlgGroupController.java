package app.backend.app.mlg.group.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import app.backend.app.mlg.group.dto.MlgGroupReqDto;
import app.backend.app.mlg.group.dto.MlgGroupRespDto;
import app.backend.app.mlg.group.dto.MlgGroupSearchCond;
import app.backend.app.mlg.group.dto.MlgPagingRespDto;
import app.backend.app.mlg.group.service.MlgGroupService;
import app.backend.core.base.component.AbstractController;
import app.backend.core.base.vo.BizRespVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "다국어 관리", description = "다국어 그룹/상세 관리 API")
@RestController
@RequestMapping("/api/v1/system/mlg")
@RequiredArgsConstructor
public class MlgGroupController extends AbstractController {

    private final MlgGroupService mlgGroupService;

    @Operation(summary = "다국어 그룹 목록 조회", description = "다국어 그룹 목록을 페이징 조회합니다")
    @GetMapping
    public BizRespVo<Page<MlgPagingRespDto>> paging(MlgGroupSearchCond cond, Pageable pageable) {
        return super.makeResponse(mlgGroupService.paging(cond, pageable));
    }

    @Operation(summary = "다국어 그룹 상세 조회", description = "다국어 그룹을 상세 조회합니다")
    @GetMapping("/{mlgCodeVal}")
    public BizRespVo<MlgGroupRespDto> getGroup(@PathVariable String mlgCodeVal) {
        return super.makeResponse(mlgGroupService.getGroup(mlgCodeVal));
    }

    @Operation(summary = "다국어 그룹 등록", description = "다국어 그룹과 상세를 등록합니다")
    @PostMapping
    public BizRespVo<MlgGroupRespDto> createGroup(
            @Valid @RequestBody MlgGroupReqDto.Create reqDto) {
        return super.makeCreatedResponse(mlgGroupService.createGroup(reqDto));
    }

    @Operation(summary = "다국어 그룹 수정", description = "다국어 그룹과 상세를 수정합니다")
    @PutMapping("/{mlgCodeVal}")
    public BizRespVo<MlgGroupRespDto> updateGroup(
            @PathVariable String mlgCodeVal, @Valid @RequestBody MlgGroupReqDto.Update reqDto) {
        return super.makeResponse(mlgGroupService.updateGroup(mlgCodeVal, reqDto));
    }

    @Operation(summary = "다국어 그룹 삭제", description = "다국어 그룹과 상세를 삭제합니다")
    @DeleteMapping("/{mlgCodeVal}")
    public BizRespVo<Void> deleteGroup(@PathVariable String mlgCodeVal) {
        mlgGroupService.deleteGroup(mlgCodeVal);
        return super.makeDeleteResponse("다국어 그룹이 삭제되었습니다.");
    }
}
