package com.tallybot.backend.tallybot_back.util;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    // 두 가지 날짜 형식을 지원하는 포맷터
    private static final DateTimeFormatter FORMATTER_1 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 공백을 사용하는 형식
    private static final DateTimeFormatter FORMATTER_2 = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); // T를 사용하는 형식

    // 두 형식 중 하나로 날짜 파싱
    public static LocalDateTime parseDate(String dateStr) {
        LocalDateTime parsedDate = null;

        try {
            // 첫 번째 형식 (yyyy-MM-dd HH:mm:ss) - 공백 형식
            parsedDate = LocalDateTime.parse(dateStr, FORMATTER_1);
        } catch (Exception e) {
            // 두 번째 형식 (yyyy-MM-dd'T'HH:mm:ss) - T 포함 형식
            try {
                parsedDate = LocalDateTime.parse(dateStr, FORMATTER_2);
            } catch (Exception ex) {
                throw new IllegalArgumentException("날짜 형식이 잘못되었습니다. 형식: yyyy-MM-dd HH:mm:ss 또는 yyyy-MM-dd'T'HH:mm:ss");
            }
        }

        return parsedDate;
    }
}
