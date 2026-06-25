<%-- ================================================================
     unauthorized.jsp - Access Denied Page
     
     Purpose:
       - Displayed when a user tries to access a resource
         they are not authorized for.
       - Example: A student tries to access /faculty/* routes.
       - Example: A faculty tries to access /student/* routes.
     
     Triggered by:
       - StudentFilter.java when non-student accesses /student/*
       - FacultyFilter.java when non-faculty accesses /faculty/*
================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    // Determine redirect URL based on user role
    String userRole = (String) session.getAttribute("userRole");
    String dashboardUrl;

    if ("STUDENT".equals(userRole)) {
        dashboardUrl = request.getContextPath() + "/student/dashboard";
    } else if ("FACULTY".equals(userRole)) {
        dashboardUrl = request.getContextPath() + "/faculty/dashboard";
    } else {
        dashboardUrl = request.getContextPath() + "/index.jsp";
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Access Denied | Smart Attendance System</title>

    <!-- Bootstrap 5 -->
    <link
        rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
        integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
        crossorigin="anonymous">

    <!-- Font Awesome -->
    <link
        rel="stylesheet"
        href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css"
        crossorigin="anonymous">

    <!-- Google Fonts -->
    <link
        href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap"
        rel="stylesheet">

    <style>
        body {
            font-family: 'Inter', sans-serif;
            background: linear-gradient(135deg, #1a237e 0%, #283593 40%, #3949ab 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 1rem;
        }

        .unauthorized-card {
            background: white;
            border-radius: 20px;
            padding: 3rem 2.5rem;
            text-align: center;
            max-width: 480px;
            width: 100%;
            box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
        }

        .icon-circle {
            width: 90px;
            height: 90px;
            border-radius: 50%;
            background: linear-gradient(135deg, #ff6b6b, #ee5a24);
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 2.5rem;
            color: white;
            margin: 0 auto 1.5rem;
            box-shadow: 0 8px 25px rgba(238, 90, 36, 0.35);
        }

        .error-code {
            font-size: 4rem;
            font-weight: 800;
            color: #dee2e6;
            line-height: 1;
            margin-bottom: 0.5rem;
        }

        .btn-home {
            border-radius: 10px;
            font-weight: 600;
            padding: 0.7rem 1.5rem;
            background: linear-gradient(135deg, #1a237e, #3949ab);
            border: none;
            color: white;
            transition: all 0.2s ease;
        }

        .btn-home:hover {
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(26, 35, 126, 0.35);
            color: white;
        }

        .btn-back {
            border-radius: 10px;
            font-weight: 500;
            padding: 0.7rem 1.5rem;
        }
    </style>
</head>
<body>

<div class="unauthorized-card">

    <!-- Error Code -->
    <div class="error-code">403</div>

    <!-- Icon -->
    <div class="icon-circle">
        <i class="fas fa-ban"></i>
    </div>

    <!-- Title -->
    <h2 class="fw-bold text-dark mb-2">Access Denied</h2>

    <!-- Description -->
    <p class="text-muted mb-1">
        You don't have permission to access this page.
    </p>
    <p class="text-muted mb-4">
        <% if ("STUDENT".equals(userRole)) { %>
            This area is restricted to <strong>Faculty members</strong> only.
        <% } else if ("FACULTY".equals(userRole)) { %>
            This area is restricted to <strong>Students</strong> only.
        <% } else { %>
            Please log in with the correct account to access this page.
        <% } %>
    </p>

    <!-- Role Badge -->
    <% if (userRole != null) { %>
    <div class="mb-4">
        <span class="badge
            <%= "STUDENT".equals(userRole) ? "bg-info" : "bg-warning text-dark" %>
            rounded-pill px-3 py-2">
            <i class="fas
               <%= "STUDENT".equals(userRole) ? "fa-user-graduate" : "fa-chalkboard-teacher" %>
               me-1"></i>
            Logged in as: <%= userRole %>
        </span>
    </div>
    <% } %>

    <!-- Action Buttons -->
    <div class="d-flex justify-content-center gap-3 flex-wrap">

        <button onclick="history.back();"
                class="btn btn-outline-secondary btn-back">
            <i class="fas fa-arrow-left me-2"></i>
            Go Back
        </button>

        <a href="<%= dashboardUrl %>"
           class="btn btn-home">
            <i class="fas fa-home me-2"></i>
            My Dashboard
        </a>

    </div>

    <!-- Help Text -->
    <p class="text-muted mt-4 mb-0 small">
        <i class="fas fa-info-circle me-1"></i>
        If you believe this is a mistake, please contact the system administrator.
    </p>

</div>

<!-- Bootstrap JS -->
<script
    src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
    crossorigin="anonymous">
</script>

</body>
</html>