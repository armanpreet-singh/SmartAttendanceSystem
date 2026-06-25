<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    String pageTitle = "Student Dashboard";
    com.smartattendance.model.Student student =
        (com.smartattendance.model.Student) request.getAttribute("student");
    if (student == null)
        student = (com.smartattendance.model.Student)
                  session.getAttribute("loggedInStudent");

    java.util.List<com.smartattendance.model.AttendanceSummary> summaries =
        (java.util.List<com.smartattendance.model.AttendanceSummary>)
        request.getAttribute("attendanceSummaries");

    java.util.List<java.util.Map<String, Object>> recentAttendance =
        (java.util.List<java.util.Map<String, Object>>)
        request.getAttribute("recentAttendance");

    Double  overallPct    = (Double)  request.getAttribute("overallPercentage");
    String  overallStatus = (String)  request.getAttribute("overallStatus");
    String  overallBadge  = (String)  request.getAttribute("overallBadge");
    Integer subjectCount  = (Integer) request.getAttribute("subjectCount");
    Integer defaulterCnt  = (Integer) request.getAttribute("defaulterCount");
    Double  minRequired   = (Double)  request.getAttribute("minRequired");

    if (overallPct   == null) overallPct   = 0.0;
    if (subjectCount == null) subjectCount = 0;
    if (defaulterCnt == null) defaulterCnt = 0;
    if (minRequired  == null) minRequired  = 75.0;
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp"  %>

<!-- ================================================================
     Student Dashboard Content
================================================================ -->
<main class="container-fluid py-4 px-4">

    <!-- ─── Welcome Banner ───────────────────────────────────────── -->
    <div class="row mb-4">
        <div class="col-12">
            <div class="welcome-banner card border-0 text-white"
                 style="background: linear-gradient(135deg, #1a237e 0%, #3949ab 100%);
                        border-radius: 16px;">
                <div class="card-body d-flex align-items-center gap-4 p-4">
                    <div class="avatar-circle bg-white bg-opacity-25 rounded-circle
                                d-flex align-items-center justify-content-center flex-shrink-0"
                         style="width:64px;height:64px;font-size:1.8rem;">
                        <i class="fas fa-user-graduate text-white"></i>
                    </div>
                    <div>
                        <h4 class="fw-bold mb-1">
                            Welcome back, <%= student != null ? student.getFirstName() : "Student" %>!
                        </h4>
                        <p class="mb-0 opacity-75">
                            <i class="fas fa-id-badge me-1"></i>
                            <%= student != null ? student.getStudentId() : "" %>
                            &nbsp;|&nbsp;
                            <i class="fas fa-layer-group me-1"></i>
                            <%= student != null ? student.getSemesterDisplay() : "" %>
                            &nbsp;|&nbsp;
                            <i class="fas fa-door-open me-1"></i>
                            Section <%= student != null ? student.getSection() : "" %>
                        </p>
                    </div>
                    <div class="ms-auto text-end d-none d-md-block">
                        <div class="text-white opacity-75 small">Academic Year</div>
                        <div class="fw-semibold">
                            <%= student != null ? student.getAcademicYearLabel() : "" %>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ─── Summary Cards ────────────────────────────────────────── -->
    <div class="row g-3 mb-4">

        <!-- Overall Attendance -->
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-body text-center p-3">
                    <div class="stat-icon mx-auto mb-2 rounded-circle d-flex
                                align-items-center justify-content-center"
                         style="width:50px;height:50px;
                                background:rgba(25,135,84,0.1);font-size:1.3rem;">
                        <i class="fas fa-chart-pie text-success"></i>
                    </div>
                    <h3 class="fw-bold mb-0 text-<%= overallBadge %>">
                        <fmt:formatNumber value="<%= overallPct %>" maxFractionDigits="1"/>%
                    </h3>
                    <small class="text-muted">Overall Attendance</small>
                    <div class="mt-1">
                        <span class="badge bg-<%= overallBadge %> rounded-pill">
                            <%= overallStatus %>
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <!-- Enrolled Subjects -->
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-body text-center p-3">
                    <div class="stat-icon mx-auto mb-2 rounded-circle d-flex
                                align-items-center justify-content-center"
                         style="width:50px;height:50px;
                                background:rgba(13,110,253,0.1);font-size:1.3rem;">
                        <i class="fas fa-book text-primary"></i>
                    </div>
                    <h3 class="fw-bold mb-0 text-primary"><%= subjectCount %></h3>
                    <small class="text-muted">Subjects Enrolled</small>
                </div>
            </div>
        </div>

        <!-- Attendance OK Subjects -->
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-body text-center p-3">
                    <div class="stat-icon mx-auto mb-2 rounded-circle d-flex
                                align-items-center justify-content-center"
                         style="width:50px;height:50px;
                                background:rgba(25,135,84,0.1);font-size:1.3rem;">
                        <i class="fas fa-check-circle text-success"></i>
                    </div>
                    <h3 class="fw-bold mb-0 text-success">
                        <%= subjectCount - defaulterCnt %>
                    </h3>
                    <small class="text-muted">Subjects OK</small>
                </div>
            </div>
        </div>

        <!-- Defaulter Subjects -->
        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-body text-center p-3">
                    <div class="stat-icon mx-auto mb-2 rounded-circle d-flex
                                align-items-center justify-content-center"
                         style="width:50px;height:50px;
                                background:rgba(220,53,69,0.1);font-size:1.3rem;">
                        <i class="fas fa-exclamation-triangle text-danger"></i>
                    </div>
                    <h3 class="fw-bold mb-0 text-danger"><%= defaulterCnt %></h3>
                    <small class="text-muted">
                        Below <%= (int)minRequired.doubleValue() %>%
                    </small>
                </div>
            </div>
        </div>

    </div>
    <!-- ── End Summary Cards ─────────────────────────────────────── -->

    <!-- ─── Subject-wise Attendance Cards + Recent Activity ─────── -->
    <div class="row g-3">

        <!-- Subject-wise Attendance -->
        <div class="col-lg-7">
            <div class="card border-0 shadow-sm" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <div class="d-flex justify-content-between align-items-center">
                        <h6 class="fw-bold text-dark mb-0">
                            <i class="fas fa-calendar-alt me-2 text-primary"></i>
                            Subject-wise Attendance
                        </h6>
                        <a href="<%= request.getContextPath() %>/student/attendance"
                           class="btn btn-sm btn-outline-primary rounded-pill">
                            View All <i class="fas fa-arrow-right ms-1"></i>
                        </a>
                    </div>
                </div>
                <div class="card-body px-4 py-3">
                    <% if (summaries == null || summaries.isEmpty()) { %>
                    <div class="text-center py-4 text-muted">
                        <i class="fas fa-folder-open fa-2x mb-2"></i>
                        <p class="mb-0">No attendance records found.</p>
                    </div>
                    <% } else {
                        for (com.smartattendance.model.AttendanceSummary s : summaries) {
                            double pct    = s.getAttendancePercentage();
                            String badge  = com.smartattendance.util.AttendanceCalculator
                                              .getStatusBadgeClass(pct);
                            String status = com.smartattendance.util.AttendanceCalculator
                                              .getAttendanceStatus(pct);
                    %>
                    <div class="subject-att-row mb-3">
                        <div class="d-flex justify-content-between align-items-center mb-1">
                            <div>
                                <span class="fw-medium text-dark small">
                                    <%= s.getSubjectCode() %>
                                </span>
                                <span class="text-muted small ms-2">
                                    <%= s.getSubjectName() %>
                                </span>
                            </div>
                            <div class="d-flex align-items-center gap-2">
                                <span class="text-muted small">
                                    <%= s.getClassesPresent() %>/<%= s.getTotalClasses() %>
                                </span>
                                <span class="badge bg-<%= badge %> rounded-pill small">
                                    <fmt:formatNumber value="<%= pct %>" maxFractionDigits="1"/>%
                                </span>
                            </div>
                        </div>
                        <div class="progress" style="height:7px;border-radius:4px;">
                            <div class="progress-bar bg-<%= badge %>"
                                 role="progressbar"
                              style="width: <%= Math.min(pct,100.0) %>%"
                                 aria-valuenow="<%= pct %>"
                                 aria-valuemin="0" aria-valuemax="100">
                            </div>
                        </div>
                        <% if ("Defaulter".equals(status) || "Critical".equals(status)) { %>
                        <small class="text-danger mt-1 d-block">
                            <i class="fas fa-exclamation-circle me-1"></i>
                            <%
                                int needed = com.smartattendance.util.AttendanceCalculator
                                    .classesNeededToReachTarget(
                                        s.getTotalClasses(), s.getClassesPresent(), 75.0);
                            %>
                            Attend <%= needed %> more consecutive class(es) to reach 75%.
                        </small>
                        <% } %>
                    </div>
                    <% } } %>
                </div>
            </div>
        </div>
        <!-- ── End Subject-wise Attendance ───────────────────────── -->

        <!-- Recent Attendance Activity -->
        <div class="col-lg-5">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold text-dark mb-0">
                        <i class="fas fa-history me-2 text-primary"></i>
                        Recent Attendance
                    </h6>
                </div>
                <div class="card-body px-4 py-3">
                    <% if (recentAttendance == null || recentAttendance.isEmpty()) { %>
                    <div class="text-center py-4 text-muted">
                        <i class="fas fa-folder-open fa-2x mb-2"></i>
                        <p class="mb-0">No recent records.</p>
                    </div>
                    <% } else {
                        for (java.util.Map<String, Object> rec : recentAttendance) {
                            String status = (String) rec.get("status");
                            String dotClass = "Present".equals(status) ? "success"
                                            : "Absent".equals(status)  ? "danger" : "warning";
                            java.time.LocalDate date =
                                (java.time.LocalDate) rec.get("date");
                    %>
                   <div class="d-flex align-items-center gap-3 mb-3">
    <div class="status-dot rounded-circle flex-shrink-0"
         style="width:10px; height:10px; background-color:var(--bs-<%= dotClass %>);">
    </div>
                        <div class="flex-grow-1">
                            <div class="small fw-medium text-dark">
                                <%= rec.get("subjectCode") %>
                                <span class="text-muted fw-normal">
                                    – <%= rec.get("subjectName") %>
                                </span>
                            </div>
                            <div class="text-muted" style="font-size:0.77rem;">
                                <%= date != null
                                    ? com.smartattendance.util.DateUtil.formatForDisplay(date)
                                    : "N/A" %>
                            </div>
                        </div>
                        <span class="badge bg-<%= dotClass %>-subtle
                                     text-<%= dotClass %> border border-<%= dotClass %>
                                     rounded-pill small">
                            <%= status %>
                        </span>
                    </div>
                    <% } } %>
                </div>
            </div>
        </div>

    </div>
    <!-- ── End Row ────────────────────────────────────────────────── -->

    <!-- ─── Quick Action Links ───────────────────────────────────── -->
    <div class="row g-3 mt-1">
        <div class="col-12">
            <div class="card border-0 shadow-sm" style="border-radius:14px;">
                <div class="card-body p-3">
                    <div class="d-flex flex-wrap gap-2 justify-content-center
                                justify-content-md-start">

                        <a href="<%= request.getContextPath() %>/student/attendance"
                           class="btn btn-outline-primary rounded-pill">
                            <i class="fas fa-calendar-check me-2"></i>View Attendance
                        </a>
                        <a href="<%= request.getContextPath() %>/student/marks"
                           class="btn btn-outline-success rounded-pill">
                            <i class="fas fa-star me-2"></i>View Marks
                        </a>
                        <a href="<%= request.getContextPath() %>/student/progress"
                           class="btn btn-outline-info rounded-pill">
                            <i class="fas fa-chart-line me-2"></i>My Progress
                        </a>
                        <a href="<%= request.getContextPath() %>/student/profile"
                           class="btn btn-outline-secondary rounded-pill">
                            <i class="fas fa-user-edit me-2"></i>My Profile
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>

</main>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>