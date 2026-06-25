-- ============================================================
-- Smart Student Attendance and Academic Monitoring System
-- Sample / Seed Data
-- Version: 1.0
-- NOTE: Passwords are stored as plain text for testing only.
--       In production, use hashed passwords (SHA-256 / BCrypt).
-- ============================================================

USE smart_attendance_db;

-- ============================================================
-- INSERT: academic_years
-- ============================================================

INSERT INTO academic_years (year_label, start_date, end_date, is_current)
VALUES
    ('2021-2022', '2021-07-01', '2022-05-31', 0),
    ('2022-2023', '2022-07-01', '2023-05-31', 0),
    ('2023-2024', '2023-07-01', '2024-05-31', 0),
    ('2024-2025', '2024-07-01', '2025-05-31', 1),
    ('2025-2026', '2025-07-01', '2026-05-31', 0);

-- ============================================================
-- INSERT: departments
-- ============================================================

INSERT INTO departments (dept_name, dept_code, description)
VALUES
    ('Computer Science and Engineering',     'CSE',  'Covers software, algorithms, and computing systems'),
    ('Electronics and Communication',        'ECE',  'Covers electronics, circuits, and communication systems'),
    ('Information Technology',               'IT',   'Covers networking, databases, and IT infrastructure'),
    ('Mechanical Engineering',               'MECH', 'Covers thermodynamics, machines, and manufacturing'),
    ('Civil Engineering',                    'CIVIL','Covers structures, construction, and geotechnics'),
    ('Electrical and Electronics',           'EEE',  'Covers power systems and electrical machines'),
    ('Artificial Intelligence and ML',       'AIML', 'Covers machine learning, AI, and data science');

-- ============================================================
-- INSERT: faculty
-- NOTE: In production, hash these passwords.
--       Test password for all: "faculty@123"
-- ============================================================

INSERT INTO faculty (faculty_id, first_name, last_name, email, password, phone, designation, department_id)
VALUES
    ('FAC001', 'Rajesh',    'Kumar',    'rajesh.kumar@college.edu',    'faculty@123', '9876543210', 'Associate Professor', 1),
    ('FAC002', 'Priya',     'Sharma',   'priya.sharma@college.edu',    'faculty@123', '9876543211', 'Assistant Professor', 1),
    ('FAC003', 'Suresh',    'Nair',     'suresh.nair@college.edu',     'faculty@123', '9876543212', 'Professor',           2),
    ('FAC004', 'Anita',     'Menon',    'anita.menon@college.edu',     'faculty@123', '9876543213', 'Assistant Professor', 3),
    ('FAC005', 'Vijay',     'Patel',    'vijay.patel@college.edu',     'faculty@123', '9876543214', 'Associate Professor', 4),
    ('FAC006', 'Deepika',   'Rao',      'deepika.rao@college.edu',     'faculty@123', '9876543215', 'Assistant Professor', 7),
    ('FAC007', 'Arun',      'Verma',    'arun.verma@college.edu',      'faculty@123', '9876543216', 'Professor',           1),
    ('FAC008', 'Kavitha',   'Reddy',    'kavitha.reddy@college.edu',   'faculty@123', '9876543217', 'Assistant Professor', 2);

-- ============================================================
-- INSERT: students
-- NOTE: In production, hash these passwords.
--       Test password for all: "student@123"
-- ============================================================

INSERT INTO students (student_id, first_name, last_name, email, password, phone, date_of_birth, gender, department_id, current_semester, section, academic_year_id)
VALUES
    ('STU001', 'Aarav',     'Singh',    'aarav.singh@student.edu',     'student@123', '9123456781', '2004-03-15', 'Male',   1, 4, 'A', 4),
    ('STU002', 'Ananya',    'Gupta',    'ananya.gupta@student.edu',    'student@123', '9123456782', '2004-07-22', 'Female', 1, 4, 'A', 4),
    ('STU003', 'Rohan',     'Mehta',    'rohan.mehta@student.edu',     'student@123', '9123456783', '2003-11-08', 'Male',   1, 4, 'B', 4),
    ('STU004', 'Sneha',     'Joshi',    'sneha.joshi@student.edu',     'student@123', '9123456784', '2004-01-30', 'Female', 1, 4, 'A', 4),
    ('STU005', 'Kiran',     'Pillai',   'kiran.pillai@student.edu',    'student@123', '9123456785', '2003-09-12', 'Male',   1, 4, 'B', 4),
    ('STU006', 'Pooja',     'Iyer',     'pooja.iyer@student.edu',      'student@123', '9123456786', '2004-05-19', 'Female', 7, 2, 'A', 4),
    ('STU007', 'Arjun',     'Das',      'arjun.das@student.edu',       'student@123', '9123456787', '2003-12-25', 'Male',   7, 2, 'A', 4),
    ('STU008', 'Meera',     'Krishnan', 'meera.krishnan@student.edu',  'student@123', '9123456788', '2004-04-11', 'Female', 3, 3, 'A', 4),
    ('STU009', 'Rahul',     'Bose',     'rahul.bose@student.edu',      'student@123', '9123456789', '2003-08-02', 'Male',   3, 3, 'B', 4),
    ('STU010', 'Divya',     'Nair',     'divya.nair@student.edu',      'student@123', '9123456790', '2004-02-14', 'Female', 2, 5, 'A', 4);

-- ============================================================
-- INSERT: exam_types
-- ============================================================

INSERT INTO exam_types (exam_name, max_marks, description)
VALUES
    ('Internal-1',   30,  'First Internal Assessment Exam'),
    ('Internal-2',   30,  'Second Internal Assessment Exam'),
    ('Mid-Term',     50,  'Mid Semester Examination'),
    ('Final-Exam',  100,  'End Semester Final Examination'),
    ('Assignment',   20,  'Subject Assignment Submission'),
    ('Lab-Exam',     50,  'Practical / Laboratory Examination'),
    ('Quiz',         10,  'In-class Quiz or Viva');

-- ============================================================
-- INSERT: subjects
-- ============================================================

INSERT INTO subjects (subject_code, subject_name, credits, semester, department_id, faculty_id, academic_year_id)
VALUES
    ('CSE401', 'Operating Systems',                 4, 4, 1, 1, 4),
    ('CSE402', 'Database Management Systems',       4, 4, 1, 2, 4),
    ('CSE403', 'Computer Networks',                 3, 4, 1, 7, 4),
    ('CSE404', 'Software Engineering',              3, 4, 1, 1, 4),
    ('AIML201', 'Machine Learning Fundamentals',    4, 2, 7, 6, 4),
    ('AIML202', 'Python for Data Science',          3, 2, 7, 6, 4),
    ('IT301',  'Web Technologies',                  3, 3, 3, 4, 4),
    ('IT302',  'Object Oriented Programming',       4, 3, 3, 4, 4),
    ('ECE501', 'Microprocessors and Microcontrollers', 4, 5, 2, 3, 4),
    ('CSE405', 'Theory of Computation',             3, 4, 1, 2, 4);

-- ============================================================
-- INSERT: student_subjects (Enrollment)
-- ============================================================

INSERT INTO student_subjects (student_id, subject_id, enrolled_date)
VALUES
    -- CSE students (STU001-STU005) enrolled in CSE Sem 4 subjects
    (1, 1, '2024-07-10'),
    (1, 2, '2024-07-10'),
    (1, 3, '2024-07-10'),
    (1, 4, '2024-07-10'),
    (1, 10,'2024-07-10'),

    (2, 1, '2024-07-10'),
    (2, 2, '2024-07-10'),
    (2, 3, '2024-07-10'),
    (2, 4, '2024-07-10'),
    (2, 10,'2024-07-10'),

    (3, 1, '2024-07-10'),
    (3, 2, '2024-07-10'),
    (3, 3, '2024-07-10'),
    (3, 4, '2024-07-10'),
    (3, 10,'2024-07-10'),

    (4, 1, '2024-07-10'),
    (4, 2, '2024-07-10'),
    (4, 3, '2024-07-10'),
    (4, 4, '2024-07-10'),
    (4, 10,'2024-07-10'),

    (5, 1, '2024-07-10'),
    (5, 2, '2024-07-10'),
    (5, 3, '2024-07-10'),
    (5, 4, '2024-07-10'),
    (5, 10,'2024-07-10'),

    -- AIML students (STU006-STU007) enrolled in AIML Sem 2 subjects
    (6, 5, '2024-07-10'),
    (6, 6, '2024-07-10'),

    (7, 5, '2024-07-10'),
    (7, 6, '2024-07-10'),

    -- IT students (STU008-STU009) enrolled in IT Sem 3 subjects
    (8, 7, '2024-07-10'),
    (8, 8, '2024-07-10'),

    (9, 7, '2024-07-10'),
    (9, 8, '2024-07-10'),

    -- ECE student (STU010) enrolled in ECE Sem 5 subject
    (10, 9, '2024-07-10');

-- ============================================================
-- INSERT: attendance
-- ============================================================

INSERT INTO attendance (student_id, subject_id, faculty_id, attendance_date, status, academic_year_id)
VALUES
    -- STU001 | Subject: Operating Systems (1) | Faculty: 1
    (1, 1, 1, '2024-08-01', 'Present', 4),
    (1, 1, 1, '2024-08-03', 'Present', 4),
    (1, 1, 1, '2024-08-05', 'Absent',  4),
    (1, 1, 1, '2024-08-07', 'Present', 4),
    (1, 1, 1, '2024-08-09', 'Present', 4),

    -- STU001 | Subject: DBMS (2) | Faculty: 2
    (1, 2, 2, '2024-08-01', 'Present', 4),
    (1, 2, 2, '2024-08-03', 'Absent',  4),
    (1, 2, 2, '2024-08-05', 'Absent',  4),
    (1, 2, 2, '2024-08-07', 'Present', 4),
    (1, 2, 2, '2024-08-09', 'Present', 4),

  -- STU002 | Subject: Operating Systems (1) | Faculty: 1
    (2, 1, 1, '2024-08-01', 'Present', 4),
    (2, 1, 1, '2024-08-03', 'Present', 4),
    (2, 1, 1, '2024-08-05', 'Present', 4),
    (2, 1, 1, '2024-08-07', 'Absent',  4),
    (2, 1, 1, '2024-08-09', 'Present', 4),

    -- STU002 | Subject: DBMS (2) | Faculty: 2
    (2, 2, 2, '2024-08-01', 'Present', 4),
    (2, 2, 2, '2024-08-03', 'Present', 4),
    (2, 2, 2, '2024-08-05', 'Late',    4),
    (2, 2, 2, '2024-08-07', 'Present', 4),
    (2, 2, 2, '2024-08-09', 'Present', 4),

    -- STU003 | Subject: Operating Systems (1) | Faculty: 1
    (3, 1, 1, '2024-08-01', 'Absent',  4),
    (3, 1, 1, '2024-08-03', 'Absent',  4),
    (3, 1, 1, '2024-08-05', 'Present', 4),
    (3, 1, 1, '2024-08-07', 'Present', 4),
    (3, 1, 1, '2024-08-09', 'Absent',  4),

    -- STU006 | Subject: Machine Learning (5) | Faculty: 6
    (6, 5, 6, '2024-08-01', 'Present', 4),
    (6, 5, 6, '2024-08-03', 'Present', 4),
    (6, 5, 6, '2024-08-05', 'Present', 4),
    (6, 5, 6, '2024-08-07', 'Present', 4),
    (6, 5, 6, '2024-08-09', 'Present', 4),

    -- STU007 | Subject: Machine Learning (5) | Faculty: 6
    (7, 5, 6, '2024-08-01', 'Present', 4),
    (7, 5, 6, '2024-08-03', 'Absent',  4),
    (7, 5, 6, '2024-08-05', 'Present', 4),
    (7, 5, 6, '2024-08-07', 'Absent',  4),
    (7, 5, 6, '2024-08-09', 'Present', 4),

    -- STU010 | Subject: Microprocessors (9) | Faculty: 3
    (10, 9, 3, '2024-08-01', 'Present', 4),
    (10, 9, 3, '2024-08-03', 'Present', 4),
    (10, 9, 3, '2024-08-05', 'Absent',  4),
    (10, 9, 3, '2024-08-07', 'Present', 4),
    (10, 9, 3, '2024-08-09', 'Present', 4);

-- ============================================================
-- INSERT: attendance_summary
-- ============================================================

INSERT INTO attendance_summary (student_id, subject_id, academic_year_id, total_classes, classes_present, classes_absent, classes_late, attendance_percentage)
VALUES
    -- STU001
    (1, 1, 4, 5, 4, 1, 0, 80.00),
    (1, 2, 4, 5, 3, 2, 0, 60.00),

    -- STU002
    (2, 1, 4, 5, 4, 1, 0, 80.00),
    (2, 2, 4, 5, 4, 0, 1, 80.00),

    -- STU003
    (3, 1, 4, 5, 2, 3, 0, 40.00),

    -- STU006
    (6, 5, 4, 5, 5, 0, 0, 100.00),

    -- STU007
    (7, 5, 4, 5, 3, 2, 0, 60.00),

    -- STU010
    (10, 9, 4, 5, 4, 1, 0, 80.00);

-- ============================================================
-- INSERT: marks
-- ============================================================

INSERT INTO marks (student_id, subject_id, exam_type_id, academic_year_id, marks_obtained, grade, recorded_by)
VALUES
    -- STU001 | Operating Systems | Internal-1
    (1, 1, 1, 4, 24.00, 'A',  1),
    -- STU001 | Operating Systems | Internal-2
    (1, 1, 2, 4, 26.00, 'A+', 1),
    -- STU001 | Operating Systems | Mid-Term
    (1, 1, 3, 4, 41.00, 'A',  1),
    -- STU001 | DBMS | Internal-1
    (1, 2, 1, 4, 20.00, 'B',  2),
    -- STU001 | DBMS | Internal-2
    (1, 2, 2, 4, 22.00, 'B+', 2),

    -- STU002 | Operating Systems | Internal-1
    (2, 1, 1, 4, 28.00, 'A+', 1),
    -- STU002 | Operating Systems | Internal-2
    (2, 1, 2, 4, 27.00, 'A+', 1),
    -- STU002 | DBMS | Internal-1
    (2, 2, 1, 4, 25.00, 'A',  2),

    -- STU003 | Operating Systems | Internal-1
    (3, 1, 1, 4, 15.00, 'C',  1),
    -- STU003 | Operating Systems | Internal-2
    (3, 1, 2, 4, 18.00, 'C+', 1),

    -- STU004 | Operating Systems | Internal-1
    (4, 1, 1, 4, 22.00, 'B+', 1),
    -- STU005 | Operating Systems | Internal-1
    (5, 1, 1, 4, 19.00, 'B',  1),

    -- STU006 | Machine Learning | Internal-1
    (6, 5, 1, 4, 29.00, 'A+', 6),
    -- STU006 | Machine Learning | Mid-Term
    (6, 5, 3, 4, 46.00, 'A+', 6),

    -- STU007 | Machine Learning | Internal-1
    (7, 5, 1, 4, 21.00, 'B',  6),
    -- STU007 | Machine Learning | Mid-Term
    (7, 5, 3, 4, 35.00, 'B',  6),

    -- STU010 | Microprocessors | Internal-1
    (10, 9, 1, 4, 23.00, 'B+', 3),
    -- STU010 | Microprocessors | Mid-Term
    (10, 9, 3, 4, 38.00, 'B+', 3);

-- ============================================================
-- END OF SAMPLE DATA
-- ============================================================F