<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    String pageTitle = "Attendance Management";
    com.smartattendance.model.Faculty faculty =
        (com.smartattendance.model.Faculty) request.getAttribute("faculty");

    String mode              = (String)  request.getAttribute("mode");
    Integer selectedSubId    = (Integer) request.getAttribute("selectedSubjectId");
    String  selectedDate     = (String)  request.getAttribute("selectedDate");
    String  selectedSection  = (String)  request.getAttribute("selectedSection");
    Boolean isEdit           = (Boolean) request.getAttribute("isEdit");
    String  errorMessage     = (String)  request.getAttribute("errorMessage");
    String  savedParam       = request.getParameter("saved");

    java.util.List<com.smartattendance.model.Subject> subjects =
        (java.util.List<com.smartattendance.model.Subject>)
        request.getAttribute("subjects");

    java.util.List<com.smartattendance.model.Student> students =
        (java.util.List<com.smartattendance.model.Student>)
        request.getAttribute("students");

    java.util.Map<Integer, String> existingMap =
        (java.util.Map<Integer, String>) request.getAttribute("existingMap");

    java.util.List<java.util.Map<String, Object>> report =
        (java.util.List<java.util.Map<String, Object>>)
        request.getAttribute("report");

    java.util.List<java.time.LocalDate> attendanceDates =
        (java.util.List<java.time.LocalDate>) request.getAttribute("attendanceDates");

    String reportLabels  = (String) request.getAttribute("reportLabels");
    String reportPresent = (String) request.getAttribute("reportPresent");
    String reportAbsent  = (String) request.getAttribute("reportAbsent");
    java.util.List<String> sections =
        (java.util.List<String>) request.getAttribute("sections");

    if (mode        == null) mode        = "select";
    if (selectedDate == null) selectedDate = com.smartattendance.util.DateUtil.todayAsHtmlDate();
    if (isEdit      == null) isEdit      = false;
    if (reportLabels  == null) reportLabels  = "[]";
    if (reportPresent == null) reportPresent = "[]";
    if (reportAbsent  == null) reportAbsent  = "[]";
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<main class="container-fluid py-4 px-4">

    <!-- ─── Page Header ──────────────────────────────────────────── -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h4 class="fw-bold text-dark mb-0">
                <i class="fas fa-clipboard-check me-2 text-success"></i>
                Attendance Management
            </h4>
            <small class="text-muted">
                Mark, edit, and view student attendance records
            </small>
        </div>
        <a href="<%= request.getContextPath() %>/faculty/dashboard"
           class="btn btn-sm btn-outline-secondary rounded-pill">
            <i class="fas fa-arrow-left me-1"></i> Dashboard
        </a>
    </div>

    <!-- ─── Alerts ───────────────────────────────────────────────── -->
    <% if (savedParam != null) { %>
    <div class="alert alert-success alert-dismissible fade show d-flex gap-2"
         role="alert">
        <i class="fas fa-check-circle"></i>
        <div><strong><%= savedParam %></strong> attendance record(s) saved successfully.</div>
        <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
    </div>
    <% } %>
    <% if (errorMessage != null) { %>
    <div class="alert alert-danger alert-dismissible fade show d-flex gap-2" role="alert">
        <i class="fas fa-exclamation-circle"></i>
        <div><%= errorMessage %></div>
        <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
    </div>
    <% } %>

    <!-- ─── Subject + Action Selection Form ──────────────────────── -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-filter me-2 text-success"></i>
                Select Subject & Action
            </h6>
        </div>
        <div class="card-body p-4">
            <form method="GET"
                  action="<%= request.getContextPath() %>/faculty/mark-attendance"
                  id="filterForm">
                <div class="row g-3 align-items-end">

                    <!-- Subject -->
                    <div class="col-md-4">
                        <label class="form-label fw-medium small">
                            <i class="fas fa-book me-1 text-muted"></i>Subject
                        </label>
                        <select name="subjectId" class="form-select"
                                style="border-radius:10px;" id="subjectSelect" required>
                            <option value="">-- Select Subject --</option>
                            <% if (subjects != null) {
                                for (com.smartattendance.model.Subject sub : subjects) { %>
                            <option value="<%= sub.getId() %>"
                                <%= (selectedSubId != null && sub.getId() == selectedSubId)
                                    ? "selected" : "" %>>
                                <%= sub.getSubjectCode() %> – <%= sub.getSubjectName() %>
                            </option>
                            <% } } %>
                        </select>
                    </div>

                    <!-- Date -->
                    <div class="col-md-3">
                        <label class="form-label fw-medium small">
                            <i class="fas fa-calendar me-1 text-muted"></i>Date
                        </label>
                        <input type="date" name="date" class="form-control"
                               style="border-radius:10px;"
                               value="<%= selectedDate %>"
                               max="<%= com.smartattendance.util.DateUtil.todayAsHtmlDate() %>">
                    </div>

                    <!-- Section -->
                    <div class="col-md-2">
                        <label class="form-label fw-medium small">
                            <i class="fas fa-door-open me-1 text-muted"></i>Section
                        </label>
                        <select name="section" class="form-select" style="border-radius:10px;">
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

                    <!-- Action Buttons -->
                    <div class="col-md-3 d-flex gap-2">
                        <button type="submit" name="action" value="mark"
                                class="btn btn-success rounded-pill flex-fill">
                            <i class="fas fa-clipboard-check me-1"></i>Mark
                        </button>
                        <button type="submit" name="action" value="report"
                                class="btn btn-outline-primary rounded-pill flex-fill">
                            <i class="fas fa-chart-bar me-1"></i>Report
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- ================================================================
         MARK MODE: Student List for Marking
    ================================================================ -->
    <% if ("mark".equals(mode) && students != null && !students.isEmpty()) { %>
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4 d-flex
                    justify-content-between align-items-center flex-wrap gap-2">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-users me-2 text-success"></i>
                Mark Attendance — <%= isEdit ? "Edit Mode" : "New Entry" %>
                <span class="badge bg-<%= isEdit ? "warning text-dark" : "success" %> ms-2 small">
                    <%= isEdit ? "Editing" : "New" %>
                </span>
            </h6>
            <div class="d-flex gap-2">
                <button type="button" class="btn btn-sm btn-outline-success rounded-pill"
                        onclick="markAll('Present')">
                    <i class="fas fa-check me-1"></i>All Present
                </button>
                <button type="button" class="btn btn-sm btn-outline-danger rounded-pill"
                        onclick="markAll('Absent')">
                    <i class="fas fa-times me-1"></i>All Absent
                </button>
            </div>
        </div>
        <div class="card-body p-0">
            <form method="POST"
                  action="<%= request.getContextPath() %>/faculty/mark-attendance"
                  id="attendanceForm">

                <input type="hidden" name="subjectId"      value="<%= selectedSubId %>">
                <input type="hidden" name="attendanceDate" value="<%= selectedDate %>">

                <!-- Search bar -->
                <div class="px-4 pt-3 pb-2">
                    <input type="text" id="studentSearch"
                           class="form-control form-control-sm"
                           placeholder="🔍 Search student by name or ID..."
                           style="border-radius:20px;max-width:320px;"
                           onkeyup="filterStudents()">
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0" id="studentTable">
                        <thead class="table-light">
                            <tr>
                                <th class="ps-4">#</th>
                                <th>Student ID</th>
                                <th>Name</th>
                                <th class="text-center">Section</th>
                                <th class="text-center">Status</th>
                                <th>Remarks</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% int sno = 1;
                               for (com.smartattendance.model.Student stu : students) {
                                   String existing = (existingMap != null)
                                       ? existingMap.getOrDefault(stu.getId(), "Present")
                                       : "Present";
                            %>
                            <tr class="student-row">
                                <td class="ps-4 text-muted small"><%= sno++ %></td>
                                <td>
                                    <span class="badge bg-light text-dark border student-id-cell">
                                        <%= stu.getStudentId() %>
                                    </span>
                                </td>
                                <td class="fw-medium text-dark student-name-cell">
                                    <%= stu.getFullName() %>
                                </td>
                                <td class="text-center text-muted"><%= stu.getSection() %></td>
                                <td class="text-center" style="min-width:180px;">
                                    <div class="btn-group" role="group">

                                        <input type="radio"
                                               class="btn-check"
                                               name="status_<%= stu.getId() %>"
                                               id="present_<%= stu.getId() %>"
                                               value="Present"
                                               <%= "Present".equals(existing) ? "checked" : "" %>>
                                        <label class="btn btn-sm btn-outline-success"
                                               for="present_<%= stu.getId() %>">
                                            <i class="fas fa-check me-1"></i>P
                                        </label>

                                        <input type="radio"
                                               class="btn-check"
                                               name="status_<%= stu.getId() %>"
                                               id="absent_<%= stu.getId() %>"
                                               value="Absent"
                                               <%= "Absent".equals(existing) ? "checked" : "" %>>
                                        <label class="btn btn-sm btn-outline-danger"
                                               for="absent_<%= stu.getId() %>">
                                            <i class="fas fa-times me-1"></i>A
                                        </label>

                                        <input type="radio"
                                               class="btn-check"
                                               name="status_<%= stu.getId() %>"
                                               id="late_<%= stu.getId() %>"
                                               value="Late"
                                               <%= "Late".equals(existing) ? "checked" : "" %>>
                                        <label class="btn btn-sm btn-outline-warning"
                                               for="late_<%= stu.getId() %>">
                                            <i class="fas fa-clock me-1"></i>L
                                        </label>

                                    </div>
                                </td>
                                <td>
                                    <input type="text"
                                           class="form-control form-control-sm"
                                           name="remarks_<%= stu.getId() %>"
                                           placeholder="Remarks (optional)"
                                           style="border-radius:8px;font-size:0.8rem;
                                                  max-width:180px;">
                                </td>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>

                <!-- Submit Bar -->
                <div class="px-4 py-3 border-top d-flex gap-3 align-items-center">
                    <button type="submit" class="btn btn-success rounded-pill px-4">
                        <i class="fas fa-save me-2"></i>
                        <%= isEdit ? "Update Attendance" : "Save Attendance" %>
                    </button>
                    <span class="text-muted small">
                        <i class="fas fa-info-circle me-1"></i>
                        Total: <strong><%= students.size() %></strong> students
                    </span>
                </div>

            </form>
        </div>
    </div>
    <% } else if ("mark".equals(mode) && (students == null || students.isEmpty())) { %>
    <div class="alert alert-info d-flex align-items-center gap-2" role="alert"
         style="border-radius:12px;">
        <i class="fas fa-info-circle fa-lg"></i>
        <div>No students enrolled in this subject.</div>
    </div>
    <% } %>

    <!-- ================================================================
         REPORT MODE: Attendance Summary Report
    ================================================================ -->
    <% if ("report".equals(mode)) { %>

    <!-- Attendance Chart -->
    <% if (report != null && !report.isEmpty()) { %>
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-chart-bar me-2 text-primary"></i>
                Attendance Distribution Chart
            </h6>
        </div>
        <div class="card-body" style="height:280px;">
            <canvas id="reportChart"></canvas>
        </div>
    </div>
    <% } %>

    <!-- Report Table -->
    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4 d-flex
                    justify-content-between align-items-center">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-table me-2 text-primary"></i>
                Attendance Report
            </h6>
            <!-- Search -->
            <input type="text" id="reportSearch"
                   class="form-control form-control-sm"
                   placeholder="🔍 Search student..."
                   style="border-radius:20px;max-width:220px;"
                   onkeyup="filterReport()">
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0" id="reportTable">
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
                        </tr>
                    </thead>
                    <tbody>
                        <% if (report == null || report.isEmpty()) { %>
                        <tr>
                            <td colspan="9" class="text-center py-4 text-muted">
                                No attendance records found.
                            </td>
                        </tr>
                        <% } else {
                            int rn = 1;
                            for (java.util.Map<String, Object> row : report) {
                                double pct    = (double) row.get("percentage");
                                String bg     = com.smartattendance.util.AttendanceCalculator
                                                   .getStatusBadgeClass(pct);
                                String stat   = com.smartattendance.util.AttendanceCalculator
                                                   .getAttendanceStatus(pct);
                        %>
                        <tr class="report-row">
                            <td class="ps-4 text-muted small"><%= rn++ %></td>
                            <td>
                                <div class="fw-medium text-dark report-name">
                                    <%= row.get("studentName") %>
                                </div>
                                <div class="text-muted report-code"
                                     style="font-size:0.77rem;">
                                    <%= row.get("studentCode") %>
                                </div>
                            </td>
                            <td class="text-center text-muted small">
                                <%= row.get("section") %>
                            </td>
                            <td class="text-center fw-medium">
                                <%= row.get("totalClasses") %>
                            </td>
                            <td class="text-center text-success fw-medium">
                                <%= row.get("classesPresent") %>
                            </td>
                            <td class="text-center text-danger fw-medium">
                                <%= row.get("classesAbsent") %>
                            </td>
                            <td class="text-center text-warning fw-medium">
                                <%= row.get("classesLate") %>
                            </td>
                            <td class="text-center">
                                <div class="d-flex align-items-center justify-content-center gap-2">
                                    <div class="progress flex-grow-1"
                                         style="height:8px;max-width:70px;">
                                        <div class="progress-bar bg-<%= bg %>"
                                             style="width:<%= Math.min(pct,100.0) %>%">
                                        </div>
                                    </div>
                                    <span class="fw-semibold text-<%= bg %>"
                                          style="min-width:42px;">
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
                        </tr>
                        <% } } %>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    <% } %>

</main>

<script>
    // ── Mark All ──────────────────────────────────────────────────
    function markAll(status) {
        document.querySelectorAll('input[type="radio"]').forEach(function (radio) {
            if (radio.value === status) radio.checked = true;
        });
    }

    // ── Student Search Filter (Mark Mode) ─────────────────────────
    function filterStudents() {
        const q = document.getElementById('studentSearch').value.toLowerCase();
        document.querySelectorAll('#studentTable .student-row').forEach(function (row) {
            const name = row.querySelector('.student-name-cell').textContent.toLowerCase();
            const id   = row.querySelector('.student-id-cell').textContent.toLowerCase();
            row.style.display = (name.includes(q) || id.includes(q)) ? '' : 'none';
        });
    }

    // ── Report Search Filter ───────────────────────────────────────
    function filterReport() {
        const q = document.getElementById('reportSearch').value.toLowerCase();
        document.querySelectorAll('#reportTable .report-row').forEach(function (row) {
            const name = row.querySelector('.report-name').textContent.toLowerCase();
            const code = row.querySelector('.report-code').textContent.toLowerCase();
            row.style.display = (name.includes(q) || code.includes(q)) ? '' : 'none';
        });
    }

    // ── Attendance Report Chart ────────────────────────────────────
    <% if ("report".equals(mode) && report != null && !report.isEmpty()) { %>
    document.addEventListener('DOMContentLoaded', function () {
        const ctx = document.getElementById('reportChart').getContext('2d');
        new Chart(ctx, {
            type: 'bar',
            data: {
                labels:   <%= reportLabels %>,
                datasets: [
                    {
                        label:           'Present',
                        data:            <%= reportPresent %>,
                        backgroundColor: 'rgba(25,135,84,0.75)',
                        borderRadius:    6,
                        borderSkipped:   false
                    },
                    {
                        label:           'Absent',
                        data:            <%= reportAbsent %>,
                        backgroundColor: 'rgba(220,53,69,0.75)',
                        borderRadius:    6,
                        borderSkipped:   false
                    }
                ]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { position: 'top' }
                },
                scales: {
                    x: { grid: { display: false }, stacked: false },
                    y: { beginAtZero: true, grid: { color: 'rgba(0,0,0,0.05)' } }
                }
            }
        });
    });
    <% } %>

    // Auto-dismiss saved alert
    const savedAlert = document.querySelector('.alert-success');
    if (savedAlert) {
        setTimeout(() => {
            const bs = bootstrap.Alert.getOrCreateInstance(savedAlert);
            if (bs) bs.close();
        }, 4000);
    }
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>