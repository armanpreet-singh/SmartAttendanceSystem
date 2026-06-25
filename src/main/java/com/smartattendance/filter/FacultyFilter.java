package com.smartattendance.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * FacultyFilter - Faculty Role Authorization Filter
 *
 * Responsibilities:
 *  - Intercepts all requests to /faculty/* routes.
 *  - Verifies that the logged-in user has the FACULTY role.
 *  - Prevents students from accessing faculty-only pages.
 *  - Redirects unauthorized users to the error/unauthorized page.
 *
 * This filter runs AFTER AuthFilter (session existence is already verified).
 * AuthFilter ensures the user is logged in.
 * FacultyFilter ensures the user is specifically a FACULTY member.
 *
 * Mapped in web.xml for: /faculty/*
 */
public class FacultyFilter implements Filter {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(FacultyFilter.class);

    // ----------------------------------------------------------------
    // init()
    // ----------------------------------------------------------------

    /**
     * Called once when the filter is initialized.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("FacultyFilter initialized. Protecting /faculty/* routes for FACULTY role.");
    }

    // ----------------------------------------------------------------
    // doFilter()
    // ----------------------------------------------------------------

    /**
     * Verifies the logged-in user has the FACULTY role.
     *
     * <p>If a student attempts to access faculty routes,
     * they are redirected to the unauthorized error page.</p>
     */
    @Override
    public void doFilter(
            ServletRequest servletRequest,
            ServletResponse servletResponse,
            FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest  request  = (HttpServletRequest)  servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession         session  = request.getSession(false);

        String requestURI = request.getRequestURI();
        logger.debug("FacultyFilter intercepted request: {}", requestURI);

        // session null check is handled by AuthFilter before this filter runs.
        // We check specifically for faculty session attribute.
        boolean isFaculty = (session != null)
                && (session.getAttribute(AuthFilter.SESSION_FACULTY) != null);

        if (isFaculty) {
            logger.debug("FacultyFilter: FACULTY role confirmed. Access granted to: {}", requestURI);
            filterChain.doFilter(request, response);
        } else {
            logger.warn("FacultyFilter: Non-faculty user attempted to access '{}'. Redirecting to unauthorized.",
                    requestURI);
            response.sendRedirect(
                    request.getContextPath() + "/WEB-INF/views/auth/unauthorized.jsp"
            );
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
        logger.info("FacultyFilter destroyed.");
    }
}