package app.backend.core.aop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import app.backend.core.aop.dto.FileBackup;
import lombok.experimental.UtilityClass;

/**
 * 파일 작업 추적을 위한 ThreadLocal 홀더
 *
 * <p>서비스 레이어에서 파일 업로드/삭제 작업을 등록하면, AOP Aspect가 트랜잭션 실패 시 자동으로 롤백합니다.
 *
 * <p>사용 예시 (Service 레이어에서):
 *
 * <pre>
 * // 파일 업로드 후 등록 → 에러 발생 시 업로드된 파일 자동 제거
 * storageHelper.uploadFile(file, path, name);
 * FileOperationHolder.addUpload(new FileBackup(uploadedPath, backupPath));
 *
 * // 파일 삭제 전 백업 후 등록 → 에러 발생 시 백업에서 자동 복원
 * Files.copy(originalPath, backupPath);
 * storageHelper.deleteFile(path, name);
 * FileOperationHolder.addDeletion(new FileBackup(backupPath, originalPath));
 * </pre>
 */
@UtilityClass
public class FileOperationHolder {

    private static final ThreadLocal<List<FileBackup>> UPLOAD_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<List<FileBackup>> DELETE_HOLDER = new ThreadLocal<>();

    /** 업로드 작업 등록 */
    public void addUpload(FileBackup fileBackup) {
        List<FileBackup> list = UPLOAD_HOLDER.get();
        if (list == null) {
            list = new ArrayList<>();
            UPLOAD_HOLDER.set(list);
        }
        list.add(fileBackup);
    }

    /** 삭제 작업 등록 */
    public void addDeletion(FileBackup fileBackup) {
        List<FileBackup> list = DELETE_HOLDER.get();
        if (list == null) {
            list = new ArrayList<>();
            DELETE_HOLDER.set(list);
        }
        list.add(fileBackup);
    }

    /** 등록된 업로드 작업 조회 */
    public List<FileBackup> getUploads() {
        List<FileBackup> list = UPLOAD_HOLDER.get();
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    /** 등록된 삭제 작업 조회 */
    public List<FileBackup> getDeletions() {
        List<FileBackup> list = DELETE_HOLDER.get();
        return list != null ? Collections.unmodifiableList(list) : Collections.emptyList();
    }

    /** 업로드 작업 ThreadLocal 정리 */
    public void clearUploads() {
        UPLOAD_HOLDER.remove();
    }

    /** 삭제 작업 ThreadLocal 정리 */
    public void clearDeletions() {
        DELETE_HOLDER.remove();
    }

    /** 모든 ThreadLocal 정리 */
    public void clearAll() {
        UPLOAD_HOLDER.remove();
        DELETE_HOLDER.remove();
    }
}
