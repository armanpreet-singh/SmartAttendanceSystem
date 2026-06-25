<%-- ================================================================
     header.jsp - Common HTML Head Section
     
     Purpose:
       - Included in all JSP pages using <%@ include %> directive.
       - Provides consistent <head> with meta tags, Bootstrap 5 CSS,
         Font Awesome icons, and custom stylesheet links.
       - Uses a dynamic pageTitle variable set by the including page.
     
     Usage in other JSP files:
       <%
           String pageTitle = "Dashboard";
       %>
       <%@ include file="/WEB-INF/views/common/header.jsp" %>
================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>

    <!-- ============================================================ -->
    <!-- Meta Tags                                                    -->
    <!-- ============================================================ -->
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="description"
          content="Smart Student Attendance and Academic Monitoring System">
    <meta name="author" content="ABC College of Engineering">

    <!-- ============================================================ -->
    <!-- Dynamic Page Title                                           -->
    <!-- ============================================================ -->
    <title>
        <%
            String pageTitle = (pageTitle != null) ? pageTitle : "Home";
        %>
        <%= pageTitle %> | Smart Attendance System
    </title>

    <!-- ============================================================ -->
    <!-- Favicon                                                      -->
    <!-- ============================================================ -->
    <link rel="icon"
          type="image/png"
          href="<%= request.getContextPath() %>/assets/images/logo.png">

    <!-- ============================================================ -->
    <!-- Bootstrap 5.3 CSS (CDN)                                     -->
    <!-- ============================================================ -->
    <link
        rel="stylesheet"
        href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
        integrity="sha384-QWTKZyjpPEjISv5WaRU9OFeRpok6YctnYmDr5pNlyT2bRjXh0JMhjY6hW+ALEwIH"
        crossorigin="anonymous">

    <!-- ============================================================ -->
    <!-- Font Awesome 6 (Icons) CDN                                  -->
    <!-- ============================================================ -->
    <link
        rel="stylesheet"
        href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.2/css/all.min.css"
        integrity="sha512-SnH5WK+bZxgPHs44uWIX+LLJAJ9/2PkPKZ5QiAj6Ta86w+fsb2TkcmfRyVX3pBnMFcV7oQPJkl9QevSCWr3W6Q=="
        crossorigin="anonymous"
        referrerpolicy="no-referrer">

    <!-- ============================================================ -->
    <!-- Google Fonts - Inter                                        -->
    <!-- ============================================================ -->
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link
        href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700&display=swap"
        rel="stylesheet">

    <!-- ============================================================ -->
    <!-- Custom Application CSS                                      -->
    <!-- ============================================================ -->
    <link
        rel="stylesheet"
        href="<%= request.getContextPath() %>/assets/css/style.css">

</head>
<body>
<!-- ================================================================
     BODY STARTS HERE.
     navbar.jsp, page content, and footer.jsp will follow.
================================================================ -->