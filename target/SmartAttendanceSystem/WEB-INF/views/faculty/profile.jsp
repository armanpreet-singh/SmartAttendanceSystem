<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    String pageTitle = "My Profile";
    com.smartattendance.model.Faculty faculty =
        (com.smartattendance.model.Faculty) request.getAttribute("faculty");
    String successMessage = (String) request.getAttribute("successMessage");
    if (successMessage == null) successMessage = request.getParameter("success");
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<main class="container-fluid py-4 px-4">

    <!-- ─── Page Header ──────────────────────────────────────────── -->
    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h4 class="fw-bold text-dark mb-0">
                <i class="fas fa-id-badge me-2" style="color:#e65100;"></i>
                My Profile
            </h4>
            <small class="text-muted">View your faculty profile and account settings</small>
        </div>
        <a href="<%= request.getContextPath() %>/faculty/dashboard"
           class="btn btn-sm btn-outline-secondary rounded-pill">
            <i class="fas fa-arrow-left me-1"></i> Dashboard
        </a>
    </div>

    <!-- ─── Success Alert ─────────────────────────────────────────── -->
    <% if (successMessage != null && !successMessage.isEmpty()) { %>
    <div class="alert alert-success alert-dismissible fade show d-flex gap-2"
         role="alert" id="successAlert">
        <i class="fas fa-check-circle"></i>
        <div><%= successMessage %></div>
        <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
    </div>
    <% } %>

    <div class="row g-4">

        <!-- ── Left: Identity Card ────────────────────────────────── -->
        <div class="col-lg-4">
            <div class="card border-0 shadow-sm text-center p-4" style="border-radius:16px;">

                <!-- Avatar -->
                <div class="rounded-circle d-flex align-items-center justify-content-center
                            text-white mx-auto mb-3"
                     style="width:100px;height:100px;font-size:2.5rem;
                            background:linear-gradient(135deg,#e65100,#f57c00);">
                    <i class="fas fa-chalkboard-teacher"></i>
                </div>

                <h5 class="fw-bold text-dark mb-1">
                    <%= faculty != null ? faculty.getFullName() : "Faculty" %>
                </h5>
                <p class="text-muted small mb-3">
                    <%= faculty != null ? faculty.getFacultyId() : "" %>
                </p>
                <span class="badge rounded-pill px-3 py-2 mb-3"
                      style="background:linear-gradient(135deg,#e65100,#f57c00);
                             font-size:0.82rem;">
                    <%= faculty != null ? faculty.getDesignation() : "" %>
                </span>

                <!-- Info pills -->
                <div class="d-flex flex-column gap-2 text-start">
                    <div class="d-flex align-items-center gap-2 px-3 py-2 rounded-3"
                         style="background:#f8f9fa;">
                        <i class="fas fa-university" style="color:#e65100;"></i>
                        <span class="small text-dark">
                            <%= faculty != null ? faculty.getDepartmentName() : "" %>
                        </span>
                    </div>
                    <div class="d-flex align-items-center gap-2 px-3 py-2 rounded-3"
                         style="background:#f8f9fa;">
                        <i class="fas fa-envelope" style="color:#e65100;"></i>
                        <span class="small text-dark">
                            <%= faculty != null ? faculty.getEmail() : "" %>
                        </span>
                    </div>
                    <div class="d-flex align-items-center gap-2 px-3 py-2 rounded-3"
                         style="background:#f8f9fa;">
                        <i class="fas fa-phone" style="color:#e65100;"></i>
                        <span class="small text-dark">
                            <%= faculty != null && faculty.getPhone() != null
                                ? faculty.getPhone() : "Not provided" %>
                        </span>
                    </div>
                    <div class="d-flex align-items-center gap-2 px-3 py-2 rounded-3"
                         style="background:#f8f9fa;">
                        <i class="fas fa-circle
                           <%= faculty != null && faculty.isActive()
                               ? " text-success" : " text-danger" %>"></i>
                        <span class="small text-dark">
                            Status:
                            <%= faculty != null && faculty.isActive() ? "Active" : "Inactive" %>
                        </span>
                    </div>
                </div>

            </div>

            <!-- Quick Links Card -->
            <div class="card border-0 shadow-sm mt-3 p-3" style="border-radius:14px;">
                <h6 class="fw-bold text-dark mb-3 px-1">
                    <i class="fas fa-cog me-2 text-muted"></i>Account Settings
                </h6>
                <div class="d-grid gap-2">
                    <a href="<%= request.getContextPath() %>/faculty/update-profile"
                       class="btn btn-outline-warning rounded-pill">
                        <i class="fas fa-user-edit me-2"></i>Edit Profile
                    </a>
                    <a href="<%= request.getContextPath() %>/faculty/change-password"
                       class="btn btn-outline-danger rounded-pill">
                        <i class="fas fa-key me-2"></i>Change Password
                    </a>
                </div>
            </div>
        </div>

        <!-- ── Right: Profile Details ─────────────────────────────── -->
        <div class="col-lg-8">
            <div class="card border-0 shadow-sm" style="border-radius:16px;">
                <div class="card-header bg-white border-0 pt-3 pb-0 px-4">
                    <h6 class="fw-bold text-dark mb-2">
                        <i class="fas fa-info-circle me-2" style="color:#e65100;"></i>
                        Profile Information
                    </h6>
                </div>
                <div class="card-body p-4">
                    <div class="row g-3">
                        <%
                            String[][] fields = {
                                {"Full Name",    faculty != null ? faculty.getFullName()       : "—"},
                                {"Faculty ID",   faculty != null ? faculty.getFacultyId()      : "—"},
                                {"Email",        faculty != null ? faculty.getEmail()          : "—"},
                                {"Phone",        faculty != null && faculty.getPhone() != null
                                                 ? faculty.getPhone() : "—"},
                                {"Designation",  faculty != null ? faculty.getDesignation()   : "—"},
                                {"Department",   faculty != null ? faculty.getDepartmentName(): "—"},
                            };
                            for (String[] field : fields) {
                        %>
                        <div class="col-md-6">
                            <div class="p-3 rounded-3" style="background:#f8f9fa;">
                                <div class="text-muted small mb-1"><%= field[0] %></div>
                                <div class="fw-medium text-dark"><%= field[1] %></div>
                            </div>
                        </div>
                        <% } %>

                        <!-- Member Since -->
                        <div class="col-12">
                            <div class="p-3 rounded-3" style="background:#f8f9fa;">
                                <div class="text-muted small mb-1">Member Since</div>
                                <div class="fw-medium text-dark">
                                    <%= faculty != null && faculty.getCreatedAt() != null
                                        ? com.smartattendance.util.DateUtil
                                            .formatForDisplay(faculty.getCreatedAt().toLocalDate())
                                        : "—" %>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>

<script>
    const sa = document.getElementById('successAlert');
    if (sa) setTimeout(() => bootstrap.Alert.getOrCreateInstance(sa).close(), 4000);
</script>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>