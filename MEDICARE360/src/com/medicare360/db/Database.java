package com.medicare360.db;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Opens connections to the SQLite database and creates it on first run.
 * The database lives in the file  data/medicare360.db  (created automatically).
 */
public final class Database {

    private static final String DB_FOLDER = "data";
    private static final String DB_URL = "jdbc:sqlite:" + DB_FOLDER + "/medicare360.db";

    private Database() { }

    /** Opens a new connection. Always use it inside try-with-resources so it is closed automatically. */
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL);
        try (Statement statement = connection.createStatement()) {
            // SQLite needs this switch to enforce relationships between tables.
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    /** Creates the folder, the tables and (only the very first time) the demo data. */
    public static void initialize() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("The SQLite driver (sqlite-jdbc jar) was not found.\n"
                    + "Put the jar files from the 'lib' folder on the classpath - see README.md, step 2.", e);
        }

        new File(DB_FOLDER).mkdirs();

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);      // all-or-nothing: either everything is saved or nothing
            try (Statement statement = connection.createStatement()) {
                runScript(statement, DatabaseScripts.SCHEMA);

                boolean firstRun;
                try (ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM admins")) {
                    rs.next();
                    firstRun = rs.getInt(1) == 0;
                }
                if (firstRun) {
                    runScript(statement, DatabaseScripts.SEED_DATA);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    /** Runs every statement of a script (statements are separated by semicolons). */
    private static void runScript(Statement statement, String script) throws SQLException {
        for (String sql : script.split(";")) {
            if (!sql.trim().isEmpty()) {
                statement.execute(sql);
            }
        }
    }

    /** Returns the id of the row that was just inserted on this connection. */
    static int lastInsertId(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery("SELECT last_insert_rowid()")) {
            return rs.next() ? rs.getInt(1) : -1;
        }
    }
}
