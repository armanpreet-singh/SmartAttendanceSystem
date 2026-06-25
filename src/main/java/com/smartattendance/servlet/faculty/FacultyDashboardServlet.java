package com.smartattendance.servlet.faculty;

import com.smartattendance.dao.FacultyDashboardDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.Faculty;
import com.smartattendance.model.Subject;
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
 * FacultyDashboardServlet.java
 * URL: /faculty/dashboard
 *
 * Loads:
 *  - Total subjects, total students, today's attendance count
 *  - Assigned subjects list with student counts
 *  - Recent attendance activity
 *  - Chart.js JSON for subject-student distribution
 */
public class FacultyDashboardServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(FacultyDashboardServlet.class);
    private static final String DASHBOARD_VIEW = "/WEB-INF/views/faculty/dashboard.jsp";

    private FacultyDashboardDAO dashboardDAO;

    @Override
    public void init() throws ServletException {
        dashboardDAO = new FacultyDashboardDAO();
        logger.info("FacultyDashboardServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Faculty faculty     = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);
        int facultyId       = faculty.getId();

        logger.debug("FacultyDashboardServlet: Loading dashboard for facultyId={}", facultyId);

        // ── Fetch summary data ─────────────────────────────────────
        int subjectCount  = dashboardDAO.getAssignedSubjectCount(facultyId);
        int studentCount  = dashboardDAO.getTotalStudents(facultyId);
        int todayPresent  = dashboardDAO.getTodayPresentCount(facultyId);

        Map<String, Integer>      todayRate   = dashboardDAO.getTodayAttendanceRate(facultyId);
        List<Subject>             subjects    = dashboardDAO.getAssignedSubjects(facultyId);
        List<Map<String, Object>> recentAct   = dashboardDAO.getRecentAttendanceActivity(facultyId);
        Map<String, Integer>      subjCounts  = dashboardDAO.getSubjectStudentCounts(facultyId);

        // ── Build Chart.js JSON ────────────────────────────────────
        StringBuilder chartLabels = new StringBuilder("[");
        StringBuilder chartData   = new StringBuilder("[");
        int i = 0;
        for (Map.Entry<String, Integer> entry : subjCounts.entrySet()) {
            if (i++ > 0) { chartLabels.append(","); chartData.append(","); }
            chartLabels.append("\"").append(entry.getKey()).append("\"");
            chartData.append(entry.getValue());
        }
        chartLabels.append("]"); chartData.append("]");

        // ── Today's attendance rate ────────────────────────────────
        int    todayTotal = todayRate.getOrDefault("totalCount",   0);
        int    todayPres  = todayRate.getOrDefault("presentCount", 0);
        double todayPct   = todayTotal > 0
                            ? Math.round((todayPres / (double) todayTotal) * 100.0 * 10.0) / 10.0
                            : 0.0;

        // ── Set request attributes ─────────────────────────────────
        request.setAttribute("faculty",          faculty);
        request.setAttribute("subjectCount",     subjectCount);
        request.setAttribute("studentCount",     studentCount);
        request.setAttribute("todayPresent",     todayPresent);
        request.setAttribute("todayTotal",       todayTotal);
        request.setAttribute("todayPct",         todayPct);
        request.setAttribute("subjects",         subjects);
        request.setAttribute("recentActivity",   recentAct);
        request.setAttribute("chartLabelsJson",  chartLabels.toString());
        request.setAttribute("chartDataJson",    chartData.toString());

        request.getRequestDispatcher(DASHBOARD_VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}