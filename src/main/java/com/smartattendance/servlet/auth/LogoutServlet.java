package com.smartattendance.servlet.auth;

import com.smartattendance.filter.AuthFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * LogoutServlet.java - Logout Controller
 *
 * URL Mapping: /auth/logout  (defined in web.xml)
 *
 * HTTP Methods:
 *  - GET  : Processes logout and redirects to login page.
 *  - POST : Same as GET (supports both form submission and link clicks).
 *
 * Logout Flow:
 *  1. Retrieve existing session (do NOT create a new one).
 *  2. Log who is logging out (for audit purposes).
 *  3. Remove all session attributes explicitly.
 *  4. Invalidate the session completely.
 *  5. Prevent browser from caching the post-logout response.
 *  6. Redirect to login.jsp with a logout success message.
 *
 * Security:
 *  - Fully invalidates the session (not just removing attributes).
 *  - Sets Cache-Control headers to prevent browser back-button exploit.
 *  - Works for both Student and Faculty sessions.
 */
public class LogoutServlet extends HttpServlet {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(LogoutServlet.class);

    // ----------------------------------------------------------------
    // Constants
    // ----------------------------------------------------------------

    /** Path to the login view */
    private static final String LOGIN_VIEW = "/WEB-INF/views/auth/login.jsp";

    // ================================================================
    // GET — Process logout
    // ================================================================

    /**
     * Handles GET requests to /auth/logout.
     * Invalidates the session and redirects to the login page.
     */
    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        processLogout(request, response);
    }

    // ================================================================
    // POST — Process logout (supports form-based logout)
    // ================================================================

    /**
     * Handles POST requests to /auth/logout.
     * Delegates to the same logout logic as GET.
     */
    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        processLogout(request, response);
    }

    // ================================================================
    // PRIVATE: Core Logout Logic
    // ================================================================

    /**
     * Performs the complete logout operation.
     *
     * <ol>
     *   <li>Identifies the user type from session.</li>
     *   <li>Logs the logout event.</li>
     *   <li>Removes session attributes.</li>
     *   <li>Invalidates the session.</li>
     *   <li>Sets no-cache headers.</li>
     *   <li>Forwards to login page with success message.</li>
     * </ol>
     *
     * @param request  The HTTP request.
     * @param response The HTTP response.
     */
    private void processLogout(HttpServletRequest  request,
                               HttpServletResponse response)
            throws ServletException, IOException {

        // ── Step 1: Get the current session (do NOT create a new one) ──
        HttpSession session = request.getSession(false);

        if (session != null) {

            // ── Step 2: Identify and log who is logging out ──────
            String userIdentifier = "Unknown User";
            String userRole       = (String) session.getAttribute(AuthFilter.SESSION_ROLE);

            if (AuthFilter.ROLE_STUDENT.equals(userRole)) {
                com.smartattendance.model.Student student =
                        (com.smartattendance.model.Student)
                                session.getAttribute(AuthFilter.SESSION_STUDENT);
                if (student != null) {
                    userIdentifier = "Student [" + student.getStudentId()
                            + " - " + student.getFullName() + "]";
                }
            } else if (AuthFilter.ROLE_FACULTY.equals(userRole)) {
                com.smartattendance.model.Faculty faculty =
                        (com.smartattendance.model.Faculty)
                                session.getAttribute(AuthFilter.SESSION_FACULTY);
                if (faculty != null) {
                    userIdentifier = "Faculty [" + faculty.getFacultyId()
                            + " - " + faculty.getFullName() + "]";
                }
            }

            logger.info("Logout initiated for: {} | Session ID: {}",
                    userIdentifier, session.getId());

            // ── Step 3: Remove all session attributes explicitly ──
            session.removeAttribute(AuthFilter.SESSION_STUDENT);
            session.removeAttribute(AuthFilter.SESSION_FACULTY);
            session.removeAttribute(AuthFilter.SESSION_ROLE);
            session.removeAttribute("redirectAfterLogin");

            // ── Step 4: Invalidate the session completely ─────────
            session.invalidate();

            logger.info("Session invalidated successfully for: {}", userIdentifier);

        } else {
            logger.debug("LogoutServlet: No active session found. User may already be logged out.");
        }

        // ── Step 5: Set no-cache headers to prevent back-button exploit ──
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma",        "no-cache");
        response.setDateHeader("Expires",   0);

        // ── Step 6: Forward to login page with logout success message ──
        request.setAttribute("logoutMessage", "You have been logged out successfully.");
        request.setAttribute("activeTab",     "student");
        request.getRequestDispatcher(LOGIN_VIEW).forward(request, response);
    }
}