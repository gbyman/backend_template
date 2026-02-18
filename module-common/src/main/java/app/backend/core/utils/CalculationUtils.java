package app.backend.core.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import lombok.experimental.UtilityClass;

/**
 * BigDecimal 계산 유틸리티
 *
 * <p>금액, 수량 등의 정밀 계산을 위한 헬퍼 메서드를 제공합니다.
 *
 * <p><strong>주요 기능:</strong>
 *
 * <ul>
 *   <li>Null-safe 합산 (리스트/가변 인자)
 *   <li>Null-safe 곱셈
 *   <li>퍼센트 계산 (비율 적용)
 *   <li>비율 계산 (백분율)
 *   <li>일관된 반올림 정책 (HALF_UP, 소수점 8자리)
 * </ul>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * // 리스트 합산
 * BigDecimal total = CalculationUtils.sumByList(items, Item::getPrice);
 *
 * // 가변 인자 합산
 * BigDecimal total = CalculationUtils.sum(price1, price2, price3);
 *
 * // 퍼센트 계산 (10% 세금)
 * BigDecimal tax = CalculationUtils.calculatePercentage(price, BigDecimal.valueOf(10));
 *
 * // 비율 계산 (부분/전체 * 100)
 * BigDecimal ratio = CalculationUtils.calculateRatio(partAmount, totalAmount);
 * </pre>
 *
 * <p><strong>반올림 정책:</strong>
 *
 * <ul>
 *   <li>RoundingMode.HALF_UP (반올림)
 *   <li>소수점 8자리 (금융 계산 권장 자릿수)
 * </ul>
 *
 * <p><strong>주의사항:</strong>
 *
 * <ul>
 *   <li>0으로 나누기는 자동으로 null 반환
 *   <li>null 값은 계산에서 제외되거나 null 반환
 *   <li>금융/회계 시스템에서 프로젝트 정책에 맞게 반올림 모드 조정 필요
 * </ul>
 */
@UtilityClass
public class CalculationUtils {

    /** 기본 소수점 자리 (금융 계산 권장 자릿수) */
    private static final int DEFAULT_SCALE = 8;

    /** 기본 반올림 모드 */
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * 리스트의 요소에서 BigDecimal 값을 추출하여 합산합니다.
     *
     * <p>Function을 사용하여 엔티티/DTO의 특정 필드를 합산할 수 있습니다.
     *
     * <p><strong>사용 예시:</strong>
     *
     * <pre>
     * // Item 리스트에서 price 합산
     * BigDecimal totalPrice = CalculationUtils.sumByList(items, Item::getPrice);
     *
     * // Order 리스트에서 amount 합산
     * BigDecimal totalAmount = CalculationUtils.sumByList(orders, Order::getAmount);
     * </pre>
     *
     * <p><strong>Null 처리:</strong>
     *
     * <ul>
     *   <li>리스트가 null이거나 비어있으면 0 반환
     *   <li>추출된 값이 null이면 합산에서 제외
     * </ul>
     *
     * @param <T> 리스트 요소 타입
     * @param list 합산할 리스트
     * @param valueGetter 값 추출 Function (예: Item::getPrice)
     * @return 합산 결과 (BigDecimal, 리스트가 비어있으면 0)
     */
    public <T> BigDecimal sumByList(List<T> list, Function<T, BigDecimal> valueGetter) {
        if (list == null || list.isEmpty()) {
            return BigDecimal.ZERO;
        }

        return list.stream()
                .map(valueGetter)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * 가변 인자로 전달된 BigDecimal 값들을 합산합니다.
     *
     * <p><strong>사용 예시:</strong>
     *
     * <pre>
     * BigDecimal total = CalculationUtils.sum(price1, price2, price3);
     * </pre>
     *
     * <p><strong>Null 처리:</strong>
     *
     * <ul>
     *   <li>null 값은 합산에서 제외
     *   <li>모든 값이 null이면 0 반환
     * </ul>
     *
     * @param values 합산할 BigDecimal 값들
     * @return 합산 결과 (BigDecimal, 모든 값이 null이면 0)
     */
    public BigDecimal sum(BigDecimal... values) {
        if (values == null || values.length == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal total = BigDecimal.ZERO;
        for (BigDecimal value : values) {
            if (value != null) {
                total = total.add(value);
            }
        }
        return total;
    }

    /**
     * 두 BigDecimal 값을 곱합니다.
     *
     * <p><strong>사용 예시:</strong>
     *
     * <pre>
     * // 단가 * 수량
     * BigDecimal totalPrice = CalculationUtils.multiply(unitPrice, quantity);
     * </pre>
     *
     * <p><strong>Null 처리:</strong>
     *
     * <ul>
     *   <li>baseValue 또는 multiplier가 null이면 null 반환
     * </ul>
     *
     * @param baseValue 기준 값
     * @param multiplier 곱할 값
     * @return 곱셈 결과 (BigDecimal, 하나라도 null이면 null)
     */
    public BigDecimal multiply(BigDecimal baseValue, BigDecimal multiplier) {
        if (baseValue == null || multiplier == null) {
            return null;
        }
        return baseValue.multiply(multiplier);
    }

    /**
     * 숫자에 퍼센트를 적용합니다. (number × percentage ÷ 100)
     *
     * <p><strong>사용 예시:</strong>
     *
     * <pre>
     * // 가격의 10% 세금 계산
     * BigDecimal tax = CalculationUtils.calculatePercentage(price, BigDecimal.valueOf(10));
     *
     * // 가격의 15% 할인액 계산
     * BigDecimal discount = CalculationUtils.calculatePercentage(price, BigDecimal.valueOf(15));
     * </pre>
     *
     * <p><strong>Null 처리:</strong>
     *
     * <ul>
     *   <li>number 또는 percentage가 null이면 null 반환
     * </ul>
     *
     * <p><strong>반올림:</strong>
     *
     * <ul>
     *   <li>RoundingMode.HALF_UP (반올림)
     *   <li>소수점 8자리
     * </ul>
     *
     * @param number 기준 숫자
     * @param percentage 적용할 퍼센트 (예: 10 = 10%)
     * @return 퍼센트 적용 결과 (BigDecimal, 하나라도 null이면 null)
     */
    public BigDecimal calculatePercentage(BigDecimal number, BigDecimal percentage) {
        if (number == null || percentage == null) {
            return null;
        }

        return number.multiply(percentage)
                .divide(BigDecimal.valueOf(100), DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 비율을 백분율로 계산합니다. (target ÷ total × 100)
     *
     * <p><strong>사용 예시:</strong>
     *
     * <pre>
     * // 부분 금액이 전체 금액에서 차지하는 비율(%)
     * BigDecimal ratio = CalculationUtils.calculateRatio(partAmount, totalAmount);
     *
     * // 달성률 계산 (실제 / 목표 * 100)
     * BigDecimal achievementRate = CalculationUtils.calculateRatio(actual, target);
     * </pre>
     *
     * <p><strong>Null 및 0 처리:</strong>
     *
     * <ul>
     *   <li>target 또는 total이 null이면 null 반환
     *   <li>total이 0이면 null 반환 (0으로 나누기 방지)
     * </ul>
     *
     * <p><strong>반올림:</strong>
     *
     * <ul>
     *   <li>RoundingMode.HALF_UP (반올림)
     *   <li>소수점 8자리
     * </ul>
     *
     * @param target 대상 값 (분자)
     * @param total 전체 값 (분모)
     * @return 비율(%) (BigDecimal, target/total이 null이거나 total이 0이면 null)
     */
    public BigDecimal calculateRatio(BigDecimal target, BigDecimal total) {
        if (target == null || total == null || BigDecimal.ZERO.compareTo(total) == 0) {
            return null;
        }

        return target.divide(total, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * BigDecimal 값을 지정된 소수점 자리로 반올림합니다.
     *
     * <p><strong>사용 예시:</strong>
     *
     * <pre>
     * // 소수점 2자리로 반올림 (화폐 단위)
     * BigDecimal rounded = CalculationUtils.round(value, 2);
     *
     * // 소수점 0자리로 반올림 (정수)
     * BigDecimal rounded = CalculationUtils.round(value, 0);
     * </pre>
     *
     * <p><strong>Null 처리:</strong>
     *
     * <ul>
     *   <li>value가 null이면 null 반환
     * </ul>
     *
     * @param value 반올림할 값
     * @param scale 소수점 자리수
     * @return 반올림된 값 (BigDecimal, value가 null이면 null)
     */
    public BigDecimal round(BigDecimal value, int scale) {
        if (value == null) {
            return null;
        }
        return value.setScale(scale, DEFAULT_ROUNDING_MODE);
    }

    /**
     * BigDecimal 값을 기본 소수점 자리(8자리)로 반올림합니다.
     *
     * <p><strong>사용 예시:</strong>
     *
     * <pre>
     * BigDecimal rounded = CalculationUtils.round(value);
     * </pre>
     *
     * <p><strong>Null 처리:</strong>
     *
     * <ul>
     *   <li>value가 null이면 null 반환
     * </ul>
     *
     * @param value 반올림할 값
     * @return 반올림된 값 (BigDecimal, value가 null이면 null)
     */
    public BigDecimal round(BigDecimal value) {
        return round(value, DEFAULT_SCALE);
    }

    /**
     * 두 BigDecimal 값을 나눕니다.
     *
     * <p><strong>사용 예시:</strong>
     *
     * <pre>
     * // 총액 / 수량 = 단가
     * BigDecimal unitPrice = CalculationUtils.divide(totalAmount, quantity);
     * </pre>
     *
     * <p><strong>Null 및 0 처리:</strong>
     *
     * <ul>
     *   <li>dividend 또는 divisor가 null이면 null 반환
     *   <li>divisor가 0이면 null 반환 (0으로 나누기 방지)
     * </ul>
     *
     * <p><strong>반올림:</strong>
     *
     * <ul>
     *   <li>RoundingMode.HALF_UP (반올림)
     *   <li>소수점 8자리
     * </ul>
     *
     * @param dividend 피제수 (나누어지는 수)
     * @param divisor 제수 (나누는 수)
     * @return 나눗셈 결과 (BigDecimal, dividend/divisor가 null이거나 divisor가 0이면 null)
     */
    public BigDecimal divide(BigDecimal dividend, BigDecimal divisor) {
        if (dividend == null || divisor == null || BigDecimal.ZERO.compareTo(divisor) == 0) {
            return null;
        }
        return dividend.divide(divisor, DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }

    /**
     * 지정된 소수점 자리와 반올림 모드로 나눗셈을 수행합니다.
     *
     * <p><strong>사용 예시:</strong>
     *
     * <pre>
     * // 소수점 2자리, 내림 모드로 나눗셈
     * BigDecimal result = CalculationUtils.divide(
     *     dividend, divisor, 2, RoundingMode.DOWN);
     * </pre>
     *
     * <p><strong>Null 및 0 처리:</strong>
     *
     * <ul>
     *   <li>dividend 또는 divisor가 null이면 null 반환
     *   <li>divisor가 0이면 null 반환 (0으로 나누기 방지)
     * </ul>
     *
     * @param dividend 피제수 (나누어지는 수)
     * @param divisor 제수 (나누는 수)
     * @param scale 소수점 자리수
     * @param roundingMode 반올림 모드
     * @return 나눗셈 결과 (BigDecimal, dividend/divisor가 null이거나 divisor가 0이면 null)
     */
    public BigDecimal divide(
            BigDecimal dividend, BigDecimal divisor, int scale, RoundingMode roundingMode) {
        if (dividend == null || divisor == null || BigDecimal.ZERO.compareTo(divisor) == 0) {
            return null;
        }
        return dividend.divide(divisor, scale, roundingMode);
    }
}
