package edu.uni.logisim.util;

import java.util.UUID;

/**
 * Utility for generating unique IDs
 */
public class IdGenerator {
    private static int counter = 0;
    
    /**
     * Generate a unique ID
     */
    public static String generateId() {
        return "comp_" + (counter++);
    }
    
    /**
     * Generate a UUID-based ID
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Generate an ID with a prefix
     */
    public static String generateId(String prefix) {
        return prefix + "_" + (counter++);
    }
}

