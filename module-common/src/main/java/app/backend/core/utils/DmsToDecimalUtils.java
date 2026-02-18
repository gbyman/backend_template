package app.backend.core.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.experimental.UtilityClass;

/**
 * DMS(Degrees Minutes Seconds) 좌표를 십진수(Decimal) 좌표로 변환하는 유틸리티
 *
 * <p>지리 정보 시스템(GIS)에서 사용되는 좌표 형식 변환을 지원합니다.
 *
 * <p><strong>좌표 형식:</strong>
 *
 * <ul>
 *   <li><strong>DMS 형식:</strong> 도(°) 분(') 초(") + 방향(N/S/E/W) <br>
 *       예: 37°33'45"N 126°58'37"E (서울 시청)
 *   <li><strong>십진수 형식:</strong> 소수점 표기 <br>
 *       예: 37.5625, 126.9769
 * </ul>
 *
 * <p><strong>변환 공식:</strong>
 *
 * <pre>
 * 십진수 = 도 + (분 / 60) + (초 / 3600)
 *
 * 방향이 남위(S) 또는 서경(W)이면 음수로 변환
 * </pre>
 *
 * <p><strong>사용 예시:</strong>
 *
 * <pre>
 * // 방법 1: 개별 파라미터로 변환
 * double latitude = DmsToDecimalUtils.dmsToDecimal("37", "33", "45", "N");
 * // 결과: 37.5625
 *
 * double longitude = DmsToDecimalUtils.dmsToDecimal("126", "58", "37", "E");
 * // 결과: 126.9769
 *
 * // 방법 2: 문자열 파싱으로 변환
 * String dmsString = "37°33'45\"N 126°58'37\"E";
 * double[] coords = DmsToDecimalUtils.convertDmsToDecimal(dmsString);
 * // coords[0]: 37.5625 (위도)
 * // coords[1]: 126.9769 (경도)
 *
 * // 방법 3: GeoCoordinate 객체로 반환
 * GeoCoordinate coordinate = DmsToDecimalUtils.convertToGeoCoordinate(dmsString);
 * // coordinate.getLatitude(): 37.5625
 * // coordinate.getLongitude(): 126.9769
 * </pre>
 *
 * <p><strong>지원 방향:</strong>
 *
 * <ul>
 *   <li>N (North, 북위) - 양수
 *   <li>S (South, 남위) - 음수
 *   <li>E (East, 동경) - 양수
 *   <li>W (West, 서경) - 음수
 * </ul>
 */
@UtilityClass
public class DmsToDecimalUtils {

    /** DMS 좌표 문자열 파싱 정규식 패턴 (예: "37°33'45"N 126°58'37"E") */
    private static final Pattern DMS_PATTERN =
            Pattern.compile(
                    "(\\d{1,3})°(\\d{1,2})'(\\d{1,2}(?:\\.\\d+)?)\"([NS])\\s*"
                            + "(\\d{1,3})°(\\d{1,2})'(\\d{1,2}(?:\\.\\d+)?)\"([EW])");

    /**
     * 도/분/초 좌표를 십진수 좌표로 변환
     *
     * <p>변환 공식: decimal = 도 + (분 / 60) + (초 / 3600)
     *
     * @param degree 도 (0-180)
     * @param minute 분 (0-59)
     * @param second 초 (0-59.999...)
     * @param direction 방향 (N: 북위, S: 남위, E: 동경, W: 서경)
     * @return 십진수 좌표
     * @throws IllegalArgumentException 입력값이 유효 범위를 벗어난 경우
     */
    public double dmsToDecimal(String degree, String minute, String second, String direction) {
        double degrees = Double.parseDouble(degree);
        double minutes = Double.parseDouble(minute);
        double seconds = Double.parseDouble(second);

        // 입력값 범위 검증
        validateDmsValues(degrees, minutes, seconds, direction);

        // 도 + (분/60) + (초/3600) 계산
        double decimal = degrees + (minutes / 60.0) + (seconds / 3600.0);

        // 남위(S) 또는 서경(W)이면 음수 변환
        if ("S".equals(direction) || "W".equals(direction)) {
            decimal *= -1;
        }

        return decimal;
    }

    /**
     * DMS 형식 문자열을 파싱하여 십진수 [위도, 경도] 배열로 변환
     *
     * <p>지원 형식: "37°33'45"N 126°58'37"E"
     *
     * @param dmsString DMS 형식 문자열
     * @return [위도, 경도] 배열
     * @throws IllegalArgumentException 문자열 형식이 올바르지 않거나 값이 유효 범위를 벗어난 경우
     */
    public double[] convertDmsToDecimal(String dmsString) {
        if (dmsString == null || dmsString.isBlank()) {
            throw new IllegalArgumentException("DMS 문자열이 null이거나 비어있습니다.");
        }

        Matcher matcher = DMS_PATTERN.matcher(dmsString);

        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "입력된 문자열의 형식이 올바르지 않습니다. " + "예상 형식: \"37°33'45\\\"N 126°58'37\\\"E\"");
        }

        // 위도 추출 및 변환
        String latDegree = matcher.group(1);
        String latMinute = matcher.group(2);
        String latSecond = matcher.group(3);
        String latDirection = matcher.group(4);
        double latitude = dmsToDecimal(latDegree, latMinute, latSecond, latDirection);

        // 경도 추출 및 변환
        String lonDegree = matcher.group(5);
        String lonMinute = matcher.group(6);
        String lonSecond = matcher.group(7);
        String lonDirection = matcher.group(8);
        double longitude = dmsToDecimal(lonDegree, lonMinute, lonSecond, lonDirection);

        return new double[] {latitude, longitude};
    }

    /**
     * DMS 형식 문자열을 GeoCoordinate 객체로 변환
     *
     * @param dmsString DMS 형식 문자열
     * @return GeoCoordinate 객체
     * @throws IllegalArgumentException 문자열 형식이 올바르지 않거나 값이 유효 범위를 벗어난 경우
     */
    public GeoCoordinate convertToGeoCoordinate(String dmsString) {
        double[] coords = convertDmsToDecimal(dmsString);
        return new GeoCoordinate(coords[0], coords[1]);
    }

    /**
     * DMS 값의 유효성 검증
     *
     * @param degrees 도
     * @param minutes 분
     * @param seconds 초
     * @param direction 방향
     * @throws IllegalArgumentException 값이 유효 범위를 벗어난 경우
     */
    private void validateDmsValues(
            double degrees, double minutes, double seconds, String direction) {
        // 도 범위 검증 (위도: 0-90, 경도: 0-180)
        double maxDegrees = ("N".equals(direction) || "S".equals(direction)) ? 90 : 180;
        if (degrees < 0 || degrees > maxDegrees) {
            throw new IllegalArgumentException(
                    String.format(
                            "도(degree) 값은 0-%d 범위여야 합니다. 입력값: %.2f", (int) maxDegrees, degrees));
        }

        // 분 범위 검증 (0-59)
        if (minutes < 0 || minutes >= 60) {
            throw new IllegalArgumentException(
                    String.format("분(minute) 값은 0-59 범위여야 합니다. 입력값: %.2f", minutes));
        }

        // 초 범위 검증 (0-59.999...)
        if (seconds < 0 || seconds >= 60) {
            throw new IllegalArgumentException(
                    String.format("초(second) 값은 0-59.999... 범위여야 합니다. 입력값: %.2f", seconds));
        }

        // 방향 검증
        if (!"N".equals(direction)
                && !"S".equals(direction)
                && !"E".equals(direction)
                && !"W".equals(direction)) {
            throw new IllegalArgumentException(
                    String.format("방향(direction)은 N, S, E, W 중 하나여야 합니다. 입력값: %s", direction));
        }
    }

    /**
     * 지리 좌표를 나타내는 불변 객체
     *
     * <p>위도(latitude)와 경도(longitude)를 포함합니다.
     */
    public static class GeoCoordinate {
        private final double latitude;
        private final double longitude;

        /**
         * GeoCoordinate 생성자
         *
         * @param latitude 위도 (-90 ~ 90)
         * @param longitude 경도 (-180 ~ 180)
         */
        public GeoCoordinate(double latitude, double longitude) {
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException(
                        String.format("위도는 -90~90 범위여야 합니다. 입력값: %.6f", latitude));
            }
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException(
                        String.format("경도는 -180~180 범위여야 합니다. 입력값: %.6f", longitude));
            }
            this.latitude = latitude;
            this.longitude = longitude;
        }

        /**
         * 위도 반환
         *
         * @return 위도
         */
        public double getLatitude() {
            return latitude;
        }

        /**
         * 경도 반환
         *
         * @return 경도
         */
        public double getLongitude() {
            return longitude;
        }

        @Override
        public String toString() {
            return String.format(
                    "GeoCoordinate{latitude=%.6f, longitude=%.6f}", latitude, longitude);
        }
    }
}
