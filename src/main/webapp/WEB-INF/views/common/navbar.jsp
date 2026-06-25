<%-- ================================================================
     navbar.jsp - Responsive Navigation Bar
     
     Purpose:
       - Included on all authenticated pages.
       - Displays different nav links based on the user role
         (STUDENT or FACULTY).
       - Shows logged-in user's name and profile options.
       - Provides a Logout button.
     
     Session Attributes Used:
       - loggedInStudent : com.smartattendance.model.Student object
       - loggedInFaculty : com.smartattendance.model.Faculty object
       - userRole        : "STUDENT" or "FACULTY" string
================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    // Retrieve session attributes for conditional rendering
    Object loggedInStudent = session.getAttribute("loggedInStudent");
    Object loggedInFaculty = session.getAttribute("loggedInFaculty");
    String userRole        = (String) session.getAttribute("userRole");

    // Determine display name for the navbar
    String displayName = "User";
    if (loggedInStudent != null) {
        displayName = ((com.smartattendance.model.Student) loggedInStudent).getFirstName()
                    + " "
                    + ((com.smartattendance.model.Student) loggedInStudent).getLastName();
    } else if (loggedInFaculty != null) {
        displayName = ((com.smartattendance.model.Faculty) loggedInFaculty).getFirstName()
                    + " "
                    + ((com.smartattendance.model.Faculty) loggedInFaculty).getLastName();
    }
%>

<!-- ================================================================
     Bootstrap 5 Responsive Navbar
================================================================ -->
<nav class="navbar navbar-expand-lg navbar-dark app-navbar sticky-top shadow-sm">
    <div class="container-fluid">

        <!-- ─── Brand / Logo ───────────────────────────────────── -->
        <a class="navbar-brand d-flex align-items-center gap-2" href="#">
            <img src="<%= request.getContextPath() %>/assets/images/logo.png"
                 alt="Logo"
                 width="32"
                 height="32"
                 class="rounded-circle">
            <span class="fw-semibold">Smart Attendance</span>
        </a>

        <!-- ─── Mobile Toggle Button ───────────────────────────── -->
        <button class="navbar-toggler border-0"
                type="button"
                data-bs-toggle="collapse"
                data-bs-target="#mainNavbar"
                aria-controls="mainNavbar"
                aria-expanded="false"
                aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>

        <!-- ─── Collapsible Nav Content ────────────────────────── -->
        <div class="collapse navbar-collapse" id="mainNavbar">

            <!-- ─── STUDENT Navigation Links ────────────────── -->
            <% if ("STUDENT".equals(userRole)) { %>
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">

                <li class="nav-item">
                    <a class="nav-link"
                       href="<%= request.getContextPath() %>/student/dashboard">
                        <i class="fas fa-tachometer-alt me-1"></i> Dashboard
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link"
                       href="<%= request.getContextPath() %>/student/attendance">
                        <i class="fas fa-calendar-check me-1"></i> Attendance
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link"
                       href="<%= request.getContextPath() %>/student/marks">
                        <i class="fas fa-star me-1"></i> My Marks
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link"
                       href="<%= request.getContextPath() %>/student/progress">
                        <i class="fas fa-chart-line me-1"></i> Progress
                    </a>
                </li>

            </ul>
            <% } %>

            <!-- ─── FACULTY Navigation Links ─────────────────── -->
            <% if ("FACULTY".equals(userRole)) { %>
            <ul class="navbar-nav me-auto mb-2 mb-lg-0">

                <li class="nav-item">
                    <a class="nav-link"
                       href="<%= request.getContextPath() %>/faculty/dashboard">
                        <i class="fas fa-tachometer-alt me-1"></i> Dashboard
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link"
                       href="<%= request.getContextPath() %>/faculty/mark-attendance">
                        <i class="fas fa-clipboard-check me-1"></i> Mark Attendance
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link"
                       href="<%= request.getContextPath() %>/faculty/attendance-report">
                        <i class="fas fa-file-alt me-1"></i> Attendance Report
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link"
                       href="<%= request.getContextPath() %>/faculty/marks">
                        <i class="fas fa-pen me-1"></i> Manage Marks
                    </a>
                </li>

                <li class="nav-item">
                    <a class="nav-link"
                       href="<%= request.getContextPath() %>/faculty/monitor">
                        <i class="fas fa-users me-1"></i> Monitor Students
                    </a>
                </li>

            </ul>
            <% } %>

            <!-- ─── Right Side: User Dropdown ────────────────── -->
            <ul class="navbar-nav ms-auto mb-2 mb-lg-0 align-items-center">

                <!-- Role Badge -->
                <li class="nav-item me-2">
                    <span class="badge
                        <%= "STUDENT".equals(userRole) ? "bg-info" : "bg-warning text-dark" %>
                        rounded-pill">
                        <i class="fas
                           <%= "STUDENT".equals(userRole)
                               ? "fa-user-graduate"
                               : "fa-chalkboard-teacher" %>
                           me-1"></i>
                        <%= userRole %>
                    </span>
                </li>

                <!-- User Dropdown -->
                <li class="nav-item dropdown">
                    <a class="nav-link dropdown-toggle d-flex align-items-center gap-2"
                       href="#"
                       id="userDropdown"
                       role="button"
                       data-bs-toggle="dropdown"
                       aria-expanded="false">

                        <img src="<%= request.getContextPath() %>/assets/images/default-avatar.png"
                             alt="Avatar"
                             width="32"
                             height="32"
                             class="rounded-circle border border-2 border-light">

                        <span class="d-none d-lg-inline fw-medium">
                            <%= displayName %>
                        </span>
                    </a>

                    <!-- Dropdown Menu -->
                    <ul class="dropdown-menu dropdown-menu-end shadow-sm border-0"
                        aria-labelledby="userDropdown">

                        <li>
                            <h6 class="dropdown-header text-muted">
                                <i class="fas fa-user-circle me-1"></i>
                                <%= displayName %>
                            </h6>
                        </li>

                        <li><hr class="dropdown-divider"></li>

                        <!-- Profile Link: conditional on role -->
                        <% if ("STUDENT".equals(userRole)) { %>
                        <li>
                            <a class="dropdown-item"
                               href="<%= request.getContextPath() %>/student/profile">
                                <i class="fas fa-id-card me-2 text-primary"></i>
                                My Profile
                            </a>
                        </li>
                        <% } else if ("FACULTY".equals(userRole)) { %>
                        <li>
                            <a class="dropdown-item"
                               href="<%= request.getContextPath() %>/faculty/profile">
                                <i class="fas fa-id-badge me-2 text-primary"></i>
                                My Profile
                            </a>
                        </li>
                        <% } %>

                        <li><hr class="dropdown-divider"></li>

                        <!-- Logout -->
                        <li>
                            <a class="dropdown-item text-danger"
                               href="<%= request.getContextPath() %>/auth/logout"
                               onclick="return confirm('Are you sure you want to logout?');">
                                <i class="fas fa-sign-out-alt me-2"></i>
                                Logout
                            </a>
                        </li>

                    </ul>
                </li>

            </ul>
            <!-- ─── End Right Side ───────────────────────────── -->

        </div>
        <!-- ─── End Collapse ────────────────────────────────────── -->

    </div>
</nav>
<!-- ================================================================
     End Navbar
================================================================ -->