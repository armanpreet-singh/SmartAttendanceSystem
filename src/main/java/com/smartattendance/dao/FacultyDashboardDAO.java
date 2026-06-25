package com.smartattendance.dao;

import com.smartattendance.model.Subject;
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
 * FacultyDashboardDAO.java
 *
 * Provides data for the Faculty Dashboard:
 *  - Total subjects assigned to faculty
 *  - Total unique students across assigned subjects
 *  - Today's attendance count
 *  - Assigned subjects list
 *  - Recent attendance activity (last 10 records)
 *  - Subject-wise student count for chart
 */
public class FacultyDashboardDAO {

    private static final Logger logger = LoggerFactory.getLogger(FacultyDashboardDAO.class);

    // ── SQL Queries ───────────────────────────────────────────────────

    private static final String SQL_ASSIGNED_SUBJECT_COUNT =
            "SELECT COUNT(*) AS cnt " +
            "FROM   subjects " +
            "WHERE  faculty_id = ? AND is_active = 1";

    private static final String SQL_TOTAL_STUDENTS =
            "SELECT COUNT(DISTINCT ss.student_id) AS cnt " +
            "FROM   student_subjects ss " +
            "INNER JOIN subjects sub ON ss.subject_id = sub.id " +
            "WHERE  sub.faculty_id = ? AND sub.is_active = 1";

    private static final String SQL_TODAY_ATTENDANCE_COUNT =
            "SELECT COUNT(*) AS cnt " +
            "FROM   attendance a " +
            "INNER JOIN subjects sub ON a.subject_id = sub.id " +
            "WHERE  sub.faculty_id = ? " +
            "  AND  a.attendance_date = ? " +
            "  AND  a.status = 'Present'";

    private static final String SQL_ASSIGNED_SUBJECTS =
            "SELECT " +
            "    sub.id, sub.subject_code, sub.subject_name, " +
            "    sub.credits, sub.semester, " +
            "    d.dept_name AS department_name, " +
            "    (SELECT COUNT(DISTINCT ss2.student_id) " +
            "     FROM   student_subjects ss2 " +
            "     WHERE  ss2.subject_id = sub.id) AS student_count " +
            "FROM   subjects sub " +
            "INNER JOIN departments d ON sub.department_id = d.id " +
            "WHERE  sub.faculty_id = ? AND sub.is_active = 1 " +
            "ORDER BY sub.subject_code ASC";

    private static final String SQL_RECENT_ATTENDANCE =
            "SELECT " +
            "    a.attendance_date, a.status, " +
            "    s.first_name, s.last_name, s.student_id AS student_code, " +
            "    sub.subject_code, sub.subject_name " +
            "FROM   attendance a " +
            "INNER JOIN students s  ON a.student_id  = s.id " +
            "INNER JOIN subjects sub ON a.subject_id = sub.id " +
            "WHERE  sub.faculty_id = ? " +
            "ORDER BY a.attendance_date DESC, a.id DESC " +
            "LIMIT 10";

    private static final String SQL_SUBJECT_STUDENT_COUNTS =
            "SELECT " +
            "    sub.subject_code, " +
            "    COUNT(DISTINCT ss.student_id) AS student_count " +
            "FROM   subjects sub " +
            "LEFT JOIN student_subjects ss ON sub.id = ss.subject_id " +
            "WHERE  sub.faculty_id = ? AND sub.is_active = 1 " +
            "GROUP BY sub.id, sub.subject_code " +
            "ORDER BY sub.subject_code ASC";

    private static final String SQL_ATTENDANCE_RATE_TODAY =
            "SELECT " +
            "    COUNT(CASE WHEN a.status = 'Present' THEN 1 END) AS present_count, " +
            "    COUNT(*) AS total_count " +
            "FROM   attendance a " +
            "INNER JOIN subjects sub ON a.subject_id = sub.id " +
            "WHERE  sub.faculty_id = ? " +
            "  AND  a.attendance_date = ?";

    // ── Public Methods ────────────────────────────────────────────────

    /**
     * Returns total subjects assigned to this faculty member.
     */
    public int getAssignedSubjectCount(int facultyId) {
        return fetchSingleInt(SQL_ASSIGNED_SUBJECT_COUNT, facultyId, "cnt");
    }

    /**
     * Returns total unique students enrolled in faculty's subjects.
     */
    public int getTotalStudents(int facultyId) {
        return fetchSingleInt(SQL_TOTAL_STUDENTS, facultyId, "cnt");
    }

    /**
     * Returns count of 'Present' records marked by faculty today.
     */
    public int getTodayPresentCount(int facultyId) {
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;
        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_TODAY_ATTENDANCE_COUNT);
            pstmt.setInt(1, facultyId);
            pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            rs    = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("cnt");
        } catch (SQLException e) {
            logger.error("FacultyDashboardDAO.getTodayPresentCount error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return 0;
    }

    /**
     * Returns today's attendance rate as a Map with
     * keys: presentCount, totalCount.
     */
    public Map<String, Integer> getTodayAttendanceRate(int facultyId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        result.put("presentCount", 0);
        result.put("totalCount",   0);

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;
        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_ATTENDANCE_RATE_TODAY);
            pstmt.setInt(1, facultyId);
            pstmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            rs    = pstmt.executeQuery();
            if (rs.next()) {
                result.put("presentCount", rs.getInt("present_count"));
                result.put("totalCount",   rs.getInt("total_count"));
            }
        } catch (SQLException e) {
            logger.error("FacultyDashboardDAO.getTodayAttendanceRate error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return result;
    }

    /**
     * Returns list of subjects assigned to faculty with student counts.
     */
    public List<Subject> getAssignedSubjects(int facultyId) {
        List<Subject>     subjects = new ArrayList<>();
        Connection        conn     = null;
        PreparedStatement pstmt    = null;
        ResultSet         rs       = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_ASSIGNED_SUBJECTS);
            pstmt.setInt(1, facultyId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Subject s = new Subject();
                s.setId(rs.getInt("id"));
                s.setSubjectCode(rs.getString("subject_code"));
                s.setSubjectName(rs.getString("subject_name"));
                s.setCredits(rs.getInt("credits"));
                s.setSemester(rs.getInt("semester"));
                s.setDepartmentName(rs.getString("department_name"));
                s.setStudentCount(rs.getInt("student_count"));
                subjects.add(s);
            }
        } catch (SQLException e) {
            logger.error("FacultyDashboardDAO.getAssignedSubjects error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return subjects;
    }

    /**
     * Returns last 10 attendance records marked by this faculty.
     */
    public List<Map<String, Object>> getRecentAttendanceActivity(int facultyId) {
        List<Map<String, Object>> records = new ArrayList<>();
        Connection                conn    = null;
        PreparedStatement         pstmt   = null;
        ResultSet                 rs      = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_RECENT_ATTENDANCE);
            pstmt.setInt(1, facultyId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> rec = new LinkedHashMap<>();
                java.sql.Date d = rs.getDate("attendance_date");
                rec.put("date",         d != null ? d.toLocalDate() : null);
                rec.put("status",       rs.getString("status"));
                rec.put("studentName",  rs.getString("first_name") + " " + rs.getString("last_name"));
                rec.put("studentCode",  rs.getString("student_code"));
                rec.put("subjectCode",  rs.getString("subject_code"));
                rec.put("subjectName",  rs.getString("subject_name"));
                records.add(rec);
            }
        } catch (SQLException e) {
            logger.error("FacultyDashboardDAO.getRecentAttendanceActivity error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return records;
    }

    /**
     * Returns subject codes and their student counts for Chart.js.
     * Returns Map<subjectCode, studentCount>.
     */
    public Map<String, Integer> getSubjectStudentCounts(int facultyId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        Connection           conn   = null;
        PreparedStatement    pstmt  = null;
        ResultSet            rs     = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_SUBJECT_STUDENT_COUNTS);
            pstmt.setInt(1, facultyId);
            rs    = pstmt.executeQuery();
            while (rs.next()) {
                result.put(rs.getString("subject_code"), rs.getInt("student_count"));
            }
        } catch (SQLException e) {
            logger.error("FacultyDashboardDAO.getSubjectStudentCounts error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return result;
    }

    // ── Private Helper ────────────────────────────────────────────────

    private int fetchSingleInt(String sql, int facultyId, String col) {
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;
        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, facultyId);
            rs    = pstmt.executeQuery();
            if (rs.next()) return rs.getInt(col);
        } catch (SQLException e) {
            logger.error("FacultyDashboardDAO.fetchSingleInt error for col={}", col, e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return 0;
    }
}