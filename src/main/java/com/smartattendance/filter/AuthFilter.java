package com.smartattendance.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * AuthFilter - Authentication Filter (Security Gate)
 *
 * Responsibilities:
 *  - Intercepts all requests to protected routes (/student/*, /faculty/*).
 *  - Checks whether the user has an active authenticated session.
 *  - Redirects unauthenticated users to the login page.
 *  - Allows authenticated users to pass through to the requested resource.
 *
 * Session Attributes Checked:
 *  - "loggedInStudent" : Set on successful student login.
 *  - "loggedInFaculty" : Set on successful faculty login.
 *
 * Mapped in web.xml for: /student/* and /faculty/*
 */
public class AuthFilter implements Filter {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(AuthFilter.class);

    // ----------------------------------------------------------------
    // Session Attribute Constants
    // ----------------------------------------------------------------
    public static final String SESSION_STUDENT = "loggedInStudent";
    public static final String SESSION_FACULTY = "loggedInFaculty";
    public static final String SESSION_ROLE    = "userRole";

    // ----------------------------------------------------------------
    // Role Constants
    // ----------------------------------------------------------------
    public static final String ROLE_STUDENT = "STUDENT";
    public static final String ROLE_FACULTY = "FACULTY";

    // ----------------------------------------------------------------
    // init()
    // ----------------------------------------------------------------

    /**
     * Called once when the filter is initialized by the servlet container.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("AuthFilter initialized. Protecting /student/* and /faculty/* routes.");
    }

    // ----------------------------------------------------------------
    // doFilter()
    // Core filtering logic.
    // ----------------------------------------------------------------

    /**
     * Intercepts the request and checks for an authenticated session.
     *
     * <p>If the user has a valid session, the request is forwarded.
     * Otherwise, the user is redirected to the login page.</p>
     */
    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain)
            throws IOException, ServletException {

        // Cast to HTTP-specific types
        HttpServletRequest  request  = (HttpServletRequest)  servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        // Retrieve the current session (do NOT create a new one)
        HttpSession session = request.getSession(false);

        String requestURI = request.getRequestURI();
        logger.debug("AuthFilter intercepted request: {}", requestURI);

        // Check if session exists and has an authenticated user
        boolean isAuthenticated = (session != null)
                && (session.getAttribute(SESSION_STUDENT) != null
                ||  session.getAttribute(SESSION_FACULTY) != null);

        if (isAuthenticated) {
            logger.debug("AuthFilter: Authenticated user. Allowing access to: {}", requestURI);
            filterChain.doFilter(request, response);
        } else {
            logger.warn("AuthFilter: Unauthenticated request to '{}'. Redirecting to login.", requestURI);

            // Store the originally requested URL so we can redirect after login
            request.getSession(true).setAttribute("redirectAfterLogin", requestURI);

            // Redirect to the main login selection page
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        }
    }

    // ----------------------------------------------------------------
    // destroy()
    // ----------------------------------------------------------------

    /**
     * Called once when the filter is taken out of service.
     */
    @Override
    public void destroy() {
        logger.info("AuthFilter destroyed.");
    }
}