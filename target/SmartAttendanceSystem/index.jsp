<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Check active session
    if (session.getAttribute("loggedInStudent") != null) {
        response.sendRedirect(request.getContextPath() + "/student/dashboard");
        return;
    }

    if (session.getAttribute("loggedInFaculty") != null) {
        response.sendRedirect(request.getContextPath() + "/faculty/dashboard");
        return;
    }

    // Show login page
    request.getRequestDispatcher("/WEB-INF/views/auth/login.jsp")
           .forward(request, response);
%>