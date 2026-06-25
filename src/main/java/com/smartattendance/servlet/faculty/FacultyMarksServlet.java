package com.smartattendance.servlet.faculty;

import com.smartattendance.dao.FacultyDashboardDAO;
import com.smartattendance.dao.FacultyMarksDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.Faculty;
import com.smartattendance.model.Marks;
import com.smartattendance.model.Student;
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
 * FacultyMarksServlet.java
 * URL: /faculty/marks
 *
 * GET  → Display marks entry/view form for a subject
 * POST → Save/update marks for all students in the submitted form
 *
 * Query params:
 *   subjectId → which subject to manage marks for
 *   saved     → success message param (after redirect)
 */
public class FacultyMarksServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(FacultyMarksServlet.class);

    private static final String MARKS_VIEW = "/WEB-INF/views/faculty/marks.jsp";

    private FacultyMarksDAO     marksDAO;
    private FacultyDashboardDAO dashboardDAO;

    @Override
    public void init() throws ServletException {
        marksDAO     = new FacultyMarksDAO();
        dashboardDAO = new FacultyDashboardDAO();
        logger.info("FacultyMarksServlet initialized.");
    }

    // ── GET ───────────────────────────────────────────────────────────

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session        = request.getSession(false);
        Faculty     faculty        = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);
        int         facultyId      = faculty.getId();
        int         academicYearId = 4; // Current year

        List<Subject>              subjects    = dashboardDAO.getAssignedSubjects(facultyId);
        List<Map<String, Object>>  examTypes   = marksDAO.getExamTypes();

        request.setAttribute("faculty",    faculty);
        request.setAttribute("subjects",   subjects);
        request.setAttribute("examTypes",  examTypes);

        String subIdStr = request.getParameter("subjectId");
        String savedMsg = request.getParameter("saved");

        if (savedMsg != null) {
            request.setAttribute("successMessage", savedMsg + " marks record(s) saved successfully.");
        }

        if (subIdStr == null || subIdStr.isEmpty()) {
            request.setAttribute("mode", "select");
            request.getRequestDispatcher(MARKS_VIEW).forward(request, response);
            return;
        }

        int subjectId = Integer.parseInt(subIdStr);

        // Validate ownership
        boolean owns = subjects.stream().anyMatch(s -> s.getId() == subjectId);
        if (!owns) {
            response.sendRedirect(request.getContextPath() + "/faculty/marks");
            return;
        }

        List<Student>                         students    = marksDAO.getStudentsForSubject(subjectId);
        Map<Integer, Map<Integer, Marks>>     marksMatrix = marksDAO.getMarksMatrix(subjectId, academicYearId);

        request.setAttribute("mode",              "entry");
        request.setAttribute("selectedSubjectId", subjectId);
        request.setAttribute("students",          students);
        request.setAttribute("marksMatrix",       marksMatrix);
        request.setAttribute("academicYearId",    academicYearId);

        request.getRequestDispatcher(MARKS_VIEW).forward(request, response);
    }

    // ── POST: Save marks ──────────────────────────────────────────────

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session        = request.getSession(false);
        Faculty     faculty        = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);
        int         facultyId      = faculty.getId();
        int         academicYearId = 4;

        String subIdStr = request.getParameter("subjectId");
        if (subIdStr == null) {
            response.sendRedirect(request.getContextPath() + "/faculty/marks");
            return;
        }

        int subjectId = Integer.parseInt(subIdStr);

        // Get exam types for validation
        List<Map<String, Object>> examTypes = marksDAO.getExamTypes();

        int savedCount = 0;
        int errorCount = 0;

        // ── Parse submitted marks ──────────────────────────────────
        // Form field names: marks_{studentPk}_{examTypeId}
        Map<String, String[]> params = request.getParameterMap();

        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String key = param.getKey();
            if (!key.startsWith("marks_")) continue;

            String[] parts = key.split("_");
            if (parts.length != 3) continue;

            try {
                int    studentPk  = Integer.parseInt(parts[1]);
                int    examTypeId = Integer.parseInt(parts[2]);
                String marksStr   = param.getValue()[0].trim();

                if (marksStr.isEmpty()) continue;

                double marksObtained = Double.parseDouble(marksStr);

                // Validate against max marks
                int maxMarks = examTypes.stream()
                        .filter(et -> (int) et.get("id") == examTypeId)
                        .mapToInt(et -> (int) et.get("maxMarks"))
                        .findFirst().orElse(100);

                if (marksObtained < 0 || marksObtained > maxMarks) {
                    errorCount++;
                    logger.warn("FacultyMarksServlet: Invalid marks {} for studentPk={}, examType={}",
                            marksObtained, studentPk, examTypeId);
                    continue;
                }

                String grade   = FacultyMarksDAO.computeGrade(marksObtained, maxMarks);
                String remarks = request.getParameter("remarks_" + studentPk + "_" + examTypeId);

                boolean ok = marksDAO.saveOrUpdateMark(
                        studentPk, subjectId, examTypeId, academicYearId,
                        marksObtained, grade, remarks, facultyId);

                if (ok) savedCount++; else errorCount++;

            } catch (NumberFormatException e) {
                logger.warn("FacultyMarksServlet: Invalid number format for key={}", key);
                errorCount++;
            }
        }

        logger.info("FacultyMarksServlet POST: saved={}, errors={}, subjectId={}",
                savedCount, errorCount, subjectId);

        String redirect = request.getContextPath()
                + "/faculty/marks?subjectId=" + subjectId
                + "&saved=" + savedCount;
        if (errorCount > 0) redirect += "&errors=" + errorCount;

        response.sendRedirect(redirect);
    }
}