package com.receiptprocessor.db;

import com.receiptprocessor.model.Item;
import com.receiptprocessor.model.Receipt;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DatabaseManager {
    
    private static final String DB_URL = "jdbc:sqlite:receipts.db";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    
    /**
     * Initializes the database and creates tables if they don't exist.
     */
    public void initialize() throws SQLException {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            
            // Create receipts table with flexible JSON storage
            String createReceiptsTable = """
                CREATE TABLE IF NOT EXISTS receipts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    filename TEXT NOT NULL,
                    processed_date TEXT NOT NULL,
                    raw_data TEXT NOT NULL
                )
                """;
            stmt.execute(createReceiptsTable);
        }
    }
    
    /**
     * Saves raw dictionary data from the extraction function.
     * 
     * @param filename The filename of the receipt
     * @param data The dictionary data from the extraction function
     * @return The ID of the saved receipt
     */
    public int saveReceiptData(String filename, Map<String, Object> data) throws SQLException {
        String insertReceipt = "INSERT INTO receipts (filename, processed_date, raw_data) VALUES (?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(insertReceipt, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, filename);
            pstmt.setString(2, LocalDateTime.now().toString());
            pstmt.setString(3, gson.toJson(data));
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Failed to get receipt ID");
                }
            }
        }
    }
    

    
    /**
     * Retrieves all receipts from the database.
     * 
     * @return List of receipt data as maps
     */
    public List<Map<String, Object>> getAllReceipts() throws SQLException {
        List<Map<String, Object>> receipts = new ArrayList<>();
        String query = "SELECT id, filename, processed_date, raw_data FROM receipts ORDER BY processed_date DESC";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Map<String, Object> receiptData = gson.fromJson(rs.getString("raw_data"), Map.class);
                receiptData.put("id", rs.getInt("id"));
                receiptData.put("filename", rs.getString("filename"));
                receiptData.put("processed_date", rs.getString("processed_date"));
                receipts.add(receiptData);
            }
        }
        
        return receipts;
    }
    
    /**
     * Retrieves the raw data for a specific receipt.
     * 
     * @param receiptId The receipt ID
     * @return The raw dictionary data
     */
    public Map<String, Object> getReceiptData(int receiptId) throws SQLException {
        String query = "SELECT raw_data FROM receipts WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setInt(1, receiptId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return gson.fromJson(rs.getString("raw_data"), Map.class);
                }
            }
        }
        
        return null;
    }
    
    /**
     * Checks if a receipt with the given filename already exists.
     * 
     * @param filename The filename to check
     * @return true if the receipt exists
     */
    public boolean receiptExists(String filename) throws SQLException {
        String query = "SELECT COUNT(*) FROM receipts WHERE filename = ?";
        
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            
            pstmt.setString(1, filename);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
