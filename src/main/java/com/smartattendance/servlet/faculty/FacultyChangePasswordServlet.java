package com.smartattendance.servlet.faculty;

import com.smartattendance.dao.FacultyProfileDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.Faculty;
import com.smartattendance.util.PasswordUtil;
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
 * FacultyChangePasswordServlet.java
 * URL: /faculty/change-password
 *
 * GET  → Display change password form
 * POST → Validate and process password change
 *
 * Validation:
 *  - Old password must be correct
 *  - New password must not be blank
 *  - New password and confirm must match
 *  - New password must be different from old
 */
public class FacultyChangePasswordServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(FacultyChangePasswordServlet.class);

    private static final String CHANGE_PWD_VIEW =
            "/WEB-INF/views/faculty/change-password.jsp";

    private FacultyProfileDAO profileDAO;

    @Override
    public void init() throws ServletException {
        profileDAO = new FacultyProfileDAO();
        logger.info("FacultyChangePasswordServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        request.setAttribute("faculty",
                session.getAttribute(AuthFilter.SESSION_FACULTY));
        request.getRequestDispatcher(CHANGE_PWD_VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Faculty     faculty = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);

        String oldPassword     = request.getParameter("oldPassword");
        String newPassword     = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmPassword");

        request.setAttribute("faculty", faculty);

        // ── Validation ─────────────────────────────────────────────
        if (ValidationUtil.isNullOrBlank(oldPassword)
                || ValidationUtil.isNullOrBlank(newPassword)
                || ValidationUtil.isNullOrBlank(confirmPassword)) {
            request.setAttribute("errorMessage", "All password fields are required.");
            request.getRequestDispatcher(CHANGE_PWD_VIEW).forward(request, response);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("errorMessage",
                    "New password and confirm password do not match.");
            request.getRequestDispatcher(CHANGE_PWD_VIEW).forward(request, response);
            return;
        }

        if (newPassword.length() < 6) {
            request.setAttribute("errorMessage",
                    "New password must be at least 6 characters long.");
            request.getRequestDispatcher(CHANGE_PWD_VIEW).forward(request, response);
            return;
        }

        if (oldPassword.equals(newPassword)) {
            request.setAttribute("errorMessage",
                    "New password must be different from the current password.");
            request.getRequestDispatcher(CHANGE_PWD_VIEW).forward(request, response);
            return;
        }

        // ── Attempt password change ────────────────────────────────
        String result = profileDAO.changePassword(faculty.getId(), oldPassword, newPassword);

        switch (result) {
            case "SUCCESS":
                logger.info("FacultyChangePasswordServlet: Password changed for pk={}",
                        faculty.getId());
                response.sendRedirect(request.getContextPath()
                        + "/faculty/profile?success=Password+changed+successfully");
                break;

            case "WRONG_OLD_PASSWORD":
                request.setAttribute("errorMessage",
                        "Current password is incorrect. Please try again.");
                request.getRequestDispatcher(CHANGE_PWD_VIEW).forward(request, response);
                break;

            default:
                request.setAttribute("errorMessage",
                        "Password change failed due to a server error. Please try again.");
                request.getRequestDispatcher(CHANGE_PWD_VIEW).forward(request, response);
                break;
        }
    }
}