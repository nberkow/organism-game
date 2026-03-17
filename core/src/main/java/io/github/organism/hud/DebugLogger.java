package io.github.organism.hud;

import java.util.ArrayList;
import java.util.List;

/**
 * Singleton debug logger for tracking game state during development.
 * Can be toggled on/off and provides structured logging.
 */
public class DebugLogger {
    private static DebugLogger instance;
    private boolean enabled = true;
    private List<String> logBuffer = new ArrayList<>();
    private int maxBufferSize = 1000;
    
    private DebugLogger() {
        // Private constructor for singleton
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
            System.out.println(message);
            
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
}
