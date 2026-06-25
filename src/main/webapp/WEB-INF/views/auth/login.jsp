<%-- ================================================================
     login.jsp - Unified Login Page for Students and Faculty
     
     Features:
       - Bootstrap 5 Tabbed Interface (Student Tab | Faculty Tab).
       - Separate forms for Student and Faculty login.
       - Each form submits to its own servlet URL.
       - Displays error messages from servlet (request attribute).
       - Displays logout success message after logout.
       - Pre-fills email field if validation fails server-side.
       - Show/Hide password toggle.
       - Client-side form validation before submission.
       - Fully responsive design.
     
     Request Attributes Used:
       - errorMessage  : Error message string from servlet.
       - logoutMessage : Logout success message string.
       - activeTab     : "student" or "faculty" (which tab to show).
       - emailValue    : Pre-fill email after failed login.
     
     Form Action URLs:
       - Student form → /auth/student-login  (POST)
       - Faculty form → /auth/faculty-login  (POST)
================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    // Determine which tab should be active
    String activeTab = (String) request.getAttribute("activeTab");
    if (activeTab == null) activeTab = "student";

    // Retrieve messages from servlet
    String errorMessage  = (String) request.getAttribute("errorMessage");
    String logoutMessage = (String) request.getAttribute("logoutMessage");
    String emailValue    = (String) request.getAttribute("emailValue");
    if (emailValue == null) emailValue = "";

    // Prevent logged-in users from accessing login page
    Object studentSession = session.getAttribute("loggedInStudent");
    Object facultySession = session.getAttribute("loggedInFaculty");
    if (studentSession != null) {
        response.sendRedirect(request.getContextPath() + "/student/dashboard");
        return;
    }
    if (facultySession != null) {
        response.sendRedirect(request.getContextPath() + "/faculty/dashboard");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <!-- ============================================================ -->
    <!-- Head Section                                                 -->
    <!-- ============================================================ -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <title>Login | Smart Attendance System</title>

    <!-- Bootstrap 5 CSS -->
    <link
        rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
        integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
        crossorigin="anonymous">

    <!-- Font Awesome -->
    <link
        rel="stylesheet"
        href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css"
        crossorigin="anonymous">

    <!-- Google Fonts -->
    <link
        href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap"
        rel="stylesheet">

    <!-- Custom CSS -->
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/style.css">
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/auth.css">

    <style>
        /* ── Inline Login Page Specific Styles ── */
        body {
            font-family: 'Inter', sans-serif;
            min-height: 100vh;
            background: linear-gradient(135deg, #1a237e 0%, #283593 40%, #3949ab 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1rem;
        }

        .login-wrapper {
            width: 100%;
            max-width: 480px;
        }

        /* College Header */
        .college-header {
            text-align: center;
            margin-bottom: 1.5rem;
            color: white;
        }

        .college-header .app-logo {
            width: 72px;
            height: 72px;
            background: rgba(255, 255, 255, 0.15);
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            margin: 0 auto 0.75rem;
            font-size: 2rem;
            color: white;
            backdrop-filter: blur(4px);
            border: 2px solid rgba(255, 255, 255, 0.3);
        }

        .college-header h1 {
            font-size: 1.4rem;
            font-weight: 700;
            margin-bottom: 0.2rem;
        }

        .college-header p {
            font-size: 0.85rem;
            opacity: 0.85;
            margin: 0;
        }

        /* Login Card */
        .login-card {
            border: none;
            border-radius: 20px;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
            overflow: hidden;
            background: #ffffff;
        }

        .login-card .card-body {
            padding: 2rem 2rem 1.5rem;
        }

        /* Tab styling */
        .nav-tabs {
            border: none;
            gap: 0.5rem;
            padding: 1.25rem 1.5rem 0;
            background: #f8f9fa;
        }

        .nav-tabs .nav-link {
            border: none;
            border-radius: 10px 10px 0 0;
            color: #6c757d;
            font-weight: 500;
            font-size: 0.9rem;
            padding: 0.6rem 1.25rem;
            transition: all 0.2s ease;
        }

        .nav-tabs .nav-link:hover {
            background-color: #e9ecef;
            color: #343a40;
        }

        .nav-tabs .nav-link.active {
            background-color: #ffffff;
            color: #1a237e;
            font-weight: 600;
            box-shadow: 0 -2px 0 #1a237e inset;
        }

        .nav-tabs .nav-link .tab-icon {
            margin-right: 0.4rem;
            font-size: 1rem;
        }

        /* Form fields */
        .form-label {
            font-size: 0.85rem;
            font-weight: 500;
            color: #495057;
            margin-bottom: 0.35rem;
        }

        .form-control {
            border-radius: 10px;
            border: 1.5px solid #dee2e6;
            padding: 0.65rem 1rem;
            font-size: 0.9rem;
            transition: border-color 0.2s ease, box-shadow 0.2s ease;
        }

        .form-control:focus {
            border-color: #3949ab;
            box-shadow: 0 0 0 3px rgba(57, 73, 171, 0.12);
        }

        /* Input group for password */
        .input-group .form-control {
            border-right: none;
            border-radius: 10px 0 0 10px;
        }

        .input-group .btn-password-toggle {
            border: 1.5px solid #dee2e6;
            border-left: none;
            border-radius: 0 10px 10px 0;
            background: #f8f9fa;
            color: #6c757d;
            padding: 0.65rem 0.85rem;
            cursor: pointer;
            transition: color 0.2s ease;
        }

        .input-group .btn-password-toggle:hover {
            color: #3949ab;
        }

        /* Submit Button */
        .btn-login {
            border-radius: 10px;
            font-weight: 600;
            font-size: 0.95rem;
            padding: 0.7rem;
            letter-spacing: 0.3px;
            transition: all 0.2s ease;
        }

        .btn-login-student {
            background: linear-gradient(135deg, #1a237e, #3949ab);
            border: none;
            color: white;
        }

        .btn-login-student:hover {
            background: linear-gradient(135deg, #283593, #3f51b5);
            color: white;
            transform: translateY(-1px);
            box-shadow: 0 4px 15px rgba(26, 35, 126, 0.35);
        }

        .btn-login-faculty {
            background: linear-gradient(135deg, #e65100, #f57c00);
            border: none;
            color: white;
        }

        .btn-login-faculty:hover {
            background: linear-gradient(135deg, #ef6c00, #fb8c00);
            color: white;
            transform: translateY(-1px);
            box-shadow: 0 4px 15px rgba(230, 81, 0, 0.35);
        }

        /* Alert Messages */
        .alert {
            border-radius: 10px;
            font-size: 0.875rem;
            border: none;
        }

        /* Footer text */
        .login-footer {
            text-align: center;
            font-size: 0.8rem;
            color: rgba(255, 255, 255, 0.7);
            margin-top: 1.25rem;
        }

        /* Loading spinner on submit */
        .btn-login .spinner {
            display: none;
        }

        .btn-login.loading .spinner {
            display: inline-block;
        }

        .btn-login.loading .btn-text {
            display: none;
        }
    </style>
</head>
<body>

<!-- ================================================================
     Login Page Wrapper
================================================================ -->
<div class="login-wrapper">

    <!-- ─── College / App Header ─────────────────────────────────── -->
    <div class="college-header">
        <div class="app-logo">
            <i class="fas fa-graduation-cap"></i>
        </div>
        <h1>Smart Attendance System</h1>
        <p>ABC College of Engineering</p>
    </div>

    <!-- ─── Login Card ───────────────────────────────────────────── -->
    <div class="login-card card">

        <!-- ── Global Alert Messages ────────────────────────────── -->
        <div class="px-4 pt-3">

            <!-- Logout Success Alert -->
            <% if (logoutMessage != null && !logoutMessage.isEmpty()) { %>
            <div class="alert alert-success alert-dismissible fade show d-flex align-items-center gap-2"
                 role="alert" id="logoutAlert">
                <i class="fas fa-check-circle fa-lg"></i>
                <div><%= logoutMessage %></div>
                <button type="button" class="btn-close ms-auto" data-bs-dismiss="alert"></button>
            </div>
            <% } %>

        </div>

        <!-- ── Tab Navigation ────────────────────────────────────── -->
        <ul class="nav nav-tabs" id="loginTabs" role="tablist">

            <!-- Student Tab -->
            <li class="nav-item" role="presentation">
                <button
                    class="nav-link <%= "student".equals(activeTab) ? "active" : "" %>"
                    id="student-tab"
                    data-bs-toggle="tab"
                    data-bs-target="#studentPanel"
                    type="button"
                    role="tab"
                    aria-controls="studentPanel"
                    aria-selected="<%= "student".equals(activeTab) ? "true" : "false" %>">
                    <i class="fas fa-user-graduate tab-icon"></i>
                    Student Login
                </button>
            </li>

            <!-- Faculty Tab -->
            <li class="nav-item" role="presentation">
                <button
                    class="nav-link <%= "faculty".equals(activeTab) ? "active" : "" %>"
                    id="faculty-tab"
                    data-bs-toggle="tab"
                    data-bs-target="#facultyPanel"
                    type="button"
                    role="tab"
                    aria-controls="facultyPanel"
                    aria-selected="<%= "faculty".equals(activeTab) ? "true" : "false" %>">
                    <i class="fas fa-chalkboard-teacher tab-icon"></i>
                    Faculty Login
                </button>
            </li>

        </ul>

        <!-- ── Tab Content Panels ─────────────────────────────────── -->
        <div class="tab-content card-body" id="loginTabContent">

            <!-- ================================================== -->
            <!-- STUDENT LOGIN PANEL                                 -->
            <!-- ================================================== -->
            <div
                class="tab-pane fade <%= "student".equals(activeTab) ? "show active" : "" %>"
                id="studentPanel"
                role="tabpanel"
                aria-labelledby="student-tab">

                <h5 class="fw-semibold text-dark mb-1">
                    Welcome Back, Student!
                </h5>
                <p class="text-muted small mb-4">
                    Sign in to view your attendance and academic records.
                </p>

                <!-- Server-side error message -->
                <% if (errorMessage != null && !errorMessage.isEmpty() && "student".equals(activeTab)) { %>
                <div class="alert alert-danger d-flex align-items-center gap-2 mb-3"
                     role="alert" id="studentError">
                    <i class="fas fa-exclamation-circle"></i>
                    <div><%= errorMessage %></div>
                </div>
                <% } %>

                <!-- Student Login Form -->
                <form
                    id="studentLoginForm"
                    action="<%= request.getContextPath() %>/auth/student-login"
                    method="POST"
                    novalidate>

                    <!-- Email Field -->
                    <div class="mb-3">
                        <label for="studentEmail" class="form-label">
                            <i class="fas fa-envelope me-1 text-muted"></i>
                            Email Address
                        </label>
                        <input
                            type="email"
                            class="form-control"
                            id="studentEmail"
                            name="email"
                            value="<%= "student".equals(activeTab) ? emailValue : "" %>"
                            placeholder="yourname@student.edu"
                            autocomplete="email"
                            required>
                        <div class="invalid-feedback">
                            Please enter a valid email address.
                        </div>
                    </div>

                    <!-- Password Field -->
                    <div class="mb-4">
                        <label for="studentPassword" class="form-label">
                            <i class="fas fa-lock me-1 text-muted"></i>
                            Password
                        </label>
                        <div class="input-group">
                            <input
                                type="password"
                                class="form-control"
                                id="studentPassword"
                                name="password"
                                placeholder="Enter your password"
                                autocomplete="current-password"
                                required>
                            <button
                                class="btn btn-password-toggle"
                                type="button"
                                id="toggleStudentPassword"
                                tabindex="-1"
                                title="Show/Hide Password">
                                <i class="fas fa-eye" id="studentEyeIcon"></i>
                            </button>
                        </div>
                        <div class="invalid-feedback">
                            Password is required.
                        </div>
                    </div>

                    <!-- Submit Button -->
                    <div class="d-grid">
                        <button
                            type="submit"
                            class="btn btn-login btn-login-student"
                            id="studentLoginBtn">
                            <span class="spinner spinner-border spinner-border-sm me-2"
                                  role="status" aria-hidden="true"></span>
                            <span class="btn-text">
                                <i class="fas fa-sign-in-alt me-2"></i>
                                Sign In as Student
                            </span>
                        </button>
                    </div>

                </form>

                <!-- Divider -->
                <div class="text-center mt-3">
                    <small class="text-muted">
                        Are you a faculty member?
                        <a href="#"
                           class="text-decoration-none fw-medium"
                           onclick="switchToFacultyTab(); return false;">
                            Faculty Login
                        </a>
                    </small>
                </div>

            </div>
            <!-- ── End Student Panel ──────────────────────────────── -->

            <!-- ================================================== -->
            <!-- FACULTY LOGIN PANEL                                 -->
            <!-- ================================================== -->
            <div
                class="tab-pane fade <%= "faculty".equals(activeTab) ? "show active" : "" %>"
                id="facultyPanel"
                role="tabpanel"
                aria-labelledby="faculty-tab">

                <h5 class="fw-semibold text-dark mb-1">
                    Welcome, Faculty!
                </h5>
                <p class="text-muted small mb-4">
                    Sign in to manage attendance and academic records.
                </p>

                <!-- Server-side error message -->
                <% if (errorMessage != null && !errorMessage.isEmpty() && "faculty".equals(activeTab)) { %>
                <div class="alert alert-danger d-flex align-items-center gap-2 mb-3"
                     role="alert" id="facultyError">
                    <i class="fas fa-exclamation-circle"></i>
                    <div><%= errorMessage %></div>
                </div>
                <% } %>

                <!-- Faculty Login Form -->
                <form
                    id="facultyLoginForm"
                    action="<%= request.getContextPath() %>/auth/faculty-login"
                    method="POST"
                    novalidate>

                    <!-- Email Field -->
                    <div class="mb-3">
                        <label for="facultyEmail" class="form-label">
                            <i class="fas fa-envelope me-1 text-muted"></i>
                            Email Address
                        </label>
                        <input
                            type="email"
                            class="form-control"
                            id="facultyEmail"
                            name="email"
                            value="<%= "faculty".equals(activeTab) ? emailValue : "" %>"
                            placeholder="yourname@college.edu"
                            autocomplete="email"
                            required>
                        <div class="invalid-feedback">
                            Please enter a valid email address.
                        </div>
                    </div>

                    <!-- Password Field -->
                    <div class="mb-4">
                        <label for="facultyPassword" class="form-label">
                            <i class="fas fa-lock me-1 text-muted"></i>
                            Password
                        </label>
                        <div class="input-group">
                            <input
                                type="password"
                                class="form-control"
                                id="facultyPassword"
                                name="password"
                                placeholder="Enter your password"
                                autocomplete="current-password"
                                required>
                            <button
                                class="btn btn-password-toggle"
                                type="button"
                                id="toggleFacultyPassword"
                                tabindex="-1"
                                title="Show/Hide Password">
                                <i class="fas fa-eye" id="facultyEyeIcon"></i>
                            </button>
                        </div>
                        <div class="invalid-feedback">
                            Password is required.
                        </div>
                    </div>

                    <!-- Submit Button -->
                    <div class="d-grid">
                        <button
                            type="submit"
                            class="btn btn-login btn-login-faculty"
                            id="facultyLoginBtn">
                            <span class="spinner spinner-border spinner-border-sm me-2"
                                  role="status" aria-hidden="true"></span>
                            <span class="btn-text">
                                <i class="fas fa-sign-in-alt me-2"></i>
                                Sign In as Faculty
                            </span>
                        </button>
                    </div>

                </form>

                <!-- Divider -->
                <div class="text-center mt-3">
                    <small class="text-muted">
                        Are you a student?
                        <a href="#"
                           class="text-decoration-none fw-medium"
                           onclick="switchToStudentTab(); return false;">
                            Student Login
                        </a>
                    </small>
                </div>

            </div>
            <!-- ── End Faculty Panel ──────────────────────────────── -->

        </div>
        <!-- ── End Tab Content ────────────────────────────────────── -->

    </div>
    <!-- ─── End Login Card ───────────────────────────────────────── -->

    <!-- ─── Footer ───────────────────────────────────────────────── -->
    <div class="login-footer">
        <i class="fas fa-shield-alt me-1"></i>
        Secure Login &nbsp;|&nbsp;
        &copy; <%= java.time.Year.now().getValue() %>
        ABC College of Engineering
    </div>

</div>
<!-- ── End Login Wrapper ──────────────────────────────────────────── -->

<!-- ================================================================
     JavaScript
================================================================ -->
<!-- Bootstrap 5 JS Bundle -->
<script
    src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
    integrity="sha384-YvpcrYf0tY3lHB60NNkmXc4s9bIOgUxi8T/jzmOXLVrFEv1GnUQ7uJRQOSjRwI"
    crossorigin="anonymous">
</script>

<script>
/* ================================================================
   Login Page JavaScript
================================================================ */

    // ── Show/Hide Password Toggle ─────────────────────────────────

    /**
     * Toggles password visibility for a given input and eye icon.
     * @param {string} inputId   - The ID of the password input field.
     * @param {string} iconId    - The ID of the Font Awesome eye icon.
     */
    function togglePassword(inputId, iconId) {
        const passwordInput = document.getElementById(inputId);
        const eyeIcon       = document.getElementById(iconId);

        if (passwordInput.type === 'password') {
            passwordInput.type = 'text';
            eyeIcon.classList.remove('fa-eye');
            eyeIcon.classList.add('fa-eye-slash');
        } else {
            passwordInput.type = 'password';
            eyeIcon.classList.remove('fa-eye-slash');
            eyeIcon.classList.add('fa-eye');
        }
    }

    // Student password toggle
    document.getElementById('toggleStudentPassword')
        .addEventListener('click', function () {
            togglePassword('studentPassword', 'studentEyeIcon');
        });

    // Faculty password toggle
    document.getElementById('toggleFacultyPassword')
        .addEventListener('click', function () {
            togglePassword('facultyPassword', 'facultyEyeIcon');
        });


    // ── Tab Switch Helpers ────────────────────────────────────────

    /**
     * Programmatically switch to the Faculty Login tab.
     */
    function switchToFacultyTab() {
        const facultyTab = document.getElementById('faculty-tab');
        const bsTab = new bootstrap.Tab(facultyTab);
        bsTab.show();
    }

    /**
     * Programmatically switch to the Student Login tab.
     */
    function switchToStudentTab() {
        const studentTab = document.getElementById('student-tab');
        const bsTab = new bootstrap.Tab(studentTab);
        bsTab.show();
    }


    // ── Client-Side Form Validation ───────────────────────────────

    /**
     * Validates a login form before submission.
     * Uses Bootstrap 5's built-in validation classes.
     *
     * @param {HTMLFormElement} form    - The form to validate.
     * @param {HTMLElement}     button  - The submit button (for loading state).
     * @returns {boolean} - True if valid, false otherwise.
     */
    function validateLoginForm(form, button) {
        // Remove previous validation state
        form.classList.remove('was-validated');

        const email    = form.querySelector('input[name="email"]');
        const password = form.querySelector('input[name="password"]');
        let   isValid  = true;

        // Validate email
        if (!email.value.trim()) {
            email.setCustomValidity('Email is required');
            isValid = false;
        } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value.trim())) {
            email.setCustomValidity('Please enter a valid email address');
            isValid = false;
        } else {
            email.setCustomValidity('');
        }

        // Validate password
        if (!password.value.trim()) {
            password.setCustomValidity('Password is required');
            isValid = false;
        } else {
            password.setCustomValidity('');
        }

        // Show Bootstrap validation feedback
        form.classList.add('was-validated');

        if (isValid) {
            // Show loading spinner
            button.classList.add('loading');
            button.disabled = true;
        }

        return isValid;
    }

    // Student form submit listener
    document.getElementById('studentLoginForm')
        .addEventListener('submit', function (event) {
            const btn = document.getElementById('studentLoginBtn');
            if (!validateLoginForm(this, btn)) {
                event.preventDefault();
                event.stopPropagation();
            }
        });

    // Faculty form submit listener
    document.getElementById('facultyLoginForm')
        .addEventListener('submit', function (event) {
            const btn = document.getElementById('facultyLoginBtn');
            if (!validateLoginForm(this, btn)) {
                event.preventDefault();
                event.stopPropagation();
            }
        });


    // ── Auto-dismiss logout alert after 4 seconds ─────────────────
    const logoutAlert = document.getElementById('logoutAlert');
    if (logoutAlert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(logoutAlert);
            bsAlert.close();
        }, 4000);
    }


    // ── Reset custom validity on user input ───────────────────────
    // Removes red border as soon as user starts typing

    document.querySelectorAll('.form-control').forEach(function (input) {
        input.addEventListener('input', function () {
            this.setCustomValidity('');
            this.closest('form').classList.remove('was-validated');
        });
    });

</script>

</body>
</html>