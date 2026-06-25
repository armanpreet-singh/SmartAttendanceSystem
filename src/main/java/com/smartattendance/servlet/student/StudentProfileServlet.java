package com.smartattendance.servlet.student;

import com.smartattendance.dao.StudentDAO;
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
 * StudentProfileServlet.java
 *
 * URL: /student/profile
 *
 * GET  → Load and display student profile (profile.jsp)
 * POST → Process profile update (phone and address only)
 *         → On success: refresh session + redirect with success message
 *         → On failure: forward with error message
 */
public class StudentProfileServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(StudentProfileServlet.class);
    private static final String PROFILE_VIEW = "/WEB-INF/views/student/profile.jsp";

    private StudentDAO studentDAO;

    @Override
    public void init() throws ServletException {
        studentDAO = new StudentDAO();
        logger.info("StudentProfileServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Student student     = (Student) session.getAttribute(AuthFilter.SESSION_STUDENT);

        // Refresh student data from DB to show latest values
        Student freshStudent = studentDAO.getStudentById(student.getId());

        if (freshStudent == null) {
            logger.error("StudentProfileServlet: Student not found in DB for pk={}", student.getId());
            response.sendRedirect(request.getContextPath() + "/student/dashboard");
            return;
        }

        request.setAttribute("student", freshStudent);
        request.getRequestDispatcher(PROFILE_VIEW).forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Student student     = (Student) session.getAttribute(AuthFilter.SESSION_STUDENT);

        String phone   = ValidationUtil.safeTrim(request.getParameter("phone"));
        String address = ValidationUtil.safeTrim(request.getParameter("address"));

        logger.debug("StudentProfileServlet POST: Updating profile for pk={}", student.getId());

        // ── Validate phone if provided ─────────────────────────────
        if (!phone.isEmpty() && !ValidationUtil.isValidPhone(phone)) {
            Student freshStudent = studentDAO.getStudentById(student.getId());
            request.setAttribute("student",       freshStudent);
            request.setAttribute("errorMessage",  "Please enter a valid 10-digit phone number.");
            request.getRequestDispatcher(PROFILE_VIEW).forward(request, response);
            return;
        }

        // ── Update in DB ───────────────────────────────────────────
        boolean updated = studentDAO.updateStudentProfile(
                student.getId(),
                phone.isEmpty()   ? null : phone,
                address.isEmpty() ? null : address
        );

        if (updated) {
            // Refresh session with latest data
            Student refreshedStudent = studentDAO.getStudentById(student.getId());
            if (refreshedStudent != null) {
                session.setAttribute(AuthFilter.SESSION_STUDENT, refreshedStudent);
            }
            logger.info("StudentProfileServlet: Profile updated for pk={}", student.getId());
            response.sendRedirect(request.getContextPath()
                    + "/student/profile?success=Profile+updated+successfully");
        } else {
            Student freshStudent = studentDAO.getStudentById(student.getId());
            request.setAttribute("student",      freshStudent);
            request.setAttribute("errorMessage", "Failed to update profile. Please try again.");
            request.getRequestDispatcher(PROFILE_VIEW).forward(request, response);
        }
    }
}