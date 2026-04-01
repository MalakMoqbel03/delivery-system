package com.ubs.delivery.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
/**
  this file manages a single MySQL connection
  We use Singleton so the whole app shares one connection

  All DAOs call getConnection() instead of creating new ones
  the database info is stored in db.properties

  ClassLoader loads db.properties from the resources folder,so it works both in intellij and when running as a JAR
 */
public class DBConnection {
    private static Connection connection = null;
    private DBConnection() {}
   /* Creates it once (first time), then reuses it
      Steps:1.Load db.properties 2. Read URL,username,password  3.Connect using DriverManager  4.Return the connection
*/

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Properties props = new Properties();
                InputStream input = DBConnection.class
                        .getClassLoader()
                        .getResourceAsStream("db.properties");

                if (input == null) {
                    throw new RuntimeException("db.properties not found in resources folder");
                }
                props.load(input);
                String url = props.getProperty("db.url");
                String user = props.getProperty("db.user");
                String password = props.getProperty("db.password");
                connection = DriverManager.getConnection(url, user, password);
                System.out.println("DB Connected to MySQL successfully");

            } catch (Exception e) {
                System.err.println("DB Connection failed: " + e.getMessage());
                System.err.println(" Check resources/db.properties");
                throw new RuntimeException(e);
            }
        }
        return connection;
    }
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("DB Connection closed.");
            }
        } catch (Exception e) {
            System.err.println("DB Error closing connection: " + e.getMessage());
        }
    }
}