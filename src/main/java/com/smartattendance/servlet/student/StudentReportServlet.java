package com.smartattendance.servlet.student;

import com.smartattendance.dao.AnalyticsDAO;
import com.smartattendance.dao.ReportDAO;
import com.smartattendance.dao.StudentDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.Student;
import com.smartattendance.model.Subject;
import com.smartattendance.util.AttendanceCalculator;
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
import java.util.List;
import java.util.Map;

/**
 * StudentReportServlet.java
 * URL: /student/reports
 *
 * Supports report types via ?type= parameter:
 *   "attendance"  → Overall + subject-wise attendance
 *   "marks"       → Full marks report
 *   "performance" → Academic performance summary
 *   "monthly"     → Monthly attendance report
 *   (default)     → Show report selection page
 *
 * Also supports:
 *   ?subjectId=X  → Subject filter for detail view
 *   ?month=X      → Month filter for monthly report
 *   ?year=X       → Year filter
 */
public class StudentReportServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(StudentReportServlet.class);

    private static final String REPORT_VIEW = "/WEB-INF/views/student/report.jsp";

    private ReportDAO    reportDAO;
    private AnalyticsDAO analyticsDAO;
    private StudentDAO   studentDAO;

    @Override
    public void init() throws ServletException {
        reportDAO    = new ReportDAO();
        analyticsDAO = new AnalyticsDAO();
        studentDAO   = new StudentDAO();
        logger.info("StudentReportServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session        = request.getSession(false);
        Student     student        = (Student) session.getAttribute(AuthFilter.SESSION_STUDENT);
        int         studentPk      = student.getId();
        int         academicYearId = student.getAcademicYearId();

        String type       = request.getParameter("type");
        String subIdStr   = request.getParameter("subjectId");
        String monthStr   = request.getParameter("month");
        String yearStr    = request.getParameter("year");

        if (type == null) type = "overview";

        logger.debug("StudentReportServlet: type={}, studentPk={}", type, studentPk);

        // Load enrolled subjects for filter dropdown
        List<Subject> subjects = studentDAO.getEnrolledSubjects(studentPk);

        // Parse optional filters
        int selectedSubjectId = parseIntSafe(subIdStr, 0);
        int selectedMonth     = parseIntSafe(monthStr, LocalDate.now().getMonthValue());
        int selectedYear      = parseIntSafe(yearStr,  LocalDate.now().getYear());

        // ── Common attributes ──────────────────────────────────────
        request.setAttribute("student",           student);
        request.setAttribute("subjects",          subjects);
        request.setAttribute("reportType",        type);
        request.setAttribute("selectedSubjectId", selectedSubjectId);
        request.setAttribute("selectedMonth",     selectedMonth);
        request.setAttribute("selectedYear",      selectedYear);
        request.setAttribute("currentYear",       LocalDate.now().getYear());
        request.setAttribute("minRequired",
                AttendanceCalculator.MINIMUM_REQUIRED_PERCENTAGE);

        switch (type) {

            // ── ATTENDANCE REPORT ──────────────────────────────────
            case "attendance": {
                List<Map<String, Object>> overallAtt =
                        reportDAO.getStudentOverallAttendance(studentPk, academicYearId);

                // Subject detail if subjectId selected
                List<Map<String, Object>> detailRecords = null;
                if (selectedSubjectId > 0) {
                    detailRecords = reportDAO.getStudentSubjectAttendanceDetail(
                            studentPk, selectedSubjectId);
                }

                // Chart data
                Map<String, Object>  attChartData = analyticsDAO
                        .getStudentAttendanceChartData(studentPk, academicYearId);
                Map<String, Integer> overallTotals = analyticsDAO
                        .getStudentOverallTotals(studentPk, academicYearId);
                Map<String, Object>  monthlyTrend  = analyticsDAO
                        .getStudentMonthlyTrendData(studentPk);

                // Build JSON for charts
                @SuppressWarnings("unchecked")
                List<String> attLabels = (List<String>) attChartData.get("labels");
                @SuppressWarnings("unchecked")
                List<Double> attPcts   = (List<Double>) attChartData.get("percentages");
                @SuppressWarnings("unchecked")
                List<String> trendLabels = (List<String>) monthlyTrend.get("labels");
                @SuppressWarnings("unchecked")
                List<Double> trendPcts   = (List<Double>) monthlyTrend.get("percentages");

                request.setAttribute("overallAttendance",   overallAtt);
                request.setAttribute("detailRecords",       detailRecords);
                request.setAttribute("overallTotals",       overallTotals);

                // Chart JSON strings
                request.setAttribute("attLabelsJson",
                        AnalyticsDAO.toJsonStringArray(attLabels));
                request.setAttribute("attPctJson",
                        AnalyticsDAO.toJsonNumberArray(attPcts));
                request.setAttribute("trendLabelsJson",
                        AnalyticsDAO.toJsonStringArray(trendLabels));
                request.setAttribute("trendPctJson",
                        AnalyticsDAO.toJsonNumberArray(trendPcts));

                int present = overallTotals.getOrDefault("present", 0);
                int absent  = overallTotals.getOrDefault("absent",  0);
                int late    = overallTotals.getOrDefault("late",     0);
                int total   = present + absent + late;
                double overallPct = AttendanceCalculator.calculatePercentageWithLate(
                        total, present, late);

                request.setAttribute("overallPct",    overallPct);
                request.setAttribute("overallStatus",
                        AttendanceCalculator.getAttendanceStatus(overallPct));
                request.setAttribute("overallBadge",
                        AttendanceCalculator.getStatusBadgeClass(overallPct));
                break;
            }

            // ── MARKS REPORT ───────────────────────────────────────
            case "marks": {
                List<Map<String, Object>> marksReport =
                        reportDAO.getStudentMarksReport(studentPk, academicYearId);

                // Group marks by subject
                Map<String, List<Map<String, Object>>> grouped =
                        new java.util.LinkedHashMap<>();
                Map<String, String> subjectNameMap = new java.util.LinkedHashMap<>();

                for (Map<String, Object> row : marksReport) {
                    String code = (String) row.get("subjectCode");
                    grouped.computeIfAbsent(code, k -> new java.util.ArrayList<>()).add(row);
                    subjectNameMap.put(code, (String) row.get("subjectName"));
                }

                // Marks chart data
                Map<String, Object> marksChartData =
                        analyticsDAO.getStudentMarksChartData(studentPk, academicYearId);
                @SuppressWarnings("unchecked")
                List<String> mLabels = (List<String>) marksChartData.get("labels");
                @SuppressWarnings("unchecked")
                List<Double> mPcts   = (List<Double>) marksChartData.get("marksPct");

                request.setAttribute("marksReport",    marksReport);
                request.setAttribute("groupedMarks",   grouped);
                request.setAttribute("subjectNameMap", subjectNameMap);
                request.setAttribute("marksLabelsJson",
                        AnalyticsDAO.toJsonStringArray(mLabels));
                request.setAttribute("marksPctJson",
                        AnalyticsDAO.toJsonNumberArray(mPcts));
                break;
            }

            // ── PERFORMANCE REPORT ─────────────────────────────────
            case "performance": {
                List<Map<String, Object>> perfSummary =
                        reportDAO.getStudentPerformanceSummary(studentPk, academicYearId);

                List<Map<String, Object>> attSummary =
                        reportDAO.getStudentOverallAttendance(studentPk, academicYearId);

                Map<String, Object> marksChartData =
                        analyticsDAO.getStudentMarksChartData(studentPk, academicYearId);
                Map<String, Object> attChartData =
                        analyticsDAO.getStudentAttendanceChartData(studentPk, academicYearId);

                @SuppressWarnings("unchecked")
                List<String> mLabels = (List<String>) marksChartData.get("labels");
                @SuppressWarnings("unchecked")
                List<Double> mPcts   = (List<Double>) marksChartData.get("marksPct");
                @SuppressWarnings("unchecked")
                List<String> aLabels = (List<String>) attChartData.get("labels");
                @SuppressWarnings("unchecked")
                List<Double> aPcts   = (List<Double>) attChartData.get("percentages");

                request.setAttribute("perfSummary",    perfSummary);
                request.setAttribute("attSummary",     attSummary);
                request.setAttribute("marksLabelsJson",
                        AnalyticsDAO.toJsonStringArray(mLabels));
                request.setAttribute("marksPctJson",
                        AnalyticsDAO.toJsonNumberArray(mPcts));
                request.setAttribute("attLabelsJson",
                        AnalyticsDAO.toJsonStringArray(aLabels));
                request.setAttribute("attPctJson",
                        AnalyticsDAO.toJsonNumberArray(aPcts));
                break;
            }

            // ── MONTHLY REPORT ─────────────────────────────────────
            case "monthly": {
                List<Map<String, Object>> monthlyAtt =
                        reportDAO.getStudentMonthlyAttendance(
                                studentPk, selectedMonth, selectedYear);

                Map<String, Object> monthlyTrend =
                        analyticsDAO.getStudentMonthlyTrendData(studentPk);

                @SuppressWarnings("unchecked")
                List<String> tLabels = (List<String>) monthlyTrend.get("labels");
                @SuppressWarnings("unchecked")
                List<Double> tPcts   = (List<Double>) monthlyTrend.get("percentages");

                request.setAttribute("monthlyAttendance", monthlyAtt);
                request.setAttribute("selectedMonthName",
                        java.time.Month.of(selectedMonth).name());
                request.setAttribute("trendLabelsJson",
                        AnalyticsDAO.toJsonStringArray(tLabels));
                request.setAttribute("trendPctJson",
                        AnalyticsDAO.toJsonNumberArray(tPcts));
                break;
            }

            // ── DEFAULT: Overview ──────────────────────────────────
            default: {
                Map<String, Object> attChartData =
                        analyticsDAO.getStudentAttendanceChartData(studentPk, academicYearId);
                Map<String, Integer> totals =
                        analyticsDAO.getStudentOverallTotals(studentPk, academicYearId);
                Map<String, Object> trendData =
                        analyticsDAO.getStudentMonthlyTrendData(studentPk);

                @SuppressWarnings("unchecked")
                List<String> aLabels = (List<String>) attChartData.get("labels");
                @SuppressWarnings("unchecked")
                List<Double> aPcts   = (List<Double>) attChartData.get("percentages");
                @SuppressWarnings("unchecked")
                List<String> tLabels = (List<String>) trendData.get("labels");
                @SuppressWarnings("unchecked")
                List<Double> tPcts   = (List<Double>) trendData.get("percentages");

                int p = totals.getOrDefault("present", 0);
                int a = totals.getOrDefault("absent",  0);
                int l = totals.getOrDefault("late",    0);

                request.setAttribute("overallTotals",   totals);
                request.setAttribute("attLabelsJson",
                        AnalyticsDAO.toJsonStringArray(aLabels));
                request.setAttribute("attPctJson",
                        AnalyticsDAO.toJsonNumberArray(aPcts));
                request.setAttribute("trendLabelsJson",
                        AnalyticsDAO.toJsonStringArray(tLabels));
                request.setAttribute("trendPctJson",
                        AnalyticsDAO.toJsonNumberArray(tPcts));
                break;
            }
        }

        request.getRequestDispatcher(REPORT_VIEW).forward(request, response);
    }

    // ── Helper ────────────────────────────────────────────────────────
    private int parseIntSafe(String str, int defaultVal) {
        if (str == null || str.trim().isEmpty()) return defaultVal;
        try { return Integer.parseInt(str.trim()); }
        catch (NumberFormatException e) { return defaultVal; }
    }
}