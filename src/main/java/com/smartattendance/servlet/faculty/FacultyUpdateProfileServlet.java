package com.smartattendance.servlet.faculty;

import com.smartattendance.dao.FacultyProfileDAO;
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
 * FacultyUpdateProfileServlet.java
 * URL: /faculty/update-profile  (POST only)
 *
 * Handles profile edit form submission.
 * Updates phone and designation.
 * Refreshes session after successful update.
 */
public class FacultyUpdateProfileServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(FacultyUpdateProfileServlet.class);

    private static final String EDIT_VIEW = "/WEB-INF/views/faculty/edit-profile.jsp";

    private FacultyProfileDAO profileDAO;

    @Override
    public void init() throws ServletException {
        profileDAO = new FacultyProfileDAO();
        logger.info("FacultyUpdateProfileServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Faculty     faculty = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);

        Faculty fresh = profileDAO.getFacultyById(faculty.getId());
        if (fresh != null) fresh.setPassword(null);

        request.setAttribute("faculty", fresh);
        request.getRequestDispatcher(EDIT_VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Faculty     faculty = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);

        String phone       = ValidationUtil.safeTrim(request.getParameter("phone"));
        String designation = ValidationUtil.safeTrim(request.getParameter("designation"));

        // ── Validation ─────────────────────────────────────────────
        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            Faculty fresh = profileDAO.getFacultyById(faculty.getId());
            if (fresh != null) fresh.setPassword(null);
            request.setAttribute("faculty",       fresh);
            request.setAttribute("errorMessage",  "Please enter a valid 10-digit phone number.");
            request.getRequestDispatcher(EDIT_VIEW).forward(request, response);
            return;
        }

        if (ValidationUtil.isNullOrBlank(designation)) {
            Faculty fresh = profileDAO.getFacultyById(faculty.getId());
            if (fresh != null) fresh.setPassword(null);
            request.setAttribute("faculty",      fresh);
            request.setAttribute("errorMessage", "Designation cannot be empty.");
            request.getRequestDispatcher(EDIT_VIEW).forward(request, response);
            return;
        }

        // ── Update ─────────────────────────────────────────────────
        boolean updated = profileDAO.updateProfile(
                faculty.getId(),
                phone.isEmpty() ? null : phone,
                designation);

        if (updated) {
            // Refresh session
            Faculty refreshed = profileDAO.getFacultyById(faculty.getId());
            if (refreshed != null) {
                refreshed.setPassword(null);
                session.setAttribute(AuthFilter.SESSION_FACULTY, refreshed);
            }
            logger.info("FacultyUpdateProfileServlet: Updated profile for facultyId={}",
                    faculty.getId());
            response.sendRedirect(request.getContextPath()
                    + "/faculty/profile?success=Profile+updated+successfully");
        } else {
            Faculty fresh = profileDAO.getFacultyById(faculty.getId());
            if (fresh != null) fresh.setPassword(null);
            request.setAttribute("faculty",      fresh);
            request.setAttribute("errorMessage", "Update failed. Please try again.");
            request.getRequestDispatcher(EDIT_VIEW).forward(request, response);
        }
    }
}