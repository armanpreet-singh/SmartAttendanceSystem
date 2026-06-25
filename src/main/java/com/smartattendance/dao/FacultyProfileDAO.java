package com.smartattendance.dao;

import com.smartattendance.model.Faculty;
import com.smartattendance.util.DBConnection;
import com.smartattendance.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * FacultyProfileDAO.java
 *
 * Handles faculty profile operations:
 *  - Fetch full profile by pk
 *  - Update editable fields (phone, designation)
 *  - Change password with old-password verification
 */
public class FacultyProfileDAO {

    private static final Logger logger = LoggerFactory.getLogger(FacultyProfileDAO.class);

    // ── SQL Queries ───────────────────────────────────────────────────

    private static final String SQL_GET_FACULTY_BY_PK =
            "SELECT " +
            "    f.id, f.faculty_id, f.first_name, f.last_name, " +
            "    f.email, f.password, f.phone, f.designation, " +
            "    f.department_id, f.profile_photo, f.is_active, " +
            "    f.created_at, f.updated_at, " +
            "    d.dept_name AS department_name " +
            "FROM   faculty f " +
            "INNER JOIN departments d ON f.department_id = d.id " +
            "WHERE  f.id = ? AND f.is_active = 1";

    private static final String SQL_UPDATE_PROFILE =
            "UPDATE faculty " +
            "SET    phone = ?, designation = ?, updated_at = NOW() " +
            "WHERE  id = ?";

    private static final String SQL_GET_PASSWORD =
            "SELECT password FROM faculty WHERE id = ?";

    private static final String SQL_UPDATE_PASSWORD =
            "UPDATE faculty SET password = ?, updated_at = NOW() WHERE id = ?";

    // ── Public Methods ────────────────────────────────────────────────

    /**
     * Fetches full faculty profile by primary key.
     */
    public Faculty getFacultyById(int facultyPk) {
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_GET_FACULTY_BY_PK);
            pstmt.setInt(1, facultyPk);
            rs    = pstmt.executeQuery();

            if (rs.next()) {
                Faculty f = new Faculty();
                f.setId(rs.getInt("id"));
                f.setFacultyId(rs.getString("faculty_id"));
                f.setFirstName(rs.getString("first_name"));
                f.setLastName(rs.getString("last_name"));
                f.setEmail(rs.getString("email"));
                f.setPassword(rs.getString("password"));
                f.setPhone(rs.getString("phone"));
                f.setDesignation(rs.getString("designation"));
                f.setDepartmentId(rs.getInt("department_id"));
                f.setDepartmentName(rs.getString("department_name"));
                f.setProfilePhoto(rs.getString("profile_photo"));
                f.setActive(rs.getInt("is_active") == 1);

                java.sql.Timestamp ca = rs.getTimestamp("created_at");
                if (ca != null) f.setCreatedAt(ca.toLocalDateTime());
                java.sql.Timestamp ua = rs.getTimestamp("updated_at");
                if (ua != null) f.setUpdatedAt(ua.toLocalDateTime());

                return f;
            }
        } catch (SQLException e) {
            logger.error("getFacultyById error for pk={}", facultyPk, e);
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
        return null;
    }

    /**
     * Updates editable profile fields: phone and designation.
     */
    public boolean updateProfile(int facultyPk, String phone, String designation) {
        Connection        conn  = null;
        PreparedStatement pstmt = null;

        try {
            conn  = DBConnection.getConnection();
            pstmt = conn.prepareStatement(SQL_UPDATE_PROFILE);
            pstmt.setString(1, phone);
            pstmt.setString(2, designation);
            pstmt.setInt   (3, facultyPk);
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            logger.error("updateProfile error for pk={}", facultyPk, e);
            return false;
        } finally {
            DBConnection.closeResources(conn, pstmt, null);
        }
    }

    /**
     * Changes password after verifying the current password.
     *
     * @return "SUCCESS" | "WRONG_OLD_PASSWORD" | "DB_ERROR"
     */
    public String changePassword(int facultyPk, String oldPassword, String newPassword) {
        Connection        conn  = null;
        PreparedStatement pstmt = null;
        ResultSet         rs    = null;

        try {
            conn  = DBConnection.getConnection();

            // Step 1: Fetch stored password
            pstmt = conn.prepareStatement(SQL_GET_PASSWORD);
            pstmt.setInt(1, facultyPk);
            rs    = pstmt.executeQuery();

            if (!rs.next()) return "DB_ERROR";

            String storedPassword = rs.getString("password");
            DBConnection.closeResources(null, pstmt, rs);

            // Step 2: Verify old password
            if (!PasswordUtil.verifyPassword(oldPassword, storedPassword)) {
                logger.warn("changePassword: Wrong old password for pk={}", facultyPk);
                return "WRONG_OLD_PASSWORD";
            }

            // Step 3: Hash and save new password
            String hashedNew = PasswordUtil.hashPassword(newPassword);
            pstmt = conn.prepareStatement(SQL_UPDATE_PASSWORD);
            pstmt.setString(1, hashedNew);
            pstmt.setInt   (2, facultyPk);
            pstmt.executeUpdate();

            logger.info("changePassword: Password changed for pk={}", facultyPk);
            return "SUCCESS";

        } catch (SQLException e) {
            logger.error("changePassword error for pk={}", facultyPk, e);
            return "DB_ERROR";
        } finally {
            DBConnection.closeResources(conn, pstmt, rs);
        }
    }
}