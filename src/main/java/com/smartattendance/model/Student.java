package com.smartattendance.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Student.java - Model / Entity Class
 *
 * Maps to the 'students' table in the database.
 *
 * Implements Serializable so the object can be stored
 * in the HTTP session safely.
 *
 * Fields map exactly to columns in the students table:
 *
 *   id, student_id, first_name, last_name, email, password,
 *   phone, date_of_birth, gender, department_id, current_semester,
 *   section, academic_year_id, address, profile_photo,
 *   is_active, created_at, updated_at
 *
 * NOTE: The password field is included in the model for DAO mapping
 *       but MUST NEVER be sent to the JSP view or stored in session.
 *       Only non-sensitive fields are stored in session.
 */
public class Student implements Serializable {

    // ----------------------------------------------------------------
    // Serialization UID
    // ----------------------------------------------------------------
    private static final long serialVersionUID = 1L;

    // ----------------------------------------------------------------
    // Fields — Map to 'students' table columns
    // ----------------------------------------------------------------

    /** Primary key (auto-incremented). Maps to: id */
    private int id;

    /** Unique student identifier (e.g., STU001). Maps to: student_id */
    private String studentId;

    /** Student's first name. Maps to: first_name */
    private String firstName;

    /** Student's last name. Maps to: last_name */
    private String lastName;

    /** Student's email address (used for login). Maps to: email */
    private String email;

    /**
     * Hashed password. Maps to: password.
     * NEVER expose this field in JSP or session.
     */
    private String password;

    /** Student's phone number. Maps to: phone */
    private String phone;

    /** Student's date of birth. Maps to: date_of_birth */
    private LocalDate dateOfBirth;

    /** Student's gender (Male / Female / Other). Maps to: gender */
    private String gender;

    /** Foreign key to departments table. Maps to: department_id */
    private int departmentId;

    /**
     * Denormalized field: department name loaded via JOIN.
     * Not a direct column — populated by DAO query.
     */
    private String departmentName;

    /** Current semester (1–8). Maps to: current_semester */
    private int currentSemester;

    /** Section (e.g., A, B). Maps to: section */
    private String section;

    /** Foreign key to academic_years table. Maps to: academic_year_id */
    private int academicYearId;

    /**
     * Denormalized field: academic year label (e.g., "2024-2025").
     * Not a direct column — populated by DAO query.
     */
    private String academicYearLabel;

    /** Student's home address. Maps to: address */
    private String address;

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
    public Student() {
    }

    /**
     * Convenience constructor for login authentication.
     * Used when only core identity fields are needed.
     *
     * @param id           Primary key.
     * @param studentId    Unique student ID string.
     * @param firstName    First name.
     * @param lastName     Last name.
     * @param email        Email address.
     * @param departmentId Department foreign key.
     */
    public Student(int id,
                   String studentId,
                   String firstName,
                   String lastName,
                   String email,
                   int departmentId) {
        this.id           = id;
        this.studentId    = studentId;
        this.firstName    = firstName;
        this.lastName     = lastName;
        this.email        = email;
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

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
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

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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

    public int getCurrentSemester() {
        return currentSemester;
    }

    public void setCurrentSemester(int currentSemester) {
        this.currentSemester = currentSemester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public int getAcademicYearId() {
        return academicYearId;
    }

    public void setAcademicYearId(int academicYearId) {
        this.academicYearId = academicYearId;
    }

    public String getAcademicYearLabel() {
        return academicYearLabel;
    }

    public void setAcademicYearLabel(String academicYearLabel) {
        this.academicYearLabel = academicYearLabel;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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
     * Returns the student's full name (firstName + lastName).
     *
     * @return Full name string.
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Returns the student's semester with ordinal suffix.
     * Example: 4 → "4th Semester"
     *
     * @return Semester string with suffix.
     */
    public String getSemesterDisplay() {
        String suffix = switch (currentSemester) {
            case 1  -> "st";
            case 2  -> "nd";
            case 3  -> "rd";
            default -> "th";
        };
        return currentSemester + suffix + " Semester";
    }

    // ----------------------------------------------------------------
    // toString (excludes password for security)
    // ----------------------------------------------------------------

    @Override
    public String toString() {
        return "Student{" +
                "id="               + id               +
                ", studentId='"     + studentId        + '\'' +
                ", fullName='"      + getFullName()    + '\'' +
                ", email='"         + email            + '\'' +
                ", departmentId="   + departmentId     +
                ", semester="       + currentSemester  +
                ", section='"       + section          + '\'' +
                ", active="         + active           +
                '}';
    }
}