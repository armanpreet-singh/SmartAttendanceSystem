package com.smartattendance.servlet.student;

import com.smartattendance.dao.StudentDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.AttendanceSummary;
import com.smartattendance.model.Student;
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
 * StudentDashboardServlet.java
 *
 * URL: /student/dashboard
 *
 * Loads and sends to dashboard.jsp:
 *  - Student profile from session
 *  - Subject count
 *  - Overall attendance percentage
 *  - Defaulter subject count
 *  - Attendance summary list (subject-wise)
 *  - Recent 7 attendance records
 */
public class StudentDashboardServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(StudentDashboardServlet.class);
    private static final String DASHBOARD_VIEW = "/WEB-INF/views/student/dashboard.jsp";

    private StudentDAO studentDAO;

    @Override
    public void init() throws ServletException {
        studentDAO = new StudentDAO();
        logger.info("StudentDashboardServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Student student     = (Student) session.getAttribute(AuthFilter.SESSION_STUDENT);

        logger.debug("StudentDashboardServlet: Loading dashboard for student pk={}",
                student.getId());

        int studentPk      = student.getId();
        int academicYearId = student.getAcademicYearId();

        // ── Fetch data from DAO ────────────────────────────────────
        int subjectCount   = studentDAO.getEnrolledSubjectCount(studentPk);
        int defaulterCount = studentDAO.getDefaulterSubjectCount(studentPk, academicYearId);

        List<AttendanceSummary>     attendanceSummaries = studentDAO.getAttendanceSummary(studentPk, academicYearId);
        Map<String, Integer>        overallTotals       = studentDAO.getOverallAttendanceTotals(studentPk, academicYearId);
        List<Map<String, Object>>   recentAttendance    = studentDAO.getRecentAttendance(studentPk);

        // ── Calculate overall attendance percentage ────────────────
        int totalAll   = overallTotals.getOrDefault("totalClassesAll", 0);
        int presentAll = overallTotals.getOrDefault("totalPresentAll", 0);
        int lateAll    = overallTotals.getOrDefault("totalLateAll",    0);
        double overallPercentage = AttendanceCalculator.calculatePercentageWithLate(
                totalAll, presentAll, lateAll);
        String overallStatus    = AttendanceCalculator.getAttendanceStatus(overallPercentage);
        String overallBadge     = AttendanceCalculator.getStatusBadgeClass(overallPercentage);

        // ── Set request attributes ─────────────────────────────────
        request.setAttribute("student",             student);
        request.setAttribute("subjectCount",         subjectCount);
        request.setAttribute("defaulterCount",       defaulterCount);
        request.setAttribute("overallPercentage",    overallPercentage);
        request.setAttribute("overallStatus",        overallStatus);
        request.setAttribute("overallBadge",         overallBadge);
        request.setAttribute("overallTotals",        overallTotals);
        request.setAttribute("attendanceSummaries",  attendanceSummaries);
        request.setAttribute("recentAttendance",     recentAttendance);
        request.setAttribute("minRequired",          AttendanceCalculator.MINIMUM_REQUIRED_PERCENTAGE);

        logger.debug("StudentDashboardServlet: Data loaded. Forwarding to dashboard view.");
        request.getRequestDispatcher(DASHBOARD_VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}