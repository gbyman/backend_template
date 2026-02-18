package app.backend.core.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * {@link CalculationUtils} 단위 테스트
 *
 * <p>BigDecimal 계산 유틸리티의 모든 메서드를 검증합니다.
 */
class CalculationUtilsTest {

    @Nested
    @DisplayName("sumByList() 테스트")
    class SumByListTest {

        @Test
        @DisplayName("리스트의 값들을 정상적으로 합산")
        void testSumByList_Success() {
            // Given
            List<TestItem> items =
                    Arrays.asList(
                            new TestItem(BigDecimal.valueOf(100)),
                            new TestItem(BigDecimal.valueOf(200)),
                            new TestItem(BigDecimal.valueOf(300)));

            // When
            BigDecimal result = CalculationUtils.sumByList(items, TestItem::getValue);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(600));
        }

        @Test
        @DisplayName("null 값은 합산에서 제외")
        void testSumByList_WithNullValues() {
            // Given
            List<TestItem> items =
                    Arrays.asList(
                            new TestItem(BigDecimal.valueOf(100)),
                            new TestItem(null),
                            new TestItem(BigDecimal.valueOf(200)));

            // When
            BigDecimal result = CalculationUtils.sumByList(items, TestItem::getValue);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(300));
        }

        @Test
        @DisplayName("빈 리스트는 0 반환")
        void testSumByList_EmptyList() {
            // Given
            List<TestItem> items = Collections.emptyList();

            // When
            BigDecimal result = CalculationUtils.sumByList(items, TestItem::getValue);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("null 리스트는 0 반환")
        void testSumByList_NullList() {
            // When
            BigDecimal result = CalculationUtils.sumByList(null, TestItem::getValue);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("sum() 테스트")
    class SumTest {

        @Test
        @DisplayName("가변 인자 값들을 정상적으로 합산")
        void testSum_Success() {
            // When
            BigDecimal result =
                    CalculationUtils.sum(
                            BigDecimal.valueOf(100),
                            BigDecimal.valueOf(200),
                            BigDecimal.valueOf(300));

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(600));
        }

        @Test
        @DisplayName("null 값은 합산에서 제외")
        void testSum_WithNullValues() {
            // When
            BigDecimal result =
                    CalculationUtils.sum(BigDecimal.valueOf(100), null, BigDecimal.valueOf(200));

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(300));
        }

        @Test
        @DisplayName("인자가 없으면 0 반환")
        void testSum_NoArgs() {
            // When
            BigDecimal result = CalculationUtils.sum();

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("null 배열은 0 반환")
        void testSum_NullArray() {
            // When
            BigDecimal result = CalculationUtils.sum((BigDecimal[]) null);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("multiply() 테스트")
    class MultiplyTest {

        @Test
        @DisplayName("두 값을 정상적으로 곱셈")
        void testMultiply_Success() {
            // When
            BigDecimal result =
                    CalculationUtils.multiply(BigDecimal.valueOf(10), BigDecimal.valueOf(5));

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(50));
        }

        @Test
        @DisplayName("baseValue가 null이면 null 반환")
        void testMultiply_BaseValueNull() {
            // When
            BigDecimal result = CalculationUtils.multiply(null, BigDecimal.valueOf(5));

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("multiplier가 null이면 null 반환")
        void testMultiply_MultiplierNull() {
            // When
            BigDecimal result = CalculationUtils.multiply(BigDecimal.valueOf(10), null);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("calculatePercentage() 테스트")
    class CalculatePercentageTest {

        @Test
        @DisplayName("퍼센트를 정상적으로 계산 (10%)")
        void testCalculatePercentage_Success() {
            // When
            BigDecimal result =
                    CalculationUtils.calculatePercentage(
                            BigDecimal.valueOf(1000), BigDecimal.valueOf(10));

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(100));
        }

        @Test
        @DisplayName("소수점 퍼센트 계산 (12.5%)")
        void testCalculatePercentage_DecimalPercentage() {
            // When
            BigDecimal result =
                    CalculationUtils.calculatePercentage(
                            BigDecimal.valueOf(1000), BigDecimal.valueOf(12.5));

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(125));
        }

        @Test
        @DisplayName("반올림 테스트 (소수점 8자리)")
        void testCalculatePercentage_Rounding() {
            // When
            BigDecimal result =
                    CalculationUtils.calculatePercentage(
                            BigDecimal.valueOf(100), BigDecimal.valueOf(33.333_333));

            // Then
            // 100 * 33.333333 / 100 = 33.333333
            assertThat(result.scale()).isEqualTo(8);
        }

        @Test
        @DisplayName("number가 null이면 null 반환")
        void testCalculatePercentage_NumberNull() {
            // When
            BigDecimal result = CalculationUtils.calculatePercentage(null, BigDecimal.valueOf(10));

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("percentage가 null이면 null 반환")
        void testCalculatePercentage_PercentageNull() {
            // When
            BigDecimal result =
                    CalculationUtils.calculatePercentage(BigDecimal.valueOf(1000), null);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("calculateRatio() 테스트")
    class CalculateRatioTest {

        @Test
        @DisplayName("비율을 정상적으로 계산 (50%)")
        void testCalculateRatio_Success() {
            // When
            BigDecimal result =
                    CalculationUtils.calculateRatio(
                            BigDecimal.valueOf(500), BigDecimal.valueOf(1000));

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(50));
        }

        @Test
        @DisplayName("비율 계산 (33.33%)")
        void testCalculateRatio_Decimal() {
            // When
            BigDecimal result =
                    CalculationUtils.calculateRatio(
                            BigDecimal.valueOf(100), BigDecimal.valueOf(300));

            // Then
            // 100 / 300 * 100 = 33.33333333...
            assertThat(result.setScale(2, RoundingMode.HALF_UP))
                    .isEqualByComparingTo(BigDecimal.valueOf(33.33));
        }

        @Test
        @DisplayName("target이 null이면 null 반환")
        void testCalculateRatio_TargetNull() {
            // When
            BigDecimal result = CalculationUtils.calculateRatio(null, BigDecimal.valueOf(1000));

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("total이 null이면 null 반환")
        void testCalculateRatio_TotalNull() {
            // When
            BigDecimal result = CalculationUtils.calculateRatio(BigDecimal.valueOf(500), null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("total이 0이면 null 반환 (0으로 나누기 방지)")
        void testCalculateRatio_TotalZero() {
            // When
            BigDecimal result =
                    CalculationUtils.calculateRatio(BigDecimal.valueOf(500), BigDecimal.ZERO);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("round() 테스트")
    class RoundTest {

        @Test
        @DisplayName("소수점 2자리로 반올림")
        void testRound_Scale2() {
            // When
            BigDecimal result = CalculationUtils.round(BigDecimal.valueOf(123.456_789), 2);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(123.46));
        }

        @Test
        @DisplayName("소수점 0자리로 반올림 (정수)")
        void testRound_Scale0() {
            // When
            BigDecimal result = CalculationUtils.round(BigDecimal.valueOf(123.56), 0);

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(124));
        }

        @Test
        @DisplayName("기본 반올림 (소수점 8자리)")
        void testRound_Default() {
            // When
            BigDecimal result = CalculationUtils.round(BigDecimal.valueOf(123.123_456_789));

            // Then
            assertThat(result.scale()).isEqualTo(8);
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(123.123_456_79));
        }

        @Test
        @DisplayName("value가 null이면 null 반환")
        void testRound_Null() {
            // When
            BigDecimal result = CalculationUtils.round(null, 2);

            // Then
            assertThat(result).isNull();
        }
    }

    @Nested
    @DisplayName("divide() 테스트")
    class DivideTest {

        @Test
        @DisplayName("두 값을 정상적으로 나눗셈")
        void testDivide_Success() {
            // When
            BigDecimal result =
                    CalculationUtils.divide(BigDecimal.valueOf(100), BigDecimal.valueOf(4));

            // Then
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(25));
        }

        @Test
        @DisplayName("나눗셈 후 소수점 8자리로 반올림")
        void testDivide_Rounding() {
            // When
            BigDecimal result =
                    CalculationUtils.divide(BigDecimal.valueOf(100), BigDecimal.valueOf(3));

            // Then
            // 100 / 3 = 33.33333333...
            assertThat(result.scale()).isEqualTo(8);
        }

        @Test
        @DisplayName("dividend가 null이면 null 반환")
        void testDivide_DividendNull() {
            // When
            BigDecimal result = CalculationUtils.divide(null, BigDecimal.valueOf(4));

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("divisor가 null이면 null 반환")
        void testDivide_DivisorNull() {
            // When
            BigDecimal result = CalculationUtils.divide(BigDecimal.valueOf(100), null);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("divisor가 0이면 null 반환 (0으로 나누기 방지)")
        void testDivide_DivisorZero() {
            // When
            BigDecimal result = CalculationUtils.divide(BigDecimal.valueOf(100), BigDecimal.ZERO);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("커스텀 소수점 자리와 반올림 모드로 나눗셈")
        void testDivide_CustomScaleAndRoundingMode() {
            // When
            BigDecimal result =
                    CalculationUtils.divide(
                            BigDecimal.valueOf(100), BigDecimal.valueOf(3), 2, RoundingMode.DOWN);

            // Then
            // 100 / 3 = 33.33... (DOWN 모드로 소수점 2자리)
            assertThat(result).isEqualByComparingTo(BigDecimal.valueOf(33.33));
        }
    }

    /** 테스트용 DTO */
    private static class TestItem {
        private final BigDecimal value;

        public TestItem(BigDecimal value) {
            this.value = value;
        }

        public BigDecimal getValue() {
            return value;
        }
    }
}
