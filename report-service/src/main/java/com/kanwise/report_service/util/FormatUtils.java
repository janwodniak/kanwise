package com.kanwise.report_service.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang.time.DurationFormatUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.time.LocalDateTime.of;

@UtilityClass
public class FormatUtils {

    public static String formatLocalDateTime(Integer[] array) {
        Integer[] localDateTimeArray = new Integer[7];
        for (int i = 0; i < 7; i++) {
            localDateTimeArray[i] = array.length > i ? array[i] : 0;
        }

        LocalDateTime localDateTime = of(
                localDateTimeArray[0],
                localDateTimeArray[1],
                localDateTimeArray[2],
                localDateTimeArray[3],
                localDateTimeArray[4],
                localDateTimeArray[5]);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return localDateTime.format(dateTimeFormatter);
    }

    public static String formatDurationFromSeconds(Long s) {
        return DurationFormatUtils.formatDurationWords(Duration.ofSeconds(s).toMillis(), true, true);
    }
}
