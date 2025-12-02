package edu.uni.logisim.util;

import java.util.logging.Logger;

/**
 * Utility for providing logging functionality
 */
public class LoggerProvider {
    
    /**
     * Get a logger for a class
     */
    public static Logger getLogger(Class<?> clazz) {
        return Logger.getLogger(clazz.getName());
    }
    
    /**
     * Get a logger with a specific name
     */
    public static Logger getLogger(String name) {
        return Logger.getLogger(name);
    }
}

