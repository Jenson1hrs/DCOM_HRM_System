/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SecurityLogger {
    private static final String LOG_FILE = "security_audit.log";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    // Initialize log file (create if doesn't exist)
    static {
        try {
            // Create empty log file if it doesn't exist
            new FileWriter(LOG_FILE, true).close();
            System.out.println(" Security log initialized: " + LOG_FILE);
        } catch (IOException e) {
            System.err.println(" Failed to initialize security log: " + e.getMessage());
        }
    }
    
    public static void logEvent(String userId, String action, String status, String details) {
        String timestamp = dateFormat.format(new Date());
        String logEntry = String.format("[%s] USER=%s | ACTION=%s | STATUS=%s | DETAILS=%s",
                timestamp, userId, action, status, details);
        
        // Print to console
        System.out.println("[SECURITY] " + logEntry);
        
        // Write to file
        writeToFile(logEntry);
    }
    
    public static void logLogin(String userId, boolean success, String source) {
        String status = success ? "SUCCESS" : "FAILED";
        logEvent(userId, "LOGIN_ATTEMPT", status, "Source: " + source);
    }
    
    public static void logPasswordChange(String userId) {
        logEvent(userId, "PASSWORD_CHANGE", "SUCCESS", "Password updated");
    }
    
    public static void logSensitiveAction(String userId, String actionType, String target) {
        logEvent(userId, "SENSITIVE_ACTION", "EXECUTED", 
                "Action: " + actionType + ", Target: " + target);
    }
    
    public static void logDataAccess(String userId, String dataType, String targetId) {
        logEvent(userId, "DATA_ACCESS", "GRANTED", 
                "Data: " + dataType + ", ID: " + targetId);
    }
    
    public static void logError(String userId, String errorType, String message) {
        logEvent(userId, "SECURITY_ERROR", "FAILED", 
                "Error: " + errorType + ", Message: " + message);
    }
    
    private static void writeToFile(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, true))) {
            out.println(message);
        } catch (IOException e) {
            System.err.println(" Failed to write to security log: " + e.getMessage());
        }
    }
    
    // View log contents (for admin/HR)
    public static String getLogContents() {
        try {
            java.nio.file.Path path = java.nio.file.Paths.get(LOG_FILE);
            if (!java.nio.file.Files.exists(path)) {
                return "No security log found. Perform some actions first.";
            }
            
            StringBuilder content = new StringBuilder();
            java.nio.file.Files.lines(path).forEach(line -> content.append(line).append("\n"));
            return content.toString();
            
        } catch (IOException e) {
            return "Error reading security log: " + e.getMessage();
        }
    }
    
    // Clear log (for testing)
    public static void clearLog() {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, false))) {
            out.print("");
            System.out.println(" Security log cleared");
        } catch (IOException e) {
            System.err.println(" Failed to clear security log: " + e.getMessage());
        }
    }
}