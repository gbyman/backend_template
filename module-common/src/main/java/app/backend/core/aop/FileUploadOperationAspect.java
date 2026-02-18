package app.backend.core.aop;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import app.backend.core.aop.dto.FileBackup;
import lombok.extern.slf4j.Slf4j;

/**
 * 파일 업로드 트랜잭션 안전성 AOP
 *
 * <p>서비스 레이어에서 예외 발생 시 업로드된 파일을 자동으로 롤백(이동)합니다. 컨트롤러가 정상 반환되면 ThreadLocal을 정리합니다.
 *
 * <p>사용법: 서비스에서 파일 업로드 후 {@link FileOperationHolder#addUpload(FileBackup)}를 호출하여 등록합니다.
 */
@Slf4j
@Aspect
@Component
public class FileUploadOperationAspect {

    /** 컨트롤러 정상 반환 시 ThreadLocal 정리 */
    @After("execution(* app.backend..controller..*.*(..))")
    public void afterControllerReturn() {
        if (!FileOperationHolder.getUploads().isEmpty()) {
            FileOperationHolder.clearUploads();
        }
    }

    /** 서비스 예외 발생 시 업로드된 파일 롤백 */
    @AfterThrowing(pointcut = "execution(* app.backend..service..*.*(..))", throwing = "ex")
    public void afterServiceException(Throwable ex) {
        List<FileBackup> fileBackups = FileOperationHolder.getUploads();
        if (fileBackups.isEmpty()) {
            return;
        }

        log.info(">>> 파일 업로드 후 에러 발생, 롤백 시작: {}", ex.getMessage());

        try {
            for (FileBackup backup : fileBackups) {
                Path originPath = Paths.get(backup.getOriginPath());
                if (Files.exists(originPath)) {
                    Path backupPath = Paths.get(backup.getBackupPath());
                    Path parentDir = backupPath.getParent();
                    if (parentDir != null) {
                        Files.createDirectories(parentDir);
                    }
                    Files.move(originPath, backupPath);
                    log.info(
                            ">>> 파일 롤백 완료: {} → {}",
                            backup.getOriginPath(),
                            backup.getBackupPath());
                }
            }
        } catch (IOException e) {
            log.error(">>> 파일 롤백 실패: {}", e.getMessage(), e);
        } finally {
            FileOperationHolder.clearUploads();
        }
    }
}
