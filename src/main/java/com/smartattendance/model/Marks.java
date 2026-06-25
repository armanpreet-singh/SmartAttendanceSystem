package com.smartattendance.model;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Marks.java - Model / Entity Class
 * Maps to the 'marks' table in the database.
 * Carries marks obtained per student per subject per exam type.
 */
public class Marks implements Serializable {

    private static final long serialVersionUID = 1L;

    private int           id;
    private int           studentId;
    private int           subjectId;
    private int           examTypeId;
    private int           academicYearId;
    private double        marksObtained;
    private String        grade;
    private String        remarks;
    private int           recordedBy;
    private LocalDateTime recordedAt;
    private LocalDateTime updatedAt;

    // Denormalized fields (from JOINs)
    private String subjectCode;
    private String subjectName;
    private int    credits;
    private String examName;
    private int    maxMarks;

    public Marks() {}

    // ── Getters and Setters ───────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }

    public int getExamTypeId() { return examTypeId; }
    public void setExamTypeId(int examTypeId) { this.examTypeId = examTypeId; }

    public int getAcademicYearId() { return academicYearId; }
    public void setAcademicYearId(int academicYearId) { this.academicYearId = academicYearId; }

    public double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(double marksObtained) { this.marksObtained = marksObtained; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public int getRecordedBy() { return recordedBy; }
    public void setRecordedBy(int recordedBy) { this.recordedBy = recordedBy; }

    public LocalDateTime getRecordedAt() { return recordedAt; }
    public void setRecordedAt(LocalDateTime recordedAt) { this.recordedAt = recordedAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getExamName() { return examName; }
    public void setExamName(String examName) { this.examName = examName; }

    public int getMaxMarks() { return maxMarks; }
    public void setMaxMarks(int maxMarks) { this.maxMarks = maxMarks; }

    /**
     * Returns marks percentage for this record.
     * Formula: (marksObtained / maxMarks) * 100
     */
    public double getMarksPercentage() {
        if (maxMarks <= 0) return 0.0;
        return Math.round((marksObtained / maxMarks) * 100.0 * 100.0) / 100.0;
    }
}