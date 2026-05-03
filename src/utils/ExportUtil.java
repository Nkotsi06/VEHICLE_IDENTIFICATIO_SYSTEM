package utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

public class ExportUtil {

    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static String getReportsDirectory() {
        String userHome = System.getProperty("user.home");
        String reportsDir = userHome + File.separator + "VehicleIdentificationSystem" + File.separator + "reports" + File.separator;
        File dir = new File(reportsDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return reportsDir;
    }

    @SuppressWarnings("unchecked")
    public static <T> void exportToCSV(TableView<T> tableView, String fileName) throws IOException {
        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String fullFileName = fileName + "_" + timestamp + ".csv";
        String filePath = getReportsDirectory() + fullFileName;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            ObservableList<TableColumn<T, ?>> columns = (ObservableList<TableColumn<T, ?>>) tableView.getColumns();

            for (int i = 0; i < columns.size(); i++) {
                writer.print("\"" + columns.get(i).getText() + "\"");
                if (i < columns.size() - 1) {
                    writer.print(",");
                }
            }
            writer.println();

            for (T rowItem : tableView.getItems()) {
                for (int i = 0; i < columns.size(); i++) {
                    TableColumn<T, ?> column = columns.get(i);
                    Object value = column.getCellData(rowItem);
                    String cellValue = value != null ? value.toString().replace("\"", "\"\"") : "";
                    writer.print("\"" + cellValue + "\"");
                    if (i < columns.size() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
        }

        AlertUtil.showInfo("Export Complete", "File saved to: " + filePath);
    }

    public static void exportToCSV(List<Map<String, Object>> data, String fileName, String[] headers, String[] fields) throws IOException {
        if (data == null || data.isEmpty()) {
            AlertUtil.showWarning("Export Failed", "No data to export.");
            return;
        }

        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String fullFileName = fileName + "_" + timestamp + ".csv";
        String filePath = getReportsDirectory() + fullFileName;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            if (headers != null && headers.length > 0) {
                for (int i = 0; i < headers.length; i++) {
                    writer.print("\"" + headers[i] + "\"");
                    if (i < headers.length - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }

            if (fields != null && fields.length > 0) {
                for (Map<String, Object> row : data) {
                    for (int i = 0; i < fields.length; i++) {
                        Object value = row.get(fields[i]);
                        String cellValue = value != null ? value.toString().replace("\"", "\"\"") : "";
                        writer.print("\"" + cellValue + "\"");
                        if (i < fields.length - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }
            }
        }

        AlertUtil.showInfo("Export Complete", "File saved to: " + filePath);
    }

    public static void exportToCSVSimple(List<String[]> data, String fileName, String[] headers) throws IOException {
        if (data == null || data.isEmpty()) {
            AlertUtil.showWarning("Export Failed", "No data to export.");
            return;
        }

        String timestamp = LocalDateTime.now().format(FILE_DATE_FORMAT);
        String fullFileName = fileName + "_" + timestamp + ".csv";
        String filePath = getReportsDirectory() + fullFileName;

        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            if (headers != null && headers.length > 0) {
                for (int i = 0; i < headers.length; i++) {
                    writer.print("\"" + headers[i] + "\"");
                    if (i < headers.length - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }

            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    String cellValue = row[i] != null ? row[i].replace("\"", "\"\"") : "";
                    writer.print("\"" + cellValue + "\"");
                    if (i < row.length - 1) {
                        writer.print(",");
                    }
                }
                writer.println();
            }
        }

        AlertUtil.showInfo("Export Complete", "File saved to: " + filePath);
    }
}