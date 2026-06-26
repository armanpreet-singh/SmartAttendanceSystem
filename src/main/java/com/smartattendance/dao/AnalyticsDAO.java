package com.smartattendance.dao;

import com.smartattendance.util.DBConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AnalyticsDAO.java - Data Access Object for Analytics/Charts Module
 *
 * Responsibilities:
 *  - Provide aggregated data for Chart.js visualizations
 *  - Attendance trend data (daily/monthly) as JSON-ready structures
 *  - Subject-wise attendance comparison data
 *  - Marks distribution data for charts
 *  - Overall attendance percentage donut chart data
 *
 * All queries use PreparedStatement to prevent SQL injection.
 */
public class AnalyticsDAO {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsDAO.class);

    // ================================================================
    // SQL CONSTANTS
    // ================================================================

    /**
     * Attendance percentage per subject for a student — for bar chart.
     */
    private static final String SQL_STUDENT_ATT_BY_SUBJECT =
            "SELECT " +
            "    sub.subject_code, " +
            "    sub.subject_name, " +
            "    att.attendance_percentage, " +
            "    att.classes_present, " +
            "    att.classes_absent " +
            "FROM   attendance_summary att " +
            "INNER JOIN subjects sub ON att.subject_id = sub.id " +
            "WHERE  att.student_id      = ? " +
            "  AND  att.academic_year_id = ? " +
            "ORDER BY sub.subject_code ASC";

    /**
     * Marks percentage per subject for a student — for radar chart.
     */
    private static final String SQL_STUDENT_MARKS_BY_SUBJECT =
            "SELECT " +
            "    sub.subject_code, " +
            "    ROUND(SUM(m.marks_obtained) * 100.0 / " +
            "          NULLIF(SUM(et.max_marks), 0), 2) AS marks_pct " +
            "FROM   marks m " +
            "INNER JOIN subjects   sub ON m.subject_id   = sub.id " +
            "INNER JOIN exam_types et  ON m.exam_type_id = et.id " +
            "WHERE  m.student_id      = ? " +
            "  AND  m.academic_year_id = ? " +
            "GROUP BY sub.id, sub.subject_code " +
            "ORDER BY sub.subject_code ASC";

    /**
     * Overall present/absent/late totals for student — for doughnut chart.
     */
    private static final String SQL_STUDENT_OVERALL_TOTALS =
            "SELECT " +
            "    SUM(classes_present) AS present_total, " +
            "    SUM(classes_absent)  AS absent_total, " +
            "    SUM(classes_late)    AS late_total " +
            "FROM   attendance_summary " +
            "WHERE  student_id      = ? " +
            "  AND  academic_year_id = ?";

    /**
     * Monthly attendance trend for a student — line chart.
     */
    private static final String SQL_STUDENT_MONTHLY_TREND =
            "SELECT " +
            "    YEAR(a.attendance_date)   AS yr, " +
            "    MONTH(a.attendance_date)  AS mo, " +
            "    COUNT(*)                  AS total, " +
            "    SUM(CASE WHEN a.status IN ('Present','Late') THEN 1 ELSE 0 END) AS attended, " +
            "    ROUND(SUM(CASE WHEN a.status IN ('Present','Late') THEN 1 ELSE 0 END) " +
            "          * 100.0 / NULLIF(COUNT(*), 0), 2) AS month_pct " +
            "FROM   attendance a " +
            "WHERE  a.student_id   = ? " +
            "GROUP BY YEAR(a.attendance_date), MONTH(a.attendance_date) " +
            "ORDER BY yr ASC, mo ASC";

    /**
     * Faculty: Attendance rate per subject for doughnut chart.
     */
    private static final String SQL_FACULTY_ATT_RATE_BY_SUBJECT =
            "SELECT " +
            "    sub.subject_code, " +
            "    ROUND(AVG(att.attendance_percentage), 2) AS avg_pct " +
            "FROM   attendance_summary att " +
            "INNER JOIN subjects sub ON att.subject_id = sub.id " +
            "WHERE  sub.faculty_id = ? AND sub.is_active = 1 " +
            "GROUP BY sub.id, sub.subject_code " +
            "ORDER BY sub.subject_code ASC";

    /**
     * Faculty: Student count above/below 75% threshold per subject.
     */
    private static final String SQL_FACULTY_DEFAULTER_DISTRIBUTION =
            "SELECT " +
            "    sub.subject_code, " +
            "    SUM(CASE WHEN att.attendance_percentage >= 75 THEN 1 ELSE 0 END) AS eligible, " +
            "    SUM(CASE WHEN att.attendance_percentage < 75  THEN 1 ELSE 0 END) AS defaulters " +
            "FROM   attendance_summary att " +
            "INNER JOIN subjects sub ON att.subject_id = sub.id " +
            "WHERE  sub.faculty_id = ? AND sub.is_active = 1 " +
            "GROUP BY sub.id, sub.subject_code " +
            "ORDER BY sub.subject_code ASC";

    /**
     * Faculty: Average marks per exam type for a subject — bar chart.
     */
    private static final String SQL_AVG_MARKS_BY_EXAM_TYPE =
            "SELECT " +
            "    et.exam_name, " +
            "    et.max_marks, " +
            "    ROUND(AVG(m.marks_obtained), 2) AS avg_obtained " +
            "FROM   marks m " +
            "INNER JOIN exam_types et ON m.exam_type_id = et.id " +
            "WHERE  m.subject_id      = ? " +
            "  AND  m.academic_year_id = ? " +
            "GROUP BY et.id, et.exam_name, et.max_marks " +
            "ORDER BY et.id ASC";

    /**
     * Grade distribution for a subject — pie chart.
     */
    private static final String SQL_GRADE_DISTRIBUTION =
            "SELECT " +
            "    m.grade, " +
            "    COUNT(*) AS grade_count " +
            "FROM   marks m " +
            "WHERE  m.subject_id      = ? " +
            "  AND  m.academic_year_id = ? " +
            "  AND  m.grade IS NOT NULL " +
            "GROUP BY m.grade " +
            "ORDER BY m.grade ASC";

    // ================================================================
    // PUBLIC METHODS — STUDENT ANALYTICS
    // ================================================================

    /**
     * Returns subject-wise attendance percentages for Chart.js bar chart.
     *
     * @param studentPk      Student primary key.
     * @param academicYearId Academic year ID.
     * @return Map with keys: labels (List<String>), percentages (List<Double>),
     *         present (List<Integer>), absent (List<Integer>)
     */
    public Map<String, Object> getStudentAttendanceChartData(
            int studentPk, int academicYearId) {

        Map<String, Object> chartData = new LinkedHashMap<>();
        List<String>  labels      = new ArrayList<>();
        List<Double>  percentages = new ArrayList<>();
        List<Integer> present     = new ArrayList<>();
        List<Integer> absent      = new ArrayList<>();

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_STUDENT_ATT_BY_SUBJECT);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                labels.add(rs.getString("subject_code"));
                percentages.add(rs.getDouble("attendance_percentage"));
                present.add(rs.getInt("classes_present"));
                absent.add(rs.getInt("classes_absent"));
            }

        } catch (SQLException e) {
            logger.error("AnalyticsDAO.getStudentAttendanceChartData: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        chartData.put("labels",      labels);
        chartData.put("percentages", percentages);
        chartData.put("present",     present);
        chartData.put("absent",      absent);
        return chartData;
    }

    /**
     * Returns subject-wise marks percentages for Chart.js radar chart.
     *
     * @param studentPk      Student primary key.
     * @param academicYearId Academic year ID.
     * @return Map with keys: labels, marksPct
     */
    public Map<String, Object> getStudentMarksChartData(
            int studentPk, int academicYearId) {

        Map<String, Object> chartData = new LinkedHashMap<>();
        List<String> labels   = new ArrayList<>();
        List<Double> marksPct = new ArrayList<>();

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_STUDENT_MARKS_BY_SUBJECT);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                labels.add(rs.getString("subject_code"));
                marksPct.add(rs.getDouble("marks_pct"));
            }

        } catch (SQLException e) {
            logger.error("AnalyticsDAO.getStudentMarksChartData: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        chartData.put("labels",   labels);
        chartData.put("marksPct", marksPct);
        return chartData;
    }

    /**
     * Returns overall attendance totals for student doughnut chart.
     *
     * @param studentPk      Student primary key.
     * @param academicYearId Academic year ID.
     * @return Map with keys: present, absent, late
     */
    public Map<String, Integer> getStudentOverallTotals(
            int studentPk, int academicYearId) {

        Map<String, Integer> totals = new LinkedHashMap<>();
        totals.put("present", 0);
        totals.put("absent",  0);
        totals.put("late",    0);

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_STUDENT_OVERALL_TOTALS);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            if (rs.next()) {
                totals.put("present", rs.getInt("present_total"));
                totals.put("absent",  rs.getInt("absent_total"));
                totals.put("late",    rs.getInt("late_total"));
            }
        } catch (SQLException e) {
            logger.error("AnalyticsDAO.getStudentOverallTotals: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return totals;
    }

    /**
     * Returns month-by-month attendance trend for a student — line chart.
     *
     * @param studentPk Student primary key.
     * @return Map with keys: labels, percentages
     */
    public Map<String, Object> getStudentMonthlyTrendData(int studentPk) {
        Map<String, Object> chartData = new LinkedHashMap<>();
        List<String> labels      = new ArrayList<>();
        List<Double> percentages = new ArrayList<>();

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_STUDENT_MONTHLY_TREND);
            pstmt.setInt(1, studentPk);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                int mo = rs.getInt("mo");
                int yr = rs.getInt("yr");
                labels.add(java.time.Month.of(mo).name().substring(0, 3) + " " + yr);
                percentages.add(rs.getDouble("month_pct"));
            }
        } catch (SQLException e) {
            logger.error("AnalyticsDAO.getStudentMonthlyTrendData: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        chartData.put("labels",      labels);
        chartData.put("percentages", percentages);
        return chartData;
    }

    // ================================================================
    // PUBLIC METHODS — FACULTY ANALYTICS
    // ================================================================

    /**
     * Returns average attendance rate per subject for faculty doughnut chart.
     *
     * @param facultyId Faculty primary key.
     * @return Map with keys: labels, avgPct
     */
    public Map<String, Object> getFacultyAttRateChartData(int facultyId) {
        Map<String, Object> chartData = new LinkedHashMap<>();
        List<String> labels = new ArrayList<>();
        List<Double> avgPct = new ArrayList<>();

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_FACULTY_ATT_RATE_BY_SUBJECT);
            pstmt.setInt(1, facultyId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                labels.add(rs.getString("subject_code"));
                avgPct.add(rs.getDouble("avg_pct"));
            }
        } catch (SQLException e) {
            logger.error("AnalyticsDAO.getFacultyAttRateChartData: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        chartData.put("labels", labels);
        chartData.put("avgPct", avgPct);
        return chartData;
    }

    /**
     * Returns eligible vs defaulter student counts per subject.
     *
     * @param facultyId Faculty primary key.
     * @return Map with keys: labels, eligible, defaulters
     */
    public Map<String, Object> getFacultyDefaulterDistribution(int facultyId) {
        Map<String, Object> chartData   = new LinkedHashMap<>();
        List<String>  labels     = new ArrayList<>();
        List<Integer> eligible   = new ArrayList<>();
        List<Integer> defaulters = new ArrayList<>();

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_FACULTY_DEFAULTER_DISTRIBUTION);
            pstmt.setInt(1, facultyId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                labels.add(rs.getString("subject_code"));
                eligible.add(rs.getInt("eligible"));
                defaulters.add(rs.getInt("defaulters"));
            }
        } catch (SQLException e) {
            logger.error("AnalyticsDAO.getFacultyDefaulterDistribution: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        chartData.put("labels",     labels);
        chartData.put("eligible",   eligible);
        chartData.put("defaulters", defaulters);
        return chartData;
    }

    /**
     * Returns average marks per exam type for a subject — bar chart.
     *
     * @param subjectId      Subject ID.
     * @param academicYearId Academic year ID.
     * @return Map with keys: labels, avgObtained, maxMarks
     */
    public Map<String, Object> getAvgMarksByExamType(
            int subjectId, int academicYearId) {

        Map<String, Object> chartData   = new LinkedHashMap<>();
        List<String>  labels      = new ArrayList<>();
        List<Double>  avgObtained = new ArrayList<>();
        List<Integer> maxMarks    = new ArrayList<>();

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_AVG_MARKS_BY_EXAM_TYPE);
            pstmt.setInt(1, subjectId);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                labels.add(rs.getString("exam_name"));
                avgObtained.add(rs.getDouble("avg_obtained"));
                maxMarks.add(rs.getInt("max_marks"));
            }
        } catch (SQLException e) {
            logger.error("AnalyticsDAO.getAvgMarksByExamType: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        chartData.put("labels",      labels);
        chartData.put("avgObtained", avgObtained);
        chartData.put("maxMarks",    maxMarks);
        return chartData;
    }

    /**
     * Returns grade distribution for a subject — pie chart.
     *
     * @param subjectId      Subject ID.
     * @param academicYearId Academic year ID.
     * @return Map with keys: grades (List<String>), counts (List<Integer>)
     */
    public Map<String, Object> getGradeDistribution(
            int subjectId, int academicYearId) {

        Map<String, Object> chartData = new LinkedHashMap<>();
        List<String>  grades = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GRADE_DISTRIBUTION);
            pstmt.setInt(1, subjectId);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                grades.add(rs.getString("grade"));
                counts.add(rs.getInt("grade_count"));
            }
        } catch (SQLException e) {
            logger.error("AnalyticsDAO.getGradeDistribution: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        chartData.put("grades", grades);
        chartData.put("counts", counts);
        return chartData;
    }

    // ================================================================
    // UTILITY: Build JSON array string from a list (for Chart.js)
    // ================================================================

    /**
     * Converts a List<String> to a JSON array string.
     * E.g.: ["CSE401","CSE402"]
     *
     * @param list List of strings.
     * @return JSON array string.
     */
    public static String toJsonStringArray(List<String> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"")
              .append(list.get(i).replace("\"", "\\\""))
              .append("\"");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Converts a List<Number> to a JSON array string.
     * E.g.: [85.5, 76.0, 92.3]
     *
     * @param list List of numbers.
     * @return JSON array string.
     */
    public static String toJsonNumberArray(List<? extends Number> list) {
        if (list == null || list.isEmpty()) return "[]";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(",");
            Number n = list.get(i);
            if (n instanceof Double) {
                sb.append(String.format("%.2f", (Double) n));
            } else {
                sb.append(n.toString());
            }
        }
        sb.append("]");
        return sb.toString();
    }
}