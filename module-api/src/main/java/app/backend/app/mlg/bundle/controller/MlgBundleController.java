package app.backend.app.mlg.bundle.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import app.backend.app.mlg.group.service.MlgGroupService;
import app.backend.core.base.component.AbstractController;
import app.backend.core.base.vo.BizRespVo;
import app.backend.core.utils.ReqContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Tag(name = "다국어 번들", description = "F/E 언어 전환용 번들 API")
@RestController
@RequestMapping("/api/v1/i18n")
@RequiredArgsConstructor
public class MlgBundleController extends AbstractController {

    private final MlgGroupService mlgGroupService;

    @Operation(
            summary = "다국어 번들 조회",
            description = "해당 언어의 전체 다국어 번들을 조회합니다. lang 미지정 시 Accept-Language 헤더 기반으로 자동 결정됩니다.")
    @GetMapping("/messages")
    public BizRespVo<Map<String, String>> getBundle(
            @Parameter(description = "언어 코드 (ISO 639-1)", example = "en")
                    @RequestParam(required = false)
                    String lang) {

        String langDivVal = (lang != null) ? lang : ReqContextUtils.getLangDivVal();
        return super.makeResponse(mlgGroupService.getBundle(langDivVal));
    }
}
