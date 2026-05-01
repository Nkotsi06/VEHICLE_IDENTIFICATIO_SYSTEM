package utils;

import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class PrintUtil {

    public static boolean printNode(Node node) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(node.getScene().getWindow())) {
            boolean success = job.printPage(node);
            if (success) {
                job.endJob();
                AlertUtil.showInfo("Print", "Print job completed successfully.");
                return true;
            }
        }
        return false;
    }

    public static boolean printPane(Pane pane, String title) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null) {
            job.getJobSettings().setJobName(title);
            if (job.showPrintDialog(pane.getScene().getWindow())) {
                boolean success = job.printPage(pane);
                if (success) {
                    job.endJob();
                    AlertUtil.showInfo("Print", "Print job completed successfully.");
                    return true;
                }
            }
        }
        return false;
    }
}