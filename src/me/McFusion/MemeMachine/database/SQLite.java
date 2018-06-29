package me.McFusion.MemeMachine.database;

import java.sql.*;

public abstract class SQLite {

    private Connection con = null;

    public SQLite(String path) {
        try {
            Class.forName("org.sqlite.JDBC");
            con = DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void close() {
        try {
            if (con != null && !con.isClosed()) con.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public ResultSet select(String tblName, String conditionQuery) {
        try {
            return con.createStatement().executeQuery("SELECT * FROM " + tblName + " " + conditionQuery);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public void execute(String statement) {
        try {
            con.createStatement().executeUpdate(statement);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Ex: createTable("settings", "key TEXT, value TEXT");
     */
    public void createTable(String tblName, String tblFields) {
        execute("CREATE TABLE " + tblName + " (" + tblFields + ")");
    }

    /**
     * Ex: insertInto("settings", "key, value", "'theme', 'none'");
     */
    public void insertInto(String tblName, String tblFields, String tblValues) {
        execute("INSERT INTO " + tblName + " (" + tblFields + ") VALUES (" + tblValues + ")");
    }

    /**
     * Ex: update("settings", "value = 'ok'", "key = 'path'");
     */
    public void update(String tblName, String query, String tblLocation) {
        execute("UPDATE " + tblName + " SET " + query + " WHERE " + tblLocation);
    }

    public void delete(String tblName, String tblLocation) {
        execute("DELETE FROM " + tblName + " WHERE " + tblLocation);
    }
}
