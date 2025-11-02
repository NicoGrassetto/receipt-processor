package com.receiptprocessor;

import java.io.File;
import java.util.Map;

/**
 * Interface for extracting receipt data from PDF and image files.
 * This is a stub interface - implementation will be added later for actual OCR/AI processing.
 */
public interface IReceiptExtractor {
    
    /**
     * Extracts receipt information from a file (PDF or image).
     * Returns a flexible dictionary structure that can contain any keys/values.
     * 
     * Expected structure (but can be dynamic):
     * {
     *   "filename": "receipt.pdf",
     *   "total_price": 45.20,
     *   "items": [
     *     {
     *       "name": "Item 1",
     *       "price": 12.99,
     *       "protein": 15.5,
     *       "calories": 250.0
     *     },
     *     ...
     *   ],
     *   ... any other fields
     * }
     * 
     * @param receiptFile The receipt file to process
     * @return Dictionary with extracted data
     * @throws Exception if processing fails
     */
    Map<String, Object> extractReceiptData(File receiptFile) throws Exception;
}
