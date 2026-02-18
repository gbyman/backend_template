package app.backend.core.utils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import lombok.experimental.UtilityClass;

/**
 * 날짜 검증 유틸리티
 *
 * <p>날짜 비교 및 범위 검증 기능을 제공합니다.
 *
 * <p>사용 예시:
 *
 * <pre>
 * // 시작일이 종료일보다 이전인지 확인
 * boolean isValid = DateValidationUtils.isFirstDateBeforeOrEqualToSecond("2024-01-01", "2024-12-31");
 *
 * // 날짜가 오늘 이전인지 확인
 * boolean isPast = DateValidationUtils.isDateBeforeOrEqualCurrentDate("2023-12-31");
 *
 * // 날짜가 범위 내에 있는지 확인
 * boolean inRange = DateValidationUtils.isDateInRange(start, end, target);
 * </pre>
 */
@UtilityClass
public class DateValidationUtils {

    /** 기본 날짜 포맷 */
    private static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 첫 번째 날짜가 두 번째 날짜보다 이전이거나 같은지 확인
     *
     * @param firstDateStr 첫 번째 날짜 (yyyy-MM-dd)
     * @param secondDateStr 두 번째 날짜 (yyyy-MM-dd)
     * @return 첫 번째 날짜가 두 번째 날짜 이전이거나 같으면 true
     * @throws IllegalArgumentException 날짜 형식이 잘못된 경우
     */
    public boolean isFirstDateBeforeOrEqualToSecond(String firstDateStr, String secondDateStr) {
        try {
            LocalDate firstDate = LocalDate.parse(firstDateStr, DEFAULT_FORMATTER);
            LocalDate secondDate = LocalDate.parse(secondDateStr, DEFAULT_FORMATTER);

            return !firstDate.isAfter(secondDate);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "날짜 형식이 잘못되었습니다: yyyy-MM-dd 형식이어야 합니다. (입력값: "
                            + firstDateStr
                            + ", "
                            + secondDateStr
                            + ")",
                    e);
        }
    }

    /**
     * 날짜가 현재 날짜보다 이전이거나 같은지 확인
     *
     * @param dateStr 날짜 문자열 (yyyy-MM-dd)
     * @return 날짜가 현재 날짜 이전이거나 같으면 true
     * @throws IllegalArgumentException 날짜 형식이 잘못된 경우
     */
    public boolean isDateBeforeOrEqualCurrentDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DEFAULT_FORMATTER);
            LocalDate now = LocalDate.now();

            return !date.isAfter(now);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "날짜 형식이 잘못되었습니다: yyyy-MM-dd 형식이어야 합니다. (입력값: " + dateStr + ")", e);
        }
    }

    /**
     * 날짜가 현재 날짜보다 이전이거나 같은지 확인 (LocalDate 버전)
     *
     * @param date 확인할 날짜
     * @return 날짜가 현재 날짜 이전이거나 같으면 true
     */
    public boolean isDateBeforeOrEqualCurrentDate(LocalDate date) {
        LocalDate now = LocalDate.now();
        return !date.isAfter(now);
    }

    /**
     * 현재 날짜가 지정된 날짜보다 이전인지 확인
     *
     * @param dateStr 날짜 문자열 (yyyy-MM-dd)
     * @return 현재 날짜가 지정된 날짜 이전이면 true
     * @throws IllegalArgumentException 날짜 형식이 잘못된 경우
     */
    public boolean isCurrentDateBeforeOrEqualDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DEFAULT_FORMATTER);
            LocalDate now = LocalDate.now();

            return !now.isAfter(date);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "날짜 형식이 잘못되었습니다: yyyy-MM-dd 형식이어야 합니다. (입력값: " + dateStr + ")", e);
        }
    }

    /**
     * 현재 날짜가 지정된 날짜보다 이전인지 확인 (LocalDate 버전)
     *
     * @param date 비교할 날짜
     * @return 현재 날짜가 지정된 날짜 이전이면 true
     */
    public boolean isCurrentDateBeforeDate(LocalDate date) {
        LocalDate now = LocalDate.now();
        return now.isBefore(date);
    }

    /**
     * 날짜가 현재 날짜보다 이후인지 확인
     *
     * @param date 확인할 날짜
     * @return 날짜가 현재 날짜 이후이면 true
     */
    public boolean isDateAfterCurrentDate(LocalDate date) {
        LocalDate now = LocalDate.now();
        return date.isAfter(now);
    }

    /**
     * 날짜가 현재 날짜보다 이후인지 확인 (문자열 버전)
     *
     * @param dateStr 날짜 문자열 (yyyy-MM-dd)
     * @return 날짜가 현재 날짜 이후이면 true
     * @throws IllegalArgumentException 날짜 형식이 잘못된 경우
     */
    public boolean isDateAfterCurrentDate(String dateStr) {
        try {
            LocalDate date = LocalDate.parse(dateStr, DEFAULT_FORMATTER);
            return isDateAfterCurrentDate(date);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "날짜 형식이 잘못되었습니다: yyyy-MM-dd 형식이어야 합니다. (입력값: " + dateStr + ")", e);
        }
    }

    /**
     * 날짜가 시작일과 종료일 사이(포함)에 있는지 확인
     *
     * @param startDate 시작일
     * @param endDate 종료일
     * @param dateToCheck 확인할 날짜
     * @return 날짜가 시작일과 종료일 사이에 있으면 true
     */
    public boolean isDateInRange(LocalDate startDate, LocalDate endDate, LocalDate dateToCheck) {
        return (dateToCheck.isEqual(startDate) || dateToCheck.isAfter(startDate))
                && (dateToCheck.isEqual(endDate) || dateToCheck.isBefore(endDate));
    }

    /**
     * 날짜가 시작일과 종료일 사이(포함)에 있는지 확인 (문자열 버전)
     *
     * @param startDateStr 시작일 (yyyy-MM-dd)
     * @param endDateStr 종료일 (yyyy-MM-dd)
     * @param dateToCheckStr 확인할 날짜 (yyyy-MM-dd)
     * @return 날짜가 시작일과 종료일 사이에 있으면 true
     * @throws IllegalArgumentException 날짜 형식이 잘못된 경우
     */
    public boolean isDateInRange(String startDateStr, String endDateStr, String dateToCheckStr) {
        try {
            LocalDate startDate = LocalDate.parse(startDateStr, DEFAULT_FORMATTER);
            LocalDate endDate = LocalDate.parse(endDateStr, DEFAULT_FORMATTER);
            LocalDate dateToCheck = LocalDate.parse(dateToCheckStr, DEFAULT_FORMATTER);

            return isDateInRange(startDate, endDate, dateToCheck);

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(
                    "날짜 형식이 잘못되었습니다: yyyy-MM-dd 형식이어야 합니다. (입력값: "
                            + startDateStr
                            + ", "
                            + endDateStr
                            + ", "
                            + dateToCheckStr
                            + ")",
                    e);
        }
    }
}
