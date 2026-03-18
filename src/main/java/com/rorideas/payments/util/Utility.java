package com.rorideas.payments.util;

import lombok.experimental.UtilityClass;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;

/**
 * Utility class for date and time operations related to the user's time zone.
 * This class provides methods to calculate the start and end of the day for a given Instant, as well as methods to calculate the end of the day plus a specified number of months.
 * The methods in this class use the user's time zone, which is obtained from the SecurityUtils class, to ensure that the calculations are accurate for the user's local time.
 */
@UtilityClass
public class Utility {

    /**
     * Returns the start of the day (00:00:00) for the given Instant in UTC.
     *
     * @param instant The Instant for which to calculate the start of the day.
     * @return An Instant representing the start of the day in UTC.
     */
    public static Instant startDay(Instant instant) {
        ZoneId zone = SecurityUtils.getUserZone();
        return instant
                .atZone(zone)
                .toLocalDate()
                .atStartOfDay(zone)
                .toInstant();
    }

    /**
     * Returns the end of the day (23:59:59.999999999) for the given Instant in UTC.
     *
     * @param instant The Instant for which to calculate the end of the day.
     * @return An Instant representing the end of the day in UTC.
     */
    public static Instant endDay(Instant instant) {
        ZoneId zone = SecurityUtils.getUserZone();
        return instant
                .atZone(zone)
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .atZone(zone)
                .toInstant();
    }

    /**
     * Returns the end of the day (23:59:59.999999999) for the given Instant in UTC.
     *
     * @param instant The Instant for which to calculate the end of the day.
     * @return An Instant representing the end of the day in UTC.
     */
    public static Instant plusMonth(Instant instant, int months, ZoneId zone) {
        return instant
                .atZone(zone)
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .atZone(zone)
                .plusMonths(months)
                .toInstant();
    }

    /**
     * Returns the start of the day (00:00:00) for the given Instant in UTC.
     *
     * @param instant The Instant for which to calculate the start of the day.
     * @return An Instant representing the start of the day in UTC.
     */
    public static Instant startDayWhitZone(Instant instant, ZoneId zone) {
        return instant
                .atZone(zone)
                .toLocalDate()
                .atStartOfDay(zone)
                .toInstant();
    }

    /**
     * Returns the end of the day (23:59:59.999999999) for the given Instant in UTC.
     *
     * @param instant The Instant for which to calculate the end of the day.
     * @return An Instant representing the end of the day in UTC.
     */
    public static Instant endDayWhitZone(Instant instant, ZoneId zone) {
        return instant
                .atZone(zone)
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .atZone(zone)
                .toInstant();
    }
}
