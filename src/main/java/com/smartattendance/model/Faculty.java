package com.smartattendance.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Faculty.java - Model / Entity Class
 *
 * Maps to the 'faculty' table in the database.
 *
 * Implements Serializable so the object can be stored
 * in the HTTP session safely.
 *
 * Fields map exactly to columns in the faculty table:
 *
 *   id, faculty_id, first_name, last_name, email, password,
 *   phone, designation, department_id, profile_photo,
 *   is_active, created_at, updated_at
 *
 * NOTE: The password field is included in the model for DAO mapping
 *       but MUST NEVER be sent to the JSP view or stored in session.
 *       Only non-sensitive fields are stored in session.
 */
public class Faculty implements Serializable {

    // ----------------------------------------------------------------
    // Serialization UID
    // ----------------------------------------------------------------
    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------
    // Fields — Map to 'faculty' table columns
    // ----------------------------------------------------------------

    /** Primary key (auto-incremented). Maps to: id */
    private int id;

    /** Unique faculty identifier (e.g., FAC001). Maps to: faculty_id */
    private String facultyId;

    /** Faculty's first name. Maps to: first_name */
    private String firstName;

    /** Faculty's last name. Maps to: last_name */
    private String lastName;

    /** Faculty's email address (used for login). Maps to: email */
    private String email;

    /**
     * Hashed password. Maps to: password.
     * NEVER expose this field in JSP or session.
     */
    private String password;

    /** Faculty's phone number. Maps to: phone */
    private String phone;

    /** Faculty's designation (e.g., Associate Professor). Maps to: designation */
    private String designation;

    /** Foreign key to departments table. Maps to: department_id */
    private int departmentId;

    /**
     * Denormalized field: department name loaded via JOIN.
     * Not a direct column — populated by DAO query.
     */
    private String departmentName;

    /** Profile photo filename. Maps to: profile_photo */
    private String profilePhoto;

    /** Active status flag. Maps to: is_active */
    private boolean active;

    /** Record creation timestamp. Maps to: created_at */
    private LocalDateTime createdAt;

    /** Record last-updated timestamp. Maps to: updated_at */
    private LocalDateTime updatedAt;

    // ----------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------

    /**
     * Default no-argument constructor.
     * Required for JavaBean conventions.
     */
    public Faculty() {
    }

    /**
     * Convenience constructor for login authentication.
     * Used when only core identity fields are needed.
     *
     * @param id           Primary key.
     * @param facultyId    Unique faculty ID string.
     * @param firstName    First name.
     * @param lastName     Last name.
     * @param email        Email address.
     * @param designation  Faculty designation.
     * @param departmentId Department foreign key.
     */
    public Faculty(int id,
                   String facultyId,
                   String firstName,
                   String lastName,
                   String email,
                   String designation,
                   int departmentId) {
        this.id           = id;
        this.facultyId    = facultyId;
        this.firstName    = firstName;
        this.lastName     = lastName;
        this.email        = email;
        this.designation  = designation;
        this.departmentId = departmentId;
    }

    // ----------------------------------------------------------------
    // Getters and Setters
    // ----------------------------------------------------------------

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public int getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(int departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ----------------------------------------------------------------
    // Utility Methods
    // ----------------------------------------------------------------

    /**
     * Returns the faculty member's full name (firstName + lastName).
     *
     * @return Full name string.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the faculty's display title (Designation + Full Name).
     * Example: "Dr. Rajesh Kumar" or "Prof. Priya Sharma"
     *
     * @return Formatted display title.
     */
    public String getDisplayTitle() {
        return designation + " " + getFullName();
    }

    // ----------------------------------------------------------------
    // toString (excludes password for security)
    // ----------------------------------------------------------------

    @Override
    public String toString() {
        return "Faculty{" +
                "id="               + id            +
                ", facultyId='"     + facultyId     + '\'' +
                ", fullName='"      + getFullName() + '\'' +
                ", email='"         + email         + '\'' +
                ", designation='"   + designation   + '\'' +
                ", departmentId="   + departmentId  +
                ", active="         + active        +
                '}';
    }
}