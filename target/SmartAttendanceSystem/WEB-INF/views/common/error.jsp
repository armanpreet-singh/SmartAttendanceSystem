<%-- ================================================================
     error.jsp - Unified Error / Exception Page
     
     Purpose:
       - Handles all HTTP error codes (400, 403, 404, 500).
       - Handles uncaught Java exceptions.
       - Provides user-friendly error messages with error code display.
       - Provides navigation options to go back or go home.
     
     Mapped in web.xml for:
       - Error codes: 400, 403, 404, 500
       - Exception type: java.lang.Exception
================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%
    // ----------------------------------------------------------------
    // Retrieve error information from request attributes
    // (Set automatically by Tomcat for error pages)
    // ----------------------------------------------------------------
    Integer errorCode    = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
    String  errorMessage = (String)  request.getAttribute("jakarta.servlet.error.message");
    String  errorUri     = (String)  request.getAttribute("jakarta.servlet.error.request_uri");
    Throwable throwable  = (Throwable) request.getAttribute("jakarta.servlet.error.exception");

    // Default values if attributes are missing
    if (errorCode    == null) errorCode    = 500;
    if (errorMessage == null || errorMessage.trim().isEmpty()) errorMessage = "An unexpected error occurred.";
    if (errorUri     == null) errorUri     = "Unknown";

    // Determine error title and icon based on HTTP status code
    String errorTitle = "Server Error";
    String errorIcon  = "fa-exclamation-triangle";
    String badgeClass = "bg-danger";

    switch (errorCode) {
        case 400:
            errorTitle = "Bad Request";
            errorIcon  = "fa-ban";
            badgeClass = "bg-warning";
            break;
        case 403:
            errorTitle = "Access Forbidden";
            errorIcon  = "fa-lock";
            badgeClass = "bg-danger";
            break;
        case 404:
            errorTitle = "Page Not Found";
            errorIcon  = "fa-search";
            badgeClass = "bg-secondary";
            break;
        case 500:
            errorTitle = "Internal Server Error";
            errorIcon  = "fa-exclamation-triangle";
            badgeClass = "bg-danger";
            break;
        default:
            errorTitle = "Unexpected Error";
    }

    // Determine where to redirect the "Go Home" button
    String homeUrl = request.getContextPath() + "/index.jsp";

    String pageTitle = "Error " + errorCode;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= pageTitle %> | Smart Attendance System</title>

    <!-- Bootstrap 5 CSS -->
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

    <!-- Custom CSS -->
    <link rel="stylesheet"
          href="<%= request.getContextPath() %>/assets/css/style.css">

    <style>
        /* ── Inline Error Page Specific Styles ── */
        body {
            font-family: 'Inter', sans-serif;
            background-color: #f8f9fa;
        }

        .error-container {
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .error-card {
            border: none;
            border-radius: 16px;
            box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
            max-width: 600px;
            width: 100%;
        }

        .error-code-display {
            font-size: 6rem;
            font-weight: 700;
            line-height: 1;
            color: #dee2e6;
        }

        .error-icon-circle {
            width: 80px;
            height: 80px;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 2rem;
            margin: 0 auto 1rem;
        }

        .error-uri {
            font-family: 'Courier New', Courier, monospace;
            font-size: 0.8rem;
            background-color: #f1f3f5;
            padding: 4px 10px;
            border-radius: 6px;
            word-break: break-all;
        }
    </style>
</head>
<body>

<div class="error-container px-3">
    <div class="error-card card p-4 p-md-5 text-center">

        <!-- ─── Error Code Large Display ──────────────────────── -->
        <div class="error-code-display mb-2">
            <%= errorCode %>
        </div>

        <!-- ─── Error Icon ────────────────────────────────────── -->
        <div class="error-icon-circle <%= badgeClass %> text-white mx-auto mb-3">
            <i class="fas <%= errorIcon %>"></i>
        </div>

        <!-- ─── Error Title ───────────────────────────────────── -->
        <h2 class="fw-bold text-dark mb-2">
            <%= errorTitle %>
        </h2>

        <!-- ─── Error Message ─────────────────────────────────── -->
        <p class="text-muted mb-1">
            <%= errorMessage %>
        </p>

        <!-- ─── Requested URI ─────────────────────────────────── -->
        <% if (!errorUri.equals("Unknown")) { %>
        <p class="mb-3">
            <small class="text-muted">Requested URL: </small>
            <span class="error-uri"><%= errorUri %></span>
        </p>
        <% } %>

        <!-- ─── Exception Details (Development Mode Only) ─────── -->
        <% if (throwable != null) { %>
        <div class="alert alert-danger text-start small mt-3" role="alert">
            <strong><i class="fas fa-bug me-1"></i> Exception Details:</strong><br>
            <code><%= throwable.getClass().getName() %>: <%= throwable.getMessage() %></code>
        </div>
        <% } %>

        <hr class="my-4">

        <!-- ─── Navigation Buttons ───────────────────────────── -->
        <div class="d-flex justify-content-center gap-3 flex-wrap">

            <!-- Go Back Button -->
            <button onclick="history.back();"
                    class="btn btn-outline-secondary">
                <i class="fas fa-arrow-left me-2"></i>
                Go Back
            </button>

            <!-- Go Home Button -->
            <a href="<%= homeUrl %>"
               class="btn btn-primary">
                <i class="fas fa-home me-2"></i>
                Go to Home
            </a>

        </div>

        <!-- ─── Help Text ─────────────────────────────────────── -->
        <p class="text-muted mt-4 mb-0 small">
            <i class="fas fa-info-circle me-1"></i>
            If this issue persists, please contact the
            <strong>System Administrator</strong>.
        </p>

    </div>
</div>

<!-- Bootstrap 5 JS -->
<script
    src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"
    integrity="sha384-YvpcrYf0tY3lHB60NNkmXc4s9bIOgUxi8T/jzmOXLVrFEv1GnUQ7uJRQOSjRwI"
    crossorigin="anonymous">
</script>

</body>
</html>