package app.backend.core.constants;

import lombok.Getter;

/**
 * 테이블 및 Primary Key 정보 Enum
 *
 * <p>@ValidateModuleAdmin 어노테이션에서 DB 조회 시 사용할 테이블명과 PK 컬럼명을 관리합니다.
 *
 * <p><strong>사용 목적:</strong>
 *
 * <ul>
 *   <li>테이블명과 PK 컬럼명을 중앙 관리
 *   <li>오타 방지 및 리팩토링 용이성
 *   <li>SQL Injection 방지 (Enum으로 제한)
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * // 코드 삭제 시 해당 코드의 moduleDivVal을 DB에서 조회하여 권한 검증
 * &#64;ValidateModuleAdmin(tableAndPkInfo = TableAndPkInfo.CODE)
 * &#64;DeleteMapping("/codes/{codeSeq}")
 * public ResponseEntity&lt;?&gt; deleteCode(&#64;PathVariable &#64;PrimaryKey Long codeSeq) {
 *     // ValidateModuleAdminAspect가 자동으로:
 *     // 1. codeSeq를 사용하여 tn_system_code 테이블에서 module_div_val 조회
 *     // 2. 로그인 사용자가 해당 모듈의 관리자인지 검증
 *     // 3. 권한 없으면 BizException 발생
 *     codeService.deleteCode(codeSeq);
 *     return ResponseEntity.ok().build();
 * }
 * </pre>
 *
 * <p><strong>프로젝트에 맞게 수정 예시:</strong>
 *
 * <pre>
 * &#64;Getter
 * public enum TableAndPkInfo {
 *     // 사용자 테이블
 *     USER("tn_user", "user_id"),
 *
 *     // 상품 테이블
 *     PRODUCT("tn_product", "product_id"),
 *
 *     // 주문 테이블
 *     ORDER("tn_order", "order_id"),
 *
 *     // 코드 테이블
 *     CODE("tn_system_code", "code_seq"),
 *
 *     // DUMMY (테이블 조회하지 않고 파라미터에서만 moduleDivVal 추출)
 *     DUMMY("", "");
 *
 *     private final String tableName;
 *     private final String pkColumnName;
 *
 *     TableAndPkInfo(String tableName, String pkColumnName) {
 *         this.tableName = tableName;
 *         this.pkColumnName = pkColumnName;
 *     }
 * }
 * </pre>
 *
 * <p><strong>주의사항:</strong>
 *
 * <ul>
 *   <li>모든 테이블은 module_div_val 컬럼을 가져야 함
 *   <li>DUMMY는 파라미터에서만 moduleDivVal을 추출할 때 사용
 *   <li>테이블명/컬럼명이 변경되면 Enum도 함께 수정
 * </ul>
 *
 * @see app.backend.core.annotation.ValidateModuleAdmin
 * @see app.backend.core.annotation.PrimaryKey
 */
@Getter
public enum TableAndPkInfo {

    /**
     * 사용자 테이블 (예시)
     *
     * <p>프로젝트에 맞게 수정하세요.
     */
    USER("tn_user", "user_id"),

    /**
     * 상품 테이블 (예시)
     *
     * <p>프로젝트에 맞게 수정하세요.
     */
    PRODUCT("tn_product", "product_id"),

    /**
     * 주문 테이블 (예시)
     *
     * <p>프로젝트에 맞게 수정하세요.
     */
    ORDER("tn_order", "order_id"),

    /**
     * 코드 그룹 테이블 (예시)
     *
     * <p>프로젝트에 맞게 수정하세요.
     */
    CODE("tn_system_code_group", "code_group_seq"),

    /**
     * DUMMY (테이블 조회하지 않음)
     *
     * <p>파라미터에 moduleDivVal이 포함되어 있는 경우 사용합니다.
     *
     * <p>사용 예시:
     *
     * <pre>
     * &#64;ValidateModuleAdmin  // tableAndPkInfo 생략 시 DUMMY가 기본값
     * &#64;PostMapping("/codes")
     * public ResponseEntity&lt;?&gt; createCode(&#64;RequestBody CodeCreateReqDto reqDto) {
     *     // reqDto.getModuleDivVal()을 사용하여 권한 검증
     *     codeService.createCode(reqDto);
     *     return ResponseEntity.ok().build();
     * }
     * </pre>
     */
    DUMMY("", "");

    /** 테이블명 */
    private final String tableName;

    /** Primary Key 컬럼명 */
    private final String pkColumnName;

    /**
     * 테이블 및 PK 정보 생성자
     *
     * @param tableName 테이블명
     * @param pkColumnName PK 컬럼명
     */
    TableAndPkInfo(String tableName, String pkColumnName) {
        this.tableName = tableName;
        this.pkColumnName = pkColumnName;
    }
}
