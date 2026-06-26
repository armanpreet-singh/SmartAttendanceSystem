<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    String pageTitle = "Faculty Dashboard";
    com.smartattendance.model.Faculty faculty =
        (com.smartattendance.model.Faculty) request.getAttribute("faculty");
    if (faculty == null)
        faculty = (com.smartattendance.model.Faculty)
                  session.getAttribute("loggedInFaculty");

    Integer subjectCount = (Integer) request.getAttribute("subjectCount");
    Integer studentCount = (Integer) request.getAttribute("studentCount");
    Integer todayPresent = (Integer) request.getAttribute("todayPresent");
    Integer todayTotal   = (Integer) request.getAttribute("todayTotal");
    Double  todayPct     = (Double)  request.getAttribute("todayPct");
    String  chartLabels  = (String)  request.getAttribute("chartLabelsJson");
    String  chartData    = (String)  request.getAttribute("chartDataJson");

    java.util.List<com.smartattendance.model.Subject> subjects =
        (java.util.List<com.smartattendance.model.Subject>)
        request.getAttribute("subjects");

    java.util.List<java.util.Map<String, Object>> recentActivity =
        (java.util.List<java.util.Map<String, Object>>)
        request.getAttribute("recentActivity");

    if (subjectCount == null) subjectCount = 0;
    if (studentCount == null) studentCount = 0;
    if (todayPresent == null) todayPresent = 0;
    if (todayTotal   == null) todayTotal   = 0;
    if (todayPct     == null) todayPct     = 0.0;
    if (chartLabels  == null) chartLabels  = "[]";
    if (chartData    == null) chartData    = "[]";
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<main class="container-fluid py-4 px-4">

    <!-- ─── Welcome Banner ───────────────────────────────────────── -->
    <div class="row mb-4">
        <div class="col-12">
            <div class="card border-0 text-white"
                 style="background:linear-gradient(135deg,#e65100,#f57c00);
                        border-radius:16px;">
                <div class="card-body d-flex align-items-center gap-4 p-4">
                    <div class="rounded-circle bg-white bg-opacity-25 d-flex
                                align-items-center justify-content-center flex-shrink-0"
                         style="width:64px;height:64px;font-size:1.8rem;">
                        <i class="fas fa-chalkboard-teacher text-white"></i>
                    </div>
                    <div>
                        <h4 class="fw-bold mb-1">
                            Welcome, <%= faculty != null ? faculty.getFirstName() : "Faculty" %>!
                        </h4>
                        <p class="mb-0 opacity-75">
                            <i class="fas fa-id-badge me-1"></i>
                            <%= faculty != null ? faculty.getFacultyId() : "" %>
                            &nbsp;|&nbsp;
                            <i class="fas fa-briefcase me-1"></i>
                            <%= faculty != null ? faculty.getDesignation() : "" %>
                            &nbsp;|&nbsp;
                            <i class="fas fa-university me-1"></i>
                            <%= faculty != null ? faculty.getDepartmentName() : "" %>
                        </p>
                    </div>
                    <div class="ms-auto text-end d-none d-md-block">
                        <div class="text-white opacity-75 small">Today</div>
                        <div class="fw-semibold">
                            <%= com.smartattendance.util.DateUtil
                                    .formatForDisplay(java.time.LocalDate.now()) %>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ─── Summary Cards ────────────────────────────────────────── -->
    <div class="row g-3 mb-4">

        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100 text-center p-3"
                 style="border-radius:14px;">
                <div class="mx-auto mb-2 rounded-circle d-flex align-items-center
                            justify-content-center"
                     style="width:50px;height:50px;background:rgba(230,81,0,0.1);">
                    <i class="fas fa-book text-warning" style="font-size:1.3rem;"></i>
                </div>
                <h3 class="fw-bold text-warning mb-0"><%= subjectCount %></h3>
                <small class="text-muted">Subjects Assigned</small>
            </div>
        </div>

        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100 text-center p-3"
                 style="border-radius:14px;">
                <div class="mx-auto mb-2 rounded-circle d-flex align-items-center
                            justify-content-center"
                     style="width:50px;height:50px;background:rgba(13,110,253,0.1);">
                    <i class="fas fa-users text-primary" style="font-size:1.3rem;"></i>
                </div>
                <h3 class="fw-bold text-primary mb-0"><%= studentCount %></h3>
                <small class="text-muted">Total Students</small>
            </div>
        </div>

        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100 text-center p-3"
                 style="border-radius:14px;">
                <div class="mx-auto mb-2 rounded-circle d-flex align-items-center
                            justify-content-center"
                     style="width:50px;height:50px;background:rgba(25,135,84,0.1);">
                    <i class="fas fa-user-check text-success" style="font-size:1.3rem;"></i>
                </div>
                <h3 class="fw-bold text-success mb-0"><%= todayPresent %></h3>
                <small class="text-muted">Present Today</small>
            </div>
        </div>

        <div class="col-6 col-md-3">
            <div class="card border-0 shadow-sm h-100 text-center p-3"
                 style="border-radius:14px;">
                <div class="mx-auto mb-2 rounded-circle d-flex align-items-center
                            justify-content-center"
                     style="width:50px;height:50px;background:rgba(108,117,125,0.1);">
                    <i class="fas fa-percentage text-secondary" style="font-size:1.3rem;"></i>
                </div>
                <h3 class="fw-bold text-secondary mb-0">
                    <fmt:formatNumber value="<%= todayPct %>" maxFractionDigits="1"/>%
                </h3>
                <small class="text-muted">Today's Rate (<%= todayTotal %> marked)</small>
            </div>
        </div>

    </div>

    <!-- ─── Charts + Subjects Row ────────────────────────────────── -->
    <div class="row g-3 mb-4">

        <!-- Student Distribution Chart -->
        <div class="col-lg-5">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-chart-doughnut me-2 text-warning"></i>
                        Students per Subject
                    </h6>
                </div>
                <div class="card-body d-flex align-items-center justify-content-center"
                     style="min-height:250px;">
                    <canvas id="studentDistChart" width="300" height="250"></canvas>
                </div>
            </div>
        </div>

        <!-- Assigned Subjects Table -->
        <div class="col-lg-7">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4 d-flex
                            justify-content-between align-items-center">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-book-open me-2 text-warning"></i>
                        My Subjects
                    </h6>
                    <a href="<%= request.getContextPath() %>/faculty/mark-attendance"
                       class="btn btn-sm btn-outline-warning rounded-pill">
                        <i class="fas fa-plus me-1"></i> Mark Attendance
                    </a>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th class="ps-4">Code</th>
                                    <th>Subject Name</th>
                                    <th class="text-center">Sem</th>
                                    <th class="text-center">Students</th>
                                    <th class="text-center">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% if (subjects == null || subjects.isEmpty()) { %>
                                <tr>
                                    <td colspan="5" class="text-center py-4 text-muted">
                                        No subjects assigned.
                                    </td>
                                </tr>
                                <% } else {
                                    for (com.smartattendance.model.Subject sub : subjects) { %>
                                <tr>
                                    <td class="ps-4">
                                        <span class="badge bg-warning text-dark rounded-pill px-3">
                                            <%= sub.getSubjectCode() %>
                                        </span>
                                    </td>
                                    <td class="fw-medium text-dark"><%= sub.getSubjectName() %></td>
                                    <td class="text-center text-muted"><%= sub.getSemester() %></td>
                                    <td class="text-center">
                                        <span class="badge bg-primary rounded-pill">
                                            <%= sub.getStudentCount() %>
                                        </span>
                                    </td>
                                    <td class="text-center">
                                        <a href="<%= request.getContextPath()
                                                   %>/faculty/mark-attendance?action=mark&subjectId=<%= sub.getId() %>"
                                           class="btn btn-sm btn-outline-success rounded-pill me-1">
                                            <i class="fas fa-clipboard-check"></i>
                                        </a>
                                        <a href="<%= request.getContextPath()
                                                   %>/faculty/marks?subjectId=<%= sub.getId() %>"
                                           class="btn btn-sm btn-outline-primary rounded-pill">
                                            <i class="fas fa-pen"></i>
                                        </a>
                                    </td>
                                </tr>
                                <% } } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>

    </div>

    <!-- ─── Recent Attendance Activity ───────────────────────────── -->
    <div class="row g-3">
        <div class="col-12">
            <div class="card border-0 shadow-sm" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-history me-2 text-warning"></i>
                        Recent Attendance Activity
                    </h6>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th class="ps-4">Date</th>
                                    <th>Student</th>
                                    <th>Subject</th>
                                    <th class="text-center">Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                <% if (recentActivity == null || recentActivity.isEmpty()) { %>
                                <tr>
                                    <td colspan="4" class="text-center py-4 text-muted">
                                        No attendance records found.
                                    </td>
                                </tr>
                                <% } else {
                                    for (java.util.Map<String, Object> rec : recentActivity) {
                                        String st = (String) rec.get("status");
                                        String bg = "Present".equals(st) ? "success"
                                                  : "Absent".equals(st)  ? "danger" : "warning";
                                        java.time.LocalDate d =
                                            (java.time.LocalDate) rec.get("date");
                                %>
                                <tr>
                                    <td class="ps-4 text-muted small">
                                        <%= d != null
                                            ? com.smartattendance.util.DateUtil
                                                .formatForDisplay(d) : "—" %>
                                    </td>
                                    <td>
                                        <div class="fw-medium text-dark small">
                                            <%= rec.get("studentName") %>
                                        </div>
                                        <div class="text-muted" style="font-size:0.76rem;">
                                            <%= rec.get("studentCode") %>
                                        </div>
                                    </td>
                                    <td>
                                        <span class="badge bg-light text-dark border">
                                            <%= rec.get("subjectCode") %>
                                        </span>
                                        <span class="text-muted small ms-1">
                                            <%= rec.get("subjectName") %>
                                        </span>
                                    </td>
                                    <td class="text-center">
                                        <span class="badge bg-<%= bg %>-subtle
                                                     text-<%= bg %>
                                                     border border-<%= bg %> rounded-pill">
                                            <%= st %>
                                        </span>
                                    </td>
                                </tr>
                                <% } } %>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- ─── Quick Actions Row ─────────────────────────────────────── -->
    <div class="row mt-3">
        <div class="col-12">
            <div class="card border-0 shadow-sm p-3" style="border-radius:14px;">
                <div class="d-flex flex-wrap gap-2 justify-content-center
                            justify-content-md-start">
                    <a href="<%= request.getContextPath() %>/faculty/mark-attendance?action=mark"
                       class="btn btn-outline-success rounded-pill">
                        <i class="fas fa-clipboard-check me-2"></i>Mark Attendance
                    </a>
                    <a href="<%= request.getContextPath() %>/faculty/mark-attendance?action=report"
                       class="btn btn-outline-primary rounded-pill">
                        <i class="fas fa-file-alt me-2"></i>View Reports
                    </a>
                    <a href="<%= request.getContextPath() %>/faculty/marks"
                       class="btn btn-outline-warning rounded-pill">
                        <i class="fas fa-pen me-2"></i>Manage Marks
                    </a>
                    <a href="<%= request.getContextPath() %>/faculty/profile"
                       class="btn btn-outline-secondary rounded-pill">
                        <i class="fas fa-user-edit me-2"></i>My Profile
                    </a>
                </div>
            </div>
        </div>
    </div>

</main>

<script>
document.addEventListener('DOMContentLoaded', function () {
    const ctx = document.getElementById('studentDistChart').getContext('2d');
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels:   <%= chartLabels %>,
            datasets: [{
                data:            <%= chartData %>,
                backgroundColor: [
                    'rgba(230,81,0,0.8)',   'rgba(245,124,0,0.8)',
                    'rgba(251,140,0,0.8)',  'rgba(255,167,38,0.8)',
                    'rgba(255,193,7,0.8)',  'rgba(255,213,79,0.8)'
                ],
                borderWidth: 2,
                borderColor: '#fff'
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: true,
            plugins: {
                legend: { position: 'bottom', labels: { padding: 12, font: { size: 12 } } },
                tooltip: {
                    callbacks: {
                        label: ctx => ' ' + ctx.label + ': ' + ctx.parsed + ' students'
                    }
                }
            }
        }
    });
});
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>