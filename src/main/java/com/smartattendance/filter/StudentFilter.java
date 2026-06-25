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
 * StudentFilter - Student Role Authorization Filter
 *
 * Responsibilities:
 *  - Intercepts all requests to /student/* routes.
 *  - Verifies that the logged-in user has the STUDENT role.
 *  - Prevents faculty members from accessing student-only pages.
 *  - Redirects unauthorized users to the error/unauthorized page.
 *
 * This filter runs AFTER AuthFilter (session existence is already verified).
 * AuthFilter ensures the user is logged in.
 * StudentFilter ensures the user is specifically a STUDENT.
 *
 * Mapped in web.xml for: /student/*
 */
public class StudentFilter implements Filter {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(StudentFilter.class);

    // ----------------------------------------------------------------
    // init()
    // ----------------------------------------------------------------

    /**
     * Called once when the filter is initialized.
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("StudentFilter initialized. Protecting /student/* routes for STUDENT role.");
    }

    // ----------------------------------------------------------------
    // doFilter()
    // ----------------------------------------------------------------

    /**
     * Verifies the logged-in user has the STUDENT role.
     *
     * <p>If the user is a faculty member trying to access student routes,
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
        logger.debug("StudentFilter intercepted request: {}", requestURI);

        // session null check is handled by AuthFilter before this filter runs.
        // We check specifically for student session attribute.
        boolean isStudent = (session != null)
                && (session.getAttribute(AuthFilter.SESSION_STUDENT) != null);

        if (isStudent) {
            logger.debug("StudentFilter: STUDENT role confirmed. Access granted to: {}", requestURI);
            filterChain.doFilter(request, response);
        } else {
            logger.warn("StudentFilter: Non-student user attempted to access '{}'. Redirecting to unauthorized.",
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
        logger.info("StudentFilter destroyed.");
    }
}