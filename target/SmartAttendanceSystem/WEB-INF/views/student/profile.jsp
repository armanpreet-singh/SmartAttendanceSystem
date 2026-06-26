<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    String pageTitle = "My Profile";
    com.smartattendance.model.Student student =
        (com.smartattendance.model.Student) request.getAttribute("student");

    String errorMessage   = (String) request.getAttribute("errorMessage");
    String successMessage = request.getParameter("success");
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp"  %>

<main class="container-fluid py-4 px-4">

    <!-- ─── Page Header ──────────────────────────────────────────── -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h4 class="fw-bold text-dark mb-0">
                <i class="fas fa-id-card me-2 text-primary"></i>
                My Profile
            </h4>
            <small class="text-muted">View and update your personal information</small>
        </div>
        <a href="<%= request.getContextPath() %>/student/dashboard"
           class="btn btn-sm btn-outline-secondary rounded-pill">
            <i class="fas fa-arrow-left me-1"></i> Dashboard
        </a>
    </div>

    <!-- ─── Alert Messages ───────────────────────────────────────── -->
    <% if (successMessage != null && !successMessage.isEmpty()) { %>
    <div class="alert alert-success alert-dismissible fade show d-flex align-items-center gap-2"
         role="alert">
        <i class="fas fa-check-circle"></i>
        <div><%= successMessage %></div>
        <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
    </div>
    <% } %>
    <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
    <div class="alert alert-danger alert-dismissible fade show d-flex align-items-center gap-2"
         role="alert">
        <i class="fas fa-exclamation-circle"></i>
        <div><%= errorMessage %></div>
        <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
    </div>
    <% } %>

    <div class="row g-4">

        <!-- ── Left: Avatar + Identity Card ──────────────────────── -->
        <div class="col-lg-4">
            <div class="card border-0 shadow-sm text-center p-4" style="border-radius:16px;">

                <!-- Avatar -->
                <div class="avatar-wrapper mx-auto mb-3">
                    <div class="rounded-circle bg-primary d-flex align-items-center
                                justify-content-center text-white mx-auto"
                         style="width:100px;height:100px;font-size:2.5rem;">
                        <i class="fas fa-user-graduate"></i>
                    </div>
                </div>

                <!-- Name + ID -->
                <h5 class="fw-bold text-dark mb-1">
                    <%= student != null ? student.getFullName() : "Student" %>
                </h5>
                <p class="text-muted small mb-3">
                    <%= student != null ? student.getStudentId() : "" %>
                </p>

                <!-- Info Pills -->
                <div class="d-flex flex-column gap-2">
                    <div class="d-flex align-items-center gap-2 px-3 py-2 rounded-3"
                         style="background:#f8f9fa;">
                        <i class="fas fa-graduation-cap text-primary"></i>
                        <span class="small text-dark">
                            <%= student != null ? student.getDepartmentName() : "" %>
                        </span>
                    </div>
                    <div class="d-flex align-items-center gap-2 px-3 py-2 rounded-3"
                         style="background:#f8f9fa;">
                        <i class="fas fa-layer-group text-primary"></i>
                        <span class="small text-dark">
                            <%= student != null ? student.getSemesterDisplay() : "" %>
                            &nbsp;|&nbsp; Section
                            <%= student != null ? student.getSection() : "" %>
                        </span>
                    </div>
                    <div class="d-flex align-items-center gap-2 px-3 py-2 rounded-3"
                         style="background:#f8f9fa;">
                        <i class="fas fa-calendar-alt text-primary"></i>
                        <span class="small text-dark">
                            Academic Year:
                            <%= student != null ? student.getAcademicYearLabel() : "" %>
                        </span>
                    </div>
                    <div class="d-flex align-items-center gap-2 px-3 py-2 rounded-3"
                         style="background:#f8f9fa;">
                        <i class="fas fa-circle
                            <%= student != null && student.isActive() ? " text-success" : " text-danger" %>">
                        </i>
                        <span class="small text-dark">
                            Status:
                            <%= student != null && student.isActive() ? "Active" : "Inactive" %>
                        </span>
                    </div>
                </div>

            </div>
        </div>

        <!-- ── Right: Profile Details + Edit Form ────────────────── -->
        <div class="col-lg-8">
            <div class="card border-0 shadow-sm" style="border-radius:16px;">

                <!-- Tab Navigation -->
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <ul class="nav nav-tabs border-0" id="profileTabs" role="tablist">
                        <li class="nav-item">
                            <button class="nav-link active fw-medium" data-bs-toggle="tab"
                                    data-bs-target="#infoPanel" type="button">
                                <i class="fas fa-info-circle me-1"></i> Details
                            </button>
                        </li>
                        <li class="nav-item">
                            <button class="nav-link fw-medium" data-bs-toggle="tab"
                                    data-bs-target="#editPanel" type="button">
                                <i class="fas fa-edit me-1"></i> Edit Profile
                            </button>
                        </li>
                    </ul>
                </div>

                <div class="tab-content card-body p-4">

                    <!-- ── Details Tab ──────────────────────────── -->
                    <div class="tab-pane fade show active" id="infoPanel">
                        <div class="row g-3">

                            <% String[][] fields = {
                                {"Full Name",      student != null ? student.getFullName() : "—"},
                                {"Student ID",     student != null ? student.getStudentId() : "—"},
                                {"Email",          student != null ? student.getEmail() : "—"},
                                {"Phone",          student != null && student.getPhone() != null
                                                   ? student.getPhone() : "—"},
                                {"Date of Birth",  student != null && student.getDateOfBirth() != null
                                                   ? com.smartattendance.util.DateUtil
                                                       .formatForDisplay(student.getDateOfBirth())
                                                   : "—"},
                                {"Gender",         student != null && student.getGender() != null
                                                   ? student.getGender() : "—"},
                                {"Department",     student != null ? student.getDepartmentName() : "—"},
                                {"Semester",       student != null ? student.getSemesterDisplay() : "—"},
                                {"Section",        student != null ? student.getSection() : "—"},
                                {"Academic Year",  student != null ? student.getAcademicYearLabel() : "—"}
                            };
                            for (String[] field : fields) { %>
                            <div class="col-md-6">
                                <div class="p-3 rounded-3" style="background:#f8f9fa;">
                                    <div class="text-muted small mb-1"><%= field[0] %></div>
                                    <div class="fw-medium text-dark"><%= field[1] %></div>
                                </div>
                            </div>
                            <% } %>

                            <!-- Address (full width) -->
                            <div class="col-12">
                                <div class="p-3 rounded-3" style="background:#f8f9fa;">
                                    <div class="text-muted small mb-1">Address</div>
                                    <div class="fw-medium text-dark">
                                        <%= student != null && student.getAddress() != null
                                            && !student.getAddress().isEmpty()
                                            ? student.getAddress() : "—" %>
                                    </div>
                                </div>
                            </div>

                        </div>
                    </div>
                    <!-- ── End Details Tab ───────────────────────── -->

                    <!-- ── Edit Profile Tab ─────────────────────── -->
                    <div class="tab-pane fade" id="editPanel">
                        <p class="text-muted small mb-3">
                            <i class="fas fa-info-circle me-1"></i>
                            You can update your <strong>phone number</strong> and
                            <strong>address</strong>. Other fields are managed by the admin.
                        </p>

                        <form action="<%= request.getContextPath() %>/student/profile"
                              method="POST" id="profileForm" novalidate>

                            <!-- Phone -->
                            <div class="mb-3">
                                <label for="phone" class="form-label fw-medium small">
                                    <i class="fas fa-phone me-1 text-muted"></i>
                                    Phone Number
                                </label>
                                <input type="tel"
                                       class="form-control"
                                       id="phone"
                                       name="phone"
                                       value="<%= student != null && student.getPhone() != null
                                                  ? student.getPhone() : "" %>"
                                       placeholder="10-digit mobile number"
                                       maxlength="15"
                                       style="border-radius:10px;">
                                <div class="form-text">Format: 10-digit Indian mobile number</div>
                            </div>

                            <!-- Address -->
                            <div class="mb-4">
                                <label for="address" class="form-label fw-medium small">
                                    <i class="fas fa-map-marker-alt me-1 text-muted"></i>
                                    Address
                                </label>
                                <textarea class="form-control"
                                          id="address"
                                          name="address"
                                          rows="3"
                                          placeholder="Your home address"
                                          maxlength="500"
                                          style="border-radius:10px;resize:none;"><%= student != null
                                          && student.getAddress() != null
                                          ? student.getAddress() : "" %></textarea>
                            </div>

                            <!-- Submit -->
                            <div class="d-flex gap-2">
                                <button type="submit"
                                        class="btn btn-primary rounded-pill px-4">
                                    <i class="fas fa-save me-2"></i>Save Changes
                                </button>
                                <button type="reset"
                                        class="btn btn-outline-secondary rounded-pill px-4">
                                    <i class="fas fa-undo me-2"></i>Reset
                                </button>
                            </div>

                        </form>
                    </div>
                    <!-- ── End Edit Tab ──────────────────────────── -->

                </div>
            </div>
        </div>
        <!-- ── End Right Column ──────────────────────────────────── -->

    </div>
    <!-- ── End Row ────────────────────────────────────────────────── -->

</main>

<script>
    // Auto-open Edit tab if there was an error on POST
    <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
    document.addEventListener('DOMContentLoaded', function () {
        const editTab = document.querySelector('[data-bs-target="#editPanel"]');
        if (editTab) new bootstrap.Tab(editTab).show();
    });
    <% } %>

    // Auto-dismiss success alert
    const successAlert = document.querySelector('.alert-success');
    if (successAlert) {
        setTimeout(() => {
            const bs = bootstrap.Alert.getOrCreateInstance(successAlert);
            if (bs) bs.close();
        }, 4000);
    }
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>