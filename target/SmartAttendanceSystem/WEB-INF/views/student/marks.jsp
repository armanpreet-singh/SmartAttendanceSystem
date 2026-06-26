<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    String pageTitle = "My Marks";
    com.smartattendance.model.Student student =
        (com.smartattendance.model.Student) request.getAttribute("student");

    java.util.Map<String, java.util.List<com.smartattendance.model.Marks>> marksGrouped =
        (java.util.Map<String, java.util.List<com.smartattendance.model.Marks>>)
        request.getAttribute("marksGrouped");

    java.util.Map<String, String>  subjectNames        =
        (java.util.Map<String, String>)  request.getAttribute("subjectNames");
    java.util.Map<String, Double>  subjectTotalObtained =
        (java.util.Map<String, Double>)  request.getAttribute("subjectTotalObtained");
    java.util.Map<String, Integer> subjectTotalMax      =
        (java.util.Map<String, Integer>) request.getAttribute("subjectTotalMax");
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp"  %>

<main class="container-fluid py-4 px-4">

    <!-- ─── Page Header ──────────────────────────────────────────── -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h4 class="fw-bold text-dark mb-0">
                <i class="fas fa-star me-2 text-warning"></i>
                My Marks
            </h4>
            <small class="text-muted">
                <%= student != null ? student.getFullName() : "" %> &nbsp;|&nbsp;
                Academic Year:
                <%= student != null ? student.getAcademicYearLabel() : "" %>
            </small>
        </div>
        <a href="<%= request.getContextPath() %>/student/dashboard"
           class="btn btn-sm btn-outline-secondary rounded-pill">
            <i class="fas fa-arrow-left me-1"></i> Dashboard
        </a>
    </div>

    <!-- ─── No Marks Placeholder ─────────────────────────────────── -->
    <% if (marksGrouped == null || marksGrouped.isEmpty()) { %>
    <div class="card border-0 shadow-sm text-center py-5" style="border-radius:14px;">
        <i class="fas fa-file-alt fa-3x text-muted mb-3"></i>
        <h5 class="text-muted">No marks recorded yet.</h5>
        <p class="text-muted small">Marks will appear here once your faculty records them.</p>
    </div>
    <% } else {
        for (java.util.Map.Entry<String, java.util.List<com.smartattendance.model.Marks>>
             entry : marksGrouped.entrySet()) {

            String code        = entry.getKey();
            String name        = subjectNames  != null ? subjectNames.getOrDefault(code, "")  : "";
            double totalObt    = subjectTotalObtained != null
                                 ? subjectTotalObtained.getOrDefault(code, 0.0) : 0.0;
            int    totalMax    = subjectTotalMax != null
                                 ? subjectTotalMax.getOrDefault(code, 0) : 0;
            double subPct      = totalMax > 0
                                 ? Math.round((totalObt / totalMax) * 100.0 * 100.0) / 100.0 : 0.0;
            String subBadge    = subPct >= 75 ? "success" : subPct >= 50 ? "warning" : "danger";
    %>

    <!-- Subject Marks Card -->
    <div class="card border-0 shadow-sm mb-4" style="border-radius:14px;">

        <!-- Card Header: Subject Info -->
        <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
            <div class="d-flex justify-content-between align-items-center flex-wrap gap-2">
                <div>
                    <h6 class="fw-bold text-dark mb-0">
                        <span class="badge bg-primary me-2"><%= code %></span>
                        <%= name %>
                    </h6>
                </div>
                <div class="d-flex align-items-center gap-3">
                    <div class="text-end">
                        <div class="small text-muted">Total Score</div>
                        <div class="fw-bold text-<%= subBadge %>">
                            <fmt:formatNumber value="<%= totalObt %>" maxFractionDigits="1"/>
                            / <%= totalMax %>
                        </div>
                    </div>
                    <div class="text-center">
                        <div class="small text-muted">Percentage</div>
                        <span class="badge bg-<%= subBadge %> fs-6 px-3 py-1">
                            <fmt:formatNumber value="<%= subPct %>" maxFractionDigits="1"/>%
                        </span>
                    </div>
                </div>
            </div>

            <!-- Overall progress bar for this subject -->
            <div class="mt-2 mb-0 pb-3">
                <div class="progress" style="height:6px;">
                    <div class="progress-bar bg-<%= subBadge %>"
                         style="width:<%= Math.min(subPct,100.0) %>%">
                    </div>
                </div>
            </div>
        </div>

        <!-- Card Body: Marks Table -->
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover align-middle mb-0">
                    <thead class="table-light">
                        <tr>
                            <th class="ps-4">Exam Type</th>
                            <th class="text-center">Max Marks</th>
                            <th class="text-center">Marks Obtained</th>
                            <th class="text-center">Percentage</th>
                            <th class="text-center">Grade</th>
                            <th>Remarks</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% for (com.smartattendance.model.Marks m : entry.getValue()) {
                               double mpct    = m.getMarksPercentage();
                               String mbadge  = mpct >= 75 ? "success"
                                              : mpct >= 50 ? "warning" : "danger";
                        %>
                        <tr>
                            <td class="ps-4">
                                <span class="badge bg-secondary-subtle text-secondary
                                             border border-secondary-subtle">
                                    <%= m.getExamName() %>
                                </span>
                            </td>
                            <td class="text-center text-muted"><%= m.getMaxMarks() %></td>
                            <td class="text-center fw-semibold text-dark">
                                <fmt:formatNumber value="<%= m.getMarksObtained() %>"
                                                  maxFractionDigits="1"/>
                            </td>
                            <td class="text-center">
                                <span class="text-<%= mbadge %> fw-medium">
                                    <fmt:formatNumber value="<%= mpct %>"
                                                      maxFractionDigits="1"/>%
                                </span>
                            </td>
                            <td class="text-center">
                                <% if (m.getGrade() != null && !m.getGrade().isEmpty()) { %>
                                <span class="badge bg-<%= mbadge %> rounded-pill px-3">
                                    <%= m.getGrade() %>
                                </span>
                                <% } else { %>
                                <span class="text-muted">—</span>
                                <% } %>
                            </td>
                            <td class="text-muted small">
                                <%= m.getRemarks() != null ? m.getRemarks() : "—" %>
                            </td>
                        </tr>
                        <% } %>
                    </tbody>
                    <tfoot class="table-light">
                        <tr>
                            <td class="ps-4 fw-semibold">Total</td>
                            <td class="text-center fw-semibold"><%= totalMax %></td>
                            <td class="text-center fw-semibold text-<%= subBadge %>">
                                <fmt:formatNumber value="<%= totalObt %>"
                                                  maxFractionDigits="1"/>
                            </td>
                            <td class="text-center fw-semibold text-<%= subBadge %>">
                                <fmt:formatNumber value="<%= subPct %>"
                                                  maxFractionDigits="1"/>%
                            </td>
                            <td colspan="2"></td>
                        </tr>
                    </tfoot>
                </table>
            </div>
        </div>
    </div>
    <% } } %>

</main>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>