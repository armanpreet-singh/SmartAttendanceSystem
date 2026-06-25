-- ============================================================
-- Smart Student Attendance and Academic Monitoring System
-- Database Schema
-- Version: 1.0
-- ============================================================

-- ------------------------------------------------------------
-- DROP AND CREATE DATABASE
-- ------------------------------------------------------------

DROP DATABASE IF EXISTS smart_attendance_db;
CREATE DATABASE smart_attendance_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE smart_attendance_db;

-- ============================================================
-- TABLE 1: academic_years
-- Purpose: Stores academic year records for multi-year tracking
-- ============================================================

CREATE TABLE academic_years (
    id          INT             NOT NULL AUTO_INCREMENT,
    year_label  VARCHAR(20)     NOT NULL,
    start_date  DATE            NOT NULL,
    end_date    DATE            NOT NULL,
    is_current  TINYINT(1)      NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_academic_years    PRIMARY KEY (id),
    CONSTRAINT uq_year_label        UNIQUE      (year_label),
    CONSTRAINT chk_year_dates       CHECK       (end_date > start_date)
);

-- ============================================================
-- TABLE 2: departments
-- Purpose: Stores department/branch details
-- ============================================================

CREATE TABLE departments (
    id              INT             NOT NULL AUTO_INCREMENT,
    dept_name       VARCHAR(100)    NOT NULL,
    dept_code       VARCHAR(10)     NOT NULL,
    description     VARCHAR(255)        NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_departments   PRIMARY KEY (id),
    CONSTRAINT uq_dept_code     UNIQUE      (dept_code),
    CONSTRAINT uq_dept_name     UNIQUE      (dept_name)
);

-- ============================================================
-- TABLE 3: faculty
-- Purpose: Stores faculty personal info and login credentials
-- ============================================================

CREATE TABLE faculty (
    id                  INT             NOT NULL AUTO_INCREMENT,
    faculty_id          VARCHAR(20)     NOT NULL,
    first_name          VARCHAR(50)     NOT NULL,
    last_name           VARCHAR(50)     NOT NULL,
    email               VARCHAR(100)    NOT NULL,
    password            VARCHAR(255)    NOT NULL,
    phone               VARCHAR(15)         NULL,
    designation         VARCHAR(100)    NOT NULL,
    department_id       INT             NOT NULL,
    profile_photo       VARCHAR(255)        NULL DEFAULT 'default-avatar.png',
    is_active           TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_faculty           PRIMARY KEY (id),
    CONSTRAINT uq_faculty_id        UNIQUE      (faculty_id),
    CONSTRAINT uq_faculty_email     UNIQUE      (email),
    CONSTRAINT fk_faculty_dept      FOREIGN KEY (department_id)
                                    REFERENCES  departments(id)
                                    ON DELETE   RESTRICT
                                    ON UPDATE   CASCADE
);

-- ============================================================
-- TABLE 4: students
-- Purpose: Stores student personal info and login credentials
-- ============================================================

CREATE TABLE students (
    id                  INT             NOT NULL AUTO_INCREMENT,
    student_id          VARCHAR(20)     NOT NULL,
    first_name          VARCHAR(50)     NOT NULL,
    last_name           VARCHAR(50)     NOT NULL,
    email               VARCHAR(100)    NOT NULL,
    password            VARCHAR(255)    NOT NULL,
    phone               VARCHAR(15)         NULL,
    date_of_birth       DATE                NULL,
    gender              ENUM('Male','Female','Other')
                                            NULL,
    department_id       INT             NOT NULL,
    current_semester    TINYINT         NOT NULL,
    section             VARCHAR(5)      NOT NULL,
    academic_year_id    INT             NOT NULL,
    address             TEXT                NULL,
    profile_photo       VARCHAR(255)        NULL DEFAULT 'default-avatar.png',
    is_active           TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_students              PRIMARY KEY (id),
    CONSTRAINT uq_student_id            UNIQUE      (student_id),
    CONSTRAINT uq_student_email         UNIQUE      (email),
    CONSTRAINT chk_semester             CHECK       (current_semester BETWEEN 1 AND 8),
    CONSTRAINT fk_student_dept          FOREIGN KEY (department_id)
                                        REFERENCES  departments(id)
                                        ON DELETE   RESTRICT
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_student_acad_year     FOREIGN KEY (academic_year_id)
                                        REFERENCES  academic_years(id)
                                        ON DELETE   RESTRICT
                                        ON UPDATE   CASCADE
);

-- ============================================================
-- TABLE 5: exam_types
-- Purpose: Lookup table for types of exams
-- ============================================================

CREATE TABLE exam_types (
    id              INT             NOT NULL AUTO_INCREMENT,
    exam_name       VARCHAR(50)     NOT NULL,
    max_marks       INT             NOT NULL,
    description     VARCHAR(255)        NULL,

    CONSTRAINT pk_exam_types        PRIMARY KEY (id),
    CONSTRAINT uq_exam_name         UNIQUE      (exam_name),
    CONSTRAINT chk_max_marks        CHECK       (max_marks > 0)
);

-- ============================================================
-- TABLE 6: subjects
-- Purpose: Stores subject details linked to department,
--          semester, faculty, and academic year
-- ============================================================

CREATE TABLE subjects (
    id                  INT             NOT NULL AUTO_INCREMENT,
    subject_code        VARCHAR(20)     NOT NULL,
    subject_name        VARCHAR(100)    NOT NULL,
    credits             TINYINT         NOT NULL DEFAULT 3,
    semester            TINYINT         NOT NULL,
    department_id       INT             NOT NULL,
    faculty_id          INT             NOT NULL,
    academic_year_id    INT             NOT NULL,
    is_active           TINYINT(1)      NOT NULL DEFAULT 1,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_subjects              PRIMARY KEY (id),
    CONSTRAINT uq_subject_code_year     UNIQUE      (subject_code, academic_year_id),
    CONSTRAINT chk_subject_semester     CHECK       (semester BETWEEN 1 AND 8),
    CONSTRAINT chk_credits             CHECK       (credits BETWEEN 1 AND 6),
    CONSTRAINT fk_subject_dept          FOREIGN KEY (department_id)
                                        REFERENCES  departments(id)
                                        ON DELETE   RESTRICT
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_subject_faculty       FOREIGN KEY (faculty_id)
                                        REFERENCES  faculty(id)
                                        ON DELETE   RESTRICT
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_subject_acad_year     FOREIGN KEY (academic_year_id)
                                        REFERENCES  academic_years(id)
                                        ON DELETE   RESTRICT
                                        ON UPDATE   CASCADE
);

-- ============================================================
-- TABLE 7: student_subjects
-- Purpose: Junction table mapping students to subjects
--          (enrollment)
-- ============================================================

CREATE TABLE student_subjects (
    id                  INT             NOT NULL AUTO_INCREMENT,
    student_id          INT             NOT NULL,
    subject_id          INT             NOT NULL,
    enrolled_date       DATE            NOT NULL,

    CONSTRAINT pk_student_subjects      PRIMARY KEY (id),
    CONSTRAINT uq_student_subject       UNIQUE      (student_id, subject_id),
    CONSTRAINT fk_ss_student            FOREIGN KEY (student_id)
                                        REFERENCES  students(id)
                                        ON DELETE   CASCADE
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_ss_subject            FOREIGN KEY (subject_id)
                                        REFERENCES  subjects(id)
                                        ON DELETE   CASCADE
                                        ON UPDATE   CASCADE
);

-- ============================================================
-- TABLE 8: attendance
-- Purpose: Stores individual attendance record per student
--          per subject per date
-- ============================================================

CREATE TABLE attendance (
    id                  INT             NOT NULL AUTO_INCREMENT,
    student_id          INT             NOT NULL,
    subject_id          INT             NOT NULL,
    faculty_id          INT             NOT NULL,
    attendance_date     DATE            NOT NULL,
    status              ENUM('Present','Absent','Late')
                                        NOT NULL DEFAULT 'Absent',
    remarks             VARCHAR(255)        NULL,
    academic_year_id    INT             NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_attendance            PRIMARY KEY (id),
    CONSTRAINT uq_attendance_record     UNIQUE      (student_id, subject_id, attendance_date),
    CONSTRAINT fk_att_student           FOREIGN KEY (student_id)
                                        REFERENCES  students(id)
                                        ON DELETE   CASCADE
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_att_subject           FOREIGN KEY (subject_id)
                                        REFERENCES  subjects(id)
                                        ON DELETE   CASCADE
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_att_faculty           FOREIGN KEY (faculty_id)
                                        REFERENCES  faculty(id)
                                        ON DELETE   RESTRICT
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_att_acad_year         FOREIGN KEY (academic_year_id)
                                        REFERENCES  academic_years(id)
                                        ON DELETE   RESTRICT
                                        ON UPDATE   CASCADE
);

-- ============================================================
-- TABLE 9: attendance_summary
-- Purpose: Aggregated attendance percentage per student
--          per subject for quick dashboard access
-- ============================================================

CREATE TABLE attendance_summary (
    id                      INT             NOT NULL AUTO_INCREMENT,
    student_id              INT             NOT NULL,
    subject_id              INT             NOT NULL,
    academic_year_id        INT             NOT NULL,
    total_classes           INT             NOT NULL DEFAULT 0,
    classes_present         INT             NOT NULL DEFAULT 0,
    classes_absent          INT             NOT NULL DEFAULT 0,
    classes_late            INT             NOT NULL DEFAULT 0,
    attendance_percentage   DECIMAL(5,2)    NOT NULL DEFAULT 0.00,
    last_updated            TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                            ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_attendance_summary    PRIMARY KEY (id),
    CONSTRAINT uq_att_summary           UNIQUE      (student_id, subject_id, academic_year_id),
    CONSTRAINT chk_total_classes        CHECK       (total_classes >= 0),
    CONSTRAINT chk_present             CHECK       (classes_present >= 0),
    CONSTRAINT chk_percentage           CHECK       (attendance_percentage BETWEEN 0.00 AND 100.00),
    CONSTRAINT fk_attsum_student        FOREIGN KEY (student_id)
                                        REFERENCES  students(id)
                                        ON DELETE   CASCADE
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_attsum_subject        FOREIGN KEY (subject_id)
                                        REFERENCES  subjects(id)
                                        ON DELETE   CASCADE
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_attsum_acad_year      FOREIGN KEY (academic_year_id)
                                        REFERENCES  academic_years(id)
                                        ON DELETE   RESTRICT
                                        ON UPDATE   CASCADE
);

-- ============================================================
-- TABLE 10: marks
-- Purpose: Stores marks obtained by each student in each
--          subject for different exam types
-- ============================================================

CREATE TABLE marks (
    id                  INT             NOT NULL AUTO_INCREMENT,
    student_id          INT             NOT NULL,
    subject_id          INT             NOT NULL,
    exam_type_id        INT             NOT NULL,
    academic_year_id    INT             NOT NULL,
    marks_obtained      DECIMAL(5,2)    NOT NULL,
    grade               VARCHAR(5)          NULL,
    remarks             VARCHAR(255)        NULL,
    recorded_by         INT             NOT NULL,
    recorded_at         TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP
                                        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT pk_marks                 PRIMARY KEY (id),
    CONSTRAINT uq_marks_record          UNIQUE      (student_id, subject_id, exam_type_id, academic_year_id),
    CONSTRAINT chk_marks_obtained       CHECK       (marks_obtained >= 0),
    CONSTRAINT fk_marks_student         FOREIGN KEY (student_id)
                                        REFERENCES  students(id)
                                        ON DELETE   CASCADE
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_marks_subject         FOREIGN KEY (subject_id)
                                        REFERENCES  subjects(id)
                                        ON DELETE   CASCADE
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_marks_exam_type       FOREIGN KEY (exam_type_id)
                                        REFERENCES  exam_types(id)
                                        ON DELETE   RESTRICT
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_marks_acad_year       FOREIGN KEY (academic_year_id)
                                        REFERENCES  academic_years(id)
                                        ON DELETE   RESTRICT
                                        ON UPDATE   CASCADE,
    CONSTRAINT fk_marks_faculty         FOREIGN KEY (recorded_by)
                                        REFERENCES  faculty(id)
                                        ON DELETE   RESTRICT
                                        ON UPDATE   CASCADE
);

-- ============================================================
-- END OF SCHEMA
-- ============================================================