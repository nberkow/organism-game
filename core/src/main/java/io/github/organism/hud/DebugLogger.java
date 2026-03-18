package io.github.organism.hud;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Singleton debug logger for tracking game state during development.
 * Logs to file: logs/organism_debug.log
 */
public class DebugLogger {
    private static DebugLogger instance;
    private boolean enabled = true;
    private List<String> logBuffer = new ArrayList<>();
    private int maxBufferSize = 1000;
    private PrintWriter fileWriter;
    private String logFilePath = "logs/organism_debug.log";
    
    private DebugLogger() {
        // Create logs directory if it doesn't exist
        try {
            java.io.File logDir = new java.io.File("logs");
            if (!logDir.exists()) {
                logDir.mkdirs();
            }
            
            // Open log file in append mode
            fileWriter = new PrintWriter(new FileWriter(logFilePath, true), true);
            
            // Write session start marker
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            fileWriter.println("\n=== Session started: " + sdf.format(new Date()) + " ===");
            
        } catch (IOException e) {
            System.err.println("Failed to open log file: " + e.getMessage());
            fileWriter = null;
        }
    }
    
    public static DebugLogger getInstance() {
        if (instance == null) {
            instance = new DebugLogger();
        }
        return instance;
    }
    
    /**
     * Log a message if debugging is enabled.
     */
    public void log(String message) {
        if (enabled) {
            // Write to file instead of console
            if (fileWriter != null) {
                fileWriter.println(message);
            }
            
            // Also buffer for potential UI display
            logBuffer.add(message);
            if (logBuffer.size() > maxBufferSize) {
                logBuffer.remove(0);
            }
        }
    }
    
    /**
     * Log a formatted message.
     */
    public void logf(String format, Object... args) {
        log(String.format(format, args));
    }
    
    /**
     * Enable or disable debug logging.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Check if logging is enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }
    
    /**
     * Clear the log buffer.
     */
    public void clear() {
        logBuffer.clear();
    }
    
    /**
     * Get recent log messages.
     */
    public List<String> getRecentLogs(int count) {
        int start = Math.max(0, logBuffer.size() - count);
        return new ArrayList<>(logBuffer.subList(start, logBuffer.size()));
    }
    
    /**
     * Get all buffered logs.
     */
    public List<String> getAllLogs() {
        return new ArrayList<>(logBuffer);
    }
    
    /**
     * Get the log file path.
     */
    public String getLogFilePath() {
        return logFilePath;
    }
    
    /**
     * Close the log file.
     */
    public void close() {
        if (fileWriter != null) {
            fileWriter.close();
            fileWriter = null;
        }
    }
}
