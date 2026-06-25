<%-- ================================================================
     navbar.jsp - Updated with Active Link Highlighting
     Replaces the previous version. All other logic remains the same.
================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    Object loggedInStudent = session.getAttribute("loggedInStudent");
    Object loggedInFaculty = session.getAttribute("loggedInFaculty");
    String userRole        = (String) session.getAttribute("userRole");

    String displayName = "User";
    String profilePhoto = "default-avatar.png";
    if (loggedInStudent != null) {
        com.smartattendance.model.Student s =
            (com.smartattendance.model.Student) loggedInStudent;
        displayName  = s.getFirstName() + " " + s.getLastName();
        if (s.getProfilePhoto() != null) profilePhoto = s.getProfilePhoto();
    } else if (loggedInFaculty != null) {
        com.smartattendance.model.Faculty f =
            (com.smartattendance.model.Faculty) loggedInFaculty;
        displayName  = f.getFirstName() + " " + f.getLastName();
        if (f.getProfilePhoto() != null) profilePhoto = f.getProfilePhoto();
    }

    // Determine current URI for active link highlighting
    String currentUri = request.getRequestURI();
%>

<nav class="navbar navbar-expand-lg navbar-dark sticky-top shadow-sm"
     style="background: linear-gradient(135deg, #1a237e 0%, #3949ab 100%);">
    <div class="container-fluid">

        <!-- ─── Brand ────────────────────────────────────────────── -->
        <a class="navbar-brand d-flex align-items-center gap-2 fw-semibold"
           href="<%= request.getContextPath() %>/index.jsp">
            <i class="fas fa-graduation-cap fa-lg"></i>
            Smart Attendance
        </a>

        <!-- ─── Mobile Toggle ────────────────────────────────────── -->
        <button class="navbar-toggler border-0" type="button"
                data-bs-toggle="collapse" data-bs-target="#mainNavbar"
                aria-controls="mainNavbar" aria-expanded="false"
                aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <div class="collapse navbar-collapse" id="mainNavbar">

            <!-- ─── STUDENT Nav Links ─────────────────────────────── -->
            <% if ("STUDENT".equals(userRole)) { %>
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">

                <li class="nav-item">
                    <a class="nav-link <%= currentUri.contains("/student/dashboard") ? "active fw-semibold" : "" %>"
                       href="<%= request.getContextPath() %>/student/dashboard">
                        <i class="fas fa-tachometer-alt me-1"></i> Dashboard
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <%= currentUri.contains("/student/attendance") ? "active fw-semibold" : "" %>"
                       href="<%= request.getContextPath() %>/student/attendance">
                        <i class="fas fa-calendar-check me-1"></i> Attendance
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <%= currentUri.contains("/student/marks") ? "active fw-semibold" : "" %>"
                       href="<%= request.getContextPath() %>/student/marks">
                        <i class="fas fa-star me-1"></i> My Marks
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <%= currentUri.contains("/student/progress") ? "active fw-semibold" : "" %>"
                       href="<%= request.getContextPath() %>/student/progress">
                        <i class="fas fa-chart-line me-1"></i> Progress
                    </a>
                </li>

            </ul>
            <% } %>

            <!-- ─── FACULTY Nav Links ─────────────────────────────── -->
            <% if ("FACULTY".equals(userRole)) { %>
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">

                <li class="nav-item">
                    <a class="nav-link <%= currentUri.contains("/faculty/dashboard") ? "active fw-semibold" : "" %>"
                       href="<%= request.getContextPath() %>/faculty/dashboard">
                        <i class="fas fa-tachometer-alt me-1"></i> Dashboard
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <%= currentUri.contains("/faculty/mark-attendance") ? "active fw-semibold" : "" %>"
                       href="<%= request.getContextPath() %>/faculty/mark-attendance">
                        <i class="fas fa-clipboard-check me-1"></i> Mark Attendance
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <%= currentUri.contains("/faculty/attendance-report") ? "active fw-semibold" : "" %>"
                       href="<%= request.getContextPath() %>/faculty/attendance-report">
                        <i class="fas fa-file-alt me-1"></i> Reports
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <%= currentUri.contains("/faculty/marks") ? "active fw-semibold" : "" %>"
                       href="<%= request.getContextPath() %>/faculty/marks">
                        <i class="fas fa-pen me-1"></i> Marks
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <%= currentUri.contains("/faculty/monitor") ? "active fw-semibold" : "" %>"
                       href="<%= request.getContextPath() %>/faculty/monitor">
                        <i class="fas fa-users me-1"></i> Students
                    </a>
                </li>

            </ul>
            <% } %>

            <!-- ─── Right: Role Badge + User Dropdown ─────────────── -->
            <ul class="navbar-nav ms-auto mb-2 mb-lg-0 align-items-center gap-2">

                <!-- Role Badge -->
                <li class="nav-item">
                    <span class="badge
                        <%= "STUDENT".equals(userRole) ? "bg-info" : "bg-warning text-dark" %>
                        rounded-pill px-3 py-2">
                        <i class="fas
                           <%= "STUDENT".equals(userRole)
                               ? "fa-user-graduate" : "fa-chalkboard-teacher" %>
                           me-1"></i>
                        <%= userRole %>
                    </span>
                </li>

                <!-- User Dropdown -->
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle d-flex align-items-center gap-2"
                       href="#" id="userDropdown" role="button"
                       data-bs-toggle="dropdown" aria-expanded="false">
                        <div class="rounded-circle bg-white bg-opacity-25 d-flex
                                    align-items-center justify-content-center"
                             style="width:34px;height:34px;font-size:0.9rem;">
                            <i class="fas fa-user text-white"></i>
                        </div>
                        <span class="d-none d-lg-inline fw-medium small">
                            <%= displayName %>
                        </span>
                    </a>

                    <ul class="dropdown-menu dropdown-menu-end shadow border-0 rounded-3 mt-2"
                        style="min-width:220px;"
                        aria-labelledby="userDropdown">

                        <li class="px-3 py-2">
                            <div class="fw-semibold text-dark small"><%= displayName %></div>
                            <div class="text-muted" style="font-size:0.75rem;">
                                <%= "STUDENT".equals(userRole) ? "Student Account" : "Faculty Account" %>
                            </div>
                        </li>
                        <li><hr class="dropdown-divider my-1"></li>

                        <% if ("STUDENT".equals(userRole)) { %>
                        <li>
                            <a class="dropdown-item py-2"
                               href="<%= request.getContextPath() %>/student/profile">
                                <i class="fas fa-id-card me-2 text-primary"></i>My Profile
                            </a>
                        </li>
                        <% } else if ("FACULTY".equals(userRole)) { %>
                        <li>
                            <a class="dropdown-item py-2"
                               href="<%= request.getContextPath() %>/faculty/profile">
                                <i class="fas fa-id-badge me-2 text-primary"></i>My Profile
                            </a>
                        </li>
                        <% } %>

                        <li><hr class="dropdown-divider my-1"></li>

                        <li>
                            <a class="dropdown-item py-2 text-danger"
                               href="<%= request.getContextPath() %>/auth/logout"
                               onclick="return confirm('Are you sure you want to logout?');">
                                <i class="fas fa-sign-out-alt me-2"></i>Logout
                            </a>
                        </li>
                    </ul>
                </li>

            </ul>
        </div>
    </div>
</nav>