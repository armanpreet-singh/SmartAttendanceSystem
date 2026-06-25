package com.smartattendance.servlet.auth;

import com.smartattendance.dao.LoginDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.Student;
import com.smartattendance.util.ValidationUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * StudentLoginServlet.java - Student Authentication Controller
 *
 * URL Mapping: /auth/student-login  (defined in web.xml)
 *
 * HTTP Methods:
 *  - GET  : Displays the login page (redirects to login.jsp).
 *  - POST : Processes the student login form submission.
 *
 * POST Flow:
 *  1. Read email and password from the request.
 *  2. Validate inputs (not blank, valid format).
 *  3. Call LoginDAO.authenticateStudent().
 *  4. If success:
 *       - Invalidate any old session (prevent session fixation).
 *       - Create a new session.
 *       - Store Student object in session as "loggedInStudent".
 *       - Store "STUDENT" role in session as "userRole".
 *       - Redirect to /student/dashboard.
 *  5. If failure:
 *       - Set error message as request attribute.
 *       - Forward back to login.jsp.
 *
 * Security:
 *  - Prevents session fixation by invalidating old session before creating new one.
 *  - Normalizes email (lowercase + trim) before DB lookup.
 *  - Does NOT expose why login failed (generic error message for wrong credentials).
 */
public class StudentLoginServlet extends HttpServlet {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(StudentLoginServlet.class);

    // ----------------------------------------------------------------
    // DAO
    // ----------------------------------------------------------------
    private LoginDAO loginDAO;

    // ----------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------

    /** Path to the login view */
    private static final String LOGIN_VIEW = "/WEB-INF/views/auth/login.jsp";

    /** Student dashboard redirect path */
    private static final String STUDENT_DASHBOARD = "/student/dashboard";

    // ----------------------------------------------------------------
    // init()
    // ----------------------------------------------------------------

    /**
     * Initializes the servlet and instantiates the LoginDAO.
     */
    @Override
    public void init() throws ServletException {
        loginDAO = new LoginDAO();
        logger.info("StudentLoginServlet initialized.");
    }

    // ================================================================
    // GET — Show login page
    // ================================================================

    /**
     * Handles GET requests to /auth/student-login.
     *
     * <p>If the student already has an active session, redirect to
     * the dashboard directly. Otherwise, forward to the login page.</p>
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Check if already logged in
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute(AuthFilter.SESSION_STUDENT) != null) {
            logger.debug("StudentLoginServlet GET: Student already logged in. Redirecting to dashboard.");
            response.sendRedirect(request.getContextPath() + STUDENT_DASHBOARD);
            return;
        }

        // Pre-set the active tab to "student" for the login page
        request.setAttribute("activeTab", "student");
        request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
    }

    // ================================================================
    // POST — Process login form
    // ================================================================

    /**
     * Handles POST requests to /auth/student-login.
     *
     * <p>Reads form parameters, validates them, authenticates against
     * the database, and either creates a session or returns an error.</p>
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // ── Step 1: Read form parameters ──────────────────────────
        String email    = request.getParameter("email");
        String password = request.getParameter("password");

        logger.debug("StudentLoginServlet POST: Login attempt for email: {}", email);

        // ── Step 2: Basic Input Validation ────────────────────────
        if (ValidationUtil.isNullOrBlank(email) || ValidationUtil.isNullOrBlank(password)) {
            logger.warn("StudentLoginServlet: Empty email or password submitted.");
            forwardWithError(request, response,
                    "Email and password are required.",
                    "student",
                    email);
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            logger.warn("StudentLoginServlet: Invalid email format: {}", email);
            forwardWithError(request, response,
                    "Please enter a valid email address.",
                    "student",
                    email);
            return;
        }

        // Normalize email: lowercase + trim
        String normalizedEmail = ValidationUtil.normalizeEmail(email);

        // ── Step 3: Authenticate using DAO ────────────────────────
        Student authenticatedStudent = loginDAO.authenticateStudent(normalizedEmail, password);

        // ── Step 4a: Authentication FAILED ───────────────────────
        if (authenticatedStudent == null) {
            logger.warn("StudentLoginServlet: Authentication failed for email: {}", normalizedEmail);
            forwardWithError(request, response,
                    "Invalid email or password. Please try again.",
                    "student",
                    email);
            return;
        }

        // ── Step 4b: Authentication SUCCESS ──────────────────────

        // Security: Invalidate existing session to prevent session fixation attack
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
            logger.debug("StudentLoginServlet: Old session invalidated for security.");
        }

        // Create a fresh session
        HttpSession newSession = request.getSession(true);
        newSession.setMaxInactiveInterval(30 * 60); // 30 minutes

        // Store student object in session (password is already cleared in DAO)
        newSession.setAttribute(AuthFilter.SESSION_STUDENT, authenticatedStudent);
        newSession.setAttribute(AuthFilter.SESSION_ROLE,    AuthFilter.ROLE_STUDENT);

        logger.info("StudentLoginServlet: Session created for student: {} | Session ID: {}",
                authenticatedStudent.getStudentId(),
                newSession.getId());

        // ── Step 5: Redirect to Student Dashboard ─────────────────
        response.sendRedirect(request.getContextPath() + STUDENT_DASHBOARD);
    }

    // ================================================================
    // PRIVATE HELPER: Forward back to login with error message
    // ================================================================

    /**
     * Sets error attributes on the request and forwards back to login.jsp.
     *
     * @param request      The current HTTP request.
     * @param response     The current HTTP response.
     * @param errorMessage The error message to display to the user.
     * @param activeTab    Which tab to show active ("student" or "faculty").
     * @param emailValue   The email value to pre-fill in the form.
     */
    private void forwardWithError(HttpServletRequest  request,
                                  HttpServletResponse response,
                                  String              errorMessage,
                                  String              activeTab,
                                  String              emailValue)
            throws ServletException, IOException {

        request.setAttribute("errorMessage", errorMessage);
        request.setAttribute("activeTab",    activeTab);
        request.setAttribute("emailValue",   emailValue != null ? emailValue : "");
        request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
    }
}