package app.backend.core.constants;

/**
 * 모듈 구분 Enum (예시)
 *
 * <p>멀티 모듈 시스템에서 각 모듈을 구분하기 위한 Enum입니다.
 *
 * <p><strong>사용 시나리오:</strong>
 *
 * <ul>
 *   <li>여러 모듈이 하나의 시스템으로 통합된 경우 (예: Module A, B, C)
 *   <li>멀티 테넌트 시스템에서 테넌트별 권한 분리
 *   <li>조직별 데이터 분리 시스템 (예: 본사, 지사A, 지사B)
 * </ul>
 *
 * <p><strong>프로젝트에 맞게 수정 예시:</strong>
 *
 * <pre>
 * // 멀티 모듈 시스템
 * public enum ModuleDiv {
 *     MODULE_A,  // A 모듈
 *     MODULE_B,  // B 모듈
 *     MODULE_C,  // C 모듈
 *     COMMON     // 공통 모듈
 * }
 *
 * // 멀티 테넌트 시스템
 * public enum ModuleDiv {
 *     TENANT_A,
 *     TENANT_B,
 *     TENANT_C,
 *     COMMON
 * }
 *
 * // 조직별 시스템
 * public enum ModuleDiv {
 *     HEADQUARTERS,  // 본사
 *     BRANCH_A,      // 지사 A
 *     BRANCH_B,      // 지사 B
 *     COMMON         // 공통
 * }
 * </pre>
 *
 * <p><strong>주의사항:</strong>
 *
 * <ul>
 *   <li>단일 모듈 시스템에서는 사용할 필요 없음
 *   <li>COMMON은 모든 모듈 관리자가 접근 가능한 공통 영역
 *   <li>DB에 저장되는 값과 동일해야 함
 * </ul>
 *
 * @see app.backend.core.annotation.ValidateModuleAdmin
 */
public enum ModuleDiv {

    /**
     * 모듈 A (예시)
     *
     * <p>프로젝트에 맞게 변경하세요.
     */
    MODULE_A,

    /**
     * 모듈 B (예시)
     *
     * <p>프로젝트에 맞게 변경하세요.
     */
    MODULE_B,

    /**
     * 모듈 C (예시)
     *
     * <p>프로젝트에 맞게 변경하세요.
     */
    MODULE_C,

    /**
     * 공통 모듈
     *
     * <p>어떤 모듈 관리자든 하나라도 권한이 있으면 접근 가능한 영역입니다.
     */
    COMMON
}
