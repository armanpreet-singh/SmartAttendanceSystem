package com.smartattendance.servlet.auth;

import com.smartattendance.dao.LoginDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.Faculty;
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
 * FacultyLoginServlet.java - Faculty Authentication Controller
 *
 * URL Mapping: /auth/faculty-login  (defined in web.xml)
 *
 * HTTP Methods:
 *  - GET  : Displays the login page (redirects to login.jsp with faculty tab active).
 *  - POST : Processes the faculty login form submission.
 *
 * POST Flow:
 *  1. Read email and password from the request.
 *  2. Validate inputs (not blank, valid format).
 *  3. Call LoginDAO.authenticateFaculty().
 *  4. If success:
 *       - Invalidate any old session (prevent session fixation).
 *       - Create a new session.
 *       - Store Faculty object in session as "loggedInFaculty".
 *       - Store "FACULTY" role in session as "userRole".
 *       - Redirect to /faculty/dashboard.
 *  5. If failure:
 *       - Set error message as request attribute.
 *       - Forward back to login.jsp with faculty tab active.
 *
 * Security:
 *  - Prevents session fixation by invalidating old session before creating new one.
 *  - Normalizes email (lowercase + trim) before DB lookup.
 *  - Does NOT expose why login failed (generic error message for wrong credentials).
 */
public class FacultyLoginServlet extends HttpServlet {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(FacultyLoginServlet.class);

    // ----------------------------------------------------------------
    // DAO
    // ----------------------------------------------------------------
    private LoginDAO loginDAO;

    // ----------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------

    /** Path to the login view */
    private static final String LOGIN_VIEW = "/WEB-INF/views/auth/login.jsp";

    /** Faculty dashboard redirect path */
    private static final String FACULTY_DASHBOARD = "/faculty/dashboard";

    // ----------------------------------------------------------------
    // init()
    // ----------------------------------------------------------------

    /**
     * Initializes the servlet and instantiates the LoginDAO.
     */
    @Override
    public void init() throws ServletException {
        loginDAO = new LoginDAO();
        logger.info("FacultyLoginServlet initialized.");
    }

    // ================================================================
    // GET — Show login page with faculty tab active
    // ================================================================

    /**
     * Handles GET requests to /auth/faculty-login.
     *
     * <p>If the faculty already has an active session, redirect to
     * the dashboard directly. Otherwise, forward to the login page
     * with the faculty tab pre-selected.</p>
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        // Check if already logged in
        HttpSession session = request.getSession(false);

        if (session != null && session.getAttribute(AuthFilter.SESSION_FACULTY) != null) {
            logger.debug("FacultyLoginServlet GET: Faculty already logged in. Redirecting to dashboard.");
            response.sendRedirect(request.getContextPath() + FACULTY_DASHBOARD);
            return;
        }

        // Pre-set the active tab to "faculty" for the login page
        request.setAttribute("activeTab", "faculty");
        request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
    }

    // ================================================================
    // POST — Process login form
    // ================================================================

    /**
     * Handles POST requests to /auth/faculty-login.
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

        logger.debug("FacultyLoginServlet POST: Login attempt for email: {}", email);

        // ── Step 2: Basic Input Validation ────────────────────────
        if (ValidationUtil.isNullOrBlank(email) || ValidationUtil.isNullOrBlank(password)) {
            logger.warn("FacultyLoginServlet: Empty email or password submitted.");
            forwardWithError(request, response,
                    "Email and password are required.",
                    "faculty",
                    email);
            return;
        }

        if (!ValidationUtil.isValidEmail(email)) {
            logger.warn("FacultyLoginServlet: Invalid email format: {}", email);
            forwardWithError(request, response,
                    "Please enter a valid email address.",
                    "faculty",
                    email);
            return;
        }

        // Normalize email: lowercase + trim
        String normalizedEmail = ValidationUtil.normalizeEmail(email);

        // ── Step 3: Authenticate using DAO ────────────────────────
        Faculty authenticatedFaculty = loginDAO.authenticateFaculty(normalizedEmail, password);

        // ── Step 4a: Authentication FAILED ───────────────────────
        if (authenticatedFaculty == null) {
            logger.warn("FacultyLoginServlet: Authentication failed for email: {}", normalizedEmail);
            forwardWithError(request, response,
                    "Invalid email or password. Please try again.",
                    "faculty",
                    email);
            return;
        }

        // ── Step 4b: Authentication SUCCESS ──────────────────────

        // Security: Invalidate existing session to prevent session fixation attack
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
            logger.debug("FacultyLoginServlet: Old session invalidated for security.");
        }

        // Create a fresh session
        HttpSession newSession = request.getSession(true);
        newSession.setMaxInactiveInterval(30 * 60); // 30 minutes

        // Store faculty object in session (password is already cleared in DAO)
        newSession.setAttribute(AuthFilter.SESSION_FACULTY, authenticatedFaculty);
        newSession.setAttribute(AuthFilter.SESSION_ROLE,    AuthFilter.ROLE_FACULTY);

        logger.info("FacultyLoginServlet: Session created for faculty: {} | Session ID: {}",
                authenticatedFaculty.getFacultyId(),
                newSession.getId());

        // ── Step 5: Redirect to Faculty Dashboard ─────────────────
        response.sendRedirect(request.getContextPath() + FACULTY_DASHBOARD);
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