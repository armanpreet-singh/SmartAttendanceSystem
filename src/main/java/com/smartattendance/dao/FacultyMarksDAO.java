package com.smartattendance.dao;

import com.smartattendance.model.Marks;
import com.smartattendance.model.Student;
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
 * FacultyMarksDAO.java
 *
 * Handles all marks-related DB operations for faculty:
 *  - Get exam types
 *  - Get students with their marks for a subject
 *  - Insert new marks record
 *  - Update existing marks record
 *  - Get marks matrix (students × exam types) for display
 */
public class FacultyMarksDAO {

    private static final Logger logger = LoggerFactory.getLogger(FacultyMarksDAO.class);

    // ── SQL Queries ───────────────────────────────────────────────────

    private static final String SQL_GET_EXAM_TYPES =
            "SELECT id, exam_name, max_marks, description " +
            "FROM   exam_types " +
            "ORDER BY id ASC";

    private static final String SQL_STUDENTS_WITH_MARKS =
            "SELECT " +
            "    s.id AS student_pk, " +
            "    s.student_id AS student_code, " +
            "    s.first_name, s.last_name, s.section, " +
            "    m.id AS mark_id, " +
            "    m.exam_type_id, " +
            "    m.marks_obtained, " +
            "    m.grade, m.remarks AS mark_remarks, " +
            "    et.exam_name, et.max_marks " +
            "FROM   student_subjects ss " +
            "INNER JOIN students   s  ON ss.student_id    = s.id " +
            "INNER JOIN exam_types et ON 1 = 1 " +
            "LEFT  JOIN marks      m  ON m.student_id    = s.id " +
            "                       AND m.subject_id     = ss.subject_id " +
            "                       AND m.exam_type_id   = et.id " +
            "                       AND m.academic_year_id = ? " +
            "WHERE  ss.subject_id = ? AND s.is_active = 1 " +
            "ORDER BY s.section ASC, s.first_name ASC, et.id ASC";

    private static final String SQL_CHECK_MARK_EXISTS =
            "SELECT id FROM marks " +
            "WHERE  student_id = ? AND subject_id = ? " +
            "  AND  exam_type_id = ? AND academic_year_id = ?";

    private static final String SQL_INSERT_MARKS =
            "INSERT INTO marks " +
            "    (student_id, subject_id, exam_type_id, academic_year_id, " +
            "     marks_obtained, grade, remarks, recorded_by) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_MARKS =
            "UPDATE marks " +
            "SET    marks_obtained = ?, grade = ?, remarks = ?, " +
            "       recorded_by = ?, updated_at = NOW() " +
            "WHERE  student_id = ? AND subject_id = ? " +
            "  AND  exam_type_id = ? AND academic_year_id = ?";

    private static final String SQL_GET_MARKS_SUMMARY =
            "SELECT " +
            "    s.id AS student_pk, " +
            "    s.student_id AS student_code, " +
            "    s.first_name, s.last_name, s.section, " +
            "    et.exam_name, et.max_marks, " +
            "    m.marks_obtained, m.grade " +
            "FROM   student_subjects ss " +
            "INNER JOIN students   s  ON ss.student_id  = s.id " +
            "INNER JOIN exam_types et ON 1 = 1 " +
            "LEFT  JOIN marks      m  ON m.student_id   = s.id " +
            "                       AND m.subject_id    = ss.subject_id " +
            "                       AND m.exam_type_id  = et.id " +
            "                       AND m.academic_year_id = ? " +
            "WHERE  ss.subject_id = ? AND s.is_active = 1 " +
            "ORDER BY s.section ASC, s.first_name ASC, et.id ASC";

    // ── Public Methods ────────────────────────────────────────────────

    /**
     * Returns all exam types.
     * Each map has keys: id, examName, maxMarks, description.
     */
    public List<Map<String, Object>> getExamTypes() {
        List<Map<String, Object>> types = new ArrayList<>();
        Connection                conn  = null;
        PreparedStatement         pstmt = null;
        ResultSet                 rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_EXAM_TYPES);
            rs    = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> t = new LinkedHashMap<>();
                t.put("id",          rs.getInt("id"));
                t.put("examName",    rs.getString("exam_name"));
                t.put("maxMarks",    rs.getInt("max_marks"));
                t.put("description", rs.getString("description"));
                types.add(t);
            }
        } catch (SQLException e) {
            logger.error("getExamTypes error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return types;
    }

    /**
     * Returns a marks matrix for a subject.
     * Structure: Map<studentPk, Map<examTypeId, Marks>>
     * Used to render the marks entry table.
     */
    public Map<Integer, Map<Integer, Marks>> getMarksMatrix(int subjectId, int academicYearId) {
        // Outer key: studentPk, Inner key: examTypeId
        Map<Integer, Map<Integer, Marks>> matrix = new LinkedHashMap<>();

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_STUDENTS_WITH_MARKS);
            pstmt.setInt(1, academicYearId);
            pstmt.setInt(2, subjectId);
            rs    = pstmt.executeQuery();

            while (rs.next()) {
                int studentPk  = rs.getInt("student_pk");
                int examTypeId = rs.getInt("exam_type_id");

                matrix.computeIfAbsent(studentPk, k -> new LinkedHashMap<>());

                Marks m = new Marks();
                m.setStudentId(studentPk);
                m.setSubjectId(subjectId);
                m.setExamTypeId(examTypeId);
                m.setExamName(rs.getString("exam_name"));
                m.setMaxMarks(rs.getInt("max_marks"));

                int markId = rs.getInt("mark_id");
                if (markId > 0) {
                    m.setId(markId);
                    m.setMarksObtained(rs.getDouble("marks_obtained"));
                    m.setGrade(rs.getString("grade"));
                    m.setRemarks(rs.getString("mark_remarks"));
                }

                matrix.get(studentPk).put(examTypeId, m);
            }
        } catch (SQLException e) {
            logger.error("getMarksMatrix error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return matrix;
    }

    /**
     * Returns student list for a subject (basic info only, for display).
     */
    public List<Student> getStudentsForSubject(int subjectId) {
        List<Student>     students = new ArrayList<>();
        Connection        conn     = null;
        PreparedStatement pstmt    = null;
        ResultSet         rs       = null;

        String sql =
            "SELECT s.id, s.student_id AS student_code, " +
            "       s.first_name, s.last_name, s.section " +
            "FROM   student_subjects ss " +
            "INNER JOIN students s ON ss.student_id = s.id " +
            "WHERE  ss.subject_id = ? AND s.is_active = 1 " +
            "ORDER BY s.section ASC, s.first_name ASC";

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, subjectId);
            rs    = pstmt.executeQuery();
            while (rs.next()) {
                Student s = new Student();
                s.setId(rs.getInt("id"));
                s.setStudentId(rs.getString("student_code"));
                s.setFirstName(rs.getString("first_name"));
                s.setLastName(rs.getString("last_name"));
                s.setSection(rs.getString("section"));
                students.add(s);
            }
        } catch (SQLException e) {
            logger.error("getStudentsForSubject error", e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return students;
    }

    /**
     * Saves or updates a single marks record.
     *
     * @return true if saved successfully.
     */
    public boolean saveOrUpdateMark(int studentId, int subjectId, int examTypeId,
                                    int academicYearId, double marksObtained,
                                    String grade, String remarks, int recordedBy) {

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();

            // Check if record exists
            pstmt = conn.prepareStatement(SQL_CHECK_MARK_EXISTS);
            pstmt.setInt(1, studentId);
            pstmt.setInt(2, subjectId);
            pstmt.setInt(3, examTypeId);
            pstmt.setInt(4, academicYearId);
            rs = pstmt.executeQuery();
            boolean exists = rs.next();
            DBConnection.closeResources(null, pstmt, rs);

            if (exists) {
                pstmt = conn.prepareStatement(SQL_UPDATE_MARKS);
                pstmt.setDouble(1, marksObtained);
                pstmt.setString(2, grade);
                pstmt.setString(3, remarks);
                pstmt.setInt   (4, recordedBy);
                pstmt.setInt   (5, studentId);
                pstmt.setInt   (6, subjectId);
                pstmt.setInt   (7, examTypeId);
                pstmt.setInt   (8, academicYearId);
            } else {
                pstmt = conn.prepareStatement(SQL_INSERT_MARKS);
                pstmt.setInt   (1, studentId);
                pstmt.setInt   (2, subjectId);
                pstmt.setInt   (3, examTypeId);
                pstmt.setInt   (4, academicYearId);
                pstmt.setDouble(5, marksObtained);
                pstmt.setString(6, grade);
                pstmt.setString(7, remarks);
                pstmt.setInt   (8, recordedBy);
            }

            int rows = pstmt.executeUpdate();
            logger.debug("saveOrUpdateMark: studentId={}, examTypeId={}, rows={}",
                    studentId, examTypeId, rows);
            return rows > 0;

        } catch (SQLException e) {
            logger.error("saveOrUpdateMark error", e);
            return false;
        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    /**
     * Computes the grade letter from marks percentage.
     */
    public static String computeGrade(double marksObtained, int maxMarks) {
        if (maxMarks <= 0) return "N/A";
        double pct = (marksObtained / maxMarks) * 100.0;
        if (pct >= 90) return "O";
        if (pct >= 80) return "A+";
        if (pct >= 70) return "A";
        if (pct >= 60) return "B+";
        if (pct >= 50) return "B";
        if (pct >= 40) return "C";
        return "F";
    }
}