package app.backend.app.sample.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.backend.app.sample.dto.SampleReqDto;
import app.backend.app.sample.dto.SampleRespDto;
import app.backend.app.sample.service.SampleService;
import app.backend.core.base.component.AbstractController;
import app.backend.core.base.vo.BizRespVo;
import app.backend.core.base.vo.GenericListVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "Sample", description = "샘플 API")
@RestController
@RequestMapping("/api/v1/samples")
@RequiredArgsConstructor
public class SampleController extends AbstractController {

    private final SampleService sampleService;

    @Operation(summary = "샘플 생성", description = "새로운 샘플을 생성합니다 (JPA 사용)")
    @PostMapping
    public BizRespVo<SampleRespDto> createSample(@Valid @RequestBody SampleReqDto.Create reqDto) {
        SampleRespDto result = sampleService.createSample(reqDto);
        return super.makeCreatedResponse("샘플이 생성되었습니다.", result);
    }

    @Operation(summary = "샘플 조회", description = "ID로 샘플을 조회합니다 (JPA 사용)")
    @GetMapping("/{id}")
    public BizRespVo<SampleRespDto> getSample(@PathVariable Long id) {
        return super.makeResponse(sampleService.getSample(id));
    }

    @Operation(summary = "샘플 목록 조회", description = "모든 샘플 목록을 조회합니다 (JPA 사용)")
    @GetMapping
    public BizRespVo<GenericListVo<SampleRespDto>> getAllSamples() {
        return super.makeGenericListResponse(sampleService.getAllSamples());
    }

    @Operation(summary = "샘플 수정", description = "샘플을 수정합니다 (JPA 사용)")
    @PutMapping("/{id}")
    public BizRespVo<SampleRespDto> updateSample(
            @PathVariable Long id, @Valid @RequestBody SampleReqDto.Update reqDto) {
        return super.makeResponse("샘플이 수정되었습니다.", sampleService.updateSample(id, reqDto));
    }

    @Operation(summary = "샘플 삭제", description = "샘플을 삭제합니다 (JPA 사용)")
    @DeleteMapping("/{id}")
    public BizRespVo<Void> deleteSample(@PathVariable Long id) {
        sampleService.deleteSample(id);
        return super.makeDeleteResponse("샘플이 삭제되었습니다.");
    }

    @Operation(summary = "샘플 목록 조회 (MyBatis)", description = "모든 샘플 목록을 조회합니다 (MyBatis 사용)")
    @GetMapping("/mybatis")
    public BizRespVo<GenericListVo<SampleRespDto>> getAllSamplesByMyBatis() {
        return super.makeGenericListResponse(sampleService.getAllSamplesByMyBatis());
    }

    @Operation(summary = "샘플 조회 (MyBatis)", description = "ID로 샘플을 조회합니다 (MyBatis 사용)")
    @GetMapping("/mybatis/{id}")
    public BizRespVo<SampleRespDto> getSampleByMyBatis(@PathVariable Long id) {
        return super.makeResponse(sampleService.getSampleByMyBatis(id));
    }

    @Operation(summary = "인사", description = "간단한 인사 메시지를 반환합니다 (테스트용)")
    @GetMapping("/hello")
    public BizRespVo<String> hello(
            @Parameter(description = "이름", example = "user") @RequestParam(defaultValue = "user")
                    String name) {
        return super.makeResponse("Hello! " + name);
    }
}
