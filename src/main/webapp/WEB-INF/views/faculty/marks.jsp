<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    String pageTitle = "Marks Management";
    com.smartattendance.model.Faculty faculty =
        (com.smartattendance.model.Faculty) request.getAttribute("faculty");

    String  mode           = (String)  request.getAttribute("mode");
    Integer selectedSubId  = (Integer) request.getAttribute("selectedSubjectId");
    String  successMessage = (String)  request.getAttribute("successMessage");
    String  errorsParam    = request.getParameter("errors");

    java.util.List<com.smartattendance.model.Subject> subjects =
        (java.util.List<com.smartattendance.model.Subject>)
        request.getAttribute("subjects");

    java.util.List<com.smartattendance.model.Student> students =
        (java.util.List<com.smartattendance.model.Student>)
        request.getAttribute("students");

    java.util.List<java.util.Map<String, Object>> examTypes =
        (java.util.List<java.util.Map<String, Object>>)
        request.getAttribute("examTypes");

    java.util.Map<Integer, java.util.Map<Integer, com.smartattendance.model.Marks>> marksMatrix =
        (java.util.Map<Integer, java.util.Map<Integer, com.smartattendance.model.Marks>>)
        request.getAttribute("marksMatrix");

    if (mode == null) mode = "select";
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<main class="container-fluid py-4 px-4">

    <!-- ─── Page Header ──────────────────────────────────────────── -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h4 class="fw-bold text-dark mb-0">
                <i class="fas fa-pen me-2 text-primary"></i>
                Marks Management
            </h4>
            <small class="text-muted">
                Enter and update student marks per exam type
            </small>
        </div>
        <a href="<%= request.getContextPath() %>/faculty/dashboard"
           class="btn btn-sm btn-outline-secondary rounded-pill">
            <i class="fas fa-arrow-left me-1"></i> Dashboard
        </a>
    </div>

    <!-- ─── Alerts ───────────────────────────────────────────────── -->
    <% if (successMessage != null) { %>
    <div class="alert alert-success alert-dismissible fade show d-flex gap-2"
         role="alert" id="successAlert">
        <i class="fas fa-check-circle"></i>
        <div><%= successMessage %></div>
        <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
    </div>
    <% } %>
    <% if (errorsParam != null) { %>
    <div class="alert alert-warning alert-dismissible fade show d-flex gap-2" role="alert">
        <i class="fas fa-exclamation-triangle"></i>
        <div><strong><%= errorsParam %></strong> record(s) had validation errors and were skipped.</div>
        <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
    </div>
    <% } %>

    <!-- ─── Subject Selection ─────────────────────────────────────── -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-book me-2 text-primary"></i>
                Select Subject
            </h6>
        </div>
        <div class="card-body p-4">
            <form method="GET"
                  action="<%= request.getContextPath() %>/faculty/marks">
                <div class="row g-3 align-items-end">
                    <div class="col-md-6">
                        <label class="form-label fw-medium small">Subject</label>
                        <select name="subjectId" class="form-select"
                                style="border-radius:10px;" required>
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
                    <div class="col-md-3">
                        <button type="submit"
                                class="btn btn-primary rounded-pill w-100">
                            <i class="fas fa-eye me-2"></i>Load Marks
                        </button>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <!-- ─── Marks Entry Table ─────────────────────────────────────── -->
    <% if ("entry".equals(mode) && students != null && !students.isEmpty()
            && examTypes != null && !examTypes.isEmpty()) { %>

    <div class="card border-0 shadow-sm" style="border-radius:14px;">
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4 d-flex
                    justify-content-between align-items-center flex-wrap gap-2">
            <h6 class="fw-bold mb-0">
                <i class="fas fa-table me-2 text-primary"></i>
                Marks Entry Table
                <span class="badge bg-primary ms-2"><%= students.size() %> students</span>
            </h6>
            <!-- Search -->
            <input type="text" id="marksSearch"
                   class="form-control form-control-sm"
                   placeholder="🔍 Search student..."
                   style="border-radius:20px;max-width:220px;"
                   onkeyup="filterMarksTable()">
        </div>

        <div class="card-body p-0">
            <form method="POST"
                  action="<%= request.getContextPath() %>/faculty/marks"
                  id="marksForm">
                <input type="hidden" name="subjectId" value="<%= selectedSubId %>">

                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0" id="marksTable">
                        <thead class="table-light">
                            <tr>
                                <th class="ps-4 sticky-left-col">#</th>
                                <th class="sticky-col">Student</th>
                                <th class="text-center">Sec</th>
                                <% for (java.util.Map<String, Object> et : examTypes) { %>
                                <th class="text-center"
                                    title="Max: <%= et.get("maxMarks") %> marks">
                                    <%= et.get("examName") %>
                                    <div class="text-muted fw-normal"
                                         style="font-size:0.7rem;">
                                        /<%= et.get("maxMarks") %>
                                    </div>
                                </th>
                                <% } %>
                            </tr>
                        </thead>
                        <tbody>
                            <% int mn = 1;
                               for (com.smartattendance.model.Student stu : students) {
                                   java.util.Map<Integer, com.smartattendance.model.Marks> stuMarks =
                                       (marksMatrix != null)
                                       ? marksMatrix.getOrDefault(stu.getId(),
                                             new java.util.LinkedHashMap<>())
                                       : new java.util.LinkedHashMap<>();
                            %>
                            <tr class="marks-row">
                                <td class="ps-4 text-muted small"><%= mn++ %></td>
                                <td>
                                    <div class="fw-medium text-dark marks-name">
                                        <%= stu.getFullName() %>
                                    </div>
                                    <div class="text-muted marks-code"
                                         style="font-size:0.76rem;">
                                        <%= stu.getStudentId() %>
                                    </div>
                                </td>
                                <td class="text-center text-muted small">
                                    <%= stu.getSection() %>
                                </td>
                                <% for (java.util.Map<String, Object> et : examTypes) {
                                       int etId  = (int) et.get("id");
                                       int etMax = (int) et.get("maxMarks");
                                       com.smartattendance.model.Marks existingMark =
                                           stuMarks.get(etId);
                                       double existing = (existingMark != null
                                                          && existingMark.getId() > 0)
                                                         ? existingMark.getMarksObtained() : -1;
                                %>
                                <td class="text-center" style="min-width:100px;">
                                    <input type="number"
                                           class="form-control form-control-sm text-center
                                                  marks-input"
                                           name="marks_<%= stu.getId() %>_<%= etId %>"
                                           value="<%= existing >= 0
                                                      ? String.format("%.1f", existing) : "" %>"
                                           min="0"
                                           max="<%= etMax %>"
                                           step="0.5"
                                           placeholder="–"
                                           style="border-radius:8px;"
                                           data-max="<%= etMax %>"
                                           oninput="validateMarks(this)">
                                </td>
                                <% } %>
                            </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>

                <!-- Save Bar -->
                <div class="px-4 py-3 border-top d-flex gap-3 align-items-center">
                    <button type="submit" class="btn btn-primary rounded-pill px-4">
                        <i class="fas fa-save me-2"></i>Save All Marks
                    </button>
                    <span class="text-muted small">
                        <i class="fas fa-info-circle me-1"></i>
                        Grades are computed automatically.
                        Leave blank to skip a field.
                    </span>
                </div>
            </form>
        </div>
    </div>

    <!-- Grade Scale Reference -->
    <div class="card border-0 shadow-sm mt-3 p-3" style="border-radius:14px;">
        <div class="row g-2 text-center">
            <div class="col"><span class="badge bg-success px-3">O ≥ 90%</span></div>
            <div class="col"><span class="badge bg-success px-3">A+ ≥ 80%</span></div>
            <div class="col"><span class="badge bg-primary px-3">A ≥ 70%</span></div>
            <div class="col"><span class="badge bg-primary px-3">B+ ≥ 60%</span></div>
            <div class="col"><span class="badge bg-warning text-dark px-3">B ≥ 50%</span></div>
            <div class="col"><span class="badge bg-warning text-dark px-3">C ≥ 40%</span></div>
            <div class="col"><span class="badge bg-danger px-3">F &lt; 40%</span></div>
        </div>
    </div>

    <% } else if ("entry".equals(mode)) { %>
    <div class="alert alert-info d-flex gap-2" role="alert" style="border-radius:12px;">
        <i class="fas fa-info-circle"></i>
        <div>No students found for this subject.</div>
    </div>
    <% } %>

</main>

<script>
    // ── Marks Validation ──────────────────────────────────────────
    function validateMarks(input) {
        const val = parseFloat(input.value);
        const max = parseFloat(input.getAttribute('data-max'));
        if (input.value !== '' && (isNaN(val) || val < 0 || val > max)) {
            input.classList.add('is-invalid');
            input.title = 'Must be between 0 and ' + max;
        } else {
            input.classList.remove('is-invalid');
            input.title = '';
        }
    }

    // ── Search Filter ─────────────────────────────────────────────
    function filterMarksTable() {
        const q = document.getElementById('marksSearch').value.toLowerCase();
        document.querySelectorAll('#marksTable .marks-row').forEach(function (row) {
            const name = row.querySelector('.marks-name').textContent.toLowerCase();
            const code = row.querySelector('.marks-code').textContent.toLowerCase();
            row.style.display = (name.includes(q) || code.includes(q)) ? '' : 'none';
        });
    }

    // ── Auto-dismiss alerts ───────────────────────────────────────
    const successAlert = document.getElementById('successAlert');
    if (successAlert) {
        setTimeout(() => bootstrap.Alert.getOrCreateInstance(successAlert).close(), 4000);
    }

    // ── Confirm before save ───────────────────────────────────────
    document.getElementById('marksForm') &&
    document.getElementById('marksForm').addEventListener('submit', function (e) {
        let hasInvalid = document.querySelectorAll('.marks-input.is-invalid').length;
        if (hasInvalid > 0) {
            e.preventDefault();
            alert('Please correct invalid marks entries before saving.');
        }
    });
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>