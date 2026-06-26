<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String pageTitle = "Edit Profile";
    com.smartattendance.model.Faculty faculty =
        (com.smartattendance.model.Faculty) request.getAttribute("faculty");
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<%@ include file="/WEB-INF/views/common/header.jsp" %>
<%@ include file="/WEB-INF/views/common/navbar.jsp" %>

<main class="container-fluid py-4 px-4">

    <div class="d-flex align-items-center justify-content-between mb-4 flex-wrap gap-2">
        <div>
            <h4 class="fw-bold text-dark mb-0">
                <i class="fas fa-user-edit me-2" style="color:#e65100;"></i>
                Edit Profile
            </h4>
            <small class="text-muted">Update your contact information and designation</small>
        </div>
        <a href="<%= request.getContextPath() %>/faculty/profile"
           class="btn btn-sm btn-outline-secondary rounded-pill">
            <i class="fas fa-arrow-left me-1"></i> Back to Profile
        </a>
    </div>

    <% if (errorMessage != null && !errorMessage.isEmpty()) { %>
    <div class="alert alert-danger d-flex gap-2" role="alert" style="border-radius:12px;">
        <i class="fas fa-exclamation-circle"></i>
        <div><%= errorMessage %></div>
    </div>
    <% } %>

    <div class="row justify-content-center">
        <div class="col-lg-7">
            <div class="card border-0 shadow-sm" style="border-radius:16px;">
                <div class="card-header bg-white border-0 pt-4 pb-0 px-4">
                    <h6 class="fw-bold text-dark mb-0">
                        <i class="fas fa-pencil-alt me-2" style="color:#e65100;"></i>
                        Editable Fields
                    </h6>
                    <p class="text-muted small mt-1 mb-0">
                        Name, email, and department are managed by the administrator.
                    </p>
                </div>
                <div class="card-body p-4">
                    <form method="POST"
                          action="<%= request.getContextPath() %>/faculty/update-profile"
                          novalidate id="editProfileForm">

                        <!-- Read-only: Full Name -->
                        <div class="mb-3">
                            <label class="form-label fw-medium small text-muted">
                                <i class="fas fa-user me-1"></i>Full Name (Read-only)
                            </label>
                            <input type="text" class="form-control bg-light"
                                   value="<%= faculty != null ? faculty.getFullName() : "" %>"
                                   readonly style="border-radius:10px;">
                        </div>

                        <!-- Read-only: Email -->
                        <div class="mb-3">
                            <label class="form-label fw-medium small text-muted">
                                <i class="fas fa-envelope me-1"></i>Email (Read-only)
                            </label>
                            <input type="email" class="form-control bg-light"
                                   value="<%= faculty != null ? faculty.getEmail() : "" %>"
                                   readonly style="border-radius:10px;">
                        </div>

                        <!-- Editable: Designation -->
                        <div class="mb-3">
                            <label for="designation" class="form-label fw-medium small">
                                <i class="fas fa-briefcase me-1 text-muted"></i>
                                Designation <span class="text-danger">*</span>
                            </label>
                            <select name="designation" id="designation"
                                    class="form-select" style="border-radius:10px;" required>
                                <%
                                    String[] designations = {
                                        "Professor", "Associate Professor",
                                        "Assistant Professor", "Lecturer",
                                        "Senior Lecturer", "Visiting Faculty"
                                    };
                                    String current = faculty != null
                                                     ? faculty.getDesignation() : "";
                                    for (String d : designations) {
                                %>
                                <option value="<%= d %>"
                                    <%= d.equals(current) ? "selected" : "" %>>
                                    <%= d %>
                                </option>
                                <% } %>
                            </select>
                        </div>

                        <!-- Editable: Phone -->
                        <div class="mb-4">
                            <label for="phone" class="form-label fw-medium small">
                                <i class="fas fa-phone me-1 text-muted"></i>
                                Phone Number
                            </label>
                            <input type="tel" name="phone" id="phone"
                                   class="form-control"
                                   value="<%= faculty != null && faculty.getPhone() != null
                                              ? faculty.getPhone() : "" %>"
                                   placeholder="10-digit mobile number"
                                   maxlength="15"
                                   style="border-radius:10px;">
                            <div class="form-text">Format: 10-digit Indian mobile number</div>
                        </div>

                        <div class="d-flex gap-2">
                            <button type="submit"
                                    class="btn btn-warning rounded-pill px-4 fw-semibold">
                                <i class="fas fa-save me-2"></i>Save Changes
                            </button>
                            <a href="<%= request.getContextPath() %>/faculty/profile"
                               class="btn btn-outline-secondary rounded-pill px-4">
                                <i class="fas fa-times me-2"></i>Cancel
                            </a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/common/footer.jsp" %>