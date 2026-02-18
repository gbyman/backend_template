package app.backend.core.property;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

/** 파일 업로드 관련 설정 application.yml의 file-upload 설정을 매핑합니다. */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "file-upload")
public class FileUploadProperties {

    /** 기본 최대 파일 크기: 10MB (바이트) */
    private static final long DEFAULT_MAX_SIZE_IN_BYTES = 10L * 1024 * 1024;

    /** 시스템 전체에서 기본으로 허용할 파일 확장자 목록 개별 어노테이션에서 오버라이드 가능 */
    private List<String> allowedExtensions = new ArrayList<>();

    /** 기본 최대 파일 크기 (바이트) -1은 제한 없음 */
    private long maxSizeInBytes = DEFAULT_MAX_SIZE_IN_BYTES;
}
