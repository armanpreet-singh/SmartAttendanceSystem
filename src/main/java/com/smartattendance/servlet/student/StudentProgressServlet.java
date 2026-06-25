package com.smartattendance.servlet.student;

import com.smartattendance.dao.StudentDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.AttendanceSummary;
import com.smartattendance.model.Marks;
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
import java.util.*;

/**
 * StudentProgressServlet.java
 *
 * URL: /student/progress
 *
 * Combines attendance + marks data to generate an academic
 * progress overview for the student.
 *
 * Sends to progress.jsp:
 *  - Attendance summaries + percentages
 *  - Marks grouped by subject
 *  - Advisory messages (classes needed to reach 75%)
 *  - JSON data strings for Chart.js charts
 *  - Overall academic health status
 */
public class StudentProgressServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(StudentProgressServlet.class);
    private static final String PROGRESS_VIEW = "/WEB-INF/views/student/progress.jsp";

    private StudentDAO studentDAO;

    @Override
    public void init() throws ServletException {
        studentDAO = new StudentDAO();
        logger.info("StudentProgressServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Student student     = (Student) session.getAttribute(AuthFilter.SESSION_STUDENT);

        int studentPk      = student.getId();
        int academicYearId = student.getAcademicYearId();

        logger.debug("StudentProgressServlet: Loading progress for pk={}", studentPk);

        // ── Fetch data ─────────────────────────────────────────────
        List<AttendanceSummary>  summaries = studentDAO.getAttendanceSummary(studentPk, academicYearId);
        List<Marks>              allMarks  = studentDAO.getAllMarksByStudent(studentPk, academicYearId);
        Map<String, Integer>     overallTotals = studentDAO.getOverallAttendanceTotals(studentPk, academicYearId);

        // ── Advisory: classes needed to reach 75% per subject ─────
        Map<String, Integer> classesNeededMap = new LinkedHashMap<>();
        Map<String, Integer> canMissMap       = new LinkedHashMap<>();
        int defaulterCount = 0;

        for (AttendanceSummary s : summaries) {
            int needed = AttendanceCalculator.classesNeededToReachTarget(
                    s.getTotalClasses(), s.getClassesPresent(), 75.0);
            int canMiss = AttendanceCalculator.classesCanAffordToMiss(
                    s.getTotalClasses(), s.getClassesPresent());

            classesNeededMap.put(s.getSubjectCode(), needed);
            canMissMap.put(s.getSubjectCode(), canMiss);

            if (s.getAttendancePercentage() < AttendanceCalculator.MINIMUM_REQUIRED_PERCENTAGE) {
                defaulterCount++;
            }
        }

        // ── Overall attendance percentage ─────────────────────────
        int    totalAll   = overallTotals.getOrDefault("totalClassesAll", 0);
        int    presentAll = overallTotals.getOrDefault("totalPresentAll", 0);
        int    lateAll    = overallTotals.getOrDefault("totalLateAll",    0);
        double overallPct = AttendanceCalculator.calculatePercentageWithLate(totalAll, presentAll, lateAll);

        // ── Build Chart.js JSON data ───────────────────────────────
        // Attendance bar chart: subject labels + percentages
        StringBuilder attLabels  = new StringBuilder("[");
        StringBuilder attData    = new StringBuilder("[");
        StringBuilder attColors  = new StringBuilder("[");

        for (int i = 0; i < summaries.size(); i++) {
            AttendanceSummary s = summaries.get(i);
            if (i > 0) { attLabels.append(","); attData.append(","); attColors.append(","); }

            attLabels.append("\"").append(s.getSubjectCode()).append("\"");
            attData.append(s.getAttendancePercentage());

            String color = switch (AttendanceCalculator.getAttendanceStatus(s.getAttendancePercentage())) {
                case AttendanceCalculator.STATUS_DEFAULTER -> "'rgba(220,53,69,0.8)'";
                case AttendanceCalculator.STATUS_CRITICAL  -> "'rgba(220,53,69,0.6)'";
                case AttendanceCalculator.STATUS_WARNING   -> "'rgba(255,193,7,0.8)'";
                default                                    -> "'rgba(25,135,84,0.8)'";
            };
            attColors.append(color);
        }
        attLabels.append("]"); attData.append("]"); attColors.append("]");

        // Marks scatter data: per subject average
        Map<String, Double>  subjectAvgMarks = new LinkedHashMap<>();
        Map<String, List<Marks>> grouped     = new LinkedHashMap<>();
        for (Marks m : allMarks) {
            grouped.computeIfAbsent(m.getSubjectCode(), k -> new ArrayList<>()).add(m);
        }
        for (Map.Entry<String, List<Marks>> entry : grouped.entrySet()) {
            double totalObtained = 0, totalMax = 0;
            for (Marks m : entry.getValue()) {
                totalObtained += m.getMarksObtained();
                totalMax      += m.getMaxMarks();
            }
            subjectAvgMarks.put(entry.getKey(),
                    totalMax > 0 ? Math.round((totalObtained / totalMax) * 100.0 * 100.0) / 100.0 : 0.0);
        }

        StringBuilder marksLabels = new StringBuilder("[");
        StringBuilder marksData   = new StringBuilder("[");
        int i = 0;
        for (Map.Entry<String, Double> entry : subjectAvgMarks.entrySet()) {
            if (i++ > 0) { marksLabels.append(","); marksData.append(","); }
            marksLabels.append("\"").append(entry.getKey()).append("\"");
            marksData.append(entry.getValue());
        }
        marksLabels.append("]"); marksData.append("]");

        // ── Set request attributes ─────────────────────────────────
        request.setAttribute("student",          student);
        request.setAttribute("summaries",        summaries);
        request.setAttribute("allMarks",         allMarks);
        request.setAttribute("groupedMarks",     grouped);
        request.setAttribute("subjectAvgMarks",  subjectAvgMarks);
        request.setAttribute("classesNeededMap", classesNeededMap);
        request.setAttribute("canMissMap",       canMissMap);
        request.setAttribute("defaulterCount",   defaulterCount);
        request.setAttribute("overallPct",       overallPct);
        request.setAttribute("overallTotals",    overallTotals);
        request.setAttribute("minRequired",      AttendanceCalculator.MINIMUM_REQUIRED_PERCENTAGE);

        // Chart JSON
        request.setAttribute("attLabelsJson",  attLabels.toString());
        request.setAttribute("attDataJson",    attData.toString());
        request.setAttribute("attColorsJson",  attColors.toString());
        request.setAttribute("marksLabelsJson", marksLabels.toString());
        request.setAttribute("marksDataJson",   marksData.toString());

        request.getRequestDispatcher(PROGRESS_VIEW).forward(request, response);
    }
}