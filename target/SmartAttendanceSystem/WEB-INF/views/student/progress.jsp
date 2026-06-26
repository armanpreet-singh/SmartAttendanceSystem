<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    String pageTitle = "Academic Progress";
    com.smartattendance.model.Student student =
        (com.smartattendance.model.Student) request.getAttribute("student");

    java.util.List<com.smartattendance.model.AttendanceSummary> summaries =
        (java.util.List<com.smartattendance.model.AttendanceSummary>)
        request.getAttribute("summaries");

    java.util.Map<String, Integer> classesNeededMap =
        (java.util.Map<String, Integer>) request.getAttribute("classesNeededMap");
    java.util.Map<String, Integer> canMissMap =
        (java.util.Map<String, Integer>) request.getAttribute("canMissMap");

    Integer defaulterCount = (Integer) request.getAttribute("defaulterCount");
    Double  overallPct     = (Double)  request.getAttribute("overallPct");
    Double  minRequired    = (Double)  request.getAttribute("minRequired");

    String attLabelsJson  = (String) request.getAttribute("attLabelsJson");
    String attDataJson    = (String) request.getAttribute("attDataJson");
    String attColorsJson  = (String) request.getAttribute("attColorsJson");
    String marksLabelsJson = (String) request.getAttribute("marksLabelsJson");
    String marksDataJson   = (String) request.getAttribute("marksDataJson");

    if (defaulterCount == null) defaulterCount = 0;
    if (overallPct     == null) overallPct     = 0.0;
    if (minRequired    == null) minRequired    = 75.0;
    if (attLabelsJson  == null) attLabelsJson  = "[]";
    if (attDataJson    == null) attDataJson    = "[]";
    if (attColorsJson  == null) attColorsJson  = "[]";
    if (marksLabelsJson == null) marksLabelsJson = "[]";
    if (marksDataJson   == null) marksDataJson   = "[]";

    String overallBadge = com.smartattendance.util.AttendanceCalculator
                            .getStatusBadgeClass(overallPct);
    boolean examEligible = com.smartattendance.util.AttendanceCalculator
                            .isEligibleForExam(overallPct);
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp"  %>

<main class="container-fluid py-4 px-4">

    <!-- ─── Page Header ──────────────────────────────────────────── -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h4 class="fw-bold text-dark mb-0">
                <i class="fas fa-chart-line me-2 text-info"></i>
                Academic Progress
            </h4>
            <small class="text-muted">
                Attendance analysis and academic performance overview
            </small>
        </div>
        <a href="<%= request.getContextPath() %>/student/dashboard"
           class="btn btn-sm btn-outline-secondary rounded-pill">
            <i class="fas fa-arrow-left me-1"></i> Dashboard
        </a>
    </div>

    <!-- ─── Exam Eligibility Banner ──────────────────────────────── -->
    <div class="alert <%= examEligible ? "alert-success" : "alert-danger" %>
                d-flex align-items-center gap-3 mb-4"
         style="border-radius:12px;" role="alert">
        <i class="fas <%= examEligible ? "fa-check-circle fa-2x" : "fa-times-circle fa-2x" %>"></i>
        <div>
            <strong>
                <%= examEligible ? "Exam Eligible!" : "At Risk — Not Exam Eligible!" %>
            </strong>
            <div class="small">
                <% if (examEligible) { %>
                    Your overall attendance of
                    <strong><fmt:formatNumber value="<%= overallPct %>" maxFractionDigits="1"/>%</strong>
                    meets the minimum <%= (int)minRequired.doubleValue() %>% requirement.
                <% } else { %>
                    Your overall attendance of
                    <strong><fmt:formatNumber value="<%= overallPct %>" maxFractionDigits="1"/>%</strong>
                    is below the minimum <%= (int)minRequired.doubleValue() %>% requirement.
                    You have <strong><%= defaulterCount %> defaulter subject(s)</strong>.
                <% } %>
            </div>
        </div>
    </div>

    <!-- ─── Charts Row ───────────────────────────────────────────── -->
    <div class="row g-3 mb-4">

        <!-- Attendance Bar Chart -->
        <div class="col-lg-6">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-chart-bar me-2 text-primary"></i>
                        Subject-wise Attendance (%)
                    </h6>
                </div>
                <div class="card-body d-flex align-items-center justify-content-center"
                     style="min-height:280px;">
                    <canvas id="attendanceChart" width="400" height="250"></canvas>
                </div>
            </div>
        </div>

        <!-- Marks Performance Chart -->
        <div class="col-lg-6">
            <div class="card border-0 shadow-sm h-100" style="border-radius:14px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold mb-0">
                        <i class="fas fa-chart-radar me-2 text-warning"></i>
                        Subject-wise Marks Performance (%)
                    </h6>
                </div>
                <div class="card-body d-flex align-items-center justify-content-center"
                     style="min-height:280px;">
                    <canvas id="marksChart" width="400" height="250"></canvas>
                </div>
            </div>
        </div>

    </div>

    <!-- ─── Advisory Table ───────────────────────────────────────── -->
    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-lightbulb me-2 text-warning"></i>
                Subject-wise Advisory
            </h6>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">Subject</th>
                            <th class="text-center">Attendance %</th>
                            <th class="text-center">Status</th>
                            <th class="text-center">Classes Needed</th>
                            <th class="text-center">Can Miss</th>
                            <th>Advisory</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (summaries == null || summaries.isEmpty()) { %>
                        <tr>
                            <td colspan="6" class="text-center py-4 text-muted">
                                No attendance data available.
                            </td>
                        </tr>
                        <% } else {
                            for (com.smartattendance.model.AttendanceSummary s : summaries) {
                                double pct    = s.getAttendancePercentage();
                                String bg     = com.smartattendance.util.AttendanceCalculator
                                                   .getStatusBadgeClass(pct);
                                String stat   = com.smartattendance.util.AttendanceCalculator
                                                   .getAttendanceStatus(pct);
                                int needed    = classesNeededMap != null
                                               ? classesNeededMap.getOrDefault(s.getSubjectCode(), 0) : 0;
                                int canMiss   = canMissMap != null
                                               ? canMissMap.getOrDefault(s.getSubjectCode(), 0) : 0;
                        %>
                        <tr>
                            <td class="ps-4">
                                <div class="fw-medium text-dark"><%= s.getSubjectCode() %></div>
                                <div class="text-muted small"><%= s.getSubjectName() %></div>
                            </td>
                            <td class="text-center">
                                <span class="fw-bold text-<%= bg %>">
                                    <fmt:formatNumber value="<%= pct %>"
                                                      maxFractionDigits="1"/>%
                                </span>
                            </td>
                            <td class="text-center">
                                <span class="badge bg-<%= bg %> rounded-pill">
                                    <%= stat %>
                                </span>
                            </td>
                            <td class="text-center">
                                <% if (needed == 0) { %>
                                    <span class="text-success fw-medium">
                                        <i class="fas fa-check me-1"></i>Met
                                    </span>
                                <% } else { %>
                                    <span class="text-danger fw-medium"><%= needed %></span>
                                <% } %>
                            </td>
                            <td class="text-center">
                                <% if (pct < 75.0) { %>
                                    <span class="text-danger">0</span>
                                <% } else { %>
                                    <span class="text-success fw-medium"><%= canMiss %></span>
                                <% } %>
                            </td>
                            <td>
                                <small class="<%= needed > 0 ? "text-danger" : "text-success" %>">
                                    <% if (needed > 0) { %>
                                        <i class="fas fa-exclamation-triangle me-1"></i>
                                        Attend <%= needed %> more class(es) to reach 75%.
                                    <% } else if (canMiss > 0) { %>
                                        <i class="fas fa-check-circle me-1"></i>
                                        You can miss up to <%= canMiss %> more class(es).
                                    <% } else { %>
                                        <i class="fas fa-info-circle me-1"></i>
                                        Maintain your attendance.
                                    <% } %>
                                </small>
                            </td>
                        </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>

</main>

<!-- ── Chart.js Initialization ─────────────────────────────────── -->
<script>
document.addEventListener('DOMContentLoaded', function () {

    // ── Attendance Bar Chart ────────────────────────────────────
    const attCtx = document.getElementById('attendanceChart').getContext('2d');
    new Chart(attCtx, {
        type: 'bar',
        data: {
            labels:   <%= attLabelsJson %>,
            datasets: [{
                label:           'Attendance %',
                data:            <%= attDataJson %>,
                backgroundColor: <%= attColorsJson %>,
                borderRadius:    8,
                borderSkipped:   false
            }]
        },
        options: {
            responsive:          true,
            maintainAspectRatio: true,
            plugins: {
                legend: { display: false },
                tooltip: {
                    callbacks: {
                        label: ctx => ' ' + ctx.parsed.y.toFixed(1) + '%'
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    max:         100,
                    ticks: { callback: v => v + '%' },
                    grid: { color: 'rgba(0,0,0,0.05)' }
                },
                x: { grid: { display: false } }
            }
        }
    });

    // ── Marks Radar/Bar Chart ────────────────────────────────────
    const marksCtx = document.getElementById('marksChart').getContext('2d');
    new Chart(marksCtx, {
        type: 'radar',
        data: {
            labels:   <%= marksLabelsJson %>,
            datasets: [{
                label:           'Marks %',
                data:            <%= marksDataJson %>,
                backgroundColor: 'rgba(255,193,7,0.2)',
                borderColor:     'rgba(255,193,7,1)',
                pointBackgroundColor: 'rgba(255,193,7,1)',
                pointBorderColor:     '#fff',
                pointHoverBackgroundColor: '#fff',
                pointHoverBorderColor:     'rgba(255,193,7,1)'
            }]
        },
        options: {
            responsive:          true,
            maintainAspectRatio: true,
            scales: {
                r: {
                    beginAtZero: true,
                    max:         100,
                    ticks: {
                        stepSize: 25,
                        callback: v => v + '%'
                    }
                }
            },
            plugins: {
                legend: { display: true, position: 'top' },
                tooltip: {
                    callbacks: {
                        label: ctx => ' ' + ctx.parsed.r.toFixed(1) + '%'
                    }
                }
            }
        }
    });
});
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>