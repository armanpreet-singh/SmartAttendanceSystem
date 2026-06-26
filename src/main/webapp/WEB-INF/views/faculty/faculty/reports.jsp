<%-- ================================================================
     faculty/reports.jsp
     
     Purpose:
       - Faculty-facing reports and analytics hub.
       - Allows faculty to generate:
           * Subject-wise attendance report
           * Student defaulter list (below 75%)
           * Marks analysis per subject
           * Class performance overview
       - Supports subject selection, date range filtering,
         section filtering, and exam type filtering.
       - Renders Chart.js visualizations for attendance and marks.
       - Integrates export links to ExportReportServlet.
     
     Session:
       - loggedInFaculty : com.smartattendance.model.Faculty
       - userRole        : "FACULTY"
     
     Request Attributes (set by FacultyReportServlet):
       - faculty             : Faculty object
       - subjects            : List<Subject> assigned to faculty
       - reportType          : "attendance" | "marks" | "defaulters" | "overview"
       - selectedSubjectId   : int
       - selectedSection     : String
       - selectedExamTypeId  : int
       - attendanceReport    : List<Map<String,Object>>
       - marksReport         : List<Map<String,Object>>
       - defaulterList       : List<Map<String,Object>>
       - examTypes           : List<Map<String,Object>>
       - overviewStats       : Map<String,Object>
       - chartLabelsJson     : String (JSON array)
       - chartDataJson       : String (JSON array)
       - chartData2Json      : String (JSON array)
       - sections            : List<String>
       - reportTitle         : String
       - generatedAt         : String
================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c"   uri="jakarta.tags.core"   %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"    %>
<%
    /* ── Resolve all request attributes ─────────────────────────── */
    String pageTitle = "Reports & Analytics";

    com.smartattendance.model.Faculty faculty =
        (com.smartattendance.model.Faculty) request.getAttribute("faculty");
    if (faculty == null)
        faculty = (com.smartattendance.model.Faculty)
                  session.getAttribute("loggedInFaculty");

    @SuppressWarnings("unchecked")
    java.util.List<com.smartattendance.model.Subject> subjects =
        (java.util.List<com.smartattendance.model.Subject>)
        request.getAttribute("subjects");

    @SuppressWarnings("unchecked")
    java.util.List<java.util.Map<String, Object>> attendanceReport =
        (java.util.List<java.util.Map<String, Object>>)
        request.getAttribute("attendanceReport");

    @SuppressWarnings("unchecked")
    java.util.List<java.util.Map<String, Object>> marksReport =
        (java.util.List<java.util.Map<String, Object>>)
        request.getAttribute("marksReport");

    @SuppressWarnings("unchecked")
    java.util.List<java.util.Map<String, Object>> defaulterList =
        (java.util.List<java.util.Map<String, Object>>)
        request.getAttribute("defaulterList");

    @SuppressWarnings("unchecked")
    java.util.List<java.util.Map<String, Object>> examTypes =
        (java.util.List<java.util.Map<String, Object>>)
        request.getAttribute("examTypes");

    @SuppressWarnings("unchecked")
    java.util.List<String> sections =
        (java.util.List<String>) request.getAttribute("sections");

    @SuppressWarnings("unchecked")
    java.util.Map<String, Object> overviewStats =
        (java.util.Map<String, Object>) request.getAttribute("overviewStats");

    String  reportType        = (String)  request.getAttribute("reportType");
    Integer selectedSubjectId = (Integer) request.getAttribute("selectedSubjectId");
    String  selectedSection   = (String)  request.getAttribute("selectedSection");
    Integer selectedExamType  = (Integer) request.getAttribute("selectedExamTypeId");
    String  reportTitle       = (String)  request.getAttribute("reportTitle");
    String  generatedAt       = (String)  request.getAttribute("generatedAt");

    String chartLabelsJson = (String) request.getAttribute("chartLabelsJson");
    String chartDataJson   = (String) request.getAttribute("chartDataJson");
    String chartData2Json  = (String) request.getAttribute("chartData2Json");

    /* ── Safe defaults ────────────────────────────────────────────── */
    if (reportType        == null) reportType        = "none";
    if (selectedSection   == null) selectedSection   = "";
    if (selectedSubjectId == null) selectedSubjectId = 0;
    if (selectedExamType  == null) selectedExamType  = 0;
    if (reportTitle       == null) reportTitle       = "Report";
    if (generatedAt       == null) generatedAt       = "";
    if (chartLabelsJson   == null) chartLabelsJson   = "[]";
    if (chartDataJson     == null) chartDataJson     = "[]";
    if (chartData2Json    == null) chartData2Json    = "[]";

    /* ── Derived helpers ──────────────────────────────────────────── */
    String ctxPath = request.getContextPath();

    /* subject name lookup for display */
    String selectedSubjectName = "";
    if (subjects != null && selectedSubjectId > 0) {
        for (com.smartattendance.model.Subject s : subjects) {
            if (s.getId() == selectedSubjectId) {
                selectedSubjectName = s.getSubjectCode() + " – " + s.getSubjectName();
                break;
            }
        }
    }
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp"  %>

<!-- ================================================================
     MAIN CONTENT
================================================================ -->
<main class="container-fluid py-4 px-4">

    <!-- ─── Page Header ──────────────────────────────────────────── -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h4 class="fw-bold text-dark mb-0">
                <i class="fas fa-chart-bar me-2 text-primary"></i>
                Reports &amp; Analytics
            </h4>
            <small class="text-muted">
                Generate and export attendance and academic performance reports
            </small>
        </div>
        <div class="d-flex gap-2">
            <!-- Export button (only when a report is loaded) -->
            <% if (!"none".equals(reportType) && selectedSubjectId > 0) { %>
            <a href="<%= ctxPath %>/report/export?reportType=<%= reportType
                    %>&subjectId=<%= selectedSubjectId
                    %>&section=<%= selectedSection
                    %>&examTypeId=<%= selectedExamType
                    %>&role=faculty"
               class="btn btn-sm btn-outline-success rounded-pill">
                <i class="fas fa-file-csv me-1"></i> Export CSV
            </a>
            <button onclick="window.print();"
                    class="btn btn-sm btn-outline-secondary rounded-pill">
                <i class="fas fa-print me-1"></i> Print
            </button>
            <% } %>
            <a href="<%= ctxPath %>/faculty/dashboard"
               class="btn btn-sm btn-outline-secondary rounded-pill">
                <i class="fas fa-arrow-left me-1"></i> Dashboard
            </a>
        </div>
    </div>

    <!-- ─── Report Filter Panel ──────────────────────────────────── -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-sliders-h me-2 text-primary"></i>
                Report Filters
            </h6>
        </div>
        <div class="card-body p-4">
            <form method="GET"
                  action="<%= ctxPath %>/faculty/reports"
                  id="reportFilterForm">

                <div class="row g-3 align-items-end">

                    <!-- Report Type -->
                    <div class="col-md-3">
                        <label class="form-label fw-medium small">
                            <i class="fas fa-file-alt me-1 text-muted"></i>
                            Report Type <span class="text-danger">*</span>
                        </label>
                        <select name="reportType" id="reportTypeSelect"
                                class="form-select" style="border-radius:10px;"
                                onchange="toggleFilters(this.value)" required>
                            <option value="">-- Select Report --</option>
                            <option value="attendance"
                                <%= "attendance".equals(reportType) ? "selected" : "" %>>
                                Attendance Summary
                            </option>
                            <option value="defaulters"
                                <%= "defaulters".equals(reportType) ? "selected" : "" %>>
                                Defaulter List (&lt;75%)
                            </option>
                            <option value="marks"
                                <%= "marks".equals(reportType) ? "selected" : "" %>>
                                Marks Analysis
                            </option>
                            <option value="overview"
                                <%= "overview".equals(reportType) ? "selected" : "" %>>
                                Class Overview
                            </option>
                        </select>
                    </div>

                    <!-- Subject -->
                    <div class="col-md-3">
                        <label class="form-label fw-medium small">
                            <i class="fas fa-book me-1 text-muted"></i>
                            Subject <span class="text-danger">*</span>
                        </label>
                        <select name="subjectId" id="subjectSelect"
                                class="form-select" style="border-radius:10px;" required>
                            <option value="">-- Select Subject --</option>
                            <% if (subjects != null) {
                                for (com.smartattendance.model.Subject sub : subjects) { %>
                            <option value="<%= sub.getId() %>"
                                <%= (selectedSubjectId == sub.getId()) ? "selected" : "" %>>
                                <%= sub.getSubjectCode() %> – <%= sub.getSubjectName() %>
                            </option>
                            <% } } %>
                        </select>
                    </div>

                    <!-- Section Filter -->
                    <div class="col-md-2" id="sectionFilterDiv">
                        <label class="form-label fw-medium small">
                            <i class="fas fa-door-open me-1 text-muted"></i>
                            Section
                        </label>
                        <select name="section" class="form-select"
                                style="border-radius:10px;">
                            <option value="">All Sections</option>
                            <% if (sections != null) {
                                for (String sec : sections) { %>
                            <option value="<%= sec %>"
                                <%= sec.equals(selectedSection) ? "selected" : "" %>>
                                Section <%= sec %>
                            </option>
                            <% } } %>
                        </select>
                    </div>

                    <!-- Exam Type Filter (for marks only) -->
                    <div class="col-md-2" id="examTypeFilterDiv"
                         style="<%= "marks".equals(reportType) ? "" : "display:none;" %>">
                        <label class="form-label fw-medium small">
                            <i class="fas fa-clipboard-list me-1 text-muted"></i>
                            Exam Type
                        </label>
                        <select name="examTypeId" class="form-select"
                                style="border-radius:10px;">
                            <option value="0">All Exams</option>
                            <% if (examTypes != null) {
                                for (java.util.Map<String, Object> et : examTypes) { %>
                            <option value="<%= et.get("id") %>"
                                <%= selectedExamType == (int)et.get("id") ? "selected" : "" %>>
                                <%= et.get("examName") %>
                            </option>
                            <% } } %>
                        </select>
                    </div>

                    <!-- Generate Button -->
                    <div class="col-md-2">
                        <button type="submit"
                                class="btn btn-primary rounded-pill w-100 fw-semibold">
                            <i class="fas fa-play me-2"></i>Generate
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- ================================================================
         REPORT CONTENT AREA
         Renders based on reportType value
    ================================================================ -->

    <!-- ── No report selected placeholder ──────────────────────── -->
    <% if ("none".equals(reportType)) { %>
    <div class="row g-3">

        <!-- Quick Report Cards -->
        <% String[][] quickCards = {
            {"attendance", "Attendance Summary",
             "fa-calendar-check", "primary",
             "Subject-wise attendance for all enrolled students"},
            {"defaulters", "Defaulter List",
             "fa-exclamation-triangle", "danger",
             "Students with attendance below 75% threshold"},
            {"marks", "Marks Analysis",
             "fa-star", "warning",
             "Exam-wise marks and grade distribution"},
            {"overview", "Class Overview",
             "fa-chart-line", "success",
             "Combined attendance and marks performance overview"}
        };
        for (String[] card : quickCards) { %>
        <div class="col-md-6 col-lg-3">
            <div class="card border-0 shadow-sm h-100 report-quick-card"
                 style="border-radius:14px;cursor:pointer;transition:transform 0.2s;"
                 onclick="selectReport('<%= card[0] %>')"
                 onmouseover="this.style.transform='translateY(-4px)'"
                 onmouseout="this.style.transform='translateY(0)'">
                <div class="card-body text-center p-4">
                    <div class="mx-auto mb-3 rounded-circle d-flex align-items-center
                                justify-content-center"
                         style="width:60px;height:60px;
                                background:rgba(var(--bs-<%= card[3] %>-rgb),0.12);">
                        <i class="fas <%= card[1] %> text-<%= card[3] %>"
                           style="font-size:1.5rem;"></i>
                    </div>
                    <h6 class="fw-bold text-dark mb-2"><%= card[1] %></h6>
                    <p class="text-muted small mb-0"><%= card[4] %></p>
                </div>
                <div class="card-footer border-0 text-center pb-3 pt-0 bg-white"
                     style="border-radius:0 0 14px 14px;">
                    <span class="badge bg-<%= card[3] %>-subtle text-<%= card[3] %>
                                 border border-<%= card[3] %> rounded-pill small">
                        Click to Select
                    </span>
                </div>
            </div>
        </div>
        <% } %>
    </div>
    <% } %>

    <!-- ================================================================
         ATTENDANCE SUMMARY REPORT
    ================================================================ -->
    <% if ("attendance".equals(reportType)) { %>

    <!-- Report Header Bar -->
    <%@ include file="/WEB-INF/views/common/report-view.jsp" %>

    <!-- Attendance Chart -->
    <% if (attendanceReport != null && !attendanceReport.isEmpty()) { %>
    <div class="row g-3 mb-4">
        <div class="col-lg-7">
            <div class="card border-0 shadow-sm" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-chart-bar me-2 text-primary"></i>
                        Attendance Distribution
                    </h6>
                </div>
                <div class="card-body" style="height:300px;">
                    <canvas id="attSummaryChart"></canvas>
                </div>
            </div>
        </div>
        <div class="col-lg-5">
            <div class="card border-0 shadow-sm" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-chart-pie me-2 text-primary"></i>
                        Status Breakdown
                    </h6>
                </div>
                <div class="card-body d-flex align-items-center justify-content-center"
                     style="height:300px;">
                    <canvas id="attStatusChart" width="280" height="280"></canvas>
                </div>
            </div>
        </div>
    </div>

    <!-- Attendance Report Table -->
    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4 d-flex
                    justify-content-between align-items-center flex-wrap gap-2">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-table me-2 text-primary"></i>
                Detailed Attendance Report
                <span class="badge bg-primary ms-2"><%= attendanceReport.size() %> students</span>
            </h6>
            <input type="text" id="attSearchInput"
                   class="form-control form-control-sm"
                   placeholder="🔍 Search student..."
                   style="border-radius:20px;max-width:220px;"
                   onkeyup="filterTable('attSearchInput','attReportTable','att-search-row')">
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0"
                       id="attReportTable">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">#</th>
                            <th>Student</th>
                            <th class="text-center">Section</th>
                            <th class="text-center">Total</th>
                            <th class="text-center text-success">Present</th>
                            <th class="text-center text-danger">Absent</th>
                            <th class="text-center text-warning">Late</th>
                            <th class="text-center">Percentage</th>
                            <th class="text-center">Status</th>
                            <th class="text-center">Eligible?</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            int rn = 1;
                            int   totalPresent = 0, totalAbsent = 0,
                                  totalLate    = 0, totalClasses = 0;

                            for (java.util.Map<String, Object> row : attendanceReport) {
                                double pct  = row.get("percentage") != null
                                              ? ((Number) row.get("percentage")).doubleValue()
                                              : 0.0;
                                String bg   = com.smartattendance.util.AttendanceCalculator
                                                 .getStatusBadgeClass(pct);
                                String stat = com.smartattendance.util.AttendanceCalculator
                                                 .getAttendanceStatus(pct);
                                boolean eligible = com.smartattendance.util.AttendanceCalculator
                                                      .isEligibleForExam(pct);

                                int tc = row.get("totalClasses")   != null
                                         ? ((Number)row.get("totalClasses")).intValue()   : 0;
                                int pc = row.get("classesPresent") != null
                                         ? ((Number)row.get("classesPresent")).intValue() : 0;
                                int ac = row.get("classesAbsent")  != null
                                         ? ((Number)row.get("classesAbsent")).intValue()  : 0;
                                int lc = row.get("classesLate")    != null
                                         ? ((Number)row.get("classesLate")).intValue()    : 0;

                                totalClasses += tc;
                                totalPresent += pc;
                                totalAbsent  += ac;
                                totalLate    += lc;
                        %>
                        <tr class="att-search-row">
                            <td class="ps-4 text-muted small"><%= rn++ %></td>
                            <td>
                                <div class="fw-medium text-dark">
                                    <%= row.get("studentName") %>
                                </div>
                                <div class="text-muted" style="font-size:0.76rem;">
                                    <%= row.get("studentCode") %>
                                </div>
                            </td>
                            <td class="text-center text-muted small">
                                <%= row.get("section") %>
                            </td>
                            <td class="text-center fw-medium"><%= tc %></td>
                            <td class="text-center text-success fw-medium"><%= pc %></td>
                            <td class="text-center text-danger fw-medium"><%= ac %></td>
                            <td class="text-center text-warning fw-medium"><%= lc %></td>
                            <td class="text-center">
                                <div class="d-flex align-items-center
                                            justify-content-center gap-2">
                                    <div class="progress"
                                         style="height:8px;width:60px;">
                                        <div class="progress-bar bg-<%= bg %>"
                                             style="width:<%= Math.min(pct,100.0) %>%">
                                        </div>
                                    </div>
                                    <span class="fw-semibold text-<%= bg %>
                                                 small" style="min-width:40px;">
                                        <fmt:formatNumber value="<%= pct %>"
                                                          maxFractionDigits="1"/>%
                                    </span>
                                </div>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-<%= bg %> rounded-pill small">
                                    <%= stat %>
                                </span>
                            </td>
                            <td class="text-center">
                                <% if (eligible) { %>
                                <i class="fas fa-check-circle text-success fa-lg"
                                   title="Eligible for Exams"></i>
                                <% } else { %>
                                <i class="fas fa-times-circle text-danger fa-lg"
                                   title="Not Eligible – Below 75%"></i>
                                <% } %>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                    <tfoot class="table-light fw-semibold">
                        <tr>
                            <td class="ps-4" colspan="3">Totals</td>
                            <td class="text-center"><%= totalClasses %></td>
                            <td class="text-center text-success"><%= totalPresent %></td>
                            <td class="text-center text-danger"><%= totalAbsent %></td>
                            <td class="text-center text-warning"><%= totalLate %></td>
                            <td colspan="3"></td>
                        </tr>
                    </tfoot>
                </table>
            </div>
        </div>
    </div>
    <% } else { %>
    <%-- Empty state --%>
    <div class="card border-0 shadow-sm text-center py-5"
         style="border-radius:14px;">
        <i class="fas fa-folder-open fa-3x text-muted mb-3"></i>
        <h5 class="text-muted">No attendance records found.</h5>
        <p class="text-muted small">
            Select a subject and generate the report.
        </p>
    </div>
    <% } %>
    <% } // end attendance %>

    <!-- ================================================================
         DEFAULTER LIST REPORT
    ================================================================ -->
    <% if ("defaulters".equals(reportType)) { %>

    <%@ include file="/WEB-INF/views/common/report-view.jsp" %>

    <% if (defaulterList != null && !defaulterList.isEmpty()) { %>

    <!-- Defaulter Alert Banner -->
    <div class="alert alert-danger d-flex align-items-center gap-3 mb-4"
         style="border-radius:12px;" role="alert">
        <i class="fas fa-exclamation-triangle fa-2x flex-shrink-0"></i>
        <div>
            <strong><%= defaulterList.size() %> student(s)</strong>
            have attendance below the minimum required
            <strong>
                <%= (int) com.smartattendance.util.AttendanceCalculator
                            .MINIMUM_REQUIRED_PERCENTAGE %>%
            </strong>
            and may be ineligible for examinations.
        </div>
    </div>

    <!-- Defaulter Bar Chart -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-chart-bar me-2 text-danger"></i>
                Defaulter Attendance Percentages
            </h6>
        </div>
        <div class="card-body" style="height:280px;">
            <canvas id="defaulterChart"></canvas>
        </div>
    </div>

    <!-- Defaulter Table -->
    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4 d-flex
                    justify-content-between align-items-center flex-wrap gap-2">
            <h6 class="fw-bold mb-0 text-danger">
                <i class="fas fa-user-times me-2"></i>
                Defaulter Students
                <span class="badge bg-danger ms-2">
                    <%= defaulterList.size() %>
                </span>
            </h6>
            <input type="text" id="defSearchInput"
                   class="form-control form-control-sm"
                   placeholder="🔍 Search student..."
                   style="border-radius:20px;max-width:220px;"
                   onkeyup="filterTable('defSearchInput','defTable','def-row')">
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0" id="defTable">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">#</th>
                            <th>Student</th>
                            <th class="text-center">Section</th>
                            <th class="text-center">Total</th>
                            <th class="text-center">Present</th>
                            <th class="text-center">Absent</th>
                            <th class="text-center">Attendance %</th>
                            <th class="text-center">Classes Needed</th>
                            <th class="text-center">Shortfall</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            int dn = 1;
                            for (java.util.Map<String, Object> row : defaulterList) {
                                double pct = row.get("percentage") != null
                                             ? ((Number)row.get("percentage")).doubleValue()
                                             : 0.0;
                                int tc  = row.get("totalClasses")   != null
                                          ? ((Number)row.get("totalClasses")).intValue()   : 0;
                                int pc  = row.get("classesPresent") != null
                                          ? ((Number)row.get("classesPresent")).intValue() : 0;
                                int ac  = row.get("classesAbsent")  != null
                                          ? ((Number)row.get("classesAbsent")).intValue()  : 0;
                                int needed = com.smartattendance.util.AttendanceCalculator
                                                 .classesNeededToReachTarget(tc, pc, 75.0);
                                double shortfall = 75.0 - pct;
                        %>
                        <tr class="def-row table-danger-subtle">
                            <td class="ps-4 text-muted small"><%= dn++ %></td>
                            <td>
                                <div class="fw-medium text-dark">
                                    <%= row.get("studentName") %>
                                </div>
                                <div class="text-muted" style="font-size:0.76rem;">
                                    <%= row.get("studentCode") %>
                                </div>
                            </td>
                            <td class="text-center text-muted small">
                                <%= row.get("section") %>
                            </td>
                            <td class="text-center"><%= tc %></td>
                            <td class="text-center text-success"><%= pc %></td>
                            <td class="text-center text-danger"><%= ac %></td>
                            <td class="text-center">
                                <span class="badge bg-danger rounded-pill">
                                    <fmt:formatNumber value="<%= pct %>"
                                                      maxFractionDigits="1"/>%
                                </span>
                            </td>
                            <td class="text-center fw-semibold text-danger">
                                <% if (needed > 0) { %>
                                <%= needed %> class(es)
                                <% } else { %>
                                <span class="text-success">—</span>
                                <% } %>
                            </td>
                            <td class="text-center text-danger small fw-medium">
                                <fmt:formatNumber value="<%= shortfall %>"
                                                  maxFractionDigits="1"/>%
                                below
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <% } else { %>
    <div class="card border-0 shadow-sm text-center py-5"
         style="border-radius:14px;">
        <i class="fas fa-check-circle fa-3x text-success mb-3"></i>
        <h5 class="text-success fw-bold">No Defaulters Found!</h5>
        <p class="text-muted small">
            All students in this subject meet the minimum attendance requirement.
        </p>
    </div>
    <% } %>
    <% } // end defaulters %>

    <!-- ================================================================
         MARKS ANALYSIS REPORT
    ================================================================ -->
    <% if ("marks".equals(reportType)) { %>

    <%@ include file="/WEB-INF/views/common/report-view.jsp" %>

    <% if (marksReport != null && !marksReport.isEmpty()) { %>

    <!-- Marks Chart -->
    <div class="row g-3 mb-4">
        <div class="col-lg-8">
            <div class="card border-0 shadow-sm" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-chart-bar me-2 text-warning"></i>
                        Marks Distribution
                    </h6>
                </div>
                <div class="card-body" style="height:300px;">
                    <canvas id="marksChart"></canvas>
                </div>
            </div>
        </div>
        <div class="col-lg-4">
            <div class="card border-0 shadow-sm" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-trophy me-2 text-warning"></i>
                        Grade Distribution
                    </h6>
                </div>
                <div class="card-body d-flex align-items-center justify-content-center"
                     style="height:300px;">
                    <canvas id="gradeChart" width="260" height="260"></canvas>
                </div>
            </div>
        </div>
    </div>

    <!-- Marks Table -->
    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4 d-flex
                    justify-content-between align-items-center flex-wrap gap-2">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-table me-2 text-warning"></i>
                Marks Analysis Table
                <span class="badge bg-warning text-dark ms-2">
                    <%= marksReport.size() %> records
                </span>
            </h6>
            <input type="text" id="marksSearchInput"
                   class="form-control form-control-sm"
                   placeholder="🔍 Search student..."
                   style="border-radius:20px;max-width:220px;"
                   onkeyup="filterTable('marksSearchInput','marksReportTable','marks-row')">
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0"
                       id="marksReportTable">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">#</th>
                            <th>Student</th>
                            <th class="text-center">Section</th>
                            <th>Exam Type</th>
                            <th class="text-center">Max</th>
                            <th class="text-center">Obtained</th>
                            <th class="text-center">Percentage</th>
                            <th class="text-center">Grade</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            int mn = 1;
                            for (java.util.Map<String, Object> row : marksReport) {
                                double obtained = row.get("marksObtained") != null
                                    ? ((Number)row.get("marksObtained")).doubleValue() : 0.0;
                                int    maxM     = row.get("maxMarks") != null
                                    ? ((Number)row.get("maxMarks")).intValue() : 100;
                                double mpct     = maxM > 0
                                    ? Math.round((obtained/maxM)*100.0*10.0)/10.0 : 0.0;
                                String mbadge   = mpct >= 75 ? "success"
                                                : mpct >= 50 ? "warning" : "danger";
                        %>
                        <tr class="marks-row">
                            <td class="ps-4 text-muted small"><%= mn++ %></td>
                            <td>
                                <div class="fw-medium text-dark">
                                    <%= row.get("studentName") %>
                                </div>
                                <div class="text-muted" style="font-size:0.76rem;">
                                    <%= row.get("studentCode") %>
                                </div>
                            </td>
                            <td class="text-center text-muted small">
                                <%= row.get("section") %>
                            </td>
                            <td>
                                <span class="badge bg-secondary-subtle text-secondary
                                             border border-secondary-subtle small">
                                    <%= row.get("examName") %>
                                </span>
                            </td>
                            <td class="text-center text-muted"><%= maxM %></td>
                            <td class="text-center fw-semibold text-dark">
                                <fmt:formatNumber value="<%= obtained %>"
                                                  maxFractionDigits="1"/>
                            </td>
                            <td class="text-center">
                                <div class="d-flex align-items-center
                                            justify-content-center gap-2">
                                    <div class="progress"
                                         style="height:7px;width:55px;">
                                        <div class="progress-bar bg-<%= mbadge %>"
                                             style="width:<%= Math.min(mpct,100.0) %>%">
                                        </div>
                                    </div>
                                    <span class="fw-semibold text-<%= mbadge %> small"
                                          style="min-width:40px;">
                                        <fmt:formatNumber value="<%= mpct %>"
                                                          maxFractionDigits="1"/>%
                                    </span>
                                </div>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-<%= mbadge %> rounded-pill px-2">
                                    <%= row.get("grade") != null
                                        ? row.get("grade") : "—" %>
                                </span>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <% } else { %>
    <div class="card border-0 shadow-sm text-center py-5"
         style="border-radius:14px;">
        <i class="fas fa-folder-open fa-3x text-muted mb-3"></i>
        <h5 class="text-muted">No marks records found.</h5>
        <p class="text-muted small">
            No marks have been recorded for the selected filters.
        </p>
    </div>
    <% } %>
    <% } // end marks %>

    <!-- ================================================================
         CLASS OVERVIEW REPORT
    ================================================================ -->
    <% if ("overview".equals(reportType) && overviewStats != null) { %>

    <%@ include file="/WEB-INF/views/common/report-view.jsp" %>

    <%
        /* Safe extraction of overview stats */
        int    ov_totalStudents  = overviewStats.get("totalStudents")   != null
                                   ? ((Number)overviewStats.get("totalStudents")).intValue()    : 0;
        double ov_avgAttendance  = overviewStats.get("avgAttendance")   != null
                                   ? ((Number)overviewStats.get("avgAttendance")).doubleValue() : 0.0;
        double ov_avgMarks       = overviewStats.get("avgMarks")        != null
                                   ? ((Number)overviewStats.get("avgMarks")).doubleValue()      : 0.0;
        int    ov_defaulterCount = overviewStats.get("defaulterCount")  != null
                                   ? ((Number)overviewStats.get("defaulterCount")).intValue()   : 0;
        int    ov_totalClasses   = overviewStats.get("totalClasses")    != null
                                   ? ((Number)overviewStats.get("totalClasses")).intValue()     : 0;
        String ov_attBadge = com.smartattendance.util.AttendanceCalculator
                                 .getStatusBadgeClass(ov_avgAttendance);
    %>

    <!-- Overview Stat Cards -->
    <div class="row g-3 mb-4">

        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100 text-center p-3"
                 style="border-radius:14px;">
                <div class="mx-auto mb-2 rounded-circle d-flex align-items-center
                            justify-content-center"
                     style="width:48px;height:48px;background:rgba(13,110,253,0.1);">
                    <i class="fas fa-users text-primary"></i>
                </div>
                <h3 class="fw-bold text-primary mb-0">
                    <%= ov_totalStudents %>
                </h3>
                <small class="text-muted">Total Students</small>
            </div>
        </div>

        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100 text-center p-3"
                 style="border-radius:14px;">
                <div class="mx-auto mb-2 rounded-circle d-flex align-items-center
                            justify-content-center"
                     style="width:48px;height:48px;
                            background:rgba(var(--bs-<%= ov_attBadge %>-rgb),0.1);">
                    <i class="fas fa-calendar-check text-<%= ov_attBadge %>"></i>
                </div>
                <h3 class="fw-bold text-<%= ov_attBadge %> mb-0">
                    <fmt:formatNumber value="<%= ov_avgAttendance %>"
                                      maxFractionDigits="1"/>%
                </h3>
                <small class="text-muted">Avg Attendance</small>
            </div>
        </div>

        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100 text-center p-3"
                 style="border-radius:14px;">
                <div class="mx-auto mb-2 rounded-circle d-flex align-items-center
                            justify-content-center"
                     style="width:48px;height:48px;background:rgba(255,193,7,0.1);">
                    <i class="fas fa-star text-warning"></i>
                </div>
                <h3 class="fw-bold text-warning mb-0">
                    <fmt:formatNumber value="<%= ov_avgMarks %>"
                                      maxFractionDigits="1"/>%
                </h3>
                <small class="text-muted">Avg Marks %</small>
            </div>
        </div>

        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100 text-center p-3"
                 style="border-radius:14px;">
                <div class="mx-auto mb-2 rounded-circle d-flex align-items-center
                            justify-content-center"
                     style="width:48px;height:48px;background:rgba(220,53,69,0.1);">
                    <i class="fas fa-exclamation-triangle text-danger"></i>
                </div>
                <h3 class="fw-bold text-danger mb-0"><%= ov_defaulterCount %></h3>
                <small class="text-muted">Defaulters</small>
            </div>
        </div>

    </div>

    <!-- Overview Combined Chart -->
    <div class="row g-3 mb-4">
        <div class="col-lg-12">
            <div class="card border-0 shadow-sm" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-chart-line me-2 text-success"></i>
                        Attendance vs. Marks Performance (per student)
                    </h6>
                </div>
                <div class="card-body" style="height:320px;">
                    <canvas id="overviewChart"></canvas>
                </div>
            </div>
        </div>
    </div>

    <!-- Overview Detail Table (uses attendanceReport loaded by servlet) -->
    <% if (attendanceReport != null && !attendanceReport.isEmpty()) { %>
    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-table me-2 text-success"></i>
                Student-level Overview
            </h6>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">#</th>
                            <th>Student</th>
                            <th class="text-center">Section</th>
                            <th class="text-center">Attendance %</th>
                            <th class="text-center">Status</th>
                            <th class="text-center">Exam Eligible?</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            int on = 1;
                            for (java.util.Map<String, Object> row : attendanceReport) {
                                double pct    = row.get("percentage") != null
                                    ? ((Number)row.get("percentage")).doubleValue() : 0.0;
                                String bg     = com.smartattendance.util.AttendanceCalculator
                                                   .getStatusBadgeClass(pct);
                                String stat   = com.smartattendance.util.AttendanceCalculator
                                                   .getAttendanceStatus(pct);
                                boolean elig  = com.smartattendance.util.AttendanceCalculator
                                                   .isEligibleForExam(pct);
                        %>
                        <tr>
                            <td class="ps-4 text-muted small"><%= on++ %></td>
                            <td>
                                <div class="fw-medium text-dark">
                                    <%= row.get("studentName") %>
                                </div>
                                <div class="text-muted" style="font-size:0.76rem;">
                                    <%= row.get("studentCode") %>
                                </div>
                            </td>
                            <td class="text-center text-muted small">
                                <%= row.get("section") %>
                            </td>
                            <td class="text-center">
                                <div class="d-flex align-items-center
                                            justify-content-center gap-2">
                                    <div class="progress"
                                         style="height:8px;width:65px;">
                                        <div class="progress-bar bg-<%= bg %>"
                                             style="width:<%= Math.min(pct,100.0) %>%">
                                        </div>
                                    </div>
                                    <span class="fw-semibold text-<%= bg %> small">
                                        <fmt:formatNumber value="<%= pct %>"
                                                          maxFractionDigits="1"/>%
                                    </span>
                                </div>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-<%= bg %> rounded-pill small">
                                    <%= stat %>
                                </span>
                            </td>
                            <td class="text-center">
                                <% if (elig) { %>
                                <i class="fas fa-check-circle text-success"
                                   title="Eligible"></i>
                                <% } else { %>
                                <i class="fas fa-times-circle text-danger"
                                   title="Not Eligible"></i>
                                <% } %>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <% } %>
    <% } // end overview %>

</main>

<!-- ================================================================
     CHART.JS SCRIPTS
================================================================ -->
<script>
document.addEventListener('DOMContentLoaded', function () {

    const ctxPath = '<%= ctxPath %>';

    /* ── Utility: safe JSON parse ─────────────────────────────── */
    function safeJson(str, fallback) {
        try { return JSON.parse(str); }
        catch (e) { return fallback; }
    }

    const chartLabels  = safeJson('<%= chartLabelsJson  %>', []);
    const chartData    = safeJson('<%= chartDataJson    %>', []);
    const chartData2   = safeJson('<%= chartData2Json   %>', []);

    /* ── Attendance Summary Chart (bar) ──────────────────────── */
    const attSummaryEl = document.getElementById('attSummaryChart');
    if (attSummaryEl) {
        new Chart(attSummaryEl.getContext('2d'), {
            type: 'bar',
            data: {
                labels:   chartLabels,
                datasets: [{
                    label:           'Attendance %',
                    data:            chartData,
                    backgroundColor: chartData.map(v =>
                        v >= 85 ? 'rgba(25,135,84,0.8)'
                      : v >= 75 ? 'rgba(255,193,7,0.8)'
                      :           'rgba(220,53,69,0.8)'),
                    borderRadius:    8,
                    borderSkipped:   false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: { callbacks: {
                        label: ctx => ' ' + ctx.parsed.y.toFixed(1) + '%'
                    }}
                },
                scales: {
                    y: {
                        beginAtZero: true, max: 100,
                        ticks: { callback: v => v + '%' },
                        grid:  { color: 'rgba(0,0,0,0.04)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    /* ── Status Breakdown Doughnut ───────────────────────────── */
    const attStatusEl = document.getElementById('attStatusChart');
    if (attStatusEl && chartData2.length > 0) {
        new Chart(attStatusEl.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels:   ['Safe (≥85%)', 'Warning (75–85%)', 'Defaulter (<75%)'],
                datasets: [{
                    data:            chartData2,
                    backgroundColor: [
                        'rgba(25,135,84,0.8)',
                        'rgba(255,193,7,0.8)',
                        'rgba(220,53,69,0.8)'
                    ],
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { padding: 12 } }
                }
            }
        });
    }

    /* ── Defaulter Chart (horizontal bar) ────────────────────── */
    const defEl = document.getElementById('defaulterChart');
    if (defEl) {
        new Chart(defEl.getContext('2d'), {
            type: 'bar',
            data: {
                labels:   chartLabels,
                datasets: [{
                    label:           'Attendance %',
                    data:            chartData,
                    backgroundColor: 'rgba(220,53,69,0.75)',
                    borderRadius:    8,
                    borderSkipped:   false
                }, {
                    label:           'Required (75%)',
                    data:            chartLabels.map(() => 75),
                    type:            'line',
                    borderColor:     'rgba(255,193,7,1)',
                    borderDash:      [6, 4],
                    borderWidth:     2,
                    pointRadius:     0,
                    fill:            false
                }]
            },
            options: {
                indexAxis: 'y',
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'top' },
                    tooltip: { callbacks: {
                        label: ctx => ' ' + ctx.parsed.x.toFixed(1) + '%'
                    }}
                },
                scales: {
                    x: {
                        beginAtZero: true, max: 100,
                        ticks: { callback: v => v + '%' }
                    },
                    y: { grid: { display: false } }
                }
            }
        });
    }

    /* ── Marks Distribution Chart ────────────────────────────── */
    const marksEl = document.getElementById('marksChart');
    if (marksEl) {
        new Chart(marksEl.getContext('2d'), {
            type: 'bar',
            data: {
                labels:   chartLabels,
                datasets: [{
                    label:           'Marks %',
                    data:            chartData,
                    backgroundColor: chartData.map(v =>
                        v >= 75 ? 'rgba(25,135,84,0.75)'
                      : v >= 50 ? 'rgba(255,193,7,0.75)'
                      :           'rgba(220,53,69,0.75)'),
                    borderRadius:    8,
                    borderSkipped:   false
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: { callbacks: {
                        label: ctx => ' ' + ctx.parsed.y.toFixed(1) + '%'
                    }}
                },
                scales: {
                    y: {
                        beginAtZero: true, max: 100,
                        ticks: { callback: v => v + '%' },
                        grid:  { color: 'rgba(0,0,0,0.04)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

    /* ── Grade Distribution Doughnut ─────────────────────────── */
    const gradeEl = document.getElementById('gradeChart');
    if (gradeEl && chartData2.length > 0) {
        new Chart(gradeEl.getContext('2d'), {
            type: 'doughnut',
            data: {
                labels:   ['O/A+', 'A/B+', 'B/C', 'F'],
                datasets: [{
                    data:            chartData2,
                    backgroundColor: [
                        'rgba(25,135,84,0.8)',
                        'rgba(13,110,253,0.8)',
                        'rgba(255,193,7,0.8)',
                        'rgba(220,53,69,0.8)'
                    ],
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'bottom', labels: { padding: 10 } }
                }
            }
        });
    }

    /* ── Overview Combined Chart (line) ──────────────────────── */
    const ovEl = document.getElementById('overviewChart');
    if (ovEl) {
        new Chart(ovEl.getContext('2d'), {
            type: 'line',
            data: {
                labels:   chartLabels,
                datasets: [
                    {
                        label:            'Attendance %',
                        data:             chartData,
                        borderColor:      'rgba(13,110,253,1)',
                        backgroundColor:  'rgba(13,110,253,0.08)',
                        fill:             true,
                        tension:          0.4,
                        pointRadius:      5,
                        pointHoverRadius: 7,
                        pointBackgroundColor: 'rgba(13,110,253,1)'
                    },
                    {
                        label:            'Marks %',
                        data:             chartData2,
                        borderColor:      'rgba(255,193,7,1)',
                        backgroundColor:  'rgba(255,193,7,0.08)',
                        fill:             true,
                        tension:          0.4,
                        pointRadius:      5,
                        pointHoverRadius: 7,
                        pointBackgroundColor: 'rgba(255,193,7,1)'
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'top' },
                    tooltip: { callbacks: {
                        label: ctx => ' ' + ctx.dataset.label + ': '
                                    + ctx.parsed.y.toFixed(1) + '%'
                    }}
                },
                scales: {
                    y: {
                        beginAtZero: true, max: 100,
                        ticks: { callback: v => v + '%' },
                        grid:  { color: 'rgba(0,0,0,0.04)' }
                    },
                    x: { grid: { display: false } }
                }
            }
        });
    }

}); // end DOMContentLoaded

/* ── Table Search Filter ─────────────────────────────────────── */
function filterTable(inputId, tableId, rowClass) {
    const q    = document.getElementById(inputId).value.toLowerCase();
    const rows = document.querySelectorAll('#' + tableId + ' .' + rowClass);
    rows.forEach(function (row) {
        row.style.display = row.textContent.toLowerCase().includes(q) ? '' : 'none';
    });
}

/* ── Quick card: pre-select report type and focus subject ─────── */
function selectReport(type) {
    const sel = document.getElementById('reportTypeSelect');
    if (sel) {
        sel.value = type;
        toggleFilters(type);
        document.getElementById('subjectSelect').focus();
    }
}

/* ── Show/hide exam type filter based on report type ──────────── */
function toggleFilters(type) {
    const examDiv = document.getElementById('examTypeFilterDiv');
    if (examDiv) {
        examDiv.style.display = (type === 'marks') ? '' : 'none';
    }
}
</script>

<!-- ── Print Styles ─────────────────────────────────────────────── -->
<style>
    @media print {
        .navbar, .app-footer,
        #reportFilterForm, .btn,
        #attSearchInput, #defSearchInput,
        #marksSearchInput, .alert-dismissible .btn-close {
            display: none !important;
        }
        .card { box-shadow: none !important; border: 1px solid #dee2e6 !important; }
        main  { padding: 0 !important; }
    }
</style>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>