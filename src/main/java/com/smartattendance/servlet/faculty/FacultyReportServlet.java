package com.smartattendance.servlet.faculty;

import com.smartattendance.dao.AnalyticsDAO;
import com.smartattendance.dao.FacultyDashboardDAO;
import com.smartattendance.dao.ReportDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.Faculty;
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
 * FacultyReportServlet.java
 * URL: /faculty/reports
 *
 * Supports ?type= parameter:
 *   "attendance"  → Subject-wise attendance report
 *   "daily"       → Daily attendance for a date
 *   "monthly"     → Monthly attendance summary
 *   "low"         → Low attendance students
 *   "marks"       → Marks report for a subject
 *   "performance" → Performance statistics
 *   "analytics"   → Analytics dashboard with all charts
 *   (default)     → Overview
 *
 * Also supports:
 *   ?subjectId=X   → Subject filter
 *   ?date=yyyy-MM-dd → Date filter for daily
 *   ?month=X, ?year=X → Month/year filter
 *   ?threshold=X   → Custom threshold for low attendance (default 75)
 */
public class FacultyReportServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(FacultyReportServlet.class);

    private static final String REPORTS_VIEW = "/WEB-INF/views/faculty/reports.jsp";

    private static final int DEFAULT_ACADEMIC_YEAR_ID = 4;

    private ReportDAO          reportDAO;
    private AnalyticsDAO       analyticsDAO;
    private FacultyDashboardDAO dashboardDAO;

    @Override
    public void init() throws ServletException {
        reportDAO    = new ReportDAO();
        analyticsDAO = new AnalyticsDAO();
        dashboardDAO = new FacultyDashboardDAO();
        logger.info("FacultyReportServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session        = request.getSession(false);
        Faculty     faculty        = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);
        int         facultyId      = faculty.getId();
        int         academicYearId = DEFAULT_ACADEMIC_YEAR_ID;

        String type       = request.getParameter("type");
        String subIdStr   = request.getParameter("subjectId");
        String dateStr    = request.getParameter("date");
        String monthStr   = request.getParameter("month");
        String yearStr    = request.getParameter("year");
        String threshStr  = request.getParameter("threshold");

        if (type == null) type = "overview";

        logger.debug("FacultyReportServlet: type={}, facultyId={}", type, facultyId);

        int selectedSubjectId = parseIntSafe(subIdStr, 0);
        int selectedMonth     = parseIntSafe(monthStr, LocalDate.now().getMonthValue());
        int selectedYear      = parseIntSafe(yearStr,  LocalDate.now().getYear());
        double threshold      = parseDoubleSafe(threshStr, 75.0);
        LocalDate selectedDate = (dateStr != null && !dateStr.isEmpty())
                ? DateUtil.parseHtmlDate(dateStr) : LocalDate.now();

        // Assigned subjects for dropdown
        List<Subject> subjects = dashboardDAO.getAssignedSubjects(facultyId);

        // ── Common attributes ──────────────────────────────────────
        request.setAttribute("faculty",           faculty);
        request.setAttribute("subjects",          subjects);
        request.setAttribute("reportType",        type);
        request.setAttribute("selectedSubjectId", selectedSubjectId);
        request.setAttribute("selectedMonth",     selectedMonth);
        request.setAttribute("selectedYear",      selectedYear);
        request.setAttribute("selectedDate",      DateUtil.formatForHtml(selectedDate));
        request.setAttribute("threshold",         threshold);
        request.setAttribute("today",             DateUtil.todayAsHtmlDate());
        request.setAttribute("currentYear",       LocalDate.now().getYear());
        request.setAttribute("minRequired",
                AttendanceCalculator.MINIMUM_REQUIRED_PERCENTAGE);

        switch (type) {

            // ── ATTENDANCE REPORT ──────────────────────────────────
            case "attendance": {
                if (selectedSubjectId == 0) {
                    request.setAttribute("errorMessage",
                            "Please select a subject to view the attendance report.");
                    break;
                }
                List<Map<String, Object>> attReport = reportDAO
                        .getFacultySubjectAttendanceReport(selectedSubjectId, academicYearId);

                List<Map<String, Object>> monthlyTrend = reportDAO
                        .getMonthlyTrend(selectedSubjectId);

                // Build chart data
                List<String>  repLabels  = new java.util.ArrayList<>();
                List<Double>  repPct     = new java.util.ArrayList<>();
                List<String>  trendLbls  = new java.util.ArrayList<>();
                List<Double>  trendPcts  = new java.util.ArrayList<>();

                for (Map<String, Object> r : attReport) {
                    repLabels.add(r.get("studentCode").toString());
                    repPct.add((Double) r.get("percentage"));
                }
                for (Map<String, Object> t : monthlyTrend) {
                    trendLbls.add(t.get("label").toString());
                    trendPcts.add((Double) t.get("monthPct"));
                }

                // Count eligible / defaulters
                long eligible   = attReport.stream()
                        .filter(r -> (Double) r.get("percentage") >= 75.0).count();
                long defaulters = attReport.size() - eligible;

                request.setAttribute("attReport",      attReport);
                request.setAttribute("eligibleCount",  eligible);
                request.setAttribute("defaulterCount", defaulters);
                request.setAttribute("repLabelsJson",
                        AnalyticsDAO.toJsonStringArray(repLabels));
                request.setAttribute("repPctJson",
                        AnalyticsDAO.toJsonNumberArray(repPct));
                request.setAttribute("trendLabelsJson",
                        AnalyticsDAO.toJsonStringArray(trendLbls));
                request.setAttribute("trendPctJson",
                        AnalyticsDAO.toJsonNumberArray(trendPcts));
                break;
            }

            // ── DAILY REPORT ───────────────────────────────────────
            case "daily": {
                if (selectedSubjectId == 0) {
                    request.setAttribute("errorMessage",
                            "Please select a subject to view the daily report.");
                    break;
                }
                List<Map<String, Object>> dailyAtt = reportDAO
                        .getFacultyDailyAttendance(selectedSubjectId, selectedDate);

                long pCount = dailyAtt.stream()
                        .filter(r -> "Present".equals(r.get("status"))).count();
                long aCount = dailyAtt.stream()
                        .filter(r -> "Absent".equals(r.get("status"))).count();
                long lCount = dailyAtt.stream()
                        .filter(r -> "Late".equals(r.get("status"))).count();

                request.setAttribute("dailyAttendance", dailyAtt);
                request.setAttribute("dailyPresent",    pCount);
                request.setAttribute("dailyAbsent",     aCount);
                request.setAttribute("dailyLate",       lCount);
                request.setAttribute("formattedDate",
                        DateUtil.formatForDisplay(selectedDate));
                break;
            }

            // ── MONTHLY REPORT ─────────────────────────────────────
            case "monthly": {
                if (selectedSubjectId == 0) {
                    request.setAttribute("errorMessage",
                            "Please select a subject to view the monthly report.");
                    break;
                }
                List<Map<String, Object>> monthlyAtt = reportDAO
                        .getFacultyMonthlyAttendance(
                                selectedSubjectId, selectedMonth, selectedYear);

                List<Map<String, Integer>> monthsWithData = reportDAO
                        .getMonthsWithData(selectedSubjectId);

                // Average monthly pct
                double avgMonthlyPct = monthlyAtt.stream()
                        .mapToDouble(r -> (Double) r.get("monthlyPct"))
                        .average().orElse(0.0);

                request.setAttribute("monthlyAttendance", monthlyAtt);
                request.setAttribute("monthsWithData",    monthsWithData);
                request.setAttribute("avgMonthlyPct",
                        Math.round(avgMonthlyPct * 100.0) / 100.0);
                request.setAttribute("selectedMonthName",
                        java.time.Month.of(selectedMonth).name());
                break;
            }

            // ── LOW ATTENDANCE STUDENTS ────────────────────────────
            case "low": {
                List<Map<String, Object>> lowStudents = reportDAO
                        .getLowAttendanceStudents(facultyId, academicYearId, threshold);

                // Group by subject for display
                Map<String, List<Map<String, Object>>> grouped =
                        new java.util.LinkedHashMap<>();
                for (Map<String, Object> row : lowStudents) {
                    String code = (String) row.get("subjectCode");
                    grouped.computeIfAbsent(code, k -> new java.util.ArrayList<>()).add(row);
                }

                request.setAttribute("lowStudents",      lowStudents);
                request.setAttribute("lowGrouped",       grouped);
                request.setAttribute("lowStudentCount",  lowStudents.size());
                break;
            }

            // ── MARKS REPORT ───────────────────────────────────────
            case "marks": {
                if (selectedSubjectId == 0) {
                    request.setAttribute("errorMessage",
                            "Please select a subject to view the marks report.");
                    break;
                }
                List<Map<String, Object>> marksReport = reportDAO
                        .getFacultyMarksReport(selectedSubjectId, academicYearId);

                // Group by studentCode
                Map<String, Map<String, Object>> marksGrouped =
                        new java.util.LinkedHashMap<>();
                Map<String, String> examTypes = new java.util.LinkedHashMap<>();

                for (Map<String, Object> row : marksReport) {
                    String sc = (String) row.get("studentCode");
                    String en = (String) row.get("examName");
                    marksGrouped.computeIfAbsent(sc, k -> {
                        Map<String, Object> m = new java.util.LinkedHashMap<>();
                        m.put("studentName", row.get("studentName"));
                        m.put("section",     row.get("section"));
                        m.put("scores",      new java.util.LinkedHashMap<String, Object>());
                        return m;
                    });
                    @SuppressWarnings("unchecked")
                    Map<String, Object> scores =
                            (Map<String, Object>) marksGrouped.get(sc).get("scores");
                    scores.put(en, row.get("obtained"));
                    examTypes.put(en, String.valueOf(row.get("maxMarks")));
                }

                // Chart: avg marks per exam type
                Map<String, Object> avgChart = analyticsDAO
                        .getAvgMarksByExamType(selectedSubjectId, academicYearId);
                Map<String, Object> gradeChart = analyticsDAO
                        .getGradeDistribution(selectedSubjectId, academicYearId);

                @SuppressWarnings("unchecked")
                List<String>  avgLabels  = (List<String>)  avgChart.get("labels");
                @SuppressWarnings("unchecked")
                List<Double>  avgObtained= (List<Double>)  avgChart.get("avgObtained");
                @SuppressWarnings("unchecked")
                List<Integer> avgMax     = (List<Integer>) avgChart.get("maxMarks");
                @SuppressWarnings("unchecked")
                List<String>  gLabels    = (List<String>)  gradeChart.get("grades");
                @SuppressWarnings("unchecked")
                List<Integer> gCounts    = (List<Integer>) gradeChart.get("counts");

                request.setAttribute("marksGrouped",     marksGrouped);
                request.setAttribute("examTypes",        examTypes);
                request.setAttribute("avgLabelsJson",
                        AnalyticsDAO.toJsonStringArray(avgLabels));
                request.setAttribute("avgObtainedJson",
                        AnalyticsDAO.toJsonNumberArray(avgObtained));
                request.setAttribute("avgMaxJson",
                        AnalyticsDAO.toJsonNumberArray(avgMax));
                request.setAttribute("gradeLabelsJson",
                        AnalyticsDAO.toJsonStringArray(gLabels));
                request.setAttribute("gradeCountsJson",
                        AnalyticsDAO.toJsonNumberArray(gCounts));
                break;
            }

            // ── PERFORMANCE STATISTICS ─────────────────────────────
            case "performance": {
                if (selectedSubjectId == 0) {
                    request.setAttribute("errorMessage",
                            "Please select a subject to view performance statistics.");
                    break;
                }
                List<Map<String, Object>> perfStats = reportDAO
                        .getFacultyPerformanceStats(selectedSubjectId, academicYearId);

                Map<String, Object> avgChart = analyticsDAO
                        .getAvgMarksByExamType(selectedSubjectId, academicYearId);
                Map<String, Object> gradeChart = analyticsDAO
                        .getGradeDistribution(selectedSubjectId, academicYearId);

                @SuppressWarnings("unchecked")
                List<String>  aL  = (List<String>)  avgChart.get("labels");
                @SuppressWarnings("unchecked")
                List<Double>  aO  = (List<Double>)  avgChart.get("avgObtained");
                @SuppressWarnings("unchecked")
                List<Integer> aM  = (List<Integer>) avgChart.get("maxMarks");
                @SuppressWarnings("unchecked")
                List<String>  gL  = (List<String>)  gradeChart.get("grades");
                @SuppressWarnings("unchecked")
                List<Integer> gC  = (List<Integer>) gradeChart.get("counts");

                request.setAttribute("perfStats",      perfStats);
                request.setAttribute("avgLabelsJson",
                        AnalyticsDAO.toJsonStringArray(aL));
                request.setAttribute("avgObtainedJson",
                        AnalyticsDAO.toJsonNumberArray(aO));
                request.setAttribute("avgMaxJson",
                        AnalyticsDAO.toJsonNumberArray(aM));
                request.setAttribute("gradeLabelsJson",
                        AnalyticsDAO.toJsonStringArray(gL));
                request.setAttribute("gradeCountsJson",
                        AnalyticsDAO.toJsonNumberArray(gC));
                break;
            }

            // ── ANALYTICS OVERVIEW ─────────────────────────────────
            default: {
                Map<String, Object>  attRateData   = analyticsDAO
                        .getFacultyAttRateChartData(facultyId);
                Map<String, Object>  defDistData   = analyticsDAO
                        .getFacultyDefaulterDistribution(facultyId);

                @SuppressWarnings("unchecked")
                List<String> arLabels    = (List<String>) attRateData.get("labels");
                @SuppressWarnings("unchecked")
                List<Double> arPct       = (List<Double>) attRateData.get("avgPct");
                @SuppressWarnings("unchecked")
                List<String> ddLabels    = (List<String>) defDistData.get("labels");
                @SuppressWarnings("unchecked")
                List<Integer> ddElig     = (List<Integer>) defDistData.get("eligible");
                @SuppressWarnings("unchecked")
                List<Integer> ddDef      = (List<Integer>) defDistData.get("defaulters");

                // Summary counts
                List<Map<String, Object>> allLow = reportDAO
                        .getLowAttendanceStudents(facultyId, academicYearId, 75.0);

                request.setAttribute("totalLowAttCount", allLow.size());
                request.setAttribute("arLabelsJson",
                        AnalyticsDAO.toJsonStringArray(arLabels));
                request.setAttribute("arPctJson",
                        AnalyticsDAO.toJsonNumberArray(arPct));
                request.setAttribute("ddLabelsJson",
                        AnalyticsDAO.toJsonStringArray(ddLabels));
                request.setAttribute("ddEligJson",
                        AnalyticsDAO.toJsonNumberArray(ddElig));
                request.setAttribute("ddDefJson",
                        AnalyticsDAO.toJsonNumberArray(ddDef));
                break;
            }
        }

        request.getRequestDispatcher(REPORTS_VIEW).forward(request, response);
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private int parseIntSafe(String s, int def) {
        if (s == null || s.trim().isEmpty()) return def;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private double parseDoubleSafe(String s, double def) {
        if (s == null || s.trim().isEmpty()) return def;
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }
}