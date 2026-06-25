package com.smartattendance.dao;

import com.smartattendance.model.AttendanceSummary;
import com.smartattendance.model.Student;
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
 * FacultyAttendanceDAO.java
 *
 * Handles all attendance-related DB operations for faculty:
 *  - Get students for a subject (for attendance marking)
 *  - Get existing attendance for a subject + date (for edit)
 *  - Save new attendance records (INSERT batch)
 *  - Update existing attendance records
 *  - Get attendance report by subject
 *  - Get attendance summary for all students in a subject
 *  - Update attendance_summary table after marking
 */
public class FacultyAttendanceDAO {

    private static final Logger logger = LoggerFactory.getLogger(FacultyAttendanceDAO.class);

    // ── SQL Queries ───────────────────────────────────────────────────

    private static final String SQL_STUDENTS_FOR_SUBJECT =
            "SELECT " +
            "    s.id, s.student_id AS student_code, " +
            "    s.first_name, s.last_name, s.section, " +
            "    d.dept_name AS department_name " +
            "FROM   student_subjects ss " +
            "INNER JOIN students    s ON ss.student_id    = s.id " +
            "INNER JOIN departments d ON s.department_id  = d.id " +
            "WHERE  ss.subject_id = ? AND s.is_active = 1 " +
            "ORDER BY s.section ASC, s.first_name ASC, s.last_name ASC";

    private static final String SQL_GET_EXISTING_ATTENDANCE =
            "SELECT student_id, status, remarks " +
            "FROM   attendance " +
            "WHERE  subject_id = ? AND attendance_date = ?";

    private static final String SQL_CHECK_ATTENDANCE_EXISTS =
            "SELECT COUNT(*) AS cnt " +
            "FROM   attendance " +
            "WHERE  student_id = ? AND subject_id = ? AND attendance_date = ?";

    private static final String SQL_INSERT_ATTENDANCE =
            "INSERT INTO attendance " +
            "    (student_id, subject_id, faculty_id, attendance_date, " +
            "     status, remarks, academic_year_id) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_ATTENDANCE =
            "UPDATE attendance " +
            "SET    status = ?, remarks = ?, faculty_id = ?, updated_at = NOW() " +
            "WHERE  student_id = ? AND subject_id = ? AND attendance_date = ?";

    private static final String SQL_ATTENDANCE_REPORT_BY_SUBJECT =
            "SELECT " +
            "    s.id AS student_pk, " +
            "    s.student_id AS student_code, " +
            "    s.first_name, s.last_name, s.section, " +
            "    COUNT(a.id)                                         AS total_classes, " +
            "    SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END) AS classes_present, " +
            "    SUM(CASE WHEN a.status = 'Absent'  THEN 1 ELSE 0 END) AS classes_absent, " +
            "    SUM(CASE WHEN a.status = 'Late'    THEN 1 ELSE 0 END) AS classes_late, " +
            "    ROUND(SUM(CASE WHEN a.status IN ('Present','Late') THEN 1 ELSE 0 END) " +
            "          * 100.0 / NULLIF(COUNT(a.id), 0), 2)         AS att_percentage " +
            "FROM   student_subjects ss " +
            "INNER JOIN students s ON ss.student_id = s.id " +
            "LEFT  JOIN attendance a ON a.student_id = s.id " +
            "        AND a.subject_id = ss.subject_id " +
            "WHERE  ss.subject_id = ? AND s.is_active = 1 " +
            "GROUP BY s.id, s.student_id, s.first_name, s.last_name, s.section " +
            "ORDER BY s.section ASC, att_percentage ASC";

    private static final String SQL_ATTENDANCE_DATES_FOR_SUBJECT =
            "SELECT DISTINCT attendance_date " +
            "FROM   attendance " +
            "WHERE  subject_id = ? " +
            "ORDER BY attendance_date DESC";

    private static final String SQL_UPSERT_ATTENDANCE_SUMMARY =
            "INSERT INTO attendance_summary " +
            "    (student_id, subject_id, academic_year_id, " +
            "     total_classes, classes_present, classes_absent, classes_late, " +
            "     attendance_percentage) " +
            "SELECT " +
            "    a.student_id, a.subject_id, a.academic_year_id, " +
            "    COUNT(*)                                                  AS total_classes, " +
            "    SUM(CASE WHEN a.status = 'Present' THEN 1 ELSE 0 END)    AS classes_present, " +
            "    SUM(CASE WHEN a.status = 'Absent'  THEN 1 ELSE 0 END)    AS classes_absent, " +
            "    SUM(CASE WHEN a.status = 'Late'    THEN 1 ELSE 0 END)    AS classes_late, " +
            "    ROUND(SUM(CASE WHEN a.status IN ('Present','Late') THEN 1 ELSE 0 END) " +
            "          * 100.0 / NULLIF(COUNT(*), 0), 2)                  AS attendance_percentage " +
            "FROM   attendance a " +
            "WHERE  a.subject_id = ? " +
            "GROUP BY a.student_id, a.subject_id, a.academic_year_id " +
            "ON DUPLICATE KEY UPDATE " +
            "    total_classes         = VALUES(total_classes), " +
            "    classes_present       = VALUES(classes_present), " +
            "    classes_absent        = VALUES(classes_absent), " +
            "    classes_late          = VALUES(classes_late), " +
            "    attendance_percentage = VALUES(attendance_percentage), " +
            "    last_updated          = NOW()";

    private static final String SQL_SECTIONS_FOR_SUBJECT =
            "SELECT DISTINCT s.section " +
            "FROM   student_subjects ss " +
            "INNER JOIN students s ON ss.student_id = s.id " +
            "WHERE  ss.subject_id = ? AND s.is_active = 1 " +
            "ORDER BY s.section ASC";

    // ── Public Methods ────────────────────────────────────────────────

    /**
     * Returns all students enrolled in a subject for attendance marking.
     * Optional section filter (pass null or empty to get all).
     */
    public List<Student> getStudentsForSubject(int subjectId, String section) {
        List<Student>     students = new ArrayList<>();
        Connection        conn     = null;
        PreparedStatement pstmt    = null;
        ResultSet         rs       = null;

        String sql = SQL_STUDENTS_FOR_SUBJECT;
        if (section != null && !section.trim().isEmpty()) {
            sql += " AND s.section = ?";
        }

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, subjectId);
            if (section != null && !section.trim().isEmpty()) {
                pstmt.setString(2, section.trim());
            }
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Student s = new Student();
                s.setId(rs.getInt("id"));
                s.setStudentId(rs.getString("student_code"));
                s.setFirstName(rs.getString("first_name"));
                s.setLastName(rs.getString("last_name"));
                s.setSection(rs.getString("section"));
                s.setDepartmentName(rs.getString("department_name"));
                students.add(s);
            }
            logger.debug("getStudentsForSubject: subjectId={}, section={}, count={}",
                    subjectId, section, students.size());
        } catch (SQLException e) {
            logger.error("getStudentsForSubject error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return students;
    }

    /**
     * Returns existing attendance for a subject on a given date.
     * Map: studentPk → status string.
     */
    public Map<Integer, String> getExistingAttendance(int subjectId, LocalDate date) {
        Map<Integer, String> existingMap = new LinkedHashMap<>();
        Connection           conn        = null;
        PreparedStatement    pstmt       = null;
        ResultSet            rs          = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_EXISTING_ATTENDANCE);
            pstmt.setInt (1, subjectId);
            pstmt.setDate(2, java.sql.Date.valueOf(date));
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                existingMap.put(rs.getInt("student_id"), rs.getString("status"));
            }
        } catch (SQLException e) {
            logger.error("getExistingAttendance error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return existingMap;
    }

    /**
     * Saves attendance records for a list of students.
     * Uses INSERT ... ON DUPLICATE KEY UPDATE logic via check + insert/update.
     *
     * @param subjectId      Subject being marked
     * @param facultyId      Faculty doing the marking
     * @param date           Attendance date
     * @param academicYearId Current academic year
     * @param statusMap      Map of studentPk → status
     * @param remarksMap     Map of studentPk → remarks (may be null/empty)
     * @return number of records processed
     */
    public int saveAttendance(int subjectId, int facultyId, LocalDate date,
                              int academicYearId,
                              Map<Integer, String> statusMap,
                              Map<Integer, String> remarksMap) {

        int count = 0;
        Connection conn = null;

        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            for (Map.Entry<Integer, String> entry : statusMap.entrySet()) {
                int    studentPk = entry.getKey();
                String status    = entry.getValue();
                String remarks   = (remarksMap != null)
                                   ? remarksMap.getOrDefault(studentPk, null) : null;

                boolean exists = recordExists(conn, studentPk, subjectId, date);

                if (exists) {
                    // UPDATE
                    try (PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE_ATTENDANCE)) {
                        pstmt.setString(1, status);
                        pstmt.setString(2, remarks);
                        pstmt.setInt   (3, facultyId);
                        pstmt.setInt   (4, studentPk);
                        pstmt.setInt   (5, subjectId);
                        pstmt.setDate  (6, java.sql.Date.valueOf(date));
                        pstmt.executeUpdate();
                    }
                } else {
                    // INSERT
                    try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT_ATTENDANCE)) {
                        pstmt.setInt   (1, studentPk);
                        pstmt.setInt   (2, subjectId);
                        pstmt.setInt   (3, facultyId);
                        pstmt.setDate  (4, java.sql.Date.valueOf(date));
                        pstmt.setString(5, status);
                        pstmt.setString(6, remarks);
                        pstmt.setInt   (7, academicYearId);
                        pstmt.executeUpdate();
                    }
                }
                count++;
            }

            conn.commit();

            // Refresh attendance_summary table
            refreshAttendanceSummary(subjectId);

            logger.info("saveAttendance: Saved {} records for subjectId={}, date={}",
                    count, subjectId, date);

        } catch (SQLException e) {
            logger.error("saveAttendance: Transaction error", e);
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {
                    logger.error("Rollback failed", ex);
                }
            }
            count = 0;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    logger.warn("Failed to reset autocommit/close conn", e);
                }
            }
        }
        return count;
    }

    /**
     * Returns attendance report for all students in a subject.
     * Each entry contains: student info + attendance stats.
     */
    public List<Map<String, Object>> getAttendanceReport(int subjectId) {
        List<Map<String, Object>> report = new ArrayList<>();
        Connection                conn   = null;
        PreparedStatement         pstmt  = null;
        ResultSet                 rs     = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_ATTENDANCE_REPORT_BY_SUBJECT);
            pstmt.setInt(1, subjectId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("studentPk",      rs.getInt("student_pk"));
                row.put("studentCode",    rs.getString("student_code"));
                row.put("studentName",    rs.getString("first_name") + " " + rs.getString("last_name"));
                row.put("section",        rs.getString("section"));
                row.put("totalClasses",   rs.getInt("total_classes"));
                row.put("classesPresent", rs.getInt("classes_present"));
                row.put("classesAbsent",  rs.getInt("classes_absent"));
                row.put("classesLate",    rs.getInt("classes_late"));
                double pct = rs.getDouble("att_percentage");
                row.put("percentage",     pct);
                report.add(row);
            }
        } catch (SQLException e) {
            logger.error("getAttendanceReport error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return report;
    }

    /**
     * Returns distinct dates for which attendance was marked in a subject.
     */
    public List<LocalDate> getAttendanceDates(int subjectId) {
        List<LocalDate>   dates = new ArrayList<>();
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_ATTENDANCE_DATES_FOR_SUBJECT);
            pstmt.setInt(1, subjectId);
            rs    = pstmt.executeQuery();
            while (rs.next()) {
                java.sql.Date d = rs.getDate("attendance_date");
                if (d != null) dates.add(d.toLocalDate());
            }
        } catch (SQLException e) {
            logger.error("getAttendanceDates error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return dates;
    }

    /**
     * Returns distinct sections for a subject.
     */
    public List<String> getSectionsForSubject(int subjectId) {
        List<String>      sections = new ArrayList<>();
        Connection        conn     = null;
        PreparedStatement pstmt    = null;
        ResultSet         rs       = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_SECTIONS_FOR_SUBJECT);
            pstmt.setInt(1, subjectId);
            rs    = pstmt.executeQuery();
            while (rs.next()) sections.add(rs.getString("section"));
        } catch (SQLException e) {
            logger.error("getSectionsForSubject error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return sections;
    }

    // ── Private Helpers ───────────────────────────────────────────────

    private boolean recordExists(Connection conn, int studentId,
                                 int subjectId, LocalDate date) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(SQL_CHECK_ATTENDANCE_EXISTS)) {
            pstmt.setInt (1, studentId);
            pstmt.setInt (2, subjectId);
            pstmt.setDate(3, java.sql.Date.valueOf(date));
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt("cnt") > 0;
            }
        }
    }

    private void refreshAttendanceSummary(int subjectId) {
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_UPSERT_ATTENDANCE_SUMMARY);
            pstmt.setInt(1, subjectId);
            int rows = pstmt.executeUpdate();
            logger.debug("refreshAttendanceSummary: subjectId={}, rows={}", subjectId, rows);
        } catch (SQLException e) {
            logger.error("refreshAttendanceSummary error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }
}