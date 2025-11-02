package com.receiptprocessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

public class GitAutoCommit {
    
    private final File projectRoot;
    
    public GitAutoCommit() {
        // Assume we're running from the project root
        this.projectRoot = new File(System.getProperty("user.dir"));
    }
    
    /**
     * Commits and pushes changes to GitHub automatically.
     * 
     * @param message The commit message
     */
    public void commitAndPush(String message) {
        try {
            // Check if git is initialized
            File gitDir = new File(projectRoot, ".git");
            if (!gitDir.exists()) {
                System.out.println("Git not initialized. Skipping auto-commit.");
                return;
            }
            
            // Stage the database file
            executeGitCommand("git", "add", "receipts.db");
            
            // Check if there are changes to commit
            String status = executeGitCommand("git", "status", "--porcelain");
            if (status.trim().isEmpty()) {
                System.out.println("No changes to commit.");
                return;
            }
            
            // Commit
            executeGitCommand("git", "commit", "-m", message);
            
            // Push to remote (if configured)
            try {
                executeGitCommand("git", "push");
                System.out.println("Successfully pushed to GitHub: " + message);
            } catch (Exception e) {
                System.out.println("Push failed (no remote configured or network issue): " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.err.println("Git auto-commit failed: " + e.getMessage());
        }
    }
    
    private String executeGitCommand(String... command) throws Exception {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(projectRoot);
        pb.redirectErrorStream(true);
        
        Process process = pb.start();
        StringBuilder output = new StringBuilder();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("Git command failed: " + output);
        }
        
        return output.toString();
    }
}
