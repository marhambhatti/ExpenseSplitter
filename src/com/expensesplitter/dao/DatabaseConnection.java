// FIXED: Moved to dao package; added getConnection() and MySQL JAR classpath note
// IMPORTANT: Add mysql-connector-j-8.x.x.jar to your project classpath.
// IntelliJ: File → Project Structure → Libraries → + → JAR
// Download: https://dev.mysql.com/downloads/connector/j/
package com.expensesplitter.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL      = "jdbc:mysql://localhost:3306/expense_splitter_db";
    private static final String USER     = "root";   // change if needed
    private static final String PASSWORD = "";        // change if needed

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
