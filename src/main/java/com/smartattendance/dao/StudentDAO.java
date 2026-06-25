package com.smartattendance.dao;

import com.smartattendance.model.AttendanceSummary;
import com.smartattendance.model.Marks;
import com.smartattendance.model.Student;
import com.smartattendance.model.Subject;
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
 * StudentDAO.java - Data Access Object for Student Module
 *
 * Responsibilities:
 *  - Fetch student profile by ID.
 *  - Fetch all enrolled subjects for a student.
 *  - Fetch subject-wise attendance records.
 *  - Fetch attendance summary (percentage per subject).
 *  - Fetch marks for each subject and exam type.
 *  - Fetch overall academic summary for progress view.
 *  - Update student profile.
 *
 * All queries use PreparedStatement to prevent SQL injection.
 * All resources are closed in finally blocks via DBConnection.closeResources().
 */
public class StudentDAO {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(StudentDAO.class);

    // ================================================================
    // SQL CONSTANTS
    // ================================================================

    // ── Profile ──────────────────────────────────────────────────────

    private static final String SQL_GET_STUDENT_BY_ID =
            "SELECT " +
            "    s.id, s.student_id, s.first_name, s.last_name, " +
            "    s.email, s.phone, s.date_of_birth, s.gender, " +
            "    s.department_id, s.current_semester, s.section, " +
            "    s.academic_year_id, s.address, s.profile_photo, " +
            "    s.is_active, s.created_at, s.updated_at, " +
            "    d.dept_name  AS department_name, " +
            "    ay.year_label AS academic_year_label " +
            "FROM   students s " +
            "INNER JOIN departments   d  ON s.department_id    = d.id " +
            "INNER JOIN academic_years ay ON s.academic_year_id = ay.id " +
            "WHERE  s.id = ? AND s.is_active = 1";

    private static final String SQL_UPDATE_PROFILE =
            "UPDATE students " +
            "SET    phone = ?, address = ?, updated_at = NOW() " +
            "WHERE  id = ?";

    // ── Enrolled Subjects ─────────────────────────────────────────────

    private static final String SQL_GET_ENROLLED_SUBJECTS =
            "SELECT " +
            "    sub.id           AS subject_id, " +
            "    sub.subject_code, " +
            "    sub.subject_name, " +
            "    sub.credits, " +
            "    sub.semester, " +
            "    f.first_name     AS faculty_first_name, " +
            "    f.last_name      AS faculty_last_name, " +
            "    f.designation    AS faculty_designation, " +
            "    d.dept_name      AS department_name " +
            "FROM   student_subjects ss " +
            "INNER JOIN subjects     sub ON ss.subject_id    = sub.id " +
            "INNER JOIN faculty      f   ON sub.faculty_id   = f.id " +
            "INNER JOIN departments  d   ON sub.department_id = d.id " +
            "WHERE  ss.student_id = ? " +
            "  AND  sub.is_active = 1 " +
            "ORDER BY sub.subject_code ASC";

    // ── Attendance Detail (Individual Records) ─────────────────────────

    private static final String SQL_GET_ATTENDANCE_BY_SUBJECT =
            "SELECT " +
            "    a.id, " +
            "    a.attendance_date, " +
            "    a.status, " +
            "    a.remarks, " +
            "    sub.subject_code, " +
            "    sub.subject_name " +
            "FROM   attendance a " +
            "INNER JOIN subjects sub ON a.subject_id = sub.id " +
            "WHERE  a.student_id = ? " +
            "  AND  a.subject_id = ? " +
            "ORDER BY a.attendance_date DESC";

    private static final String SQL_GET_ALL_ATTENDANCE =
            "SELECT " +
            "    a.id, " +
            "    a.attendance_date, " +
            "    a.status, " +
            "    a.remarks, " +
            "    sub.subject_code, " +
            "    sub.subject_name " +
            "FROM   attendance a " +
            "INNER JOIN subjects sub ON a.subject_id = sub.id " +
            "WHERE  a.student_id = ? " +
            "ORDER BY a.attendance_date DESC, sub.subject_code ASC";

    // ── Attendance Summary ─────────────────────────────────────────────

    private static final String SQL_GET_ATTENDANCE_SUMMARY =
            "SELECT " +
            "    att_sum.id, " +
            "    att_sum.student_id, " +
            "    att_sum.subject_id, " +
            "    att_sum.total_classes, " +
            "    att_sum.classes_present, " +
            "    att_sum.classes_absent, " +
            "    att_sum.classes_late, " +
            "    att_sum.attendance_percentage, " +
            "    att_sum.last_updated, " +
            "    sub.subject_code, " +
            "    sub.subject_name, " +
            "    sub.credits " +
            "FROM   attendance_summary att_sum " +
            "INNER JOIN subjects sub ON att_sum.subject_id = sub.id " +
            "WHERE  att_sum.student_id      = ? " +
            "  AND  att_sum.academic_year_id = ? " +
            "ORDER BY sub.subject_code ASC";

    private static final String SQL_GET_OVERALL_ATTENDANCE =
            "SELECT " +
            "    SUM(total_classes)   AS total_classes_all, " +
            "    SUM(classes_present) AS total_present_all, " +
            "    SUM(classes_absent)  AS total_absent_all, " +
            "    SUM(classes_late)    AS total_late_all " +
            "FROM   attendance_summary " +
            "WHERE  student_id      = ? " +
            "  AND  academic_year_id = ?";

    // ── Marks ──────────────────────────────────────────────────────────

    private static final String SQL_GET_MARKS_BY_STUDENT =
            "SELECT " +
            "    m.id, " +
            "    m.student_id, " +
            "    m.subject_id, " +
            "    m.exam_type_id, " +
            "    m.marks_obtained, " +
            "    m.grade, " +
            "    m.remarks, " +
            "    m.recorded_at, " +
            "    sub.subject_code, " +
            "    sub.subject_name, " +
            "    sub.credits, " +
            "    et.exam_name, " +
            "    et.max_marks " +
            "FROM   marks m " +
            "INNER JOIN subjects    sub ON m.subject_id   = sub.id " +
            "INNER JOIN exam_types  et  ON m.exam_type_id = et.id " +
            "WHERE  m.student_id      = ? " +
            "  AND  m.academic_year_id = ? " +
            "ORDER BY sub.subject_code ASC, et.exam_name ASC";

    private static final String SQL_GET_MARKS_BY_SUBJECT =
            "SELECT " +
            "    m.id, " +
            "    m.marks_obtained, " +
            "    m.grade, " +
            "    m.remarks, " +
            "    m.recorded_at, " +
            "    sub.subject_code, " +
            "    sub.subject_name, " +
            "    sub.credits, " +
            "    et.exam_name, " +
            "    et.max_marks " +
            "FROM   marks m " +
            "INNER JOIN subjects   sub ON m.subject_id   = sub.id " +
            "INNER JOIN exam_types et  ON m.exam_type_id = et.id " +
            "WHERE  m.student_id  = ? " +
            "  AND  m.subject_id  = ? " +
            "ORDER BY et.exam_name ASC";

    // ── Dashboard Summary ──────────────────────────────────────────────

    private static final String SQL_COUNT_ENROLLED_SUBJECTS =
            "SELECT COUNT(*) AS subject_count " +
            "FROM   student_subjects ss " +
            "INNER JOIN subjects sub ON ss.subject_id = sub.id " +
            "WHERE  ss.student_id = ? AND sub.is_active = 1";

    private static final String SQL_COUNT_DEFAULTER_SUBJECTS =
            "SELECT COUNT(*) AS defaulter_count " +
            "FROM   attendance_summary " +
            "WHERE  student_id           = ? " +
            "  AND  academic_year_id      = ? " +
            "  AND  attendance_percentage < 75.0";

    private static final String SQL_GET_RECENT_ATTENDANCE =
            "SELECT " +
            "    a.attendance_date, " +
            "    a.status, " +
            "    sub.subject_code, " +
            "    sub.subject_name " +
            "FROM   attendance a " +
            "INNER JOIN subjects sub ON a.subject_id = sub.id " +
            "WHERE  a.student_id = ? " +
            "ORDER BY a.attendance_date DESC, a.id DESC " +
            "LIMIT 7";

    // ================================================================
    // PUBLIC DAO METHODS
    // ================================================================

    // ── Profile Methods ───────────────────────────────────────────────

    /**
     * Fetches a complete Student profile by primary key (id).
     *
     * @param studentPk The primary key (id column) of the student.
     * @return Fully populated {@link Student} object, or {@code null} if not found.
     */
    public Student getStudentById(int studentPk) {
        logger.debug("StudentDAO.getStudentById: Fetching student with pk={}", studentPk);

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_STUDENT_BY_ID);
            pstmt.setInt(1, studentPk);
            rs    = pstmt.executeQuery();

            if (rs.next()) {
                Student student = mapStudentFromResultSet(rs);
                logger.debug("StudentDAO.getStudentById: Found student: {}", student.getFullName());
                return student;
            } else {
                logger.warn("StudentDAO.getStudentById: No student found with pk={}", studentPk);
                return null;
            }

        } catch (SQLException e) {
            logger.error("StudentDAO.getStudentById: DB error for pk={}", studentPk, e);
            return null;
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Updates the editable profile fields (phone and address) for a student.
     *
     * @param studentPk The primary key of the student to update.
     * @param phone     New phone number (can be null to clear).
     * @param address   New address (can be null to clear).
     * @return {@code true} if the update was successful.
     */
    public boolean updateStudentProfile(int studentPk, String phone, String address) {
        logger.debug("StudentDAO.updateStudentProfile: Updating pk={}", studentPk);

        Connection        conn  = null;
        PreparedStatement pstmt = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_UPDATE_PROFILE);
            pstmt.setString(1, phone);
            pstmt.setString(2, address);
            pstmt.setInt   (3, studentPk);

            int rowsAffected = pstmt.executeUpdate();
            boolean success  = rowsAffected > 0;

            if (success) {
                logger.info("StudentDAO.updateStudentProfile: Updated pk={} successfully.", studentPk);
            } else {
                logger.warn("StudentDAO.updateStudentProfile: No rows updated for pk={}.", studentPk);
            }

            return success;

        } catch (SQLException e) {
            logger.error("StudentDAO.updateStudentProfile: DB error for pk={}", studentPk, e);
            return false;
        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    // ── Enrolled Subjects ─────────────────────────────────────────────

    /**
     * Returns a list of all subjects a student is enrolled in.
     *
     * @param studentPk The primary key of the student.
     * @return List of {@link Subject} objects, or empty list if none.
     */
    public List<Subject> getEnrolledSubjects(int studentPk) {
        logger.debug("StudentDAO.getEnrolledSubjects: Fetching subjects for student pk={}", studentPk);

        List<Subject>     subjects = new ArrayList<>();
        Connection        conn     = null;
        PreparedStatement pstmt    = null;
        ResultSet         rs       = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ENROLLED_SUBJECTS);
            pstmt.setInt(1, studentPk);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Subject subject = new Subject();
                subject.setId(rs.getInt("subject_id"));
                subject.setSubjectCode(rs.getString("subject_code"));
                subject.setSubjectName(rs.getString("subject_name"));
                subject.setCredits(rs.getInt("credits"));
                subject.setSemester(rs.getInt("semester"));
                subject.setDepartmentName(rs.getString("department_name"));

                String facultyName = rs.getString("faculty_first_name")
                        + " " + rs.getString("faculty_last_name");
                subject.setFacultyName(facultyName);
                subject.setFacultyDesignation(rs.getString("faculty_designation"));

                subjects.add(subject);
            }

            logger.debug("StudentDAO.getEnrolledSubjects: Found {} subjects for pk={}",
                    subjects.size(), studentPk);

        } catch (SQLException e) {
            logger.error("StudentDAO.getEnrolledSubjects: DB error for pk={}", studentPk, e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return subjects;
    }

    // ── Attendance Methods ─────────────────────────────────────────────

    /**
     * Returns attendance summary records (one per subject) for a student
     * in a given academic year.
     *
     * @param studentPk      The primary key of the student.
     * @param academicYearId The academic year ID.
     * @return List of {@link AttendanceSummary} objects.
     */
    public List<AttendanceSummary> getAttendanceSummary(int studentPk, int academicYearId) {
        logger.debug("StudentDAO.getAttendanceSummary: student pk={}, year={}",
                studentPk, academicYearId);

        List<AttendanceSummary> summaries = new ArrayList<>();
        Connection              conn      = null;
        PreparedStatement       pstmt     = null;
        ResultSet               rs        = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_ATTENDANCE_SUMMARY);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                AttendanceSummary summary = new AttendanceSummary();
                summary.setId(rs.getInt("id"));
                summary.setStudentId(rs.getInt("student_id"));
                summary.setSubjectId(rs.getInt("subject_id"));
                summary.setSubjectCode(rs.getString("subject_code"));
                summary.setSubjectName(rs.getString("subject_name"));
                summary.setCredits(rs.getInt("credits"));
                summary.setTotalClasses(rs.getInt("total_classes"));
                summary.setClassesPresent(rs.getInt("classes_present"));
                summary.setClassesAbsent(rs.getInt("classes_absent"));
                summary.setClassesLate(rs.getInt("classes_late"));
                summary.setAttendancePercentage(rs.getDouble("attendance_percentage"));

                java.sql.Timestamp lastUpdated = rs.getTimestamp("last_updated");
                if (lastUpdated != null) {
                    summary.setLastUpdated(lastUpdated.toLocalDateTime());
                }

                summaries.add(summary);
            }

            logger.debug("StudentDAO.getAttendanceSummary: Found {} records.", summaries.size());

        } catch (SQLException e) {
            logger.error("StudentDAO.getAttendanceSummary: DB error for pk={}", studentPk, e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return summaries;
    }

    /**
     * Returns individual attendance records for a student in a specific subject.
     *
     * @param studentPk The primary key of the student.
     * @param subjectId The subject ID to filter by.
     * @return List of {@link AttendanceSummary} objects representing individual records.
     */
    public List<Map<String, Object>> getAttendanceDetailBySubject(int studentPk, int subjectId) {
        logger.debug("StudentDAO.getAttendanceDetailBySubject: student pk={}, subject={}",
                studentPk, subjectId);

        List<Map<String, Object>> records = new ArrayList<>();
        Connection                conn    = null;
        PreparedStatement         pstmt   = null;
        ResultSet                 rs      = null;

        String sql = (subjectId > 0) ? SQL_GET_ATTENDANCE_BY_SUBJECT : SQL_GET_ALL_ATTENDANCE;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, studentPk);
            if (subjectId > 0) {
                pstmt.setInt(2, subjectId);
            }
            rs = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> record = new LinkedHashMap<>();
                record.put("id",           rs.getInt("id"));
                record.put("date",         rs.getDate("attendance_date") != null
                                           ? rs.getDate("attendance_date").toLocalDate() : null);
                record.put("status",       rs.getString("status"));
                record.put("remarks",      rs.getString("remarks"));
                record.put("subjectCode",  rs.getString("subject_code"));
                record.put("subjectName",  rs.getString("subject_name"));
                records.add(record);
            }

            logger.debug("StudentDAO.getAttendanceDetailBySubject: Found {} records.", records.size());

        } catch (SQLException e) {
            logger.error("StudentDAO.getAttendanceDetailBySubject: DB error for pk={}", studentPk, e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return records;
    }

    /**
     * Returns overall aggregated attendance totals across all subjects.
     *
     * @param studentPk      The primary key of the student.
     * @param academicYearId The academic year ID.
     * @return Map with keys: totalClassesAll, totalPresentAll, totalAbsentAll, totalLateAll.
     */
    public Map<String, Integer> getOverallAttendanceTotals(int studentPk, int academicYearId) {
        Map<String, Integer> totals = new LinkedHashMap<>();
        totals.put("totalClassesAll", 0);
        totals.put("totalPresentAll", 0);
        totals.put("totalAbsentAll",  0);
        totals.put("totalLateAll",    0);

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_OVERALL_ATTENDANCE);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            if (rs.next()) {
                totals.put("totalClassesAll", rs.getInt("total_classes_all"));
                totals.put("totalPresentAll", rs.getInt("total_present_all"));
                totals.put("totalAbsentAll",  rs.getInt("total_absent_all"));
                totals.put("totalLateAll",    rs.getInt("total_late_all"));
            }

        } catch (SQLException e) {
            logger.error("StudentDAO.getOverallAttendanceTotals: DB error for pk={}", studentPk, e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return totals;
    }

    // ── Marks Methods ─────────────────────────────────────────────────

    /**
     * Returns all marks for a student in a given academic year,
     * grouped by subject for easy JSP rendering.
     *
     * @param studentPk      The primary key of the student.
     * @param academicYearId The academic year ID.
     * @return List of {@link Marks} objects.
     */
    public List<Marks> getAllMarksByStudent(int studentPk, int academicYearId) {
        logger.debug("StudentDAO.getAllMarksByStudent: student pk={}, year={}",
                studentPk, academicYearId);

        List<Marks>       marksList = new ArrayList<>();
        Connection        conn      = null;
        PreparedStatement pstmt     = null;
        ResultSet         rs        = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_MARKS_BY_STUDENT);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Marks marks = new Marks();
                marks.setId(rs.getInt("id"));
                marks.setStudentId(rs.getInt("student_id"));
                marks.setSubjectId(rs.getInt("subject_id"));
                marks.setExamTypeId(rs.getInt("exam_type_id"));
                marks.setMarksObtained(rs.getDouble("marks_obtained"));
                marks.setGrade(rs.getString("grade"));
                marks.setRemarks(rs.getString("remarks"));
                marks.setSubjectCode(rs.getString("subject_code"));
                marks.setSubjectName(rs.getString("subject_name"));
                marks.setCredits(rs.getInt("credits"));
                marks.setExamName(rs.getString("exam_name"));
                marks.setMaxMarks(rs.getInt("max_marks"));

                java.sql.Timestamp recordedAt = rs.getTimestamp("recorded_at");
                if (recordedAt != null) {
                    marks.setRecordedAt(recordedAt.toLocalDateTime());
                }

                marksList.add(marks);
            }

            logger.debug("StudentDAO.getAllMarksByStudent: Found {} marks records.", marksList.size());

        } catch (SQLException e) {
            logger.error("StudentDAO.getAllMarksByStudent: DB error for pk={}", studentPk, e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return marksList;
    }

    // ── Dashboard Summary Methods ──────────────────────────────────────

    /**
     * Returns count of subjects the student is enrolled in.
     *
     * @param studentPk The primary key of the student.
     * @return Subject count.
     */
    public int getEnrolledSubjectCount(int studentPk) {
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_COUNT_ENROLLED_SUBJECTS);
            pstmt.setInt(1, studentPk);
            rs    = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("subject_count");
        } catch (SQLException e) {
            logger.error("StudentDAO.getEnrolledSubjectCount: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return 0;
    }

    /**
     * Returns count of subjects where the student is a defaulter
     * (attendance below 75%).
     *
     * @param studentPk      The primary key of the student.
     * @param academicYearId The academic year ID.
     * @return Defaulter subject count.
     */
    public int getDefaulterSubjectCount(int studentPk, int academicYearId) {
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_COUNT_DEFAULTER_SUBJECTS);
            pstmt.setInt(1, studentPk);
            pstmt.setInt(2, academicYearId);
            rs    = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("defaulter_count");
        } catch (SQLException e) {
            logger.error("StudentDAO.getDefaulterSubjectCount: DB error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return 0;
    }

    /**
     * Returns the 7 most recent attendance records for a student.
     * Used in the Student Dashboard.
     *
     * @param studentPk The primary key of the student.
     * @return List of maps, each map representing one attendance record.
     */
    public List<Map<String, Object>> getRecentAttendance(int studentPk) {
        List<Map<String, Object>> records = new ArrayList<>();
        Connection                conn    = null;
        PreparedStatement         pstmt   = null;
        ResultSet                 rs      = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_RECENT_ATTENDANCE);
            pstmt.setInt(1, studentPk);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> record = new LinkedHashMap<>();
                record.put("date",        rs.getDate("attendance_date") != null
                                          ? rs.getDate("attendance_date").toLocalDate() : null);
                record.put("status",      rs.getString("status"));
                record.put("subjectCode", rs.getString("subject_code"));
                record.put("subjectName", rs.getString("subject_name"));
                records.add(record);
            }

        } catch (SQLException e) {
            logger.error("StudentDAO.getRecentAttendance: DB error for pk={}", studentPk, e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }

        return records;
    }

    // ================================================================
    // PRIVATE MAPPING HELPERS
    // ================================================================

    /**
     * Maps a ResultSet row to a Student object.
     * Does NOT include the password field (not selected in student queries).
     */
    private Student mapStudentFromResultSet(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id"));
        s.setStudentId(rs.getString("student_id"));
        s.setFirstName(rs.getString("first_name"));
        s.setLastName(rs.getString("last_name"));
        s.setEmail(rs.getString("email"));
        s.setPhone(rs.getString("phone"));

        java.sql.Date dob = rs.getDate("date_of_birth");
        if (dob != null) s.setDateOfBirth(dob.toLocalDate());

        s.setGender(rs.getString("gender"));
        s.setDepartmentId(rs.getInt("department_id"));
        s.setDepartmentName(rs.getString("department_name"));
        s.setCurrentSemester(rs.getInt("current_semester"));
        s.setSection(rs.getString("section"));
        s.setAcademicYearId(rs.getInt("academic_year_id"));
        s.setAcademicYearLabel(rs.getString("academic_year_label"));
        s.setAddress(rs.getString("address"));
        s.setProfilePhoto(rs.getString("profile_photo"));
        s.setActive(rs.getInt("is_active") == 1);

        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) s.setCreatedAt(createdAt.toLocalDateTime());

        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) s.setUpdatedAt(updatedAt.toLocalDateTime());

        return s;
    }
}