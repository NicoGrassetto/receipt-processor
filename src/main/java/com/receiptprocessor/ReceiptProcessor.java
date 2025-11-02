package com.receiptprocessor;

import com.receiptprocessor.db.DatabaseManager;

import java.io.File;
import java.util.*;

public class ReceiptProcessor {
    
    private final DatabaseManager dbManager;
    private final FileScanner fileScanner;
    private final IReceiptExtractor extractor;
    private final GitAutoCommit gitAutoCommit;

    public ReceiptProcessor() {
        this.dbManager = new DatabaseManager();
        this.fileScanner = new FileScanner();
        this.extractor = new StubReceiptExtractor(); // Using stub implementation for now
        this.gitAutoCommit = new GitAutoCommit();
        
        try {
            dbManager.initialize();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    /**
     * Processes all receipts in the specified directory.
     * 
     * @param inputDirectory The directory containing receipt files
     * @return Number of receipts processed
     */
    public int processReceipts(File inputDirectory) throws Exception {
        List<File> files = fileScanner.scanDirectory(inputDirectory);
        int processedCount = 0;

        for (File file : files) {
            // Skip if already processed
            if (dbManager.receiptExists(file.getName())) {
                continue;
            }

            try {
                // Get dictionary output from extraction function
                Map<String, Object> data = extractor.extractReceiptData(file);
                
                // Save to database
                dbManager.saveReceiptData(file.getName(), data);
                processedCount++;
                
                // Auto-commit to Git after each receipt is processed
                gitAutoCommit.commitAndPush("Added receipt: " + file.getName());
                
            } catch (Exception e) {
                System.err.println("Failed to process " + file.getName() + ": " + e.getMessage());
            }
        }

        return processedCount;
    }

    /**
     * Gets all receipts from the database.
     * 
     * @return List of all receipt data as maps
     */
    public List<Map<String, Object>> getAllReceipts() throws Exception {
        return dbManager.getAllReceipts();
    }

    /**
     * Gets the raw data for a specific receipt.
     * 
     * @param receiptId The receipt ID
     * @return The raw dictionary data
     */
    public Map<String, Object> getReceiptData(int receiptId) throws Exception {
        return dbManager.getReceiptData(receiptId);
    }

    /**
     * Stub implementation of IReceiptExtractor.
     * Returns mock dictionary data for testing the pipeline.
     * Replace this with your actual extraction implementation.
     */
    private static class StubReceiptExtractor implements IReceiptExtractor {
        
        @Override
        public Map<String, Object> extractReceiptData(File receiptFile) throws Exception {
            // Create a stub dictionary with mock data
            Map<String, Object> data = new LinkedHashMap<>();
            
            // Add mock items
            List<Map<String, Object>> items = new ArrayList<>();
            
            Map<String, Object> item1 = new LinkedHashMap<>();
            item1.put("name", "Mock Item 1");
            item1.put("price", 12.99);
            item1.put("protein", 15.5);
            item1.put("calories", 250.0);
            items.add(item1);
            
            Map<String, Object> item2 = new LinkedHashMap<>();
            item2.put("name", "Mock Item 2");
            item2.put("price", 8.50);
            item2.put("protein", 20.0);
            item2.put("calories", 180.0);
            items.add(item2);
            
            Map<String, Object> item3 = new LinkedHashMap<>();
            item3.put("name", "Mock Item 3");
            item3.put("price", 5.75);
            item3.put("protein", 5.0);
            item3.put("calories", 120.0);
            items.add(item3);
            
            // Build the main data structure
            data.put("items", items);
            
            // Calculate total
            double total = items.stream()
                .mapToDouble(item -> ((Number) item.get("price")).doubleValue())
                .sum();
            data.put("total_price", total);
            
            // Add any other fields
            data.put("store_name", "Mock Store");
            data.put("date", "2025-11-02");
            
            return data;
        }
    }
}
