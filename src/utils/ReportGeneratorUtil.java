package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for generating HTML and PDF reports.
 * Creates formatted reports from data collections.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class ReportGeneratorUtil {

    private static final Logger LOGGER = Logger.getLogger(ReportGeneratorUtil.class.getName());

    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    // HTML template constants
    private static final String HTML_HEADER = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset='UTF-8'>
            <title>%s</title>
            <style>
                body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }
                .container { max-width: 1200px; margin: 0 auto; background-color: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }
                h1 { color: #2c3e50; border-bottom: 2px solid #006400; padding-bottom: 10px; }
                h2 { color: #34495e; margin-top: 25px; }
                table { border-collapse: collapse; width: 100%; margin-top: 15px; }
                th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
                th { background-color: #006400; color: white; }
                tr:nth-child(even) { background-color: #f9f9f9; }
                .footer { margin-top: 30px; font-size: 12px; color: #666; text-align: center; border-top: 1px solid #ddd; padding-top: 15px; }
                .summary { background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 20px 0; }
            </style>
        </head>
        <body>
        <div class='container'>
    """;

    private static final String HTML_FOOTER = """
            <div class='footer'>
                Vehicle Identification System - Generated Report<br>
                Generated on: %s
            </div>
        </div>
        </body>
        </html>
    """;

    private ReportGeneratorUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generates an HTML report from data.
     *
     * @param title    the report title
     * @param headers  the column headers
     * @param data     the report data (list of maps)
     * @param fileName base name for the report file
     * @throws IOException if file writing fails
     */
    public static void generateHTMLReport(String title, List<String> headers,
                                          List<Map<String, String>> data, String fileName) throws IOException {
        if (title == null || title.trim().isEmpty()) {
            title = "System Report";
        }

        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "report";
        }

        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String filePath = FileHandler.getReportsPath() + sanitizeFileName(fileName + "_" + timestamp + ".html");

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Write header
            writer.println(String.format(HTML_HEADER, escapeHtml(title)));

            // Write title and metadata
            writer.println("<h1>" + escapeHtml(title) + "</h1>");
            writer.println("<p><strong>Generated on:</strong> " + LocalDateTime.now().format(REPORT_DATE_FORMAT) + "</p>");

            if (data != null && !data.isEmpty()) {
                writer.println("<p><strong>Total Records:</strong> " + data.size() + "</p>");
            }

            // Write table
            if (headers != null && !headers.isEmpty()) {
                writer.println("<table>");
                writer.println("<thead><tr>");
                for (String header : headers) {
                    writer.println("<th>" + escapeHtml(header) + "</th>");
                }
                writer.println("</tr></thead>");
                writer.println("<tbody>");

                if (data != null) {
                    for (Map<String, String> row : data) {
                        writer.println("<tr>");
                        for (String header : headers) {
                            String value = row != null ? row.getOrDefault(header, "") : "";
                            writer.println("<td>" + escapeHtml(value) + "</td>");
                        }
                        writer.println("</tr>");
                    }
                }

                writer.println("</tbody>");
                writer.println("</table>");
            } else if (data != null) {
                // No headers provided, try to infer from first row
                writer.println("<table>");
                if (!data.isEmpty() && data.get(0) != null) {
                    writer.println("<tbody>");
                    for (Map<String, String> row : data) {
                        writer.println("<tr>");
                        for (String value : row.values()) {
                            writer.println("<td>" + escapeHtml(value) + "</td>");
                        }
                        writer.println("</tr>");
                    }
                    writer.println("</tbody>");
                }
                writer.println("</table>");
            }

            // Write footer
            writer.println(String.format(HTML_FOOTER, LocalDateTime.now().format(REPORT_DATE_FORMAT)));
        }

        LOGGER.info("HTML report generated: " + filePath);
        AlertUtil.showInfo("Report Generated", "HTML report saved to: " + filePath);
    }

    /**
     * Generates an HTML report with additional summary information.
     *
     * @param title      the report title
     * @param headers    the column headers
     * @param data       the report data
     * @param summary    summary information to display
     * @param fileName   base name for the report file
     * @throws IOException if file writing fails
     */
    public static void generateHTMLReportWithSummary(String title, List<String> headers,
                                                     List<Map<String, String>> data,
                                                     Map<String, String> summary, String fileName) throws IOException {
        if (fileName == null || fileName.trim().isEmpty()) {
            fileName = "report";
        }

        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String filePath = FileHandler.getReportsPath() + sanitizeFileName(fileName + "_" + timestamp + ".html");

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println(String.format(HTML_HEADER, escapeHtml(title)));
            writer.println("<h1>" + escapeHtml(title) + "</h1>");
            writer.println("<p><strong>Generated on:</strong> " + LocalDateTime.now().format(REPORT_DATE_FORMAT) + "</p>");

            // Write summary section
            if (summary != null && !summary.isEmpty()) {
                writer.println("<div class='summary'>");
                writer.println("<h3>Report Summary</h3>");
                writer.println("<ul>");
                for (Map.Entry<String, String> entry : summary.entrySet()) {
                    writer.println("<li><strong>" + escapeHtml(entry.getKey()) + ":</strong> " + escapeHtml(entry.getValue()) + "</li>");
                }
                writer.println("</ul>");
                writer.println("</div>");
            }

            // Write table
            if (headers != null && !headers.isEmpty()) {
                writer.println("<table>");
                writer.println("<thead><tr>");
                for (String header : headers) {
                    writer.println("<th>" + escapeHtml(header) + "</th>");
                }
                writer.println("</tr></thead>");
                writer.println("<tbody>");

                if (data != null) {
                    for (Map<String, String> row : data) {
                        writer.println("<tr>");
                        for (String header : headers) {
                            String value = row != null ? row.getOrDefault(header, "") : "";
                            writer.println("<td>" + escapeHtml(value) + "</td>");
                        }
                        writer.println("</tr>");
                    }
                }

                writer.println("</tbody>");
                writer.println("</table>");
            }

            writer.println(String.format(HTML_FOOTER, LocalDateTime.now().format(REPORT_DATE_FORMAT)));
        }

        AlertUtil.showInfo("Report Generated", "HTML report saved to: " + filePath);
    }

    /**
     * Generates a placeholder for PDF generation.
     * Note: Full PDF generation requires additional libraries (iText, Apache PDFBox).
     *
     * @param title    the report title
     * @param headers  the column headers
     * @param data     the report data
     * @param fileName base name for the report file
     */
    public static void generatePDFReport(String title, List<String> headers,
                                         List<Map<String, String>> data, String fileName) {
        LOGGER.info("PDF generation requested but requires additional libraries");
        AlertUtil.showInfo("PDF Generation",
                "PDF generation requires additional libraries. Please use HTML or CSV export instead.");
    }

    /**
     * Escapes HTML special characters to prevent XSS.
     *
     * @param text the text to escape
     * @return escaped text
     */
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    /**
     * Sanitizes a filename by removing invalid characters.
     *
     * @param fileName the original filename
     * @return sanitized filename
     */
    private static String sanitizeFileName(String fileName) {
        if (fileName == null) return "report.html";
        return fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
    }
}