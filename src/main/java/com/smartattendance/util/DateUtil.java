package com.smartattendance.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

/**
 * DateUtil - Date and Time Utility Class
 *
 * Responsibilities:
 *  - Format dates and date-times for display in JSP views.
 *  - Parse date strings from HTML form inputs.
 *  - Calculate differences between dates.
 *  - Provide commonly used date constants.
 *
 * Uses: Java 17 java.time API (LocalDate, LocalDateTime).
 *
 * Pattern: Utility Class (all static methods, no instantiation).
 */
public final class DateUtil {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(DateUtil.class);

    // ----------------------------------------------------------------
    // Date Format Constants
    // ----------------------------------------------------------------

    /** Display format: 15 Jan 2024 */
    public static final String FORMAT_DISPLAY_DATE        = "dd MMM yyyy";

    /** Display format: 15 Jan 2024, 10:30 AM */
    public static final String FORMAT_DISPLAY_DATETIME    = "dd MMM yyyy, hh:mm a";

    /** HTML input[type=date] format: 2024-01-15 */
    public static final String FORMAT_HTML_DATE           = "yyyy-MM-dd";

    /** Database / SQL format: 2024-01-15 */
    public static final String FORMAT_DB_DATE             = "yyyy-MM-dd";

    /** Report heading format: January 2024 */
    public static final String FORMAT_MONTH_YEAR          = "MMMM yyyy";

    /** Short date format: 15/01/2024 */
    public static final String FORMAT_SHORT_DATE          = "dd/MM/yyyy";

    // ----------------------------------------------------------------
    // DateTimeFormatter Instances
    // ----------------------------------------------------------------
    private static final DateTimeFormatter FORMATTER_DISPLAY_DATE     =
            DateTimeFormatter.ofPattern(FORMAT_DISPLAY_DATE);

    private static final DateTimeFormatter FORMATTER_DISPLAY_DATETIME =
            DateTimeFormatter.ofPattern(FORMAT_DISPLAY_DATETIME);

    private static final DateTimeFormatter FORMATTER_HTML_DATE        =
            DateTimeFormatter.ofPattern(FORMAT_HTML_DATE);

    private static final DateTimeFormatter FORMATTER_DB_DATE          =
            DateTimeFormatter.ofPattern(FORMAT_DB_DATE);

    private static final DateTimeFormatter FORMATTER_MONTH_YEAR       =
            DateTimeFormatter.ofPattern(FORMAT_MONTH_YEAR);

    private static final DateTimeFormatter FORMATTER_SHORT_DATE       =
            DateTimeFormatter.ofPattern(FORMAT_SHORT_DATE);

    // ----------------------------------------------------------------
    // Private Constructor (Utility class - no instantiation)
    // ----------------------------------------------------------------
    private DateUtil() {
        throw new UnsupportedOperationException(
                "DateUtil is a utility class and cannot be instantiated."
        );
    }

    // ================================================================
    // DATE RETRIEVAL METHODS
    // ================================================================

    /**
     * Returns today's date as a {@link LocalDate}.
     *
     * @return Today's {@link LocalDate}.
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * Returns the current date and time as a {@link LocalDateTime}.
     *
     * @return Current {@link LocalDateTime}.
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Returns today's date as a string formatted for HTML date inputs.
     * Format: yyyy-MM-dd (e.g., "2024-08-15")
     *
     * @return Today's date as an HTML-compatible string.
     */
    public static String todayAsHtmlDate() {
        return LocalDate.now().format(FORMATTER_HTML_DATE);
    }

    // ================================================================
    // FORMATTING METHODS
    // ================================================================

    /**
     * Formats a {@link LocalDate} for user-friendly display.
     * Format: dd MMM yyyy (e.g., "15 Aug 2024")
     *
     * @param date The {@link LocalDate} to format.
     * @return Formatted date string, or empty string if date is null.
     */
    public static String formatForDisplay(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(FORMATTER_DISPLAY_DATE);
    }

    /**
     * Formats a {@link LocalDateTime} for user-friendly display.
     * Format: dd MMM yyyy, hh:mm a (e.g., "15 Aug 2024, 10:30 AM")
     *
     * @param dateTime The {@link LocalDateTime} to format.
     * @return Formatted date-time string, or empty string if null.
     */
    public static String formatForDisplay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(FORMATTER_DISPLAY_DATETIME);
    }

    /**
     * Formats a {@link LocalDate} for HTML date input fields.
     * Format: yyyy-MM-dd (e.g., "2024-08-15")
     *
     * @param date The {@link LocalDate} to format.
     * @return HTML-compatible date string, or empty string if null.
     */
    public static String formatForHtml(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(FORMATTER_HTML_DATE);
    }

    /**
     * Formats a {@link LocalDate} as Month Year for report headings.
     * Format: MMMM yyyy (e.g., "August 2024")
     *
     * @param date The {@link LocalDate} to format.
     * @return Month-Year string, or empty string if null.
     */
    public static String formatAsMonthYear(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(FORMATTER_MONTH_YEAR);
    }

    /**
     * Formats a {@link LocalDate} in short format.
     * Format: dd/MM/yyyy (e.g., "15/08/2024")
     *
     * @param date The {@link LocalDate} to format.
     * @return Short date string, or empty string if null.
     */
    public static String formatShort(LocalDate date) {
        if (date == null) {
            return "";
        }
        return date.format(FORMATTER_SHORT_DATE);
    }

    // ================================================================
    // PARSING METHODS
    // ================================================================

    /**
     * Parses a date string from an HTML input[type=date] field.
     * Expected format: yyyy-MM-dd
     *
     * @param dateString The date string to parse.
     * @return Parsed {@link LocalDate}, or {@code null} if parsing fails.
     */
    public static LocalDate parseHtmlDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.trim(), FORMATTER_HTML_DATE);
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse HTML date string: '{}'", dateString);
            return null;
        }
    }

    /**
     * Parses a date string in dd/MM/yyyy format.
     *
     * @param dateString The date string to parse (format: dd/MM/yyyy).
     * @return Parsed {@link LocalDate}, or {@code null} if parsing fails.
     */
    public static LocalDate parseShortDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateString.trim(), FORMATTER_SHORT_DATE);
        } catch (DateTimeParseException e) {
            logger.warn("Failed to parse short date string: '{}'", dateString);
            return null;
        }
    }

    // ================================================================
    // CALCULATION METHODS
    // ================================================================

    /**
     * Calculates the number of days between two dates (inclusive of start).
     *
     * @param startDate The start date.
     * @param endDate   The end date.
     * @return Number of days between the two dates, or -1 if dates are null.
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            logger.warn("daysBetween: one or both dates are null.");
            return -1L;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * Checks if a given date is today.
     *
     * @param date The {@link LocalDate} to check.
     * @return {@code true} if the date is today.
     */
    public static boolean isToday(LocalDate date) {
        return date != null && date.equals(LocalDate.now());
    }

    /**
     * Checks if a given date is in the past.
     *
     * @param date The {@link LocalDate} to check.
     * @return {@code true} if the date is before today.
     */
    public static boolean isPastDate(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    /**
     * Checks if a given date is in the future.
     *
     * @param date The {@link LocalDate} to check.
     * @return {@code true} if the date is after today.
     */
    public static boolean isFutureDate(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    /**
     * Converts a {@link java.sql.Date} to {@link LocalDate}.
     * Useful when reading DATE values from JDBC ResultSet.
     *
     * @param sqlDate The {@link java.sql.Date} from JDBC.
     * @return Equivalent {@link LocalDate}, or {@code null} if input is null.
     */
    public static LocalDate fromSqlDate(java.sql.Date sqlDate) {
        if (sqlDate == null) {
            return null;
        }
        return sqlDate.toLocalDate();
    }

    /**
     * Converts a {@link LocalDate} to {@link java.sql.Date}.
     * Useful when setting DATE parameters in PreparedStatement.
     *
     * @param localDate The {@link LocalDate} to convert.
     * @return Equivalent {@link java.sql.Date}, or {@code null} if input is null.
     */
    public static java.sql.Date toSqlDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return java.sql.Date.valueOf(localDate);
    }
}