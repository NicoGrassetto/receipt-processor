package com.receiptprocessor;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileScanner {
    
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(
        ".pdf", ".jpg", ".jpeg", ".png", ".gif", ".bmp"
    );

    /**
     * Scans a directory for receipt files (PDFs and images).
     * 
     * @param directory The directory to scan
     * @return List of receipt files found
     */
    public List<File> scanDirectory(File directory) {
        List<File> receiptFiles = new ArrayList<>();
        
        if (directory == null || !directory.exists() || !directory.isDirectory()) {
            return receiptFiles;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return receiptFiles;
        }

        for (File file : files) {
            if (file.isFile() && isSupportedFile(file)) {
                receiptFiles.add(file);
            }
        }

        return receiptFiles;
    }

    /**
     * Checks if a file has a supported extension.
     * 
     * @param file The file to check
     * @return true if the file is supported
     */
    private boolean isSupportedFile(File file) {
        String filename = file.getName().toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(filename::endsWith);
    }
}
