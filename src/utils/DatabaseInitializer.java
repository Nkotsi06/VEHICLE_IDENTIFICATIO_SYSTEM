package utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

import database.DatabaseConnection;

public class DatabaseInitializer {

    public boolean initialize() {
        try {
            if (!isDatabaseInitialized()) {
                executeSqlScript("sql/01_create_tables.sql");
                executeSqlScript("sql/02_create_views.sql");
                executeSqlScript("sql/03_create_procedures.sql");
                executeSqlScript("sql/04_insert_defaults.sql");
                executeSqlScript("sql/05_create_indexes.sql");
                return true;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean isDatabaseInitialized() {
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            ResultSet rs = conn.getMetaData().getTables(null, null, "users", null);
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private void executeSqlScript(String scriptPath) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(scriptPath)) {
            if (input == null) {
                System.err.println("Script not found: " + scriptPath);
                return;
            }

            String sql = new BufferedReader(new InputStreamReader(input))
                    .lines()
                    .collect(Collectors.joining("\n"));

            String[] statements = sql.split(";");

            try (Connection conn = DatabaseConnection.getInstance().getConnection();
                 Statement stmt = conn.createStatement()) {

                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty() && !trimmed.startsWith("--")) {
                        try {
                            stmt.execute(trimmed);
                        } catch (SQLException e) {
                            System.err.println("Error executing: " + trimmed.substring(0, Math.min(100, trimmed.length())));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}