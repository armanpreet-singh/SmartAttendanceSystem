package com.smartattendance.dao;

import com.smartattendance.model.AttendanceSummary;
import com.smartattendance.model.Marks;
import com.smartattendance.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * ReportDAO.java - Data Access Object for Reports Module
 *
 * Responsibilities:
 *  - Fetch attendance reports for students and faculty
 *  - Fetch marks reports
 *  - Fetch daily, monthly, and subject-wise reports
 *  - Identify low-attendance students
 *  - Fetch data needed for export (PDF/Excel)
 *
 * All queries use PreparedStatement to prevent SQL injection.
 * All resources are closed via DBConnection.closeResources().
 */
public class ReportDAO {

    private static final Logger logger = LoggerFactory.getLogger(ReportDAO.class);

    // ================================================================
    // SQL CONSTANTS — STUDENT REPORTS
    // ================================================================

    /**
     * Overall attendance summary for a student across all subjects.
     */
    private static final String SQL_STUDENT_OVERALL_ATTENDANCE =
            "SELECT " +
            "    att.subject_id, " +
            "    sub.subject_code, " +
            "    sub.subject_name, " +
            "    sub.credits, " +
            "    sub.semester, " +
            "    att.total_classes, " +
            "    att.classes_present, " +
            "    att.classes_absent, " +
            "    att.classes_late, " +
            "    att.attendance_percentage, " +
            "    f.first_name  AS faculty_first, " +
            "    f.last_name   AS faculty_last " +
            "FROM   attendance_summary att " +
            "INNER JOIN subjects    sub ON att.subject_id  = sub.id " +
            "INNER JOIN faculty     f   ON sub.faculty_id  = f.id " +
            "WHERE  att.student_id      = ? " +
            "  AND  att.academic_year_id = ? " +
            "ORDER BY sub.subject_code ASC";

    /**
     * Date-wise attendance detail for a student in one subject.
     */
    private static final String SQL_STUDENT_SUBJECT_ATTENDANCE_DETAIL =
            "SELECT " +
            "    a.attendance_date, " +
            "    a.status, " +
            "    a.remarks, " +
            "    sub.subject_code, " +
            "    sub.subject_name " +
            "FROM   attendance a " +
            "INNER JOIN subjects sub ON a.subject_id = sub.id " +
            "WHERE  a.student_id  = ? " +
            "  AND  a.subject_id  = ? " +
            "ORDER BY a.attendance_date DESC";

    /**
     * Full marks report for a student — all subjects, all exam types.
     */
    private static final String SQL_STUDENT_MARKS_REPORT =
            "SELECT " +
            "    m.subject_id, " +
            "    sub.subject_code, " +
            "    sub.subject_name, " +
            "    sub.credits, " +
            "    et.exam_name, " +
            "    et.max_marks, " +
            "    m.marks_obtained, " +
            "    m.grade, " +
            "    m.remarks, " +
            "    m.recorded_at " +
            "FROM   marks m " +
            "INNER JOIN subjects   sub ON m.subject_id   = sub.id " +
            "INNER JOIN exam_types et  ON m.exam_type_id = et.id " +
            "WHERE  m.student_id      = ? " +
            "  AND  m.academic_year_id = ? " +
            "ORDER BY sub.subject_code ASC, et.id ASC";

    /**
     * Performance summary per subject (total marks, percentage, best/worst).
     */
    private static final String SQL_STUDENT_PERFORMANCE_SUMMARY =
            "SELECT " +
            "    sub.subject_code, " +
            "    sub.subject_name, " +
            "    sub.credits, " +
            "    SUM(m.marks_obtained)   AS total_obtained, " +
            "    SUM(et.max_marks)       AS total_max, " +
            "    ROUND(SUM(m.marks_obtained) * 100.0 / " +
            "          NULLIF(SUM(et.max_marks), 0), 2) AS overall_pct, " +
            "    MAX(m.marks_obtained)   AS best_score, " +
            "    MIN(m.marks_obtained)   AS lowest_score " +
            "FROM   marks m " +
            "INNER JOIN subjects   sub ON m.subject_id   = sub.id " +
            "INNER JOIN exam_types et  ON m.exam_type_id = et.id " +
            "WHERE  m.student_id      = ? " +
            "  AND  m.academic_year_id = ? " +
            "GROUP BY sub.id, sub.subject_code, sub.subject_name, sub.credits " +
            "ORDER BY overall_pct DESC";

    /**
     * Monthly attendance summary for a student in a specific month.
     */
    private static final String SQL_STUDENT_MONTHLY_ATTENDANCE =
            "SELECT " +
            "    sub.subject_code, " +
            "    sub.subject_name, " +
            "    COUNT(a.id)                                             AS total, " +
            "    SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END)  AS present, " +
            "    SUM(CASE WHEN a.status = 'Absent'  THEN 1 ELSE 0 END)  AS absent, " +
            "    SUM(CASE WHEN a.status = 'Late'    THEN 1 ELSE 0 END)  AS late, " +
            "    ROUND(SUM(CASE WHEN a.status IN ('Present','Late') THEN 1 ELSE 0 END) " +
            "          * 100.0 / NULLIF(COUNT(a.id), 0), 2)             AS monthly_pct " +
            "FROM   attendance a " +
            "INNER JOIN subjects sub ON a.subject_id = sub.id " +
            "WHERE  a.student_id   = ? " +
            "  AND  MONTH(a.attendance_date) = ? " +
            "  AND  YEAR(a.attendance_date)  = ? " +
            "GROUP BY sub.id, sub.subject_code, sub.subject_name " +
            "ORDER BY sub.subject_code ASC";

    // ================================================================
    // SQL CONSTANTS — FACULTY REPORTS
    // ================================================================

    /**
     * Attendance report for all students in a subject (faculty view).
     */
    private static final String SQL_FACULTY_SUBJECT_ATTENDANCE_REPORT =
            "SELECT " +
            "    s.id          AS student_pk, " +
            "    s.student_id  AS student_code, " +
            "    s.first_name, " +
            "    s.last_name, " +
            "    s.section, " +
            "    att.total_classes, " +
            "    att.classes_present, " +
            "    att.classes_absent, " +
            "    att.classes_late, " +
            "    att.attendance_percentage " +
            "FROM   attendance_summary att " +
            "INNER JOIN students s ON att.student_id = s.id " +
            "WHERE  att.subject_id      = ? " +
            "  AND  att.academic_year_id = ? " +
            "ORDER BY att.attendance_percentage ASC";

    /**
     * Daily attendance detail for a subject on a given date.
     */
    private static final String SQL_FACULTY_DAILY_ATTENDANCE =
            "SELECT " +
            "    s.student_id  AS student_code, " +
            "    s.first_name, " +
            "    s.last_name, " +
            "    s.section, " +
            "    a.status, " +
            "    a.remarks " +
            "FROM   attendance a " +
            "INNER JOIN students s ON a.student_id = s.id " +
            "WHERE  a.subject_id      = ? " +
            "  AND  a.attendance_date = ? " +
            "ORDER BY s.section ASC, s.first_name ASC";

    /**
     * Monthly attendance summary per student for a subject.
     */
    private static final String SQL_FACULTY_MONTHLY_ATTENDANCE =
            "SELECT " +
            "    s.student_id AS student_code, " +
            "    s.first_name, s.last_name, s.section, " +
            "    COUNT(a.id)                                             AS total, " +
            "    SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END)  AS present, " +
            "    SUM(CASE WHEN a.status = 'Absent'  THEN 1 ELSE 0 END)  AS absent, " +
            "    SUM(CASE WHEN a.status = 'Late'    THEN 1 ELSE 0 END)  AS late, " +
            "    ROUND(SUM(CASE WHEN a.status IN ('Present','Late') THEN 1 ELSE 0 END) " +
            "          * 100.0 / NULLIF(COUNT(a.id), 0), 2)             AS monthly_pct " +
            "FROM   attendance a " +
            "INNER JOIN students s ON a.student_id = s.id " +
            "WHERE  a.subject_id                 = ? " +
            "  AND  MONTH(a.attendance_date)      = ? " +
            "  AND  YEAR(a.attendance_date)       = ? " +
            "GROUP BY s.id, s.student_id, s.first_name, s.last_name, s.section " +
            "ORDER BY monthly_pct ASC";

    /**
     * Low attendance students (below threshold) for a subject.
     */
    private static final String SQL_LOW_ATTENDANCE_STUDENTS =
            "SELECT " +
            "    s.id          AS student_pk, " +
            "    s.student_id  AS student_code, " +
            "    s.first_name, " +
            "    s.last_name, " +
            "    s.section, " +
            "    s.email, " +
            "    att.total_classes, " +
            "    att.classes_present, " +
            "    att.classes_absent, " +
            "    att.attendance_percentage, " +
            "    sub.subject_code, " +
            "    sub.subject_name " +
            "FROM   attendance_summary att " +
            "INNER JOIN students s   ON att.student_id  = s.id " +
            "INNER JOIN subjects sub ON att.subject_id  = sub.id " +
            "WHERE  sub.faculty_id          = ? " +
            "  AND  att.academic_year_id     = ? " +
            "  AND  att.attendance_percentage < ? " +
            "ORDER BY att.attendance_percentage ASC, sub.subject_code ASC";

    /**
     * Marks report for all students in a subject (faculty view).
     */
    private static final String SQL_FACULTY_MARKS_REPORT =
            "SELECT " +
            "    s.student_id AS student_code, " +
            "    s.first_name, s.last_name, s.section, " +
            "    et.exam_name, et.max_marks, " +
            "    m.marks_obtained, m.grade " +
            "FROM   student_subjects ss " +
            "INNER JOIN students   s  ON ss.student_id    = s.id " +
            "INNER JOIN exam_types et ON 1 = 1 " +
            "LEFT  JOIN marks      m  ON m.student_id     = s.id " +
            "                       AND m.subject_id      = ss.subject_id " +
            "                       AND m.exam_type_id    = et.id " +
            "                       AND m.academic_year_id = ? " +
            "WHERE  ss.subject_id = ? AND s.is_active = 1 " +
            "ORDER BY s.section ASC, s.first_name ASC, et.id ASC";

    /**
     * Performance statistics per exam type for a subject.
     */
    private static final String SQL_FACULTY_PERFORMANCE_STATS =
            "SELECT " +
            "    et.exam_name, " +
            "    et.max_marks, " +
            "    COUNT(m.id)                   AS appeared, " +
            "    ROUND(AVG(m.marks_obtained), 2) AS avg_marks, " +
            "    MAX(m.marks_obtained)           AS highest, " +
            "    MIN(m.marks_obtained)           AS lowest, " +
            "    SUM(CASE WHEN m.marks_obtained >= et.max_marks * 0.4 " +
            "             THEN 1 ELSE 0 END)     AS passed, " +
            "    SUM(CASE WHEN m.marks_obtained < et.max_marks * 0.4 " +
            "             THEN 1 ELSE 0 END)     AS failed " +
            "FROM   marks m " +
            "INNER JOIN exam_types et ON m.exam_type_id = et.id " +
            "WHERE  m.subject_id      = ? " +
            "  AND  m.academic_year_id = ? " +
            "GROUP BY et.id, et.exam_name, et.max_marks " +
            "ORDER BY et.id ASC";

    /**
     * Day-by-day attendance trend for a subject over a date range.
     */
    private static final String SQL_DAILY_TREND =
            "SELECT " +
            "    a.attendance_date, " +
            "    COUNT(*)                                                 AS total, " +
            "    SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END)   AS present, " +
            "    SUM(CASE WHEN a.status = 'Absent'  THEN 1 ELSE 0 END)   AS absent, " +
            "    ROUND(SUM(CASE WHEN a.status IN ('Present','Late') THEN 1 ELSE 0 END) " +
            "          * 100.0 / NULLIF(COUNT(*), 0), 2)                 AS day_pct " +
            "FROM   attendance a " +
            "WHERE  a.subject_id = ? " +
            "  AND  a.attendance_date BETWEEN ? AND ? " +
            "GROUP BY a.attendance_date " +
            "ORDER BY a.attendance_date ASC";

    /**
     * Monthly trend for a subject (grouped by month).
     */
    private static final String SQL_MONTHLY_TREND =
            "SELECT " +
            "    YEAR(a.attendance_date)  AS yr, " +
            "    MONTH(a.attendance_date) AS mo, " +
            "    COUNT(*)                                                AS total, " +
            "    SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END)  AS present, " +
            "    SUM(CASE WHEN a.status = 'Absent'  THEN 1 ELSE 0 END)  AS absent, " +
            "    ROUND(SUM(CASE WHEN a.status IN ('Present','Late') " +
            "                  THEN 1 ELSE 0 END) " +
            "          * 100.0 / NULLIF(COUNT(*), 0), 2)                AS month_pct " +
            "FROM   attendance a " +
            "WHERE  a.subject_id = ? " +
            "GROUP BY YEAR(a.attendance_date), MONTH(a.attendance_date) " +
            "ORDER BY yr ASC, mo ASC";

    /**
     * Distinct months that have attendance data for a subject.
     */
    private static final String SQL_MONTHS_WITH_DATA =
            "SELECT DISTINCT " +
            "    YEAR(attendance_date)  AS yr, " +
            "    MONTH(attendance_date) AS mo " +
            "FROM   attendance " +
            "WHERE  subject_id = ? " +
            "ORDER BY yr DESC, mo DESC";

    // ================================================================
    // PUBLIC METHODS — STUDENT REPORTS
    // ================================================================

    /**
     * Returns overall attendance summary for a student across all subjects.
     *
     * @param studentPk      Student primary key.
     * @param academicYearId Academic year ID.
     * @return List of maps, each representing one subject's attendance summary.
     */
    public List<Map<String, Object>> getStudentOverallAttendance(
            int studentPk, int academicYearId) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        logger.debug("ReportDAO.getStudentOverallAttendance: pk={}, year={}",
                studentPk, academicYearId);

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_STUDENT_OVERALL_ATTENDANCE);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("subjectId",    rs.getInt("subject_id"));
                row.put("subjectCode",  rs.getString("subject_code"));
                row.put("subjectName",  rs.getString("subject_name"));
                row.put("credits",      rs.getInt("credits"));
                row.put("semester",     rs.getInt("semester"));
                row.put("totalClasses", rs.getInt("total_classes"));
                row.put("present",      rs.getInt("classes_present"));
                row.put("absent",       rs.getInt("classes_absent"));
                row.put("late",         rs.getInt("classes_late"));
                row.put("percentage",   rs.getDouble("attendance_percentage"));
                row.put("facultyName",  rs.getString("faculty_first") + " "
                                        + rs.getString("faculty_last"));
                result.add(row);
            }

            logger.debug("ReportDAO.getStudentOverallAttendance: {} rows fetched.", result.size());

        } catch (SQLException e) {
            logger.error("ReportDAO.getStudentOverallAttendance: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns date-wise attendance detail for a student in one subject.
     *
     * @param studentPk Student primary key.
     * @param subjectId Subject ID.
     * @return List of attendance record maps.
     */
    public List<Map<String, Object>> getStudentSubjectAttendanceDetail(
            int studentPk, int subjectId) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_STUDENT_SUBJECT_ATTENDANCE_DETAIL);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, subjectId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                java.sql.Date d = rs.getDate("attendance_date");
                row.put("date",        d != null ? d.toLocalDate() : null);
                row.put("status",      rs.getString("status"));
                row.put("remarks",     rs.getString("remarks"));
                row.put("subjectCode", rs.getString("subject_code"));
                row.put("subjectName", rs.getString("subject_name"));
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getStudentSubjectAttendanceDetail: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns full marks report for a student.
     *
     * @param studentPk      Student primary key.
     * @param academicYearId Academic year ID.
     * @return List of marks records.
     */
    public List<Map<String, Object>> getStudentMarksReport(
            int studentPk, int academicYearId) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_STUDENT_MARKS_REPORT);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("subjectId",    rs.getInt("subject_id"));
                row.put("subjectCode",  rs.getString("subject_code"));
                row.put("subjectName",  rs.getString("subject_name"));
                row.put("credits",      rs.getInt("credits"));
                row.put("examName",     rs.getString("exam_name"));
                row.put("maxMarks",     rs.getInt("max_marks"));
                row.put("obtained",     rs.getDouble("marks_obtained"));
                row.put("grade",        rs.getString("grade"));
                row.put("remarks",      rs.getString("remarks"));
                java.sql.Timestamp ts = rs.getTimestamp("recorded_at");
                row.put("recordedAt",   ts != null ? ts.toLocalDateTime() : null);
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getStudentMarksReport: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns subject-wise performance summary for a student.
     *
     * @param studentPk      Student primary key.
     * @param academicYearId Academic year ID.
     * @return List of performance summary maps.
     */
    public List<Map<String, Object>> getStudentPerformanceSummary(
            int studentPk, int academicYearId) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_STUDENT_PERFORMANCE_SUMMARY);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("subjectCode",    rs.getString("subject_code"));
                row.put("subjectName",    rs.getString("subject_name"));
                row.put("credits",        rs.getInt("credits"));
                row.put("totalObtained",  rs.getDouble("total_obtained"));
                row.put("totalMax",       rs.getInt("total_max"));
                row.put("overallPct",     rs.getDouble("overall_pct"));
                row.put("bestScore",      rs.getDouble("best_score"));
                row.put("lowestScore",    rs.getDouble("lowest_score"));
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getStudentPerformanceSummary: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns monthly attendance for a student in a given month/year.
     *
     * @param studentPk Student primary key.
     * @param month     Month number (1–12).
     * @param year      Year (e.g., 2024).
     * @return List of monthly attendance maps.
     */
    public List<Map<String, Object>> getStudentMonthlyAttendance(
            int studentPk, int month, int year) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_STUDENT_MONTHLY_ATTENDANCE);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("subjectCode", rs.getString("subject_code"));
                row.put("subjectName", rs.getString("subject_name"));
                row.put("total",       rs.getInt("total"));
                row.put("present",     rs.getInt("present"));
                row.put("absent",      rs.getInt("absent"));
                row.put("late",        rs.getInt("late"));
                row.put("monthlyPct",  rs.getDouble("monthly_pct"));
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getStudentMonthlyAttendance: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    // ================================================================
    // PUBLIC METHODS — FACULTY REPORTS
    // ================================================================

    /**
     * Returns full attendance report for all students in a subject.
     *
     * @param subjectId      Subject ID.
     * @param academicYearId Academic year ID.
     * @return List of student attendance rows.
     */
    public List<Map<String, Object>> getFacultySubjectAttendanceReport(
            int subjectId, int academicYearId) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_FACULTY_SUBJECT_ATTENDANCE_REPORT);
            pstmt.setInt(1, subjectId);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("studentPk",     rs.getInt("student_pk"));
                row.put("studentCode",   rs.getString("student_code"));
                row.put("studentName",   rs.getString("first_name")
                                         + " " + rs.getString("last_name"));
                row.put("section",       rs.getString("section"));
                row.put("totalClasses",  rs.getInt("total_classes"));
                row.put("present",       rs.getInt("classes_present"));
                row.put("absent",        rs.getInt("classes_absent"));
                row.put("late",          rs.getInt("classes_late"));
                row.put("percentage",    rs.getDouble("attendance_percentage"));
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getFacultySubjectAttendanceReport: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns daily attendance detail for a subject on a specific date.
     *
     * @param subjectId Subject ID.
     * @param date      Attendance date.
     * @return List of daily attendance records.
     */
    public List<Map<String, Object>> getFacultyDailyAttendance(
            int subjectId, LocalDate date) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_FACULTY_DAILY_ATTENDANCE);
            pstmt.setInt (1, subjectId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("studentCode", rs.getString("student_code"));
                row.put("studentName", rs.getString("first_name")
                                       + " " + rs.getString("last_name"));
                row.put("section",     rs.getString("section"));
                row.put("status",      rs.getString("status"));
                row.put("remarks",     rs.getString("remarks"));
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getFacultyDailyAttendance: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns monthly attendance for all students in a subject.
     *
     * @param subjectId Subject ID.
     * @param month     Month (1–12).
     * @param year      Year.
     * @return List of monthly attendance rows per student.
     */
    public List<Map<String, Object>> getFacultyMonthlyAttendance(
            int subjectId, int month, int year) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_FACULTY_MONTHLY_ATTENDANCE);
            pstmt.setInt(1, subjectId);
            pstmt.setInt(2, month);
            pstmt.setInt(3, year);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("studentCode", rs.getString("student_code"));
                row.put("studentName", rs.getString("first_name")
                                       + " " + rs.getString("last_name"));
                row.put("section",     rs.getString("section"));
                row.put("total",       rs.getInt("total"));
                row.put("present",     rs.getInt("present"));
                row.put("absent",      rs.getInt("absent"));
                row.put("late",        rs.getInt("late"));
                row.put("monthlyPct",  rs.getDouble("monthly_pct"));
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getFacultyMonthlyAttendance: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns students with attendance below a given threshold.
     *
     * @param facultyId      Faculty primary key.
     * @param academicYearId Academic year ID.
     * @param threshold      Attendance percentage threshold (e.g., 75.0).
     * @return List of low-attendance student records.
     */
    public List<Map<String, Object>> getLowAttendanceStudents(
            int facultyId, int academicYearId, double threshold) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_LOW_ATTENDANCE_STUDENTS);
            pstmt.setInt   (1, facultyId);
            pstmt.setInt   (2, academicYearId);
            pstmt.setDouble(3, threshold);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("studentPk",     rs.getInt("student_pk"));
                row.put("studentCode",   rs.getString("student_code"));
                row.put("studentName",   rs.getString("first_name")
                                         + " " + rs.getString("last_name"));
                row.put("section",       rs.getString("section"));
                row.put("email",         rs.getString("email"));
                row.put("totalClasses",  rs.getInt("total_classes"));
                row.put("present",       rs.getInt("classes_present"));
                row.put("absent",        rs.getInt("classes_absent"));
                row.put("percentage",    rs.getDouble("attendance_percentage"));
                row.put("subjectCode",   rs.getString("subject_code"));
                row.put("subjectName",   rs.getString("subject_name"));
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getLowAttendanceStudents: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns marks report for all students in a subject.
     *
     * @param subjectId      Subject ID.
     * @param academicYearId Academic year ID.
     * @return List of marks records per student per exam type.
     */
    public List<Map<String, Object>> getFacultyMarksReport(
            int subjectId, int academicYearId) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_FACULTY_MARKS_REPORT);
            pstmt.setInt(1, academicYearId);
            pstmt.setInt(2, subjectId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("studentCode", rs.getString("student_code"));
                row.put("studentName", rs.getString("first_name")
                                       + " " + rs.getString("last_name"));
                row.put("section",     rs.getString("section"));
                row.put("examName",    rs.getString("exam_name"));
                row.put("maxMarks",    rs.getInt("max_marks"));
                row.put("obtained",    rs.getObject("marks_obtained")); // nullable
                row.put("grade",       rs.getString("grade"));
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getFacultyMarksReport: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns performance statistics per exam type for a subject.
     *
     * @param subjectId      Subject ID.
     * @param academicYearId Academic year ID.
     * @return List of performance stat maps.
     */
    public List<Map<String, Object>> getFacultyPerformanceStats(
            int subjectId, int academicYearId) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_FACULTY_PERFORMANCE_STATS);
            pstmt.setInt(1, subjectId);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("examName",  rs.getString("exam_name"));
                row.put("maxMarks",  rs.getInt("max_marks"));
                row.put("appeared",  rs.getInt("appeared"));
                row.put("avgMarks",  rs.getDouble("avg_marks"));
                row.put("highest",   rs.getDouble("highest"));
                row.put("lowest",    rs.getDouble("lowest"));
                row.put("passed",    rs.getInt("passed"));
                row.put("failed",    rs.getInt("failed"));
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getFacultyPerformanceStats: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns day-wise attendance trend for a subject over a date range.
     *
     * @param subjectId Subject ID.
     * @param from      Start date.
     * @param to        End date.
     * @return List of daily trend rows.
     */
    public List<Map<String, Object>> getDailyTrend(
            int subjectId, LocalDate from, LocalDate to) {

        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_DAILY_TREND);
            pstmt.setInt (1, subjectId);
            pstmt.setDate(2, java.sql.Date.valueOf(from));
            pstmt.setDate(3, java.sql.Date.valueOf(to));
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                java.sql.Date d = rs.getDate("attendance_date");
                row.put("date",    d != null ? d.toLocalDate() : null);
                row.put("total",   rs.getInt("total"));
                row.put("present", rs.getInt("present"));
                row.put("absent",  rs.getInt("absent"));
                row.put("dayPct",  rs.getDouble("day_pct"));
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getDailyTrend: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns monthly trend data for a subject (all recorded months).
     *
     * @param subjectId Subject ID.
     * @return List of monthly aggregated rows.
     */
    public List<Map<String, Object>> getMonthlyTrend(int subjectId) {
        List<Map<String, Object>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_MONTHLY_TREND);
            pstmt.setInt(1, subjectId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                int mo = rs.getInt("mo");
                int yr = rs.getInt("yr");
                row.put("month",    mo);
                row.put("year",     yr);
                row.put("label",    java.time.Month.of(mo).name().substring(0,3)
                                    + " " + yr);
                row.put("total",    rs.getInt("total"));
                row.put("present",  rs.getInt("present"));
                row.put("absent",   rs.getInt("absent"));
                row.put("monthPct", rs.getDouble("month_pct"));
                result.add(row);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getMonthlyTrend: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }

    /**
     * Returns list of distinct year-month pairs that have data for a subject.
     * Used to populate month filter dropdowns.
     *
     * @param subjectId Subject ID.
     * @return List of maps with keys: yr, mo.
     */
    public List<Map<String, Integer>> getMonthsWithData(int subjectId) {
        List<Map<String, Integer>> result = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_MONTHS_WITH_DATA);
            pstmt.setInt(1, subjectId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Integer> m = new LinkedHashMap<>();
                m.put("yr", rs.getInt("yr"));
                m.put("mo", rs.getInt("mo"));
                result.add(m);
            }
        } catch (SQLException e) {
            logger.error("ReportDAO.getMonthsWithData: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return result;
    }
}