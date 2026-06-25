package com.smartattendance.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * AttendanceCalculator - Attendance Calculation Utility Class
 *
 * Responsibilities:
 *  - Calculate attendance percentage for a student in a subject.
 *  - Determine attendance status (Safe, Warning, Critical, Defaulter).
 *  - Calculate classes needed to reach a target percentage.
 *  - Calculate how many classes a student can afford to miss.
 *
 * Pattern: Utility Class (all static methods, no instantiation).
 */
public final class AttendanceCalculator {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(AttendanceCalculator.class);

    // ----------------------------------------------------------------
    // Attendance Threshold Constants
    // These mirror the college's attendance policy.
    // ----------------------------------------------------------------

    /** Minimum required attendance percentage to be eligible for exams. */
    public static final double MINIMUM_REQUIRED_PERCENTAGE = 75.0;

    /** Warning threshold: student is approaching the minimum limit. */
    public static final double WARNING_THRESHOLD = 80.0;

    /** Safe threshold: student has comfortable attendance. */
    public static final double SAFE_THRESHOLD = 85.0;

    // ----------------------------------------------------------------
    // Attendance Status Labels
    // ----------------------------------------------------------------
    public static final String STATUS_SAFE      = "Safe";
    public static final String STATUS_WARNING   = "Warning";
    public static final String STATUS_CRITICAL  = "Critical";
    public static final String STATUS_DEFAULTER = "Defaulter";

    // ----------------------------------------------------------------
    // Private Constructor (Utility class - no instantiation)
    // ----------------------------------------------------------------
    private AttendanceCalculator() {
        throw new UnsupportedOperationException(
                "AttendanceCalculator is a utility class and cannot be instantiated."
        );
    }

    // ================================================================
    // CORE CALCULATION METHODS
    // ================================================================

    /**
     * Calculates the attendance percentage for a student.
     *
     * <p>Formula: (classesPresent / totalClasses) * 100</p>
     *
     * @param totalClasses   Total number of classes conducted.
     * @param classesPresent Number of classes attended by the student.
     * @return Attendance percentage rounded to 2 decimal places,
     *         or 0.0 if totalClasses is zero.
     */
    public static double calculatePercentage(int totalClasses, int classesPresent) {
        if (totalClasses <= 0) {
            logger.debug("calculatePercentage: totalClasses is 0 or negative. Returning 0.0.");
            return 0.0;
        }

        if (classesPresent < 0) {
            logger.warn("calculatePercentage: classesPresent is negative ({}). Returning 0.0.", classesPresent);
            return 0.0;
        }

        if (classesPresent > totalClasses) {
            logger.warn("calculatePercentage: classesPresent ({}) > totalClasses ({}). Capping at 100.0.",
                    classesPresent, totalClasses);
            return 100.0;
        }

        double percentage = ((double) classesPresent / totalClasses) * 100.0;
        return Math.round(percentage * 100.0) / 100.0;
    }

    /**
     * Calculates attendance percentage treating 'Late' as 'Present'.
     *
     * @param totalClasses   Total number of classes conducted.
     * @param classesPresent Number of classes attended (Present).
     * @param classesLate    Number of classes marked Late.
     * @return Attendance percentage including Late attendance, rounded to 2 decimal places.
     */
    public static double calculatePercentageWithLate(int totalClasses, int classesPresent, int classesLate) {
        int effectivePresent = classesPresent + classesLate;
        return calculatePercentage(totalClasses, effectivePresent);
    }

    // ================================================================
    // STATUS DETERMINATION METHODS
    // ================================================================

    /**
     * Determines the attendance status label based on percentage.
     *
     * <p>Status levels:
     * <ul>
     *   <li>{@code Defaulter} : below 75% (below minimum required)</li>
     *   <li>{@code Critical}  : 75% to below 80% (at risk)</li>
     *   <li>{@code Warning}   : 80% to below 85% (approaching safe zone)</li>
     *   <li>{@code Safe}      : 85% and above</li>
     * </ul>
     * </p>
     *
     * @param percentage The computed attendance percentage.
     * @return One of: "Defaulter", "Critical", "Warning", "Safe".
     */
    public static String getAttendanceStatus(double percentage) {
        if (percentage < MINIMUM_REQUIRED_PERCENTAGE) {
            return STATUS_DEFAULTER;
        } else if (percentage < WARNING_THRESHOLD) {
            return STATUS_CRITICAL;
        } else if (percentage < SAFE_THRESHOLD) {
            return STATUS_WARNING;
        } else {
            return STATUS_SAFE;
        }
    }

    /**
     * Returns a Bootstrap CSS class name for color-coding attendance status.
     *
     * <ul>
     *   <li>Defaulter → "danger"   (red)</li>
     *   <li>Critical  → "danger"   (red)</li>
     *   <li>Warning   → "warning"  (yellow)</li>
     *   <li>Safe      → "success"  (green)</li>
     * </ul>
     *
     * @param percentage The attendance percentage.
     * @return Bootstrap color class name (danger / warning / success).
     */
    public static String getStatusBadgeClass(double percentage) {
        String status = getAttendanceStatus(percentage);
        return switch (status) {
            case STATUS_DEFAULTER, STATUS_CRITICAL -> "danger";
            case STATUS_WARNING                    -> "warning";
            default                                -> "success";
        };
    }

    /**
     * Checks whether a student is eligible to appear for exams
     * based on the minimum required attendance percentage.
     *
     * @param percentage The student's attendance percentage.
     * @return {@code true} if attendance meets or exceeds the minimum requirement.
     */
    public static boolean isEligibleForExam(double percentage) {
        return percentage >= MINIMUM_REQUIRED_PERCENTAGE;
    }

    // ================================================================
    // PROJECTION / ADVISORY METHODS
    // ================================================================

    /**
     * Calculates how many more classes a student needs to attend
     * consecutively to reach a target percentage.
     *
     * <p>Formula (solving for x):
     * (classesPresent + x) / (totalClasses + x) >= targetPercent / 100</p>
     *
     * @param totalClasses   Total classes conducted so far.
     * @param classesPresent Classes attended so far.
     * @param targetPercent  Target attendance percentage to achieve.
     * @return Number of consecutive classes to attend, or 0 if already met.
     *         Returns -1 if target is impossible (> 100%).
     */
    public static int classesNeededToReachTarget(
            int totalClasses,
            int classesPresent,
            double targetPercent) {

        if (targetPercent > 100.0) {
            logger.warn("classesNeededToReachTarget: targetPercent {} > 100. Impossible.", targetPercent);
            return -1;
        }

        double currentPercentage = calculatePercentage(totalClasses, classesPresent);

        if (currentPercentage >= targetPercent) {
            return 0; // Already at or above target
        }

        // Solve: (classesPresent + x) / (totalClasses + x) >= target / 100
        // => 100 * (classesPresent + x) >= target * (totalClasses + x)
        // => 100 * classesPresent + 100x >= target * totalClasses + target * x
        // => x * (100 - target) >= target * totalClasses - 100 * classesPresent
        // => x >= (target * totalClasses - 100 * classesPresent) / (100 - target)

        double numerator   = (targetPercent * totalClasses) - (100.0 * classesPresent);
        double denominator = 100.0 - targetPercent;

        if (denominator <= 0) {
            return -1; // 100% target is practically unachievable mid-term
        }

        int classesNeeded = (int) Math.ceil(numerator / denominator);
        return Math.max(classesNeeded, 0);
    }

    /**
     * Calculates how many classes a student can afford to miss
     * while still maintaining the minimum required attendance.
     *
     * <p>Formula (solving for x where x = classes that can be missed):
     * (classesPresent) / (totalClasses + futureClasses - x) >= minimumPercent / 100</p>
     *
     * <p>Simplified approach: Given current state, how many total
     * future classes can be missed?</p>
     *
     * @param totalClasses   Total classes conducted so far.
     * @param classesPresent Classes attended so far.
     * @return Number of future classes the student can miss
     *         (assuming all remaining classes in the semester are considered),
     *         or 0 if already below or at minimum.
     */
    public static int classesCanAffordToMiss(int totalClasses, int classesPresent) {
        if (totalClasses <= 0) {
            return 0;
        }

        double currentPercentage = calculatePercentage(totalClasses, classesPresent);

        if (currentPercentage < MINIMUM_REQUIRED_PERCENTAGE) {
            return 0; // Already below minimum, cannot miss any more
        }

        // Solve for max absentable classes (x):
        // classesPresent / (totalClasses + x) >= MINIMUM / 100
        // => 100 * classesPresent >= MINIMUM * (totalClasses + x)
        // => x <= (100 * classesPresent / MINIMUM) - totalClasses

        double maxAffordable = ((100.0 * classesPresent) / MINIMUM_REQUIRED_PERCENTAGE) - totalClasses;
        return Math.max((int) Math.floor(maxAffordable), 0);
    }

    // ================================================================
    // OVERALL / AGGREGATE METHODS
    // ================================================================

    /**
     * Calculates the overall attendance percentage across all subjects.
     *
     * @param totalClassesAllSubjects   Sum of total classes across all subjects.
     * @param presentClassesAllSubjects Sum of classes attended across all subjects.
     * @return Overall attendance percentage, rounded to 2 decimal places.
     */
    public static double calculateOverallPercentage(
            int totalClassesAllSubjects,
            int presentClassesAllSubjects) {
        return calculatePercentage(totalClassesAllSubjects, presentClassesAllSubjects);
    }
}