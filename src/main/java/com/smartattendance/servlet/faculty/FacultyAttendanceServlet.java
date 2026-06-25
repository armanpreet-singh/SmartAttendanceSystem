package com.smartattendance.servlet.faculty;

import com.smartattendance.dao.FacultyAttendanceDAO;
import com.smartattendance.dao.FacultyDashboardDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.Faculty;
import com.smartattendance.model.Student;
import com.smartattendance.model.Subject;
import com.smartattendance.util.DateUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

/**
 * FacultyAttendanceServlet.java
 * URL: /faculty/mark-attendance  (GET + POST)
 *      /faculty/attendance-report (GET)
 *
 * GET  → Show attendance marking form OR attendance report
 * POST → Save attendance records
 *
 * Query params:
 *   action = "mark"   → show student list for marking
 *   action = "report" → show attendance report for subject
 *   subjectId         → which subject
 *   date              → attendance date (yyyy-MM-dd)
 *   section           → optional section filter
 */
public class FacultyAttendanceServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(FacultyAttendanceServlet.class);

    private static final String ATTENDANCE_VIEW = "/WEB-INF/views/faculty/attendance.jsp";

    private FacultyAttendanceDAO attendanceDAO;
    private FacultyDashboardDAO  dashboardDAO;

    @Override
    public void init() throws ServletException {
        attendanceDAO = new FacultyAttendanceDAO();
        dashboardDAO  = new FacultyDashboardDAO();
        logger.info("FacultyAttendanceServlet initialized.");
    }

    // ── GET ───────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session   = request.getSession(false);
        Faculty     faculty   = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);
        int         facultyId = faculty.getId();

        String action    = request.getParameter("action");
        String subIdStr  = request.getParameter("subjectId");
        String dateStr   = request.getParameter("date");
        String section   = request.getParameter("section");

        // Load assigned subjects for dropdown
        List<Subject> subjects = dashboardDAO.getAssignedSubjects(facultyId);
        request.setAttribute("faculty",   faculty);
        request.setAttribute("subjects",  subjects);
        request.setAttribute("today",     DateUtil.todayAsHtmlDate());
        request.setAttribute("action",    action != null ? action : "select");

        // ── No subject selected yet ────────────────────────────────
        if (subIdStr == null || subIdStr.isEmpty()) {
            request.setAttribute("mode", "select");
            request.getRequestDispatcher(ATTENDANCE_VIEW).forward(request, response);
            return;
        }

        int       subjectId = Integer.parseInt(subIdStr);
        LocalDate attDate   = (dateStr != null && !dateStr.isEmpty())
                              ? DateUtil.parseHtmlDate(dateStr) : LocalDate.now();

        // Validate subject belongs to this faculty
        boolean ownsSubject = subjects.stream().anyMatch(s -> s.getId() == subjectId);
        if (!ownsSubject) {
            response.sendRedirect(request.getContextPath() + "/faculty/mark-attendance");
            return;
        }

        request.setAttribute("selectedSubjectId", subjectId);
        request.setAttribute("selectedDate",      DateUtil.formatForHtml(attDate));
        request.setAttribute("sections",          attendanceDAO.getSectionsForSubject(subjectId));
        request.setAttribute("selectedSection",   section != null ? section : "");

        // ── MARK mode: show student list ───────────────────────────
        if ("mark".equals(action) || action == null) {
            List<Student>        students    = attendanceDAO.getStudentsForSubject(subjectId, section);
            Map<Integer, String> existingMap = attendanceDAO.getExistingAttendance(subjectId, attDate);

            request.setAttribute("mode",        "mark");
            request.setAttribute("students",    students);
            request.setAttribute("existingMap", existingMap);
            request.setAttribute("isEdit",      !existingMap.isEmpty());
        }

        // ── REPORT mode: show attendance report ───────────────────
        if ("report".equals(action)) {
            List<Map<String, Object>> report = attendanceDAO.getAttendanceReport(subjectId);
            List<LocalDate>           dates  = attendanceDAO.getAttendanceDates(subjectId);

            // Build chart data
            StringBuilder repLabels  = new StringBuilder("[");
            StringBuilder repPresent = new StringBuilder("[");
            StringBuilder repAbsent  = new StringBuilder("[");
            int ri = 0;
            for (Map<String, Object> row : report) {
                if (ri++ > 0) {
                    repLabels.append(",");
                    repPresent.append(",");
                    repAbsent.append(",");
                }
                String name = "\"" + row.get("studentCode") + "\"";
                repLabels.append(name);
                repPresent.append(row.get("classesPresent"));
                repAbsent.append(row.get("classesAbsent"));
            }
            repLabels.append("]"); repPresent.append("]"); repAbsent.append("]");

            request.setAttribute("mode",           "report");
            request.setAttribute("report",         report);
            request.setAttribute("attendanceDates", dates);
            request.setAttribute("reportLabels",   repLabels.toString());
            request.setAttribute("reportPresent",  repPresent.toString());
            request.setAttribute("reportAbsent",   repAbsent.toString());
        }

        request.getRequestDispatcher(ATTENDANCE_VIEW).forward(request, response);
    }

    // ── POST: Save attendance ─────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session        = request.getSession(false);
        Faculty     faculty        = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);
        int         facultyId      = faculty.getId();
        int         academicYearId = 4; // Current year – ideally from session/config

        String subIdStr = request.getParameter("subjectId");
        String dateStr  = request.getParameter("attendanceDate");

        if (subIdStr == null || dateStr == null) {
            response.sendRedirect(request.getContextPath() + "/faculty/mark-attendance");
            return;
        }

        int       subjectId = Integer.parseInt(subIdStr);
        LocalDate attDate   = DateUtil.parseHtmlDate(dateStr);

        if (attDate == null || DateUtil.isFutureDate(attDate)) {
            request.setAttribute("errorMessage", "Invalid or future date selected.");
            doGet(request, response);
            return;
        }

        // ── Build statusMap and remarksMap from form params ────────
        Map<Integer, String> statusMap  = new LinkedHashMap<>();
        Map<Integer, String> remarksMap = new LinkedHashMap<>();

        Map<String, String[]> params = request.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String key = param.getKey();
            if (key.startsWith("status_")) {
                int studentPk = Integer.parseInt(key.substring(7));
                statusMap.put(studentPk, param.getValue()[0]);
            }
            if (key.startsWith("remarks_")) {
                int studentPk = Integer.parseInt(key.substring(8));
                String val    = param.getValue()[0];
                if (val != null && !val.trim().isEmpty()) {
                    remarksMap.put(studentPk, val.trim());
                }
            }
        }

        if (statusMap.isEmpty()) {
            request.setAttribute("errorMessage", "No student attendance data received.");
            doGet(request, response);
            return;
        }

        // ── Save to DB ─────────────────────────────────────────────
        int saved = attendanceDAO.saveAttendance(
                subjectId, facultyId, attDate, academicYearId, statusMap, remarksMap);

        logger.info("FacultyAttendanceServlet POST: Saved {} records for subjectId={}, date={}",
                saved, subjectId, attDate);

        response.sendRedirect(request.getContextPath()
                + "/faculty/mark-attendance?action=report&subjectId=" + subjectId
                + "&saved=" + saved);
    }
}