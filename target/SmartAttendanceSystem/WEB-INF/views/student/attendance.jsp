<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    String pageTitle = "My Attendance";
    com.smartattendance.model.Student student =
        (com.smartattendance.model.Student) request.getAttribute("student");

    java.util.List<com.smartattendance.model.AttendanceSummary> summaries =
        (java.util.List<com.smartattendance.model.AttendanceSummary>)
        request.getAttribute("summaries");

    java.util.List<com.smartattendance.model.Subject> subjects =
        (java.util.List<com.smartattendance.model.Subject>)
        request.getAttribute("subjects");

    java.util.List<java.util.Map<String, Object>> detailRecords =
        (java.util.List<java.util.Map<String, Object>>)
        request.getAttribute("detailRecords");

    java.util.Map<String, Integer> overallTotals =
        (java.util.Map<String, Integer>) request.getAttribute("overallTotals");

    Double  overallPct   = (Double)  request.getAttribute("overallPercentage");
    Integer selectedSub  = (Integer) request.getAttribute("selectedSubjectId");
    Double  minRequired  = (Double)  request.getAttribute("minRequired");

    if (overallPct  == null) overallPct  = 0.0;
    if (selectedSub == null) selectedSub = 0;
    if (minRequired == null) minRequired = 75.0;

    int totalAll   = overallTotals != null ? overallTotals.getOrDefault("totalClassesAll",0) : 0;
    int presentAll = overallTotals != null ? overallTotals.getOrDefault("totalPresentAll",0) : 0;
    int absentAll  = overallTotals != null ? overallTotals.getOrDefault("totalAbsentAll", 0) : 0;
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp"  %>

<main class="container-fluid py-4 px-4">

    <!-- ─── Page Header ──────────────────────────────────────────── -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h4 class="fw-bold text-dark mb-0">
                <i class="fas fa-calendar-check me-2 text-primary"></i>
                My Attendance
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

    <!-- ─── Overall Stats Row ────────────────────────────────────── -->
    <div class="row g-3 mb-4">
        <% String badge = com.smartattendance.util.AttendanceCalculator
                             .getStatusBadgeClass(overallPct);
           String statusLabel = com.smartattendance.util.AttendanceCalculator
                             .getAttendanceStatus(overallPct); %>

        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center p-3" style="border-radius:14px;">
                <h3 class="fw-bold text-<%= badge %> mb-0">
                    <fmt:formatNumber value="<%= overallPct %>" maxFractionDigits="1"/>%
                </h3>
                <small class="text-muted">Overall</small>
                <div class="mt-1">
                    <span class="badge bg-<%= badge %>"><%= statusLabel %></span>
                </div>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center p-3" style="border-radius:14px;">
                <h3 class="fw-bold text-dark mb-0"><%= totalAll %></h3>
                <small class="text-muted">Total Classes</small>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center p-3" style="border-radius:14px;">
                <h3 class="fw-bold text-success mb-0"><%= presentAll %></h3>
                <small class="text-muted">Present</small>
            </div>
        </div>
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm text-center p-3" style="border-radius:14px;">
                <h3 class="fw-bold text-danger mb-0"><%= absentAll %></h3>
                <small class="text-muted">Absent</small>
            </div>
        </div>
    </div>

    <!-- ─── Subject-wise Summary Table ──────────────────────────── -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold text-dark mb-0">
                <i class="fas fa-table me-2 text-primary"></i>
                Subject-wise Summary
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
                            <th class="text-center">Percentage</th>
                            <th class="text-center">Status</th>
                            <th class="text-center">Details</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (summaries == null || summaries.isEmpty()) { %>
                        <tr>
                            <td colspan="8" class="text-center py-4 text-muted">
                                <i class="fas fa-folder-open me-2"></i>
                                No attendance records found.
                            </td>
                        </tr>
                        <% } else {
                            for (com.smartattendance.model.AttendanceSummary s : summaries) {
                                double  pct  = s.getAttendancePercentage();
                                String  bg   = com.smartattendance.util.AttendanceCalculator
                                                  .getStatusBadgeClass(pct);
                                String  stat = com.smartattendance.util.AttendanceCalculator
                                                  .getAttendanceStatus(pct);
                        %>
                        <tr>
                            <td class="ps-4">
                                <div class="fw-medium text-dark"><%= s.getSubjectCode() %></div>
                                <div class="text-muted small"><%= s.getSubjectName() %></div>
                            </td>
                            <td class="text-center fw-medium"><%= s.getTotalClasses() %></td>
                            <td class="text-center text-success fw-medium">
                                <%= s.getClassesPresent() %>
                            </td>
                            <td class="text-center text-danger fw-medium">
                                <%= s.getClassesAbsent() %>
                            </td>
                            <td class="text-center text-warning fw-medium">
                                <%= s.getClassesLate() %>
                            </td>
                            <td class="text-center">
                                <div class="d-flex align-items-center justify-content-center gap-2">
                                    <div class="progress flex-grow-1" style="height:8px;max-width:80px;">
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
                                    <%= stat %>
                                </span>
                            </td>
                            <td class="text-center">
                                <a href="<%= request.getContextPath() %>/student/attendance?subjectId=<%= s.getSubjectId() %>"
                                   class="btn btn-sm btn-outline-primary rounded-pill">
                                    <i class="fas fa-eye me-1"></i>View
                                </a>
                            </td>
                        </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

    <!-- ─── Detailed Records (when subject selected) ──────────────  -->
    <% if (selectedSub > 0 && detailRecords != null && !detailRecords.isEmpty()) { %>
    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4 d-flex
                    justify-content-between align-items-center">
            <h6 class="fw-bold text-dark mb-0">
                <i class="fas fa-list me-2 text-primary"></i>
                Detailed Attendance Records
            </h6>
            <a href="<%= request.getContextPath() %>/student/attendance"
               class="btn btn-sm btn-outline-secondary rounded-pill">
                <i class="fas fa-times me-1"></i>Clear Filter
            </a>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">#</th>
                            <th>Date</th>
                            <th>Subject</th>
                            <th class="text-center">Status</th>
                            <th>Remarks</th>
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            int rowNum = 1;
                            for (java.util.Map<String, Object> rec : detailRecords) {
                                String  st   = (String) rec.get("status");
                                String  dotC = "Present".equals(st) ? "success"
                                             : "Absent".equals(st)  ? "danger" : "warning";
                                java.time.LocalDate d =
                                    (java.time.LocalDate) rec.get("date");
                        %>
                        <tr>
                            <td class="ps-4 text-muted small"><%= rowNum++ %></td>
                            <td class="fw-medium">
                                <%= d != null
                                    ? com.smartattendance.util.DateUtil.formatForDisplay(d)
                                    : "—" %>
                            </td>
                            <td>
                                <span class="badge bg-light text-dark border me-1">
                                    <%= rec.get("subjectCode") %>
                                </span>
                                <span class="text-muted small"><%= rec.get("subjectName") %></span>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-<%= dotC %>-subtle
                                             text-<%= dotC %>
                                             border border-<%= dotC %> rounded-pill">
                                    <i class="fas <%= "Present".equals(st) ? "fa-check"
                                                   : "Absent".equals(st)  ? "fa-times"
                                                   : "fa-clock" %> me-1"></i>
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

</main>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>