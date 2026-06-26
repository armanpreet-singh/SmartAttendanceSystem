<%-- ================================================================
     footer.jsp - Common HTML Footer Section
     
     Purpose:
       - Included at the bottom of all JSP pages.
       - Closes the <body> and <html> tags.
       - Loads Bootstrap 5 JS Bundle (Popper.js included).
       - Loads Chart.js for attendance/marks charts.
       - Loads custom JavaScript files.
       - Displays application footer with copyright info.
================================================================ --%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

    <!-- ============================================================ -->
    <!-- Page Footer                                                  -->
    <!-- ============================================================ -->
    <footer class="app-footer mt-auto py-3">
        <div class="container-fluid">
            <div class="row align-items-center">

                <!-- Left: App Name and Version -->
                <div class="col-md-4 text-center text-md-start">
                    <span class="footer-brand">
                        <i class="fas fa-graduation-cap me-1"></i>
                        Smart Attendance System
                    </span>
                    <small class="text-muted ms-1">v1.0.0</small>
                </div>

                <!-- Center: College Name -->
                <div class="col-md-4 text-center my-2 my-md-0">
                    <small class="text-muted">
                        <i class="fas fa-university me-1"></i>
                        ABC College of Engineering
                    </small>
                </div>

                <!-- Right: Copyright -->
                <div class="col-md-4 text-center text-md-end">
                    <small class="text-muted">
                        &copy; <%= java.time.Year.now().getValue() %>
                        All Rights Reserved.
                    </small>
                </div>

            </div>
        </div>
    </footer>

    <!-- ============================================================ -->
    <!-- Bootstrap 5.3 JS Bundle (includes Popper.js) CDN           -->
    <!-- ============================================================ -->
    <!-- Bootstrap JS -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

    <!-- ============================================================ -->
    <!-- Chart.js CDN (for attendance and marks charts)              -->
    <!-- ============================================================ -->
    <script
        src="https://cdn.jsdelivr.net/npm/chart.js@4.4.3/dist/chart.umd.min.js">
    </script>

    <!-- ============================================================ -->
    <!-- Custom JavaScript Files                                     -->
    <!-- ============================================================ -->
    <script src="<%= request.getContextPath() %>/assets/js/main.js"></script>
    <script src="<%= request.getContextPath() %>/assets/js/validation.js"></script>

</body>
</html>