package app.backend.core.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Primary Key 지정 어노테이션
 *
 * <p>{@link ValidateModuleAdmin} 어노테이션과 함께 사용하여 DB에서 moduleDivVal을 조회할 때 사용할 PK 값을 지정합니다.
 *
 * <p><strong>사용 위치:</strong>
 *
 * <ul>
 *   <li>메서드 파라미터
 *   <li>DTO 필드
 * </ul>
 *
 * <p><strong>사용 예시 1: 메서드 파라미터에 적용</strong>
 *
 * <pre>
 * &#64;ValidateModuleAdmin(tableAndPkInfo = TableAndPkInfo.CODE)
 * &#64;DeleteMapping("/codes/{codeSeq}")
 * public ResponseEntity&lt;?&gt; deleteCode(&#64;PathVariable &#64;PrimaryKey Long codeSeq) {
 *     // ValidateModuleAdminAspect가 자동으로:
 *     // 1. codeSeq 값 추출 (&#64;PrimaryKey로 표시)
 *     // 2. TableAndPkInfo.CODE의 테이블에서 module_div_val 조회
 *     // 3. 권한 검증
 *     codeService.deleteCode(codeSeq);
 *     return ResponseEntity.ok().build();
 * }
 * </pre>
 *
 * <p><strong>사용 예시 2: DTO 필드에 적용</strong>
 *
 * <pre>
 * public class CodeUpdateReqDto {
 *     &#64;PrimaryKey
 *     private Long codeSeq;  // PK 값
 *
 *     private String codeName;
 *     private String codeValue;
 *     // ... 기타 필드
 * }
 *
 * &#64;ValidateModuleAdmin(tableAndPkInfo = TableAndPkInfo.CODE)
 * &#64;PutMapping("/codes")
 * public ResponseEntity&lt;?&gt; updateCode(&#64;RequestBody CodeUpdateReqDto reqDto) {
 *     // ValidateModuleAdminAspect가 자동으로:
 *     // 1. reqDto.codeSeq 값 추출 (&#64;PrimaryKey로 표시된 필드)
 *     // 2. TableAndPkInfo.CODE의 테이블에서 module_div_val 조회
 *     // 3. 권한 검증
 *     codeService.updateCode(reqDto);
 *     return ResponseEntity.ok().build();
 * }
 * </pre>
 *
 * <p><strong>동작 원리:</strong>
 *
 * <ol>
 *   <li>ValidateModuleAdminAspect가 &#64;PrimaryKey가 붙은 파라미터/필드를 찾음
 *   <li>해당 값을 사용하여 DB 조회 SQL 실행
 *   <li>조회된 module_div_val로 권한 검증
 * </ol>
 *
 * <p><strong>주의사항:</strong>
 *
 * <ul>
 *   <li>&#64;ValidateModuleAdmin(tableAndPkInfo)가 DUMMY가 아닐 때만 필요
 *   <li>하나의 메서드/DTO에 여러 개의 &#64;PrimaryKey가 있으면 첫 번째 것만 사용
 *   <li>&#64;PrimaryKey 없이 DB 조회 모드를 사용하면 IllegalArgumentException 발생
 * </ul>
 *
 * @see ValidateModuleAdmin
 * @see app.backend.core.constants.TableAndPkInfo
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PrimaryKey {}
