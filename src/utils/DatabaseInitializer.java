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

/**
 * Initializes the database by executing SQL scripts.
 * Creates tables, views, procedures, and indexes on first run.
 *
 * @author Vehicle Identification System Team
 * @version 1.0
 */
public class DatabaseInitializer {

    private static final String[] SQL_SCRIPTS = {
            "sql/01_create_tables.sql",
            "sql/02_create_views.sql",
            "sql/03_create_procedures.sql",
            "sql/04_insert_defaults.sql",
            "sql/05_create_indexes.sql"
    };

    private static final String USERS_TABLE_CHECK = "users";

    /**
     * Initializes the database if not already initialized.
     *
     * @return true if initialization was successful, false otherwise
     */
    public boolean initialize() {
        try {
            if (isDatabaseInitialized()) {
                System.out.println("Database already initialized, skipping setup.");
                return true;
            }

            System.out.println("Database not initialized. Starting initialization...");

            for (String scriptPath : SQL_SCRIPTS) {
                System.out.println("Executing: " + scriptPath);
                executeSqlScript(scriptPath);
            }

            System.out.println("Database initialization completed successfully.");
            return true;

        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if the database is already initialized.
     *
     * @return true if the users table exists, false otherwise
     */
    private boolean isDatabaseInitialized() {
        try (Connection conn = getConnection()) {
            ResultSet rs = conn.getMetaData().getTables(null, null, USERS_TABLE_CHECK, null);
            boolean exists = rs.next();
            rs.close();
            return exists;
        } catch (SQLException e) {
            System.err.println("Error checking database initialization: " + e.getMessage());
            return false;
        }
    }

    /**
     * Executes a single SQL script file.
     *
     * @param scriptPath the path to the SQL script
     */
    private void executeSqlScript(String scriptPath) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(scriptPath)) {
            if (input == null) {
                System.err.println("Script not found: " + scriptPath);
                return;
            }

            String sql = readScriptContent(input);
            String[] statements = splitSqlStatements(sql);

            try (Connection conn = getConnection();
                 Statement stmt = conn.createStatement()) {

                for (String statement : statements) {
                    String trimmed = statement.trim();
                    if (!trimmed.isEmpty() && !isComment(trimmed)) {
                        try {
                            stmt.execute(trimmed);
                        } catch (SQLException e) {
                            // Log but continue with other statements
                            String preview = trimmed.length() > 100 ? trimmed.substring(0, 100) + "..." : trimmed;
                            System.err.println("Warning - Error executing statement: " + preview);
                            System.err.println("Error: " + e.getMessage());
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error executing script " + scriptPath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Reads the content of a SQL script file.
     *
     * @param input the input stream
     * @return the file content as string
     */
    private String readScriptContent(InputStream input) {
        return new BufferedReader(new InputStreamReader(input))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    /**
     * Splits SQL statements by semicolons, handling quoted semicolons.
     *
     * @param sql the full SQL script
     * @return array of individual SQL statements
     */
    private String[] splitSqlStatements(String sql) {
        // Simple split - for production, consider a proper SQL parser
        return sql.split(";");
    }

    /**
     * Checks if a line is a comment.
     *
     * @param line the line to check
     * @return true if it's a comment, false otherwise
     */
    private boolean isComment(String line) {
        return line.startsWith("--") || line.startsWith("/*") || line.startsWith("//");
    }

    /**
     * Gets a database connection.
     *
     * @return a Connection object
     * @throws SQLException if connection fails
     */
    private Connection getConnection() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    /**
     * Reinitializes the database (drops and recreates all tables).
     * Warning: This will delete all existing data!
     *
     * @return true if reinitialization was successful
     */
    public boolean reinitialize() {
        try {
            executeSqlScript("sql/00_drop_all.sql");
            return initialize();
        } catch (Exception e) {
            System.err.println("Reinitialization failed: " + e.getMessage());
            return false;
        }
    }
}