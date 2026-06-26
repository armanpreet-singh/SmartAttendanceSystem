package com.smartattendance.servlet;


import com.smartattendance.dao.ReportDAO;
import com.smartattendance.filter.AuthFilter;
import com.smartattendance.model.Faculty;
import com.smartattendance.model.Student;
import com.smartattendance.util.DateUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
// ===========================================================
// iText PDF Imports
// ===========================================================
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;

import com.itextpdf.text.Phrase;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator;



import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.VerticalAlignment;

// ===========================================================
// Apache POI Excel Imports
// ===========================================================
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * ExportReportServlet.java
 * URL: /export/report
 *
 * Handles export of reports to PDF and Excel.
 *
 * Required query parameters:
 *   ?format=pdf|excel
 *   ?report=attendance|marks|low|daily|monthly
 *   ?role=student|faculty
 *   ?subjectId=X          (for subject-specific reports)
 *   ?date=yyyy-MM-dd      (for daily report)
 *   ?month=X, ?year=X     (for monthly report)
 *   ?threshold=X          (for low attendance report)
 */
public class ExportReportServlet extends HttpServlet {

    private static final Logger logger =
            LoggerFactory.getLogger(ExportReportServlet.class);

    private ReportDAO reportDAO;

    // ── iText PDF Fonts ───────────────────────────────────────────────
  private static final com.itextpdf.text.Font PDF_TITLE_FONT =
        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.DARK_GRAY);

private static final com.itextpdf.text.Font PDF_HEADER_FONT =
        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);

private static final com.itextpdf.text.Font PDF_BODY_FONT =
        FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);

private static final com.itextpdf.text.Font PDF_SUBTITLE_FONT =
        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, BaseColor.DARK_GRAY);

    private static final BaseColor HEADER_BG = new BaseColor(26, 35, 126);  // Dark blue
    private static final BaseColor ALT_ROW   = new BaseColor(240, 244, 255); // Light blue

    @Override
    public void init() throws ServletException {
        reportDAO = new ReportDAO();
        logger.info("ExportReportServlet initialized.");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        String format     = request.getParameter("format");
        String reportType = request.getParameter("report");
        String role       = request.getParameter("role");
        String subIdStr   = request.getParameter("subjectId");
        String dateStr    = request.getParameter("date");
        String monthStr   = request.getParameter("month");
        String yearStr    = request.getParameter("year");
        String threshStr  = request.getParameter("threshold");

        if (format == null || reportType == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Missing required parameters: format and report.");
            return;
        }

        int    subjectId = parseIntSafe(subIdStr, 0);
        int    month     = parseIntSafe(monthStr, LocalDate.now().getMonthValue());
        int    year      = parseIntSafe(yearStr,  LocalDate.now().getYear());
        double threshold = parseDoubleSafe(threshStr, 75.0);
        LocalDate date   = (dateStr != null && !dateStr.isEmpty())
                ? DateUtil.parseHtmlDate(dateStr) : LocalDate.now();

        int academicYearId = 4; // Current year

        logger.info("ExportReportServlet: format={}, report={}, role={}, subjectId={}",
                format, reportType, role, subjectId);

        try {
            if ("excel".equalsIgnoreCase(format)) {
                exportExcel(request, response, session, role, reportType,
                        subjectId, academicYearId, date, month, year, threshold);
            } else {
                exportPdf(request, response, session, role, reportType,
                        subjectId, academicYearId, date, month, year, threshold);
            }
        } catch (Exception e) {
            logger.error("ExportReportServlet: Export failed", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Export failed: " + e.getMessage());
        }
    }

    // ================================================================
    // PDF EXPORT
    // ================================================================

    private void exportPdf(HttpServletRequest request, HttpServletResponse response,
                           HttpSession session, String role, String reportType,
                           int subjectId, int academicYearId,
                           LocalDate date, int month, int year, double threshold)
            throws Exception {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + buildFilename(reportType, role, "pdf") + "\"");

        Document doc = new Document(PageSize.A4.rotate(), 36, 36, 54, 36);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        // ── Title Section ──────────────────────────────────────────
        String collegeName = "ABC College of Engineering";
        String reportTitle = buildReportTitle(reportType, role);
        String generatedBy = buildGeneratedBy(session, role);

        addPdfTitle(doc, collegeName, reportTitle,
                DateUtil.formatForDisplay(LocalDate.now()), generatedBy);

        // ── Report Content ─────────────────────────────────────────
        switch (reportType) {

            case "attendance": {
                if ("student".equals(role)) {
                    Student stu = (Student) session.getAttribute(AuthFilter.SESSION_STUDENT);
                    List<Map<String, Object>> data = reportDAO
                            .getStudentOverallAttendance(stu.getId(), academicYearId);
                    addStudentAttendancePdfTable(doc, data);
                } else {
                    List<Map<String, Object>> data = reportDAO
                            .getFacultySubjectAttendanceReport(subjectId, academicYearId);
                    addFacultyAttendancePdfTable(doc, data);
                }
                break;
            }

            case "marks": {
                if ("student".equals(role)) {
                    Student stu = (Student) session.getAttribute(AuthFilter.SESSION_STUDENT);
                    List<Map<String, Object>> data = reportDAO
                            .getStudentMarksReport(stu.getId(), academicYearId);
                    addStudentMarksPdfTable(doc, data);
                } else {
                    List<Map<String, Object>> data = reportDAO
                            .getFacultyMarksReport(subjectId, academicYearId);
                    addFacultyMarksPdfTable(doc, data);
                }
                break;
            }

            case "low": {
                Faculty fac = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);
                List<Map<String, Object>> data = reportDAO
                        .getLowAttendanceStudents(fac.getId(), academicYearId, threshold);
                addLowAttendancePdfTable(doc, data, threshold);
                break;
            }

            case "daily": {
                List<Map<String, Object>> data = reportDAO
                        .getFacultyDailyAttendance(subjectId, date);
                addDailyAttendancePdfTable(doc, data, date);
                break;
            }

            case "monthly": {
                List<Map<String, Object>> data = reportDAO
                        .getFacultyMonthlyAttendance(subjectId, month, year);
                addMonthlyAttendancePdfTable(doc, data, month, year);
                break;
            }

            default:
                doc.add(new Paragraph("Report type not supported for PDF export.",
                        PDF_BODY_FONT));
        }

        doc.close();
        logger.info("ExportReportServlet: PDF export completed for reportType={}", reportType);
    }

    // ── PDF Helper: Title Block ────────────────────────────────────────

    private void addPdfTitle(Document doc, String college, String title,
                             String date, String by) throws DocumentException {
        // College name
        Paragraph collegePara = new Paragraph(college, PDF_SUBTITLE_FONT);
        collegePara.setAlignment(Element.ALIGN_CENTER);
        collegePara.setSpacingAfter(4f);
        doc.add(collegePara);

        // Report title
        Paragraph titlePara = new Paragraph(title, PDF_TITLE_FONT);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(4f);
        doc.add(titlePara);

        // Generated date + by
        Paragraph metaPara = new Paragraph(
                "Generated: " + date + "    |    " + by, PDF_BODY_FONT);
        metaPara.setAlignment(Element.ALIGN_CENTER);
        metaPara.setSpacingAfter(16f);
        doc.add(metaPara);

        // Divider line
        LineSeparator ls = new LineSeparator();
        ls.setLineColor(HEADER_BG);
        doc.add(new Chunk(ls));
        doc.add(Chunk.NEWLINE);
    }

    // ── PDF Tables ─────────────────────────────────────────────────────

    private void addStudentAttendancePdfTable(Document doc,
            List<Map<String, Object>> data) throws DocumentException {
        String[] headers = {"#","Subject Code","Subject Name","Total","Present",
                "Absent","Late","Percentage","Status"};
        PdfPTable table = createPdfTable(headers);

        int row = 1;
        for (Map<String, Object> r : data) {
            double pct = (double) r.get("percentage");
            String status = pct >= 85 ? "Safe" : pct >= 80 ? "Warning"
                    : pct >= 75 ? "Critical" : "Defaulter";
            boolean alt = (row % 2 == 0);

            addPdfRow(table, alt,
                    String.valueOf(row++),
                    str(r.get("subjectCode")),
                    str(r.get("subjectName")),
                    str(r.get("totalClasses")),
                    str(r.get("present")),
                    str(r.get("absent")),
                    str(r.get("late")),
                    String.format("%.1f%%", pct),
                    status);
        }
        doc.add(table);
    }

    private void addFacultyAttendancePdfTable(Document doc,
            List<Map<String, Object>> data) throws DocumentException {
        String[] headers = {"#","Student ID","Name","Section","Total",
                "Present","Absent","Late","Percentage","Status"};
        PdfPTable table = createPdfTable(headers);

        int row = 1;
        for (Map<String, Object> r : data) {
            double pct = (double) r.get("percentage");
            String status = pct >= 75 ? "Eligible" : "Defaulter";
            boolean alt = (row % 2 == 0);
            addPdfRow(table, alt,
                    String.valueOf(row++),
                    str(r.get("studentCode")),
                    str(r.get("studentName")),
                    str(r.get("section")),
                    str(r.get("totalClasses")),
                    str(r.get("present")),
                    str(r.get("absent")),
                    str(r.get("late")),
                    String.format("%.1f%%", pct),
                    status);
        }
        doc.add(table);
    }

    private void addStudentMarksPdfTable(Document doc,
            List<Map<String, Object>> data) throws DocumentException {
        String[] headers = {"#","Subject","Exam Type","Max Marks","Obtained","Grade"};
        PdfPTable table = createPdfTable(headers);
        int row = 1;
        for (Map<String, Object> r : data) {
            boolean alt = (row % 2 == 0);
            addPdfRow(table, alt,
                    String.valueOf(row++),
                    str(r.get("subjectCode")),
                    str(r.get("examName")),
                    str(r.get("maxMarks")),
                    str(r.get("obtained")),
                    str(r.get("grade")));
        }
        doc.add(table);
    }

    private void addFacultyMarksPdfTable(Document doc,
            List<Map<String, Object>> data) throws DocumentException {
        String[] headers = {"#","Student ID","Name","Section","Exam Type","Max","Obtained","Grade"};
        PdfPTable table = createPdfTable(headers);
        int row = 1;
        for (Map<String, Object> r : data) {
            boolean alt = (row % 2 == 0);
            addPdfRow(table, alt,
                    String.valueOf(row++),
                    str(r.get("studentCode")),
                    str(r.get("studentName")),
                    str(r.get("section")),
                    str(r.get("examName")),
                    str(r.get("maxMarks")),
                    str(r.get("obtained")),
                    str(r.get("grade")));
        }
        doc.add(table);
    }

    private void addLowAttendancePdfTable(Document doc,
            List<Map<String, Object>> data, double threshold)
            throws DocumentException {
        Paragraph note = new Paragraph(
                "Students with attendance below " + threshold + "%  |  Total: "
                + data.size(), PDF_SUBTITLE_FONT);
        note.setSpacingBefore(8f);
        note.setSpacingAfter(8f);
        doc.add(note);

        String[] headers = {"#","Student ID","Name","Section","Subject","Total",
                "Present","Absent","Percentage"};
        PdfPTable table = createPdfTable(headers);
        int row = 1;
        for (Map<String, Object> r : data) {
            double pct = (double) r.get("percentage");
            boolean alt = (row % 2 == 0);
            addPdfRow(table, alt,
                    String.valueOf(row++),
                    str(r.get("studentCode")),
                    str(r.get("studentName")),
                    str(r.get("section")),
                    str(r.get("subjectCode")),
                    str(r.get("totalClasses")),
                    str(r.get("present")),
                    str(r.get("absent")),
                    String.format("%.1f%%", pct));
        }
        doc.add(table);
    }

    private void addDailyAttendancePdfTable(Document doc,
            List<Map<String, Object>> data, LocalDate date)
            throws DocumentException {
        Paragraph note = new Paragraph(
                "Date: " + DateUtil.formatForDisplay(date)
                + "  |  Total: " + data.size(), PDF_SUBTITLE_FONT);
       note.setSpacingBefore(8f);
note.setSpacingAfter(8f);
        doc.add(note);

        String[] headers = {"#","Student ID","Name","Section","Status","Remarks"};
        PdfPTable table  = createPdfTable(headers);
        int row = 1;
        for (Map<String, Object> r : data) {
            boolean alt = (row % 2 == 0);
            addPdfRow(table, alt,
                    String.valueOf(row++),
                    str(r.get("studentCode")),
                    str(r.get("studentName")),
                    str(r.get("section")),
                    str(r.get("status")),
                    str(r.get("remarks")));
        }
        doc.add(table);
    }

    private void addMonthlyAttendancePdfTable(Document doc,
            List<Map<String, Object>> data, int month, int year)
            throws DocumentException {
        Paragraph note = new Paragraph(
                "Month: " + java.time.Month.of(month).name() + " " + year
                + "  |  Students: " + data.size(), PDF_SUBTITLE_FONT);
     note.setSpacingBefore(8f);
note.setSpacingAfter(8f);
        doc.add(note);

        String[] headers = {"#","Student ID","Name","Section","Total",
                "Present","Absent","Late","Monthly %"};
        PdfPTable table  = createPdfTable(headers);
        int row = 1;
        for (Map<String, Object> r : data) {
            double pct = (double) r.get("monthlyPct");
            boolean alt = (row % 2 == 0);
            addPdfRow(table, alt,
                    String.valueOf(row++),
                    str(r.get("studentCode")),
                    str(r.get("studentName")),
                    str(r.get("section")),
                    str(r.get("total")),
                    str(r.get("present")),
                    str(r.get("absent")),
                    str(r.get("late")),
                    String.format("%.1f%%", pct));
        }
        doc.add(table);
    }

    // ── PDF Utility Methods ────────────────────────────────────────────

    private PdfPTable createPdfTable(String[] headers) throws DocumentException {
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, PDF_HEADER_FONT));
            cell.setBackgroundColor(HEADER_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(6f);
            cell.setBorderColor(BaseColor.WHITE);
            table.addCell(cell);
        }
        return table;
    }

    private void addPdfRow(PdfPTable table, boolean alt, String... values) {
        for (String v : values) {
            PdfPCell cell = new PdfPCell(new Phrase(v != null ? v : "—", PDF_BODY_FONT));
            if (alt) cell.setBackgroundColor(ALT_ROW);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(5f);
            cell.setBorderColor(new BaseColor(220, 220, 220));
            table.addCell(cell);
        }
    }

    // ================================================================
    // EXCEL EXPORT
    // ================================================================

    private void exportExcel(HttpServletRequest request, HttpServletResponse response,
                             HttpSession session, String role, String reportType,
                             int subjectId, int academicYearId,
                             LocalDate date, int month, int year, double threshold)
            throws Exception {

        response.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"" + buildFilename(reportType, role, "xlsx") + "\"");

        try (Workbook wb = new XSSFWorkbook();
             OutputStream out = response.getOutputStream()) {

            // ── Cell Styles ────────────────────────────────────────
            CellStyle titleStyle  = createTitleStyle(wb);
            CellStyle headerStyle = createHeaderStyle(wb);
            CellStyle altStyle    = createAltStyle(wb);
            CellStyle normalStyle = createNormalStyle(wb);

            Sheet sheet = wb.createSheet(buildReportTitle(reportType, role));

            // ── Title Row ──────────────────────────────────────────
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(buildReportTitle(reportType, role)
                    + " | " + "ABC College of Engineering"
                    + " | Generated: " + DateUtil.formatForDisplay(LocalDate.now()));
            titleCell.setCellStyle(titleStyle);

            int dataStartRow = 2;

            switch (reportType) {

                case "attendance": {
                    if ("student".equals(role)) {
                        Student stu = (Student) session.getAttribute(
                                AuthFilter.SESSION_STUDENT);
                        List<Map<String, Object>> data = reportDAO
                                .getStudentOverallAttendance(stu.getId(), academicYearId);
                        String[] headers = {"#","Subject Code","Subject Name",
                                "Total Classes","Present","Absent","Late",
                                "Percentage","Status"};
                        writeExcelHeaders(sheet, dataStartRow, headers, headerStyle);
                        int r = dataStartRow + 1;
                        for (Map<String, Object> row : data) {
                            double pct = (double) row.get("percentage");
                            writeExcelRow(sheet, r++,
                                    (r - dataStartRow - 1) % 2 == 0 ? altStyle : normalStyle,
                                    String.valueOf(r - dataStartRow - 1),
                                    str(row.get("subjectCode")),
                                    str(row.get("subjectName")),
                                    str(row.get("totalClasses")),
                                    str(row.get("present")),
                                    str(row.get("absent")),
                                    str(row.get("late")),
                                    String.format("%.1f%%", pct),
                                    pct >= 75 ? "Eligible" : "Defaulter");
                        }
                        autoSizeColumns(sheet, headers.length);
                    } else {
                        List<Map<String, Object>> data = reportDAO
                                .getFacultySubjectAttendanceReport(subjectId, academicYearId);
                        String[] headers = {"#","Student ID","Name","Section","Total",
                                "Present","Absent","Late","Percentage","Status"};
                        writeExcelHeaders(sheet, dataStartRow, headers, headerStyle);
                        int r = dataStartRow + 1;
                        for (Map<String, Object> row : data) {
                            double pct = (double) row.get("percentage");
                            writeExcelRow(sheet, r++,
                                    (r - dataStartRow - 1) % 2 == 0 ? altStyle : normalStyle,
                                    String.valueOf(r - dataStartRow - 1),
                                    str(row.get("studentCode")),
                                    str(row.get("studentName")),
                                    str(row.get("section")),
                                    str(row.get("totalClasses")),
                                    str(row.get("present")),
                                    str(row.get("absent")),
                                    str(row.get("late")),
                                    String.format("%.1f%%", pct),
                                    pct >= 75 ? "Eligible" : "Defaulter");
                        }
                        autoSizeColumns(sheet, headers.length);
                    }
                    break;
                }

                case "marks": {
                    List<Map<String, Object>> data;
                    String[] headers;
                    if ("student".equals(role)) {
                        Student stu = (Student) session.getAttribute(
                                AuthFilter.SESSION_STUDENT);
                        data = reportDAO.getStudentMarksReport(stu.getId(), academicYearId);
                        headers = new String[]{"#","Subject Code","Subject Name",
                                "Exam Type","Max Marks","Obtained","Grade"};
                        writeExcelHeaders(sheet, dataStartRow, headers, headerStyle);
                        int r = dataStartRow + 1;
                        for (Map<String, Object> row : data) {
                            writeExcelRow(sheet, r++,
                                    (r - dataStartRow - 1) % 2 == 0 ? altStyle : normalStyle,
                                    String.valueOf(r - dataStartRow - 1),
                                    str(row.get("subjectCode")),
                                    str(row.get("subjectName")),
                                    str(row.get("examName")),
                                    str(row.get("maxMarks")),
                                    str(row.get("obtained")),
                                    str(row.get("grade")));
                        }
                    } else {
                        data = reportDAO.getFacultyMarksReport(subjectId, academicYearId);
                        headers = new String[]{"#","Student ID","Name","Section",
                                "Exam Type","Max Marks","Obtained","Grade"};
                        writeExcelHeaders(sheet, dataStartRow, headers, headerStyle);
                        int r = dataStartRow + 1;
                        for (Map<String, Object> row : data) {
                            writeExcelRow(sheet, r++,
                                    (r - dataStartRow - 1) % 2 == 0 ? altStyle : normalStyle,
                                    String.valueOf(r - dataStartRow - 1),
                                    str(row.get("studentCode")),
                                    str(row.get("studentName")),
                                    str(row.get("section")),
                                    str(row.get("examName")),
                                    str(row.get("maxMarks")),
                                    str(row.get("obtained")),
                                    str(row.get("grade")));
                        }
                    }
                    autoSizeColumns(sheet, headers.length);
                    break;
                }

                case "low": {
                    Faculty fac = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);
                    List<Map<String, Object>> data = reportDAO
                            .getLowAttendanceStudents(fac.getId(), academicYearId, threshold);
                    String[] headers = {"#","Student ID","Name","Section","Subject",
                            "Total","Present","Absent","Percentage"};
                    writeExcelHeaders(sheet, dataStartRow, headers, headerStyle);
                    int r = dataStartRow + 1;
                    for (Map<String, Object> row : data) {
                        double pct = (double) row.get("percentage");
                        writeExcelRow(sheet, r++,
                                (r - dataStartRow - 1) % 2 == 0 ? altStyle : normalStyle,
                                String.valueOf(r - dataStartRow - 1),
                                str(row.get("studentCode")),
                                str(row.get("studentName")),
                                str(row.get("section")),
                                str(row.get("subjectCode")),
                                str(row.get("totalClasses")),
                                str(row.get("present")),
                                str(row.get("absent")),
                                String.format("%.1f%%", pct));
                    }
                    autoSizeColumns(sheet, headers.length);
                    break;
                }

                case "daily": {
                    List<Map<String, Object>> data = reportDAO
                            .getFacultyDailyAttendance(subjectId, date);
                    String[] headers = {"#","Student ID","Name","Section","Status","Remarks"};
                    writeExcelHeaders(sheet, dataStartRow, headers, headerStyle);
                    int r = dataStartRow + 1;
                    for (Map<String, Object> row : data) {
                        writeExcelRow(sheet, r++,
                                (r - dataStartRow - 1) % 2 == 0 ? altStyle : normalStyle,
                                String.valueOf(r - dataStartRow - 1),
                                str(row.get("studentCode")),
                                str(row.get("studentName")),
                                str(row.get("section")),
                                str(row.get("status")),
                                str(row.get("remarks")));
                    }
                    autoSizeColumns(sheet, headers.length);
                    break;
                }

                case "monthly": {
                    List<Map<String, Object>> data = reportDAO
                            .getFacultyMonthlyAttendance(subjectId, month, year);
                    String[] headers = {"#","Student ID","Name","Section","Total",
                            "Present","Absent","Late","Monthly %"};
                    writeExcelHeaders(sheet, dataStartRow, headers, headerStyle);
                    int r = dataStartRow + 1;
                    for (Map<String, Object> row : data) {
                        double pct = (double) row.get("monthlyPct");
                        writeExcelRow(sheet, r++,
                                (r - dataStartRow - 1) % 2 == 0 ? altStyle : normalStyle,
                                String.valueOf(r - dataStartRow - 1),
                                str(row.get("studentCode")),
                                str(row.get("studentName")),
                                str(row.get("section")),
                                str(row.get("total")),
                                str(row.get("present")),
                                str(row.get("absent")),
                                str(row.get("late")),
                                String.format("%.1f%%", pct));
                    }
                    autoSizeColumns(sheet, headers.length);
                    break;
                }

                default:
                    sheet.createRow(dataStartRow).createCell(0)
                            .setCellValue("Report type not supported.");
            }

            wb.write(out);
        }
        logger.info("ExportReportServlet: Excel export completed for reportType={}", reportType);
    }

    // ── Excel Style Helpers ───────────────────────────────────────────

    private CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 13);
        style.setFont(font);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = wb.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private CellStyle createAltStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private CellStyle createNormalStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        setBorder(style);
        return style;
    }

    private void setBorder(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setTopBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setBottomBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setLeftBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setRightBorderColor(IndexedColors.GREY_25_PERCENT.getIndex());
    }

    private void writeExcelHeaders(Sheet sheet, int rowNum,
                                   String[] headers, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        row.setHeight((short) 400);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }

    private void writeExcelRow(Sheet sheet, int rowNum,
                               CellStyle style, String... values) {
        Row row = sheet.createRow(rowNum);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(values[i] != null ? values[i] : "—");
            cell.setCellStyle(style);
        }
    }

    private void autoSizeColumns(Sheet sheet, int count) {
        for (int i = 0; i < count; i++) {
            sheet.autoSizeColumn(i);
            // Add a small buffer
            sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
        }
    }

    // ── Utility Helpers ───────────────────────────────────────────────

    private String buildFilename(String reportType, String role, String ext) {
        return "Report_" + (role != null ? role : "") + "_"
                + reportType + "_"
                + LocalDate.now().toString().replace("-", "")
                + "." + ext;
    }

    private String buildReportTitle(String reportType, String role) {
        String r = role != null ? role.substring(0, 1).toUpperCase()
                + role.substring(1) + " " : "";
        return switch (reportType) {
            case "attendance" -> r + "Attendance Report";
            case "marks"      -> r + "Marks Report";
            case "low"        -> "Low Attendance Students Report";
            case "daily"      -> "Daily Attendance Report";
            case "monthly"    -> "Monthly Attendance Report";
            case "performance"-> r + "Performance Report";
            default           -> r + "Report";
        };
    }

    private String buildGeneratedBy(HttpSession session, String role) {
        if ("student".equals(role)) {
            Student s = (Student) session.getAttribute(AuthFilter.SESSION_STUDENT);
            return s != null ? "Student: " + s.getFullName() : "";
        }
        Faculty f = (Faculty) session.getAttribute(AuthFilter.SESSION_FACULTY);
        return f != null ? "Faculty: " + f.getFullName() : "";
    }

    private String str(Object o) {
        if (o == null) return "—";
        if (o instanceof Double) return String.format("%.1f", (Double) o);
        return o.toString();
    }

    private int parseIntSafe(String s, int def) {
        if (s == null || s.trim().isEmpty()) return def;
        try { return Integer.parseInt(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }

    private double parseDoubleSafe(String s, double def) {
        if (s == null || s.trim().isEmpty()) return def;
        try { return Double.parseDouble(s.trim()); }
        catch (NumberFormatException e) { return def; }
    }
}