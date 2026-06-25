package com.smartattendance.servlet.faculty;

import com.smartattendance.dao.FacultyProfileDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.Faculty;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * FacultyProfileServlet.java
 * URL: /faculty/profile  (GET only)
 *
 * Loads and displays the faculty profile view page.
 * Refreshes faculty data from DB to show latest values.
 */
public class FacultyProfileServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(FacultyProfileServlet.class);
    private static final String PROFILE_VIEW = "/WEB-INF/views/faculty/profile.jsp";

    private FacultyProfileDAO profileDAO;

    @Override
    public void init() throws ServletException {
        profileDAO = new FacultyProfileDAO();
        logger.info("FacultyProfileServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Faculty     faculty = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);

        // Refresh from DB
        Faculty fresh = profileDAO.getFacultyById(faculty.getId());
        if (fresh == null) {
            response.sendRedirect(request.getContextPath() + "/faculty/dashboard");
            return;
        }

        // Don't expose password to view
        fresh.setPassword(null);

        String successMsg = request.getParameter("success");
        if (successMsg != null) {
            request.setAttribute("successMessage", successMsg);
        }

        request.setAttribute("faculty", fresh);
        request.getRequestDispatcher(PROFILE_VIEW).forward(request, response);
    }
}