package app.backend.core.aop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import app.backend.core.aop.dto.FileBackup;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 삭제 트랜잭션 안전성 AOP
 *
 * <p>서비스에서 파일 삭제 전 백업본을 만들고 등록합니다. 예외 발생 시 백업에서 원본 위치로 자동 복원합니다. 정상 완료 시 백업 파일을 제거합니다.
 *
 * <p>사용법: 서비스에서 파일 삭제 전 백업 후 {@link FileOperationHolder#addDeletion(FileBackup)}를 호출합니다.
 */
@Slf4j
@Aspect
@Component
public class FileDeleteOperationAspect {

    /** 서비스 예외 발생 시 삭제된 파일 복원 */
    @AfterThrowing(pointcut = "execution(* app.backend..service..*.*(..))", throwing = "ex")
    public void afterServiceException(Throwable ex) {
        List<FileBackup> fileBackups = FileOperationHolder.getDeletions();
        if (fileBackups.isEmpty()) {
            return;
        }

        log.info(">>> 파일 삭제 후 에러 발생, 복원 시작: {}", ex.getMessage());

        try {
            for (FileBackup backup : fileBackups) {
                Path backupPath = Paths.get(backup.getOriginPath());
                if (Files.exists(backupPath)) {
                    Path restorePath = Paths.get(backup.getBackupPath());
                    Path parentDir = restorePath.getParent();
                    if (parentDir != null) {
                        Files.createDirectories(parentDir);
                    }
                    Files.move(backupPath, restorePath);
                    log.info(
                            ">>> 파일 복원 완료: {} → {}",
                            backup.getOriginPath(),
                            backup.getBackupPath());
                }
            }
        } catch (IOException e) {
            log.error(">>> 파일 복원 실패: {}", e.getMessage(), e);
        } finally {
            FileOperationHolder.clearDeletions();
        }
    }

    /** 컨트롤러 정상 반환 시 백업 파일 제거 */
    @AfterReturning(pointcut = "execution(* app.backend..controller..*.*(..))")
    public void afterControllerReturn() {
        List<FileBackup> fileBackups = FileOperationHolder.getDeletions();
        if (fileBackups.isEmpty()) {
            return;
        }

        try {
            for (FileBackup backup : fileBackups) {
                Path backupPath = Paths.get(backup.getOriginPath());
                Files.deleteIfExists(backupPath);
                log.info(">>> 백업 파일 정리: {}", backup.getOriginPath());
            }
        } catch (IOException e) {
            log.error(">>> 백업 파일 정리 실패: {}", e.getMessage(), e);
        } finally {
            FileOperationHolder.clearDeletions();
        }
    }
}
