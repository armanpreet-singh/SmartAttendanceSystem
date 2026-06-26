<%-- ================================================================
     common/report-view.jsp
     
     Purpose:
       Reusable report header banner included inside any report
       JSP page (student/report.jsp and faculty/reports.jsp) using:
         <%@ include file="/WEB-INF/views/common/report-view.jsp" %>
     
     Renders:
       - Report title with icon
       - Subject name + selected filters (section, exam type)
       - Report generation timestamp
       - Export (CSV) and Print action buttons
       - Minimum required attendance reference badge
     
     Expects these variables to already be in scope
     (set by the parent JSP scriptlet):
       - reportTitle        : String   — display title of the report
       - selectedSubjectName: String   — "CODE – Subject Name"
       - selectedSection    : String   — filter section (or "")
       - generatedAt        : String   — formatted timestamp string
       - reportType         : String   — "attendance"|"marks"|etc.
       - selectedSubjectId  : int      — subject PK
       - selectedExamType   : int      — exam type id (0 = all)
       - ctxPath            : String   — request.getContextPath()
     
     NOTE:
       This file contains NO <%@ page %> directive intentionally,
       so it can be statically included (<%@ include %>) inside
       pages that already have their own page directive.
================================================================ --%>
<%@ taglib prefix="c"   uri="jakarta.tags.core"   %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"    %>

<div class="card border-0 shadow-sm mb-4 report-header-card"
     style="border-radius:14px;
            background:linear-gradient(135deg,#f8f9fa 0%,#e9ecef 100%);
            border-left:5px solid var(--bs-primary) !important;"
     id="reportHeaderBanner">
    <div class="card-body px-4 py-3">
        <div class="row align-items-center g-2">

            <!-- ── Left: Title and metadata ──────────────────────── -->
            <div class="col-lg-8">

                <!-- Report Title Row -->
                <div class="d-flex align-items-center gap-2 mb-2 flex-wrap">
                    <%-- Icon changes per report type --%>
                    <%
                        String rIcon = "fa-file-alt";
                        String rColor = "primary";
                        if ("attendance".equals(reportType)) {
                            rIcon  = "fa-calendar-check";
                            rColor = "primary";
                        } else if ("defaulters".equals(reportType)) {
                            rIcon  = "fa-exclamation-triangle";
                            rColor = "danger";
                        } else if ("marks".equals(reportType)) {
                            rIcon  = "fa-star";
                            rColor = "warning";
                        } else if ("overview".equals(reportType)) {
                            rIcon  = "fa-chart-line";
                            rColor = "success";
                        }
                    %>
                    <div class="rounded-circle d-flex align-items-center justify-content-center
                                flex-shrink-0"
                         style="width:40px;height:40px;
                                background:rgba(var(--bs-<%= rColor %>-rgb),0.12);">
                        <i class="fas <%= rIcon %> text-<%= rColor %>"></i>
                    </div>
                    <div>
                        <h5 class="fw-bold text-dark mb-0 lh-1">
                            <%= reportTitle != null ? reportTitle : "Report" %>
                        </h5>
                        <small class="text-muted">
                            Academic Report — Smart Attendance System
                        </small>
                    </div>
                </div>

                <!-- Metadata Tags Row -->
                <div class="d-flex align-items-center gap-2 flex-wrap">

                    <!-- Subject -->
                    <% if (selectedSubjectName != null && !selectedSubjectName.isEmpty()) { %>
                    <span class="badge bg-primary-subtle text-primary border
                                 border-primary-subtle rounded-pill px-3 py-1 small">
                        <i class="fas fa-book me-1"></i>
                        <%= selectedSubjectName %>
                    </span>
                    <% } %>

                    <!-- Section filter -->
                    <% if (selectedSection != null && !selectedSection.isEmpty()) { %>
                    <span class="badge bg-secondary-subtle text-secondary border
                                 border-secondary-subtle rounded-pill px-3 py-1 small">
                        <i class="fas fa-door-open me-1"></i>
                        Section <%= selectedSection %>
                    </span>
                    <% } else { %>
                    <span class="badge bg-light text-muted border
                                 rounded-pill px-3 py-1 small">
                        <i class="fas fa-users me-1"></i>
                        All Sections
                    </span>
                    <% } %>

                    <!-- Exam type filter (marks report only) -->
                    <% if ("marks".equals(reportType)) { %>
                    <span class="badge bg-warning-subtle text-warning border
                                 border-warning-subtle rounded-pill px-3 py-1 small">
                        <i class="fas fa-clipboard-list me-1"></i>
                        <%= (selectedExamType > 0) ? "Filtered by Exam" : "All Exams" %>
                    </span>
                    <% } %>

                    <!-- Minimum attendance reference -->
                    <% if ("attendance".equals(reportType)
                            || "defaulters".equals(reportType)
                            || "overview".equals(reportType)) { %>
                    <span class="badge bg-danger-subtle text-danger border
                                 border-danger-subtle rounded-pill px-3 py-1 small">
                        <i class="fas fa-exclamation-circle me-1"></i>
                        Min Required:
                        <%= (int) com.smartattendance.util.AttendanceCalculator
                                      .MINIMUM_REQUIRED_PERCENTAGE %>%
                    </span>
                    <% } %>

                </div>
            </div>

            <!-- ── Right: Timestamp + Actions ────────────────────── -->
            <div class="col-lg-4 text-lg-end">

                <!-- Generation time -->
                <div class="text-muted small mb-2">
                    <i class="fas fa-clock me-1"></i>
                    Generated:
                    <strong>
                        <%= generatedAt != null && !generatedAt.isEmpty()
                            ? generatedAt
                            : com.smartattendance.util.DateUtil
                                  .formatForDisplay(java.time.LocalDateTime.now()) %>
                    </strong>
                </div>

                <!-- Action buttons -->
                <div class="d-flex gap-2 justify-content-lg-end flex-wrap">

                    <!-- Export CSV -->
                    <a href="<%= ctxPath %>/report/export?reportType=<%= reportType
                               %>&subjectId=<%= selectedSubjectId
                               %>&section=<%= selectedSection
                               %>&examTypeId=<%= selectedExamType %>"
                       class="btn btn-sm btn-outline-success rounded-pill"
                       title="Export as CSV">
                        <i class="fas fa-file-csv me-1"></i>
                        Export CSV
                    </a>

                    <!-- Print -->
                    <button type="button"
                            onclick="window.print();"
                            class="btn btn-sm btn-outline-secondary rounded-pill"
                            title="Print this report">
                        <i class="fas fa-print me-1"></i>
                        Print
                    </button>

                </div>

            </div>
        </div>
    </div>
</div>

<!-- ── Report Legend (colour coding reference) ──────────────────── -->
<div class="d-flex flex-wrap gap-2 align-items-center mb-3 px-1"
     id="reportLegend">
    <small class="text-muted fw-medium me-1">Legend:</small>

    <% if ("attendance".equals(reportType)
            || "defaulters".equals(reportType)
            || "overview".equals(reportType)) { %>
    <span class="badge bg-success-subtle text-success border
                 border-success-subtle rounded-pill px-3">
        <i class="fas fa-circle me-1" style="font-size:0.5rem;"></i>
        Safe ≥ 85%
    </span>
    <span class="badge bg-warning-subtle text-warning border
                 border-warning-subtle rounded-pill px-3">
        <i class="fas fa-circle me-1" style="font-size:0.5rem;"></i>
        Warning 75–85%
    </span>
    <span class="badge bg-danger-subtle text-danger border
                 border-danger-subtle rounded-pill px-3">
        <i class="fas fa-circle me-1" style="font-size:0.5rem;"></i>
        Defaulter &lt; 75%
    </span>
    <% } %>

    <% if ("marks".equals(reportType) || "overview".equals(reportType)) { %>
    <span class="badge bg-success-subtle text-success border
                 border-success-subtle rounded-pill px-3">
        <i class="fas fa-circle me-1" style="font-size:0.5rem;"></i>
        Good ≥ 75%
    </span>
    <span class="badge bg-warning-subtle text-warning border
                 border-warning-subtle rounded-pill px-3">
        <i class="fas fa-circle me-1" style="font-size:0.5rem;"></i>
        Average 50–75%
    </span>
    <span class="badge bg-danger-subtle text-danger border
                 border-danger-subtle rounded-pill px-3">
        <i class="fas fa-circle me-1" style="font-size:0.5rem;"></i>
        Poor &lt; 50%
    </span>
    <% } %>

</div>
<%-- ── End report-view.jsp ──────────────────────────────────────── --%>