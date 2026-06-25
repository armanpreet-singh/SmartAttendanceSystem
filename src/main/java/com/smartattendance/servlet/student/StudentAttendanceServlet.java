package com.smartattendance.servlet.student;

import com.smartattendance.dao.StudentDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.AttendanceSummary;
import com.smartattendance.model.Student;
import com.smartattendance.model.Subject;
import com.smartattendance.util.AttendanceCalculator;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * StudentAttendanceServlet.java
 *
 * URL: /student/attendance
 *
 * Supports:
 *  - GET with no params     → Show all subjects' attendance summary
 *  - GET with ?subjectId=X  → Show detailed records for that subject
 *
 * Sends to attendance.jsp:
 *  - Attendance summary list (all subjects)
 *  - Subject list (for filter dropdown)
 *  - Detailed records (if subjectId selected)
 *  - Overall attendance totals
 *  - AttendanceCalculator advisory info per subject
 */
public class StudentAttendanceServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(StudentAttendanceServlet.class);
    private static final String ATTENDANCE_VIEW = "/WEB-INF/views/student/attendance.jsp";

    private StudentDAO studentDAO;

    @Override
    public void init() throws ServletException {
        studentDAO = new StudentDAO();
        logger.info("StudentAttendanceServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Student student     = (Student) session.getAttribute(AuthFilter.SESSION_STUDENT);

        int studentPk      = student.getId();
        int academicYearId = student.getAcademicYearId();

        logger.debug("StudentAttendanceServlet: Loading attendance for pk={}", studentPk);

        // ── Parse optional subject filter ──────────────────────────
        int selectedSubjectId = 0;
        String subjectIdParam = request.getParameter("subjectId");
        if (subjectIdParam != null && !subjectIdParam.isEmpty()) {
            try {
                selectedSubjectId = Integer.parseInt(subjectIdParam);
            } catch (NumberFormatException e) {
                logger.warn("Invalid subjectId param: {}", subjectIdParam);
            }
        }

        // ── Fetch data ─────────────────────────────────────────────
        List<AttendanceSummary>   summaries      = studentDAO.getAttendanceSummary(studentPk, academicYearId);
        List<Subject>             subjects       = studentDAO.getEnrolledSubjects(studentPk);
        Map<String, Integer>      overallTotals  = studentDAO.getOverallAttendanceTotals(studentPk, academicYearId);
        List<Map<String, Object>> detailRecords  = studentDAO.getAttendanceDetailBySubject(studentPk, selectedSubjectId);

        // ── Overall Percentage ─────────────────────────────────────
        int    totalAll          = overallTotals.getOrDefault("totalClassesAll", 0);
        int    presentAll        = overallTotals.getOrDefault("totalPresentAll", 0);
        int    lateAll           = overallTotals.getOrDefault("totalLateAll",    0);
        double overallPercentage = AttendanceCalculator.calculatePercentageWithLate(
                totalAll, presentAll, lateAll);

        // ── Set request attributes ─────────────────────────────────
        request.setAttribute("student",             student);
        request.setAttribute("summaries",           summaries);
        request.setAttribute("subjects",            subjects);
        request.setAttribute("detailRecords",       detailRecords);
        request.setAttribute("selectedSubjectId",   selectedSubjectId);
        request.setAttribute("overallTotals",       overallTotals);
        request.setAttribute("overallPercentage",   overallPercentage);
        request.setAttribute("minRequired",         AttendanceCalculator.MINIMUM_REQUIRED_PERCENTAGE);
        request.setAttribute("safeThreshold",       AttendanceCalculator.SAFE_THRESHOLD);
        request.setAttribute("warningThreshold",    AttendanceCalculator.WARNING_THRESHOLD);

        request.getRequestDispatcher(ATTENDANCE_VIEW).forward(request, response);
    }
}