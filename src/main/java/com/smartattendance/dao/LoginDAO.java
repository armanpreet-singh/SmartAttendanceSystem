package com.smartattendance.dao;

import com.smartattendance.model.Faculty;
import com.smartattendance.model.Student;
import com.smartattendance.util.DBConnection;
import com.smartattendance.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * LoginDAO.java - Data Access Object for Authentication
 *
 * Responsibilities:
 *  - Fetch Student record from DB using email.
 *  - Fetch Faculty record from DB using email.
 *  - Verify password using PasswordUtil.
 *  - Return authenticated model object (Student or Faculty).
 *  - Return null if credentials are invalid.
 *
 * SQL Strategy:
 *  - Uses JOIN with departments and academic_years to load
 *    denormalized fields (departmentName, academicYearLabel).
 *  - Uses PreparedStatement to prevent SQL injection.
 *  - Password is verified in Java (not in SQL) for security.
 *
 * Pattern: DAO (Data Access Object)
 */
public class LoginDAO {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(LoginDAO.class);

    // ----------------------------------------------------------------
    // SQL Queries
    // ----------------------------------------------------------------

    /**
     * Fetches a full student record by email address.
     * JOINs with departments and academic_years for display info.
     * Only fetches active students (is_active = 1).
     */
    private static final String SQL_GET_STUDENT_BY_EMAIL =
            "SELECT " +
            "    s.id, " +
            "    s.student_id, " +
            "    s.first_name, " +
            "    s.last_name, " +
            "    s.email, " +
            "    s.password, " +
            "    s.phone, " +
            "    s.date_of_birth, " +
            "    s.gender, " +
            "    s.department_id, " +
            "    s.current_semester, " +
            "    s.section, " +
            "    s.academic_year_id, " +
            "    s.address, " +
            "    s.profile_photo, " +
            "    s.is_active, " +
            "    s.created_at, " +
            "    s.updated_at, " +
            "    d.dept_name  AS department_name, " +
            "    ay.year_label AS academic_year_label " +
            "FROM students s " +
            "INNER JOIN departments  d  ON s.department_id    = d.id " +
            "INNER JOIN academic_years ay ON s.academic_year_id = ay.id " +
            "WHERE s.email = ? " +
            "  AND s.is_active = 1";

    /**
     * Fetches a full faculty record by email address.
     * JOINs with departments for display info.
     * Only fetches active faculty (is_active = 1).
     */
    private static final String SQL_GET_FACULTY_BY_EMAIL =
            "SELECT " +
            "    f.id, " +
            "    f.faculty_id, " +
            "    f.first_name, " +
            "    f.last_name, " +
            "    f.email, " +
            "    f.password, " +
            "    f.phone, " +
            "    f.designation, " +
            "    f.department_id, " +
            "    f.profile_photo, " +
            "    f.is_active, " +
            "    f.created_at, " +
            "    f.updated_at, " +
            "    d.dept_name AS department_name " +
            "FROM faculty f " +
            "INNER JOIN departments d ON f.department_id = d.id " +
            "WHERE f.email = ? " +
            "  AND f.is_active = 1";

    // ================================================================
    // PUBLIC AUTHENTICATION METHODS
    // ================================================================

    /**
     * Authenticates a student using email and password.
     *
     * <p>Process:
     * <ol>
     *   <li>Fetches the student record from DB using the email.</li>
     *   <li>Verifies the provided password against the stored hash.</li>
     *   <li>If valid, clears the password field from the object
     *       before returning it (security best practice).</li>
     *   <li>Returns null if email not found or password is wrong.</li>
     * </ol>
     * </p>
     *
     * @param email    The email address entered by the student.
     * @param password The plain-text password entered by the student.
     * @return Authenticated {@link Student} object (without password),
     *         or {@code null} if authentication fails.
     */
    public Student authenticateStudent(String email, String password) {
        logger.info("Student login attempt for email: {}", email);

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_STUDENT_BY_EMAIL);
            pstmt.setString(1, email.trim().toLowerCase());

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // ── Row found: map to Student object ──
                String storedPassword = rs.getString("password");

                // ── Verify password ──
                boolean passwordMatch = PasswordUtil.verifyPassword(password, storedPassword);

                if (!passwordMatch) {
                    logger.warn("Student login FAILED (wrong password) for email: {}", email);
                    return null;
                }

                // ── Password verified: map full Student object ──
                Student student = mapStudentFromResultSet(rs);

                // ── Security: clear password from object ──
                student.setPassword(null);

                logger.info("Student login SUCCESSFUL for: {} (ID: {})",
                        student.getFullName(), student.getStudentId());

                return student;

            } else {
                logger.warn("Student login FAILED (email not found): {}", email);
                return null;
            }

        } catch (SQLException e) {
            logger.error("Database error during student authentication for email: {}", email, e);
            return null;
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    /**
     * Authenticates a faculty member using email and password.
     *
     * <p>Process:
     * <ol>
     *   <li>Fetches the faculty record from DB using the email.</li>
     *   <li>Verifies the provided password against the stored hash.</li>
     *   <li>If valid, clears the password field from the object
     *       before returning it (security best practice).</li>
     *   <li>Returns null if email not found or password is wrong.</li>
     * </ol>
     * </p>
     *
     * @param email    The email address entered by the faculty member.
     * @param password The plain-text password entered by the faculty member.
     * @return Authenticated {@link Faculty} object (without password),
     *         or {@code null} if authentication fails.
     */
    public Faculty authenticateFaculty(String email, String password) {
        logger.info("Faculty login attempt for email: {}", email);

        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_FACULTY_BY_EMAIL);
            pstmt.setString(1, email.trim().toLowerCase());

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // ── Row found: map to Faculty object ──
                String storedPassword = rs.getString("password");

                // ── Verify password ──
                boolean passwordMatch = PasswordUtil.verifyPassword(password, storedPassword);

                if (!passwordMatch) {
                    logger.warn("Faculty login FAILED (wrong password) for email: {}", email);
                    return null;
                }

                // ── Password verified: map full Faculty object ──
                Faculty faculty = mapFacultyFromResultSet(rs);

                // ── Security: clear password from object ──
                faculty.setPassword(null);

                logger.info("Faculty login SUCCESSFUL for: {} (ID: {})",
                        faculty.getFullName(), faculty.getFacultyId());

                return faculty;

            } else {
                logger.warn("Faculty login FAILED (email not found): {}", email);
                return null;
            }

        } catch (SQLException e) {
            logger.error("Database error during faculty authentication for email: {}", email, e);
            return null;
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }

    // ================================================================
    // PRIVATE MAPPING HELPER METHODS
    // ================================================================

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Student} object.
     *
     * <p>Called after {@code rs.next()} has been confirmed to return true.</p>
     *
     * @param rs The {@link ResultSet} positioned at the student row.
     * @return A fully populated {@link Student} object.
     * @throws SQLException if any column name is invalid or DB error occurs.
     */
    private Student mapStudentFromResultSet(ResultSet rs) throws SQLException {
        Student student = new Student();

        student.setId(rs.getInt("id"));
        student.setStudentId(rs.getString("student_id"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setEmail(rs.getString("email"));

        // Map password — will be cleared after verification
        student.setPassword(rs.getString("password"));

        student.setPhone(rs.getString("phone"));

        // Map date_of_birth (java.sql.Date → LocalDate)
        java.sql.Date dob = rs.getDate("date_of_birth");
        if (dob != null) {
            student.setDateOfBirth(dob.toLocalDate());
        }

        student.setGender(rs.getString("gender"));
        student.setDepartmentId(rs.getInt("department_id"));
        student.setDepartmentName(rs.getString("department_name"));
        student.setCurrentSemester(rs.getInt("current_semester"));
        student.setSection(rs.getString("section"));
        student.setAcademicYearId(rs.getInt("academic_year_id"));
        student.setAcademicYearLabel(rs.getString("academic_year_label"));
        student.setAddress(rs.getString("address"));
        student.setProfilePhoto(rs.getString("profile_photo"));
        student.setActive(rs.getInt("is_active") == 1);

        // Map timestamps (java.sql.Timestamp → LocalDateTime)
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            student.setCreatedAt(createdAt.toLocalDateTime());
        }

        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            student.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return student;
    }

    /**
     * Maps the current row of a {@link ResultSet} to a {@link Faculty} object.
     *
     * <p>Called after {@code rs.next()} has been confirmed to return true.</p>
     *
     * @param rs The {@link ResultSet} positioned at the faculty row.
     * @return A fully populated {@link Faculty} object.
     * @throws SQLException if any column name is invalid or DB error occurs.
     */
    private Faculty mapFacultyFromResultSet(ResultSet rs) throws SQLException {
        Faculty faculty = new Faculty();

        faculty.setId(rs.getInt("id"));
        faculty.setFacultyId(rs.getString("faculty_id"));
        faculty.setFirstName(rs.getString("first_name"));
        faculty.setLastName(rs.getString("last_name"));
        faculty.setEmail(rs.getString("email"));

        // Map password — will be cleared after verification
        faculty.setPassword(rs.getString("password"));

        faculty.setPhone(rs.getString("phone"));
        faculty.setDesignation(rs.getString("designation"));
        faculty.setDepartmentId(rs.getInt("department_id"));
        faculty.setDepartmentName(rs.getString("department_name"));
        faculty.setProfilePhoto(rs.getString("profile_photo"));
        faculty.setActive(rs.getInt("is_active") == 1);

        // Map timestamps (java.sql.Timestamp → LocalDateTime)
        java.sql.Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            faculty.setCreatedAt(createdAt.toLocalDateTime());
        }

        java.sql.Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            faculty.setUpdatedAt(updatedAt.toLocalDateTime());
        }

        return faculty;
    }
}