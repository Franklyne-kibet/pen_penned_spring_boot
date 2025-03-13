package com.pen_penned.blog.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimeZoneUtil {

    public static ZonedDateTime convertToUserTimeZone(
            LocalDateTime utcDateTime, String userTimeZone) {
        return utcDateTime.atZone(ZoneId.of("UTC"))
                .withZoneSameInstant(ZoneId.of(userTimeZone));
    }
}
