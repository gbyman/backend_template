package app.backend.core.aop.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 파일 작업 롤백용 백업 경로 DTO
 *
 * <p>파일 업로드/삭제 트랜잭션 안전성을 위해 원본 경로와 백업 경로를 추적합니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * // 업로드 시: 롤백하면 업로드된 파일을 제거
 * FileOperationHolder.addUpload(new FileBackup("/uploads/new.jpg", "/backup/new.jpg"));
 *
 * // 삭제 시: 롤백하면 백업에서 원본 위치로 복원
 * FileOperationHolder.addDeletion(new FileBackup("/backup/old.jpg", "/original/old.jpg"));
 * </pre>
 */
@Getter
@AllArgsConstructor
public class FileBackup {
    /** 원본 파일 경로 */
    private String originPath;

    /** 백업 파일 경로 */
    private String backupPath;
}
