<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    String pageTitle = "My Reports";
    com.smartattendance.model.Student student =
        (com.smartattendance.model.Student) request.getAttribute("student");
    String reportType = (String) request.getAttribute("reportType");
    if (reportType == null) reportType = "overview";

    Integer selSub   = (Integer) request.getAttribute("selectedSubjectId");
    Integer selMonth = (Integer) request.getAttribute("selectedMonth");
    Integer selYear  = (Integer) request.getAttribute("selectedYear");
    Integer curYear  = (Integer) request.getAttribute("currentYear");
    Double  minReq   = (Double)  request.getAttribute("minRequired");

    if (selSub   == null) selSub   = 0;
    if (selMonth == null) selMonth = java.time.LocalDate.now().getMonthValue();
    if (selYear  == null) selYear  = java.time.LocalDate.now().getYear();
    if (curYear  == null) curYear  = java.time.LocalDate.now().getYear();
    if (minReq   == null) minReq   = 75.0;

    java.util.List<com.smartattendance.model.Subject> subjects =
        (java.util.List<com.smartattendance.model.Subject>)
        request.getAttribute("subjects");

    // Chart JSON strings
    String attLabelsJson   = (String) request.getAttribute("attLabelsJson");
    String attPctJson      = (String) request.getAttribute("attPctJson");
    String trendLabelsJson = (String) request.getAttribute("trendLabelsJson");
    String trendPctJson    = (String) request.getAttribute("trendPctJson");
    String marksLabelsJson = (String) request.getAttribute("marksLabelsJson");
    String marksPctJson    = (String) request.getAttribute("marksPctJson");

    if (attLabelsJson   == null) attLabelsJson   = "[]";
    if (attPctJson      == null) attPctJson      = "[]";
    if (trendLabelsJson == null) trendLabelsJson = "[]";
    if (trendPctJson    == null) trendPctJson    = "[]";
    if (marksLabelsJson == null) marksLabelsJson = "[]";
    if (marksPctJson    == null) marksPctJson    = "[]";
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<main class="container-fluid py-4 px-4">

    <!-- ─── Page Header ──────────────────────────────────────────── -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h4 class="fw-bold text-dark mb-0">
                <i class="fas fa-chart-bar me-2 text-primary"></i>
                My Reports & Analytics
            </h4>
            <small class="text-muted">
                <%= student != null ? student.getFullName() : "" %> &nbsp;|&nbsp;
                <%= student != null ? student.getSemesterDisplay() : "" %>
            </small>
        </div>
        <a href="<%= request.getContextPath() %>/student/dashboard"
           class="btn btn-sm btn-outline-secondary rounded-pill">
            <i class="fas fa-arrow-left me-1"></i> Dashboard
        </a>
    </div>

    <!-- ─── Report Type Navigation ───────────────────────────────── -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-body p-3">
            <div class="d-flex flex-wrap gap-2">
                <% String[] types  = {"overview","attendance","marks","performance","monthly"};
                   String[] labels = {"Overview","Attendance","Marks","Performance","Monthly"};
                   String[] icons  = {"fa-th-large","fa-calendar-check","fa-star",
                                      "fa-chart-line","fa-calendar-alt"};
                   for (int ti = 0; ti < types.length; ti++) {
                       boolean active = types[ti].equals(reportType);
                %>
                <a href="<%= request.getContextPath() %>/student/reports?type=<%= types[ti] %>"
                   class="btn <%= active ? "btn-primary" : "btn-outline-primary" %> rounded-pill">
                    <i class="fas <%= icons[ti] %> me-1"></i><%= labels[ti] %>
                </a>
                <% } %>
            </div>
        </div>
    </div>

    <!-- ================================================================
         OVERVIEW / DEFAULT
    ================================================================ -->
    <% if ("overview".equals(reportType)) { %>

    <div class="row g-3 mb-4">
        <!-- Attendance Doughnut -->
        <div class="col-lg-4">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-chart-pie me-2 text-primary"></i>
                        Overall Attendance
                    </h6>
                </div>
                <div class="card-body d-flex align-items-center justify-content-center"
                     style="min-height:240px;">
                    <canvas id="overallDoughnut"></canvas>
                </div>
            </div>
        </div>
        <!-- Monthly Trend Line -->
        <div class="col-lg-8">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-chart-line me-2 text-primary"></i>
                        Monthly Attendance Trend
                    </h6>
                </div>
                <div class="card-body" style="min-height:240px;">
                    <canvas id="trendLine"></canvas>
                </div>
            </div>
        </div>
    </div>

    <!-- Quick Actions -->
    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-body p-3">
            <div class="row g-3">
                <% String[][] qactions = {
                    {"attendance","Attendance Report","fa-calendar-check","primary",
                     "View subject-wise attendance details"},
                    {"marks","Marks Report","fa-star","success",
                     "View your marks for all subjects"},
                    {"performance","Performance Report","fa-chart-line","info",
                     "View your overall academic performance"},
                    {"monthly","Monthly Report","fa-calendar-alt","warning",
                     "View month-wise attendance breakdown"}
                };
                for (String[] qa : qactions) { %>
                <div class="col-md-3">
                    <a href="<%= request.getContextPath() %>/student/reports?type=<%= qa[0] %>"
                       class="card border-0 shadow-sm text-center p-3 text-decoration-none
                              d-block h-100"
                       style="border-radius:12px;transition:transform 0.2s;"
                       onmouseover="this.style.transform='translateY(-4px)'"
                       onmouseout="this.style.transform='translateY(0)'">
                        <i class="fas <%= qa[2] %> fa-2x text-<%= qa[3] %> mb-2"></i>
                        <div class="fw-semibold text-dark"><%= qa[1] %></div>
                        <small class="text-muted"><%= qa[4] %></small>
                    </a>
                </div>
                <% } %>
            </div>
        </div>
    </div>

    <% } %>

    <!-- ================================================================
         ATTENDANCE REPORT
    ================================================================ -->
    <% if ("attendance".equals(reportType)) {
        java.util.List<java.util.Map<String, Object>> overallAtt =
            (java.util.List<java.util.Map<String, Object>>)
            request.getAttribute("overallAttendance");
        java.util.List<java.util.Map<String, Object>> detailRecords =
            (java.util.List<java.util.Map<String, Object>>)
            request.getAttribute("detailRecords");
        java.util.Map<String, Integer> totals =
            (java.util.Map<String, Integer>) request.getAttribute("overallTotals");
        Double overallPct = (Double) request.getAttribute("overallPct");
        if (overallPct == null) overallPct = 0.0;
        String ovBadge = com.smartattendance.util.AttendanceCalculator
                            .getStatusBadgeClass(overallPct);
    %>

    <!-- Subject Filter + Export -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-body p-3">
            <div class="row g-3 align-items-end">
                <div class="col-md-4">
                    <label class="form-label fw-medium small">Subject Filter</label>
                    <select class="form-select" style="border-radius:10px;"
                            onchange="if(this.value) window.location.href=
                            '<%= request.getContextPath() %>/student/reports?type=attendance&subjectId='
                            +this.value; else window.location.href=
                            '<%= request.getContextPath() %>/student/reports?type=attendance';">
                        <option value="">All Subjects</option>
                        <% if (subjects != null) {
                            for (com.smartattendance.model.Subject sub : subjects) { %>
                        <option value="<%= sub.getId() %>"
                            <%= sub.getId() == selSub ? "selected" : "" %>>
                            <%= sub.getSubjectCode() %> – <%= sub.getSubjectName() %>
                        </option>
                        <% } } %>
                    </select>
                </div>
                <div class="col-md-8 d-flex gap-2 flex-wrap">
                    <a href="<%= request.getContextPath()
                       %>/export/report?format=pdf&report=attendance&role=student"
                       class="btn btn-outline-danger rounded-pill">
                        <i class="fas fa-file-pdf me-1"></i>Export PDF
                    </a>
                    <a href="<%= request.getContextPath()
                       %>/export/report?format=excel&report=attendance&role=student"
                       class="btn btn-outline-success rounded-pill">
                        <i class="fas fa-file-excel me-1"></i>Export Excel
                    </a>
                </div>
            </div>
        </div>
    </div>

    <!-- Overall Stats -->
    <div class="row g-3 mb-4">
        <% int pAll = totals != null ? totals.getOrDefault("present",0) : 0;
           int aAll = totals != null ? totals.getOrDefault("absent",0)  : 0;
           int lAll = totals != null ? totals.getOrDefault("late",0)    : 0;
           int tAll = pAll + aAll + lAll;
        %>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center p-3" style="border-radius:14px;">
                <h3 class="fw-bold text-<%= ovBadge %> mb-0">
                    <fmt:formatNumber value="<%= overallPct %>" maxFractionDigits="1"/>%
                </h3>
                <small class="text-muted">Overall</small>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center p-3" style="border-radius:14px;">
                <h3 class="fw-bold text-dark mb-0"><%= tAll %></h3>
                <small class="text-muted">Total Classes</small>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center p-3" style="border-radius:14px;">
                <h3 class="fw-bold text-success mb-0"><%= pAll + lAll %></h3>
                <small class="text-muted">Attended</small>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center p-3" style="border-radius:14px;">
                <h3 class="fw-bold text-danger mb-0"><%= aAll %></h3>
                <small class="text-muted">Absent</small>
            </div>
        </div>
    </div>

    <!-- Charts Row -->
    <div class="row g-3 mb-4">
        <div class="col-lg-6">
            <div class="card border-0 shadow-sm" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">Subject-wise Attendance %</h6>
                </div>
                <div class="card-body" style="height:260px;">
                    <canvas id="attBarChart"></canvas>
                </div>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="card border-0 shadow-sm" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">Monthly Attendance Trend</h6>
                </div>
                <div class="card-body" style="height:260px;">
                    <canvas id="attTrendChart"></canvas>
                </div>
            </div>
        </div>
    </div>

    <!-- Attendance Table -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-table me-2 text-primary"></i>
                Subject-wise Attendance Summary
            </h6>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">Subject</th>
                            <th class="text-center">Total</th>
                            <th class="text-center text-success">Present</th>
                            <th class="text-center text-danger">Absent</th>
                            <th class="text-center text-warning">Late</th>
                            <th class="text-center">Percentage</th>
                            <th class="text-center">Status</th>
                            <th class="text-center">Details</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (overallAtt == null || overallAtt.isEmpty()) { %>
                        <tr>
                            <td colspan="8" class="text-center py-4 text-muted">
                                No attendance records found.
                            </td>
                        </tr>
                        <% } else {
                            for (java.util.Map<String, Object> row : overallAtt) {
                                double pct = (double) row.get("percentage");
                                String bg  = com.smartattendance.util.AttendanceCalculator
                                                .getStatusBadgeClass(pct);
                                String st  = com.smartattendance.util.AttendanceCalculator
                                                .getAttendanceStatus(pct);
                        %>
                        <tr>
                            <td class="ps-4">
                                <div class="fw-medium text-dark">
                                    <%= row.get("subjectCode") %>
                                </div>
                                <div class="text-muted small">
                                    <%= row.get("subjectName") %>
                                </div>
                            </td>
                            <td class="text-center"><%= row.get("totalClasses") %></td>
                            <td class="text-center text-success fw-medium">
                                <%= row.get("present") %>
                            </td>
                            <td class="text-center text-danger fw-medium">
                                <%= row.get("absent") %>
                            </td>
                            <td class="text-center text-warning fw-medium">
                                <%= row.get("late") %>
                            </td>
                            <td class="text-center">
                                <div class="d-flex align-items-center
                                            justify-content-center gap-2">
                                    <div class="progress flex-grow-1"
                                         style="height:8px;max-width:70px;">
                                        <div class="progress-bar bg-<%= bg %>"
                                             style="width:<%= Math.min(pct,100.0) %>%">
                                        </div>
                                    </div>
                                    <span class="fw-semibold text-<%= bg %>">
                                        <fmt:formatNumber value="<%= pct %>"
                                                          maxFractionDigits="1"/>%
                                    </span>
                                </div>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-<%= bg %> rounded-pill">
                                    <%= st %>
                                </span>
                            </td>
                            <td class="text-center">
                                <a href="<%= request.getContextPath()
                                   %>/student/reports?type=attendance&subjectId=<%= row.get("subjectId") %>"
                                   class="btn btn-sm btn-outline-primary rounded-pill">
                                    <i class="fas fa-eye me-1"></i>Detail
                                </a>
                            </td>
                        </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- Detail Records (when subject selected) -->
    <% if (selSub > 0 && detailRecords != null && !detailRecords.isEmpty()) { %>
    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4 d-flex
                    justify-content-between align-items-center">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-list me-2 text-primary"></i>
                Detailed Records
            </h6>
            <a href="<%= request.getContextPath() %>/student/reports?type=attendance"
               class="btn btn-sm btn-outline-secondary rounded-pill">
                <i class="fas fa-times me-1"></i>Clear
            </a>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">#</th>
                            <th>Date</th>
                            <th class="text-center">Status</th>
                            <th>Remarks</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% int dn = 1;
                           for (java.util.Map<String, Object> rec : detailRecords) {
                               String st   = (String) rec.get("status");
                               String bg   = "Present".equals(st) ? "success"
                                           : "Absent".equals(st)  ? "danger" : "warning";
                               java.time.LocalDate d =
                                   (java.time.LocalDate) rec.get("date");
                        %>
                        <tr>
                            <td class="ps-4 text-muted small"><%= dn++ %></td>
                            <td class="fw-medium">
                                <%= d != null
                                    ? com.smartattendance.util.DateUtil.formatForDisplay(d)
                                    : "—" %>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-<%= bg %>-subtle text-<%= bg %>
                                             border border-<%= bg %> rounded-pill">
                                    <%= st %>
                                </span>
                            </td>
                            <td class="text-muted small">
                                <%= rec.get("remarks") != null ? rec.get("remarks") : "—" %>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <% } %>

    <% } /* end attendance */ %>

    <!-- ================================================================
         MARKS REPORT
    ================================================================ -->
    <% if ("marks".equals(reportType)) {
        java.util.Map<String, java.util.List<java.util.Map<String, Object>>> groupedMarks =
            (java.util.Map<String, java.util.List<java.util.Map<String, Object>>>)
            request.getAttribute("groupedMarks");
        java.util.Map<String, String> subjectNameMap =
            (java.util.Map<String, String>) request.getAttribute("subjectNameMap");
    %>

    <!-- Export Buttons -->
    <div class="card border-0 shadow-sm mb-4 p-3" style="border-radius:14px;">
        <div class="d-flex gap-2 flex-wrap">
            <a href="<%= request.getContextPath()
               %>/export/report?format=pdf&report=marks&role=student"
               class="btn btn-outline-danger rounded-pill">
                <i class="fas fa-file-pdf me-1"></i>Export PDF
            </a>
            <a href="<%= request.getContextPath()
               %>/export/report?format=excel&report=marks&role=student"
               class="btn btn-outline-success rounded-pill">
                <i class="fas fa-file-excel me-1"></i>Export Excel
            </a>
        </div>
    </div>

    <!-- Marks Chart -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-chart-radar me-2 text-warning"></i>
                Subject-wise Marks Performance (%)
            </h6>
        </div>
        <div class="card-body" style="height:300px;">
            <canvas id="marksRadarChart"></canvas>
        </div>
    </div>

    <!-- Marks Tables -->
    <% if (groupedMarks == null || groupedMarks.isEmpty()) { %>
    <div class="alert alert-info" style="border-radius:12px;">
        <i class="fas fa-info-circle me-2"></i>
        No marks recorded yet.
    </div>
    <% } else {
        for (java.util.Map.Entry<String,
                java.util.List<java.util.Map<String, Object>>> entry
                : groupedMarks.entrySet()) {
            String code = entry.getKey();
            String name = subjectNameMap != null
                          ? subjectNameMap.getOrDefault(code, "") : "";
            double totalObt = 0, totalMax = 0;
            for (java.util.Map<String, Object> m : entry.getValue()) {
                Object ob = m.get("obtained");
                if (ob instanceof Double) totalObt += (Double) ob;
                Object mx = m.get("maxMarks");
                if (mx instanceof Integer) totalMax += (Integer) mx;
            }
            double subPct = totalMax > 0
                ? Math.round((totalObt / totalMax) * 100.0 * 100.0) / 100.0 : 0.0;
            String subBg  = subPct >= 75 ? "success" : subPct >= 50 ? "warning" : "danger";
    %>
    <div class="card border-0 shadow-sm mb-3" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4 d-flex
                    justify-content-between align-items-center">
            <h6 class="fw-bold mb-0">
                <span class="badge bg-primary me-2"><%= code %></span>
                <%= name %>
            </h6>
            <span class="badge bg-<%= subBg %> fs-6 px-3 py-1">
                <fmt:formatNumber value="<%= subPct %>" maxFractionDigits="1"/>%
            </span>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">Exam Type</th>
                            <th class="text-center">Max</th>
                            <th class="text-center">Obtained</th>
                            <th class="text-center">%</th>
                            <th class="text-center">Grade</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (java.util.Map<String, Object> mRow : entry.getValue()) {
                               Object ob = mRow.get("obtained");
                               Object mx = mRow.get("maxMarks");
                               double mPct = (ob instanceof Double && mx instanceof Integer
                                             && (Integer) mx > 0)
                                   ? Math.round(((Double) ob / (Integer) mx) * 100.0 * 100.0) / 100.0
                                   : 0.0;
                               String mBg = mPct >= 75 ? "success"
                                          : mPct >= 50 ? "warning" : "danger";
                        %>
                        <tr>
                            <td class="ps-4">
                                <span class="badge bg-secondary-subtle text-secondary border">
                                    <%= mRow.get("examName") %>
                                </span>
                            </td>
                            <td class="text-center text-muted"><%= mRow.get("maxMarks") %></td>
                            <td class="text-center fw-semibold">
                                <%= ob != null
                                    ? String.format("%.1f", ((Double) ob))
                                    : "—" %>
                            </td>
                            <td class="text-center text-<%= mBg %>">
                                <fmt:formatNumber value="<%= mPct %>"
                                                  maxFractionDigits="1"/>%
                            </td>
                            <td class="text-center">
                                <% String grade = (String) mRow.get("grade"); %>
                                <% if (grade != null && !grade.isEmpty()) { %>
                                <span class="badge bg-<%= mBg %> rounded-pill px-3">
                                    <%= grade %>
                                </span>
                                <% } else { %>
                                <span class="text-muted">—</span>
                                <% } %>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <% } } %>

    <% } /* end marks */ %>

    <!-- ================================================================
         PERFORMANCE REPORT
    ================================================================ -->
    <% if ("performance".equals(reportType)) {
        java.util.List<java.util.Map<String, Object>> perfSummary =
            (java.util.List<java.util.Map<String, Object>>)
            request.getAttribute("perfSummary");
        java.util.List<java.util.Map<String, Object>> attSummary =
            (java.util.List<java.util.Map<String, Object>>)
            request.getAttribute("attSummary");
    %>

    <!-- Charts Row -->
    <div class="row g-3 mb-4">
        <div class="col-lg-6">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">Marks Performance % per Subject</h6>
                </div>
                <div class="card-body" style="height:260px;">
                    <canvas id="perfMarksBar"></canvas>
                </div>
            </div>
        </div>
        <div class="col-lg-6">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">Attendance % per Subject</h6>
                </div>
                <div class="card-body" style="height:260px;">
                    <canvas id="perfAttBar"></canvas>
                </div>
            </div>
        </div>
    </div>

    <!-- Performance Table -->
    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-trophy me-2 text-warning"></i>
                Academic Performance Summary
            </h6>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">Subject</th>
                            <th class="text-center">Credits</th>
                            <th class="text-center">Obtained</th>
                            <th class="text-center">Max</th>
                            <th class="text-center">Overall %</th>
                            <th class="text-center">Best</th>
                            <th class="text-center">Lowest</th>
                            <th class="text-center">Grade</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (perfSummary == null || perfSummary.isEmpty()) { %>
                        <tr>
                            <td colspan="8" class="text-center py-4 text-muted">
                                No performance data available yet.
                            </td>
                        </tr>
                        <% } else {
                            for (java.util.Map<String, Object> row : perfSummary) {
                                double pct = (double) row.get("overallPct");
                                String bg  = pct >= 75 ? "success"
                                           : pct >= 50 ? "warning" : "danger";
                                String grade = com.smartattendance.dao.FacultyMarksDAO
                                    .computeGrade((double) row.get("totalObtained"),
                                                  (int)    row.get("totalMax"));
                        %>
                        <tr>
                            <td class="ps-4">
                                <div class="fw-medium"><%= row.get("subjectCode") %></div>
                                <div class="text-muted small"><%= row.get("subjectName") %></div>
                            </td>
                            <td class="text-center text-muted"><%= row.get("credits") %></td>
                            <td class="text-center fw-semibold">
                                <fmt:formatNumber value="<%= row.get("totalObtained") %>"
                                                  maxFractionDigits="1"/>
                            </td>
                            <td class="text-center text-muted"><%= row.get("totalMax") %></td>
                            <td class="text-center">
                                <span class="badge bg-<%= bg %> rounded-pill px-3">
                                    <fmt:formatNumber value="<%= pct %>"
                                                      maxFractionDigits="1"/>%
                                </span>
                            </td>
                            <td class="text-center text-success fw-medium">
                                <fmt:formatNumber value="<%= row.get("bestScore") %>"
                                                  maxFractionDigits="1"/>
                            </td>
                            <td class="text-center text-danger fw-medium">
                                <fmt:formatNumber value="<%= row.get("lowestScore") %>"
                                                  maxFractionDigits="1"/>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-<%= bg %> rounded-pill">
                                    <%= grade %>
                                </span>
                            </td>
                        </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <% } /* end performance */ %>

    <!-- ================================================================
         MONTHLY REPORT
    ================================================================ -->
    <% if ("monthly".equals(reportType)) {
        java.util.List<java.util.Map<String, Object>> monthlyAtt =
            (java.util.List<java.util.Map<String, Object>>)
            request.getAttribute("monthlyAttendance");
        String selMonthName = (String) request.getAttribute("selectedMonthName");
    %>

    <!-- Month/Year Filter -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-body p-3">
            <form method="GET"
                  action="<%= request.getContextPath() %>/student/reports">
                <input type="hidden" name="type" value="monthly">
                <div class="row g-3 align-items-end">
                    <div class="col-md-3">
                        <label class="form-label fw-medium small">Month</label>
                        <select name="month" class="form-select" style="border-radius:10px;">
                            <% for (int mo = 1; mo <= 12; mo++) { %>
                            <option value="<%= mo %>" <%= mo == selMonth ? "selected" : "" %>>
                                <%= java.time.Month.of(mo).name() %>
                            </option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label class="form-label fw-medium small">Year</label>
                        <select name="year" class="form-select" style="border-radius:10px;">
                            <% for (int y = curYear; y >= curYear - 4; y--) { %>
                            <option value="<%= y %>" <%= y == selYear ? "selected" : "" %>>
                                <%= y %>
                            </option>
                            <% } %>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <button type="submit" class="btn btn-primary rounded-pill w-100">
                            <i class="fas fa-search me-1"></i>Load
                        </button>
                    </div>
                    <div class="col-md-5 d-flex gap-2 flex-wrap">
                        <a href="<%= request.getContextPath()
                           %>/export/report?format=pdf&report=monthly&role=student
                           &month=<%= selMonth %>&year=<%= selYear %>"
                           class="btn btn-outline-danger rounded-pill">
                            <i class="fas fa-file-pdf me-1"></i>PDF
                        </a>
                        <a href="<%= request.getContextPath()
                           %>/export/report?format=excel&report=monthly&role=student
                           &month=<%= selMonth %>&year=<%= selYear %>"
                           class="btn btn-outline-success rounded-pill">
                            <i class="fas fa-file-excel me-1"></i>Excel
                        </a>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- Monthly Trend Chart -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">Monthly Attendance Trend</h6>
        </div>
        <div class="card-body" style="height:250px;">
            <canvas id="monthlyTrendChart"></canvas>
        </div>
    </div>

    <!-- Monthly Table -->
    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">
                <%= selMonthName %> <%= selYear %> — Attendance Summary
            </h6>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">Subject</th>
                            <th class="text-center">Total</th>
                            <th class="text-center">Present</th>
                            <th class="text-center">Absent</th>
                            <th class="text-center">Late</th>
                            <th class="text-center">Monthly %</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (monthlyAtt == null || monthlyAtt.isEmpty()) { %>
                        <tr>
                            <td colspan="6" class="text-center py-4 text-muted">
                                No attendance data for this month.
                            </td>
                        </tr>
                        <% } else {
                            for (java.util.Map<String, Object> row : monthlyAtt) {
                                double pct = (double) row.get("monthlyPct");
                                String bg  = pct >= 75 ? "success"
                                           : pct >= 50 ? "warning" : "danger";
                        %>
                        <tr>
                            <td class="ps-4">
                                <div class="fw-medium"><%= row.get("subjectCode") %></div>
                                <div class="text-muted small"><%= row.get("subjectName") %></div>
                            </td>
                            <td class="text-center"><%= row.get("total") %></td>
                            <td class="text-center text-success fw-medium">
                                <%= row.get("present") %>
                            </td>
                            <td class="text-center text-danger fw-medium">
                                <%= row.get("absent") %>
                            </td>
                            <td class="text-center text-warning fw-medium">
                                <%= row.get("late") %>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-<%= bg %> rounded-pill px-3">
                                    <fmt:formatNumber value="<%= pct %>"
                                                      maxFractionDigits="1"/>%
                                </span>
                            </td>
                        </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <% } /* end monthly */ %>

</main>

<!-- ── Chart.js Initialization ─────────────────────────────────── -->
<script>
document.addEventListener('DOMContentLoaded', function () {

    const chartDefaults = {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'top' } }
    };

    // ── Helper: get canvas context safely ─────────────────────────
    function getCtx(id) {
        const el = document.getElementById(id);
        return el ? el.getContext('2d') : null;
    }

    // ── Attendance Bar Chart ───────────────────────────────────────
    const attBarCtx = getCtx('attBarChart');
    if (attBarCtx) {
        new Chart(attBarCtx, {
            type: 'bar',
            data: {
                labels:   <%= attLabelsJson %>,
                datasets: [{
                    label:           'Attendance %',
                    data:            <%= attPctJson %>,
                    backgroundColor: 'rgba(13,110,253,0.7)',
                    borderRadius:    6,
                    borderSkipped:   false
                }]
            },
            options: {
                ...chartDefaults,
                scales: {
                    y: { beginAtZero:true, max:100,
                         ticks:{ callback: v => v+'%' } },
                    x: { grid:{ display:false } }
                }
            }
        });
    }

    // ── Trend Line Chart ───────────────────────────────────────────
    const trendCtx = getCtx('trendLine') || getCtx('attTrendChart')
                     || getCtx('monthlyTrendChart');
    if (trendCtx) {
        new Chart(trendCtx, {
            type: 'line',
            data: {
                labels:   <%= trendLabelsJson %>,
                datasets: [{
                    label:           'Attendance %',
                    data:            <%= trendPctJson %>,
                    borderColor:     'rgba(13,110,253,1)',
                    backgroundColor: 'rgba(13,110,253,0.1)',
                    tension:         0.4,
                    fill:            true,
                    pointRadius:     5,
                    pointHoverRadius:8
                }]
            },
            options: {
                ...chartDefaults,
                scales: {
                    y: { beginAtZero:true, max:100,
                         ticks:{ callback: v => v+'%' } },
                    x: { grid:{ color:'rgba(0,0,0,0.05)' } }
                }
            }
        });
    }

    // ── Overall Doughnut Chart ─────────────────────────────────────
    const doughCtx = getCtx('overallDoughnut');
    if (doughCtx) {
        <%
            java.util.Map<String, Integer> totDon =
                (java.util.Map<String, Integer>) request.getAttribute("overallTotals");
            int dP = totDon != null ? totDon.getOrDefault("present", 0) : 0;
            int dA = totDon != null ? totDon.getOrDefault("absent",  0) : 0;
            int dL = totDon != null ? totDon.getOrDefault("late",    0) : 0;
        %>
        new Chart(doughCtx, {
            type: 'doughnut',
            data: {
                labels: ['Present','Absent','Late'],
                datasets: [{
                    data: [<%= dP %>, <%= dA %>, <%= dL %>],
                    backgroundColor: [
                        'rgba(25,135,84,0.8)',
                        'rgba(220,53,69,0.8)',
                        'rgba(255,193,7,0.8)'
                    ],
                    borderWidth: 2,
                    borderColor: '#fff'
                }]
            },
            options: {
                responsive:          true,
                maintainAspectRatio: true,
                plugins: {
                    legend:{ position:'bottom' },
                    tooltip:{
                        callbacks:{
                            label: ctx => ' '+ctx.label+': '+ctx.parsed+' classes'
                        }
                    }
                }
            }
        });
    }

    // ── Marks Radar / Bar Chart ────────────────────────────────────
    const marksCtx = getCtx('marksRadarChart')
                     || getCtx('perfMarksBar');
    if (marksCtx) {
        new Chart(marksCtx, {
            type: 'radar',
            data: {
                labels:   <%= marksLabelsJson %>,
                datasets: [{
                    label:           'Marks %',
                    data:            <%= marksPctJson %>,
                    backgroundColor: 'rgba(255,193,7,0.2)',
                    borderColor:     'rgba(255,193,7,1)',
                    pointBackgroundColor: 'rgba(255,193,7,1)'
                }]
            },
            options: {
                responsive:          true,
                maintainAspectRatio: false,
                scales: {
                    r: { beginAtZero:true, max:100,
                         ticks:{ stepSize:25, callback: v => v+'%' } }
                }
            }
        });
    }

    // ── Performance Attendance Bar ─────────────────────────────────
    const perfAttCtx = getCtx('perfAttBar');
    if (perfAttCtx) {
        new Chart(perfAttCtx, {
            type: 'bar',
            data: {
                labels:   <%= attLabelsJson %>,
                datasets: [{
                    label:           'Attendance %',
                    data:            <%= attPctJson %>,
                    backgroundColor: 'rgba(25,135,84,0.7)',
                    borderRadius:    6,
                    borderSkipped:   false
                }]
            },
            options: {
                ...chartDefaults,
                scales: {
                    y: { beginAtZero:true, max:100,
                         ticks:{ callback: v => v+'%' } },
                    x: { grid:{ display:false } }
                }
            }
        });
    }
});
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>