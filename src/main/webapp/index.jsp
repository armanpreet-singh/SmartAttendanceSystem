<%-- ================================================================
     index.jsp - Application Entry Point
     
     Purpose:
       - Acts as the root landing page.
       - Checks if the user already has an active session.
       - Redirects authenticated users to their respective dashboards.
       - Redirects new/unauthenticated users to the login page.
     
     Flow:
       Student  session active → /student/dashboard
       Faculty  session active → /faculty/dashboard
       No session              → /WEB-INF/views/auth/login.jsp
================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page session="true" %>
<%
    // ----------------------------------------------------------------
    // Check for existing authenticated session
    // ----------------------------------------------------------------
    Object loggedInStudent = session.getAttribute("loggedInStudent");
    Object loggedInFaculty = session.getAttribute("loggedInFaculty");

    if (loggedInStudent != null) {
        // Student is already logged in → redirect to student dashboard
        response.sendRedirect(request.getContextPath() + "/student/dashboard");
        return;
    }

    if (loggedInFaculty != null) {
        // Faculty is already logged in → redirect to faculty dashboard
        response.sendRedirect(request.getContextPath() + "/faculty/dashboard");
        return;
    }

    // No active session → redirect to login page
    response.sendRedirect(request.getContextPath() + "/WEB-INF/views/auth/login.jsp");
%>