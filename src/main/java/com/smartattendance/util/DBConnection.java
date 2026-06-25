package com.smartattendance.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection - Database Connection Utility Class
 *
 * Responsibilities:
 *  - Reads database configuration from db.properties.
 *  - Loads the JDBC driver.
 *  - Provides a static method to get a database connection.
 *  - Provides utility methods to close DB resources safely.
 *
 * Pattern: Utility Class (all static methods, no instantiation).
 *
 * Usage:
 *   Connection conn = DBConnection.getConnection();
 *   // ... use connection ...
 *   DBConnection.closeConnection(conn);
 */
public final class DBConnection {

    // ----------------------------------------------------------------
    // Logger
    // ----------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    // ----------------------------------------------------------------
    // Properties File Name (must be on classpath)
    // ----------------------------------------------------------------
    private static final String PROPERTIES_FILE = "db.properties";

    // ----------------------------------------------------------------
    // Database Configuration Fields (loaded once at class load time)
    // ----------------------------------------------------------------
    private static String DB_DRIVER;
    private static String DB_URL;
    private static String DB_USERNAME;
    private static String DB_PASSWORD;

    // ----------------------------------------------------------------
    // Static Initializer Block
    // Runs once when the class is first loaded by the JVM.
    // Loads db.properties and registers the JDBC driver.
    // ----------------------------------------------------------------
    static {
        loadProperties();
        registerDriver();
    }

    // ----------------------------------------------------------------
    // Private Constructor
    // Prevents instantiation of this utility class.
    // ----------------------------------------------------------------
    private DBConnection() {
        throw new UnsupportedOperationException(
                "DBConnection is a utility class and cannot be instantiated."
        );
    }

    // ----------------------------------------------------------------
    // loadProperties()
    // Reads configuration values from db.properties file.
    // ----------------------------------------------------------------
    private static void loadProperties() {
        Properties properties = new Properties();

        try (InputStream inputStream =
                     DBConnection.class
                             .getClassLoader()
                             .getResourceAsStream(PROPERTIES_FILE)) {

            if (inputStream == null) {
                logger.error("FATAL: '{}' not found on the classpath.", PROPERTIES_FILE);
                throw new RuntimeException(
                        "Configuration file '" + PROPERTIES_FILE + "' not found on classpath."
                );
            }

            properties.load(inputStream);

            DB_DRIVER   = properties.getProperty("db.driver");
            DB_URL      = properties.getProperty("db.url");
            DB_USERNAME = properties.getProperty("db.username");
            DB_PASSWORD = properties.getProperty("db.password");

            logger.info("Database properties loaded successfully from '{}'.", PROPERTIES_FILE);
            logger.info("Target Database URL: {}", DB_URL);

        } catch (IOException e) {
            logger.error("FATAL: Failed to load database properties from '{}'.", PROPERTIES_FILE, e);
            throw new RuntimeException(
                    "Failed to load database properties from '" + PROPERTIES_FILE + "'.", e
            );
        }
    }

    // ----------------------------------------------------------------
    // registerDriver()
    // Explicitly registers the MySQL JDBC driver with DriverManager.
    // ----------------------------------------------------------------
    private static void registerDriver() {
        try {
            Class.forName(DB_DRIVER);
            logger.info("JDBC Driver '{}' registered successfully.", DB_DRIVER);
        } catch (ClassNotFoundException e) {
            logger.error("FATAL: JDBC Driver '{}' not found on classpath.", DB_DRIVER, e);
            throw new RuntimeException(
                    "JDBC Driver '" + DB_DRIVER + "' not found. Check pom.xml dependencies.", e
            );
        }
    }

    // ----------------------------------------------------------------
    // getConnection()
    // Opens and returns a new database connection.
    // Caller is responsible for closing the connection.
    // ----------------------------------------------------------------

    /**
     * Returns a new database {@link Connection}.
     *
     * <p>The caller is responsible for closing the connection
     * using {@link #closeConnection(Connection)} or a try-with-resources block.</p>
     *
     * @return A valid, open {@link Connection} to the database.
     * @throws SQLException if a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        logger.debug("Database connection opened successfully. URL: {}", DB_URL);
        return connection;
    }

    // ----------------------------------------------------------------
    // closeConnection()
    // Safely closes a Connection object, suppressing null checks.
    // ----------------------------------------------------------------

    /**
     * Safely closes a {@link Connection}.
     *
     * @param connection The {@link Connection} to close. Can be null.
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                logger.debug("Database connection closed successfully.");
            } catch (SQLException e) {
                logger.warn("Failed to close database connection.", e);
            }
        }
    }

    // ----------------------------------------------------------------
    // closeResources()
    // Safely closes Connection, Statement, and ResultSet together.
    // ----------------------------------------------------------------

    /**
     * Safely closes a {@link java.sql.ResultSet},
     * {@link java.sql.Statement}, and {@link Connection} in order.
     *
     * @param connection The {@link Connection} to close.
     * @param statement  The {@link java.sql.Statement} to close.
     * @param resultSet  The {@link java.sql.ResultSet} to close.
     */
    public static void closeResources(
            Connection connection,
            java.sql.Statement statement,
            java.sql.ResultSet resultSet) {

        if (resultSet != null) {
            try {
                resultSet.close();
                logger.debug("ResultSet closed.");
            } catch (SQLException e) {
                logger.warn("Failed to close ResultSet.", e);
            }
        }

        if (statement != null) {
            try {
                statement.close();
                logger.debug("Statement closed.");
            } catch (SQLException e) {
                logger.warn("Failed to close Statement.", e);
            }
        }

        closeConnection(connection);
    }

    // ----------------------------------------------------------------
    // testConnection()
    // Utility method to verify database connectivity.
    // Useful during development and startup health checks.
    // ----------------------------------------------------------------

    /**
     * Tests database connectivity by attempting to open and
     * immediately close a connection.
     *
     * @return {@code true} if connection is successful, {@code false} otherwise.
     */
    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            boolean isValid = connection != null && !connection.isClosed();
            if (isValid) {
                logger.info("Database connectivity test PASSED.");
            } else {
                logger.warn("Database connectivity test FAILED.");
            }
            return isValid;
        } catch (SQLException e) {
            logger.error("Database connectivity test FAILED with exception.", e);
            return false;
        }
    }
}