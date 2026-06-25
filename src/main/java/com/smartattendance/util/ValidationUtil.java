package com.smartattendance.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * ValidationUtil - Input Validation Utility Class
 *
 * Responsibilities:
 *  - Validate user inputs from HTML forms before processing.
 *  - Provide reusable regex-based validators.
 *  - Sanitize strings to prevent XSS and SQL Injection (basic).
 *
 * Pattern: Utility Class (all static methods, no instantiation).
 */
public final class ValidationUtil {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(ValidationUtil.class);

    // ----------------------------------------------------------------
    // Regex Patterns
    // ----------------------------------------------------------------

    /** Standard email format validation */
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,6}$");

    /** Indian mobile number: 10 digits, starts with 6-9 */
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^[6-9]\\d{9}$");

    /** Student ID: Alphanumeric, 3-20 characters (e.g., STU001) */
    private static final Pattern STUDENT_ID_PATTERN =
            Pattern.compile("^[A-Za-z0-9]{3,20}$");

    /** Faculty ID: Alphanumeric, 3-20 characters (e.g., FAC001) */
    private static final Pattern FACULTY_ID_PATTERN =
            Pattern.compile("^[A-Za-z0-9]{3,20}$");

    /** Subject Code: Alphanumeric + hyphen, 3-20 characters (e.g., CSE401) */
    private static final Pattern SUBJECT_CODE_PATTERN =
            Pattern.compile("^[A-Za-z0-9\\-]{3,20}$");

    /** Name: Letters, spaces, hyphens, apostrophes only */
    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-Z\\s'\\-]{2,100}$");

    /** Marks: Non-negative decimal number (e.g., 85.50) */
    private static final Pattern MARKS_PATTERN =
            Pattern.compile("^\\d{1,3}(\\.\\d{1,2})?$");

    // ----------------------------------------------------------------
    // Private Constructor (Utility class - no instantiation)
    // ----------------------------------------------------------------
    private ValidationUtil() {
        throw new UnsupportedOperationException(
                "ValidationUtil is a utility class and cannot be instantiated."
        );
    }

    // ================================================================
    // NULL / EMPTY CHECKS
    // ================================================================

    /**
     * Checks if a string is null or blank (empty or whitespace only).
     *
     * @param value The string to check.
     * @return {@code true} if null or blank.
     */
    public static boolean isNullOrBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Checks if a string is not null and not blank.
     *
     * @param value The string to check.
     * @return {@code true} if not null and not blank.
     */
    public static boolean isNotBlank(String value) {
        return !isNullOrBlank(value);
    }

    // ================================================================
    // FORMAT VALIDATORS
    // ================================================================

    /**
     * Validates an email address format.
     *
     * @param email The email string to validate.
     * @return {@code true} if the email format is valid.
     */
    public static boolean isValidEmail(String email) {
        if (isNullOrBlank(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates an Indian phone number (10 digits, starts with 6-9).
     *
     * @param phone The phone number string to validate.
     * @return {@code true} if the phone number format is valid.
     */
    public static boolean isValidPhone(String phone) {
        if (isNullOrBlank(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Validates a Student ID format (alphanumeric, 3-20 chars).
     *
     * @param studentId The student ID to validate.
     * @return {@code true} if the student ID format is valid.
     */
    public static boolean isValidStudentId(String studentId) {
        if (isNullOrBlank(studentId)) {
            return false;
        }
        return STUDENT_ID_PATTERN.matcher(studentId.trim()).matches();
    }

    /**
     * Validates a Faculty ID format (alphanumeric, 3-20 chars).
     *
     * @param facultyId The faculty ID to validate.
     * @return {@code true} if the faculty ID format is valid.
     */
    public static boolean isValidFacultyId(String facultyId) {
        if (isNullOrBlank(facultyId)) {
            return false;
        }
        return FACULTY_ID_PATTERN.matcher(facultyId.trim()).matches();
    }

    /**
     * Validates a Subject Code format.
     *
     * @param subjectCode The subject code to validate.
     * @return {@code true} if the subject code format is valid.
     */
    public static boolean isValidSubjectCode(String subjectCode) {
        if (isNullOrBlank(subjectCode)) {
            return false;
        }
        return SUBJECT_CODE_PATTERN.matcher(subjectCode.trim()).matches();
    }

    /**
     * Validates a name (letters, spaces, hyphens, apostrophes).
     *
     * @param name The name to validate.
     * @return {@code true} if the name format is valid.
     */
    public static boolean isValidName(String name) {
        if (isNullOrBlank(name)) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    /**
     * Validates a marks value (non-negative decimal, up to 999.99).
     *
     * @param marks The marks string to validate.
     * @return {@code true} if the marks value is in valid format.
     */
    public static boolean isValidMarks(String marks) {
        if (isNullOrBlank(marks)) {
            return false;
        }
        return MARKS_PATTERN.matcher(marks.trim()).matches();
    }

    // ================================================================
    // RANGE VALIDATORS
    // ================================================================

    /**
     * Validates that a semester value is between 1 and 8.
     *
     * @param semester The semester number to validate.
     * @return {@code true} if semester is between 1 and 8 (inclusive).
     */
    public static boolean isValidSemester(int semester) {
        return semester >= 1 && semester <= 8;
    }

    /**
     * Validates that marks obtained do not exceed the maximum allowed marks.
     *
     * @param marksObtained The marks scored by the student.
     * @param maxMarks      The maximum marks allowed for the exam.
     * @return {@code true} if marksObtained is between 0 and maxMarks (inclusive).
     */
    public static boolean isMarksInRange(double marksObtained, double maxMarks) {
        return marksObtained >= 0 && marksObtained <= maxMarks;
    }

    /**
     * Validates that a string does not exceed a maximum length.
     *
     * @param value     The string to check.
     * @param maxLength The maximum allowed length.
     * @return {@code true} if the string length is within the limit.
     */
    public static boolean isWithinLength(String value, int maxLength) {
        if (isNullOrBlank(value)) {
            return true;
        }
        return value.trim().length() <= maxLength;
    }

    // ================================================================
    // SANITIZATION METHODS
    // ================================================================

    /**
     * Sanitizes a string to prevent basic XSS attacks by escaping
     * HTML special characters.
     *
     * <p>Replaces: {@code &, <, >, ", '} with their HTML entity equivalents.</p>
     *
     * @param input The raw input string from user.
     * @return Sanitized string safe for HTML rendering, or empty string if null.
     */
    public static String sanitizeHtml(String input) {
        if (input == null) {
            return "";
        }
        return input
                .replace("&",  "&amp;")
                .replace("<",  "&lt;")
                .replace(">",  "&gt;")
                .replace("\"", "&quot;")
                .replace("'",  "&#x27;");
    }

    /**
     * Trims and returns the string, or empty string if null.
     *
     * @param input The raw input string.
     * @return Trimmed string, or empty string if null.
     */
    public static String safeTrim(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }

    /**
     * Trims and converts a string to lowercase.
     * Useful for normalizing emails before DB lookup.
     *
     * @param input The raw input string.
     * @return Trimmed lowercase string, or empty string if null.
     */
    public static String normalizeEmail(String email) {
        if (isNullOrBlank(email)) {
            return "";
        }
        return email.trim().toLowerCase();
    }
}