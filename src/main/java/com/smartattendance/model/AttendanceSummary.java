package com.smartattendance.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * AttendanceSummary.java - Model / Entity Class
 * Maps to the 'attendance_summary' table.
 * Carries aggregated attendance data per student per subject.
 */
public class AttendanceSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private int           id;
    private int           studentId;
    private int           subjectId;
    private int           academicYearId;
    private int           totalClasses;
    private int           classesPresent;
    private int           classesAbsent;
    private int           classesLate;
    private double        attendancePercentage;
    private LocalDateTime lastUpdated;

    // Denormalized fields
    private String subjectCode;
    private String subjectName;
    private int    credits;

    public AttendanceSummary() {}

    // ── Getters and Setters ───────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }

    public int getTotalClasses() { return totalClasses; }
    public void setTotalClasses(int totalClasses) { this.totalClasses = totalClasses; }

    public int getClassesPresent() { return classesPresent; }
    public void setClassesPresent(int classesPresent) { this.classesPresent = classesPresent; }

    public int getClassesAbsent() { return classesAbsent; }
    public void setClassesAbsent(int classesAbsent) { this.classesAbsent = classesAbsent; }

    public int getClassesLate() { return classesLate; }
    public void setClassesLate(int classesLate) { this.classesLate = classesLate; }

    public double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
    }

    public LocalDateTime getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(LocalDateTime lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }
}