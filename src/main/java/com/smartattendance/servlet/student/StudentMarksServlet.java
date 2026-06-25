package com.smartattendance.servlet.student;

import com.smartattendance.dao.StudentDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.Marks;
import com.smartattendance.model.Student;
import com.smartattendance.model.Subject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * StudentMarksServlet.java
 *
 * URL: /student/marks
 *
 * Loads all marks for the student's current academic year.
 * Groups marks by subject code for easy table rendering in JSP.
 *
 * Sends to marks.jsp:
 *  - Raw marks list
 *  - marksGrouped: Map<subjectCode, List<Marks>> for subject-wise tables
 *  - Enrolled subjects list (for filter dropdown)
 *  - Subject filter (optional ?subjectId=X)
 */
public class StudentMarksServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(StudentMarksServlet.class);
    private static final String MARKS_VIEW = "/WEB-INF/views/student/marks.jsp";

    private StudentDAO studentDAO;

    @Override
    public void init() throws ServletException {
        studentDAO = new StudentDAO();
        logger.info("StudentMarksServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        Student student     = (Student) session.getAttribute(AuthFilter.SESSION_STUDENT);

        int studentPk      = student.getId();
        int academicYearId = student.getAcademicYearId();

        logger.debug("StudentMarksServlet: Loading marks for student pk={}", studentPk);

        // ── Fetch all marks ────────────────────────────────────────
        List<Marks>  allMarks = studentDAO.getAllMarksByStudent(studentPk, academicYearId);
        List<Subject> subjects = studentDAO.getEnrolledSubjects(studentPk);

        // ── Group marks by subject code ────────────────────────────
        Map<String, List<Marks>> marksGrouped = new LinkedHashMap<>();
        Map<String, String>      subjectNames = new LinkedHashMap<>();

        for (Marks m : allMarks) {
            String code = m.getSubjectCode();
            marksGrouped.computeIfAbsent(code, k -> new ArrayList<>()).add(m);
            subjectNames.put(code, m.getSubjectName());
        }

        // ── Calculate subject-wise totals ──────────────────────────
        Map<String, Double> subjectTotalObtained = new LinkedHashMap<>();
        Map<String, Integer> subjectTotalMax     = new LinkedHashMap<>();

        for (Map.Entry<String, List<Marks>> entry : marksGrouped.entrySet()) {
            double totalObtained = 0;
            int    totalMax      = 0;
            for (Marks m : entry.getValue()) {
                totalObtained += m.getMarksObtained();
                totalMax      += m.getMaxMarks();
            }
            subjectTotalObtained.put(entry.getKey(), totalObtained);
            subjectTotalMax.put(entry.getKey(), totalMax);
        }

        // ── Set request attributes ─────────────────────────────────
        request.setAttribute("student",               student);
        request.setAttribute("allMarks",              allMarks);
        request.setAttribute("marksGrouped",          marksGrouped);
        request.setAttribute("subjectNames",          subjectNames);
        request.setAttribute("subjectTotalObtained",  subjectTotalObtained);
        request.setAttribute("subjectTotalMax",       subjectTotalMax);
        request.setAttribute("subjects",              subjects);

        request.getRequestDispatcher(MARKS_VIEW).forward(request, response);
    }
}