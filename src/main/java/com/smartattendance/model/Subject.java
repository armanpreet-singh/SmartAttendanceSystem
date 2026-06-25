package com.smartattendance.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Subject.java - Model / Entity Class
 * Maps to the 'subjects' table in the database.
 * Also carries denormalized display fields populated by DAO JOINs.
 */
public class Subject implements Serializable {

    private static final long serialVersionUID = 1L;

    private int           id;
    private String        subjectCode;
    private String        subjectName;
    private int           credits;
    private int           semester;
    private int           departmentId;
    private int           facultyId;
    private int           academicYearId;
    private boolean       active;
    private LocalDateTime createdAt;

    // Denormalized fields (populated by DAO JOINs)
    private String departmentName;
    private String facultyName;
    private String facultyDesignation;
    private String academicYearLabel;

    public Subject() {}

    // ── Getters and Setters ───────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public int getSemester() { return semester; }
    public void setSemester(int semester) { this.semester = semester; }

    public int getDepartmentId() { return departmentId; }
    public void setDepartmentId(int departmentId) { this.departmentId = departmentId; }

    public int getFacultyId() { return facultyId; }
    public void setFacultyId(int facultyId) { this.facultyId = facultyId; }

    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getFacultyDesignation() { return facultyDesignation; }
    public void setFacultyDesignation(String facultyDesignation) {
        this.facultyDesignation = facultyDesignation;
    }

    public String getAcademicYearLabel() { return academicYearLabel; }
    public void setAcademicYearLabel(String academicYearLabel) {
        this.academicYearLabel = academicYearLabel;
    }

    @Override
    public String toString() {
        return "Subject{id=" + id + ", code='" + subjectCode + "', name='" + subjectName + "'}";
    }
}