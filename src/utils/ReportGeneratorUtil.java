package utils;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class ReportGeneratorUtil {

    private static final DateTimeFormatter REPORT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void generateHTMLReport(String title, List<String> headers, List<Map<String, String>> data, String fileName) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filePath = FileHandler.getReportsPath() + fileName + "_" + timestamp + ".html";

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("<!DOCTYPE html>");
            writer.println("<html>");
            writer.println("<head>");
            writer.println("<meta charset='UTF-8'>");
            writer.println("<title>" + title + "</title>");
            writer.println("<style>");
            writer.println("body { font-family: Arial, sans-serif; margin: 20px; }");
            writer.println("h1 { color: #333; }");
            writer.println("table { border-collapse: collapse; width: 100%; }");
            writer.println("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }");
            writer.println("th { background-color: #006400; color: white; }");
            writer.println("tr:nth-child(even) { background-color: #f2f2f2; }");
            writer.println(".footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }");
            writer.println("</style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("<h1>" + title + "</h1>");
            writer.println("<p>Generated on: " + LocalDateTime.now().format(REPORT_DATE_FORMAT) + "</p>");
            writer.println("<table>");
            writer.println("<tr>");
            for (String header : headers) {
                writer.println("<th>" + header + "</th>");
            }
            writer.println("</tr>");

            if (data != null) {
                for (Map<String, String> row : data) {
                    writer.println("<tr>");
                    for (String header : headers) {
                        String value = row.getOrDefault(header, "");
                        writer.println("<td>" + value + "</td>");
                    }
                    writer.println("</tr>");
                }
            }

            writer.println("</table>");
            writer.println("<div class='footer'>");
            writer.println("Vehicle Identification System - Generated Report");
            writer.println("</div>");
            writer.println("</body>");
            writer.println("</html>");
        }

        AlertUtil.showInfo("Report Generated", "HTML report saved to: " + filePath);
    }

    public static void generatePDFReport(String title, List<String> headers, List<Map<String, String>> data, String fileName) {
        AlertUtil.showInfo("PDF Generation", "PDF generation requires additional libraries. CSV export recommended.");
    }
}