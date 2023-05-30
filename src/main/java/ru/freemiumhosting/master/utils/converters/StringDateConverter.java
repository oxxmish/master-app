package ru.freemiumhosting.master.utils.converters;

import java.time.OffsetDateTime;

public class StringDateConverter {
    public static String fromDate(OffsetDateTime date) {
        return date.toString();
    }

    public static OffsetDateTime fromString(String date) {
        return OffsetDateTime.parse(date);
    }
}
