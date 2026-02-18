package app.backend.core.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DateTimeUtils {

    /**
     * 초 단위 timestamp를 LocalDate로 변환
     *
     * @param timestamp 유닉스 초 (예: 1748240595.0)
     * @return LocalDate (시스템 기본 시간대 기준)
     */
    public LocalDate toLocalDateFromEpochSeconds(double timestamp) {
        long epochSeconds = (long) timestamp;
        return Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 초 단위 timestamp를 LocalDate로 변환 (시간대 지정)
     *
     * @param timestamp 유닉스 초
     * @param zoneId 시간대
     * @return LocalDate
     */
    public LocalDate toLocalDateFromEpochSeconds(double timestamp, ZoneId zoneId) {
        long epochSeconds = (long) timestamp;
        return Instant.ofEpochSecond(epochSeconds).atZone(zoneId).toLocalDate();
    }

    /**
     * 초 단위 timestamp를 LocalDateTime으로 변환
     *
     * @param timestamp 유닉스 초
     * @return LocalDateTime (시스템 기본 시간대 기준)
     */
    public LocalDateTime toLocalDateTimeFromEpochSeconds(double timestamp) {
        long epochSeconds = (long) timestamp;
        return Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 밀리초 단위 timestamp를 LocalDateTime으로 변환
     *
     * @param timestampMillis 유닉스 밀리초
     * @return LocalDateTime (시스템 기본 시간대 기준)
     */
    public LocalDateTime toLocalDateTimeFromEpochMillis(long timestampMillis) {
        return Instant.ofEpochMilli(timestampMillis)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}
