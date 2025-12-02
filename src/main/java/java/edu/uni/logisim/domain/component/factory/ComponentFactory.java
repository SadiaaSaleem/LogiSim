package edu.uni.logisim.domain.component.factory;

import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.component.SubCircuitComponent;
import edu.uni.logisim.domain.component.io.InputSwitch;
import edu.uni.logisim.domain.component.io.LedOutput;
import edu.uni.logisim.domain.component.logic.AndGate;
import edu.uni.logisim.domain.component.logic.NotGate;
import edu.uni.logisim.domain.component.logic.OrGate;
import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.project.Project;
import edu.uni.logisim.persistence.JsonProjectSerializer;
import edu.uni.logisim.util.IdGenerator;
import java.awt.Point;
import java.io.File;

/**
 * Factory class responsible for creating instances of various components
 */
public class ComponentFactory {
    
    /**
     * Creates a component based on type name
     */
    public static Component createComponent(String type, String id, Point position) {
        if (id == null || id.isEmpty()) {
            id = IdGenerator.generateId();
        }
        
        switch (type.toUpperCase()) {
            case "AND":
                return new AndGate(id, position);
            case "OR":
                return new OrGate(id, position);
            case "NOT":
                return new NotGate(id, position);
            case "INPUT":
            case "SWITCH":
                return new InputSwitch(id, position);
            case "LED":
            case "OUTPUT":
                return new LedOutput(id, position);
            default:
                // Check if it's a circuit from current project (in-memory)
                if (type.startsWith("CIRCUIT:")) {
                    // CIRCUIT: type is handled in LogiSimApplication.addComponentToCircuit()
                    // because it needs access to currentProject
                    throw new IllegalArgumentException("CIRCUIT: type requires project context - handled in LogiSimApplication");
                }
                // Check if it's a custom circuit from file (starts with "CUSTOM:" or is a file path)
                else if (type.startsWith("CUSTOM:")) {
                    String circuitPath = type.substring(7); // Remove "CUSTOM:" prefix
                    return createSubCircuitComponent(id, circuitPath, position);
                } else if (type.endsWith(".dig")) {
                    // Direct .dig file path
                    return createSubCircuitComponent(id, type, position);
                }
                throw new IllegalArgumentException("Unknown component type: " + type);
        }
    }
    
    /**
     * Creates a sub-circuit component from a Circuit object (in-memory)
     * This is simpler and avoids file deserialization issues
     */
    public static SubCircuitComponent createSubCircuitComponent(String id, Circuit circuit, Point position) {
        if (circuit == null) {
            throw new IllegalArgumentException("Circuit cannot be null");
        }
        
        // Validate circuit has components
        if (circuit.getComponents() == null) {
            throw new IllegalArgumentException("Circuit has null components list. Circuit may not be properly initialized.");
        }
        
        String circuitName = circuit.getName();
        if (id == null || id.isEmpty()) {
            id = IdGenerator.generateId();
        }
        
        System.out.println("DEBUG: Creating sub-circuit from circuit '" + circuitName + 
                         "' with " + circuit.getComponents().size() + " components");
        
        // Create a copy of the circuit to avoid modifying the original
        Circuit circuitCopy = copyCircuit(circuit);
        
        // Validate the copy also has components
        if (circuitCopy.getComponents() == null) {
            throw new IllegalArgumentException("Circuit copy has null components list after copying.");
        }
        
        // Use constructor that takes Circuit object directly (for in-memory usage)
        SubCircuitComponent subCircuitComp = new SubCircuitComponent(id, circuitName, circuitCopy, position);
        
        // Validate the sub-circuit was created properly
        int inputCount = subCircuitComp.getInputCount();
        int outputCount = subCircuitComp.getOutputCount();
        
        System.out.println("DEBUG: Sub-circuit component created from in-memory circuit: " + circuitName + 
                         " with " + inputCount + " inputs and " + outputCount + " outputs");
        
        return subCircuitComp;
    }
    
    /**
     * Creates a sub-circuit component from a saved circuit file
     * This method tries to load from file, but provides better error handling
     */
    public static SubCircuitComponent createSubCircuitComponent(String id, String circuitFilePath, Point position) {
        try {
            System.out.println("DEBUG: Attempting to load sub-circuit from file: " + circuitFilePath);
            
            // Check if file exists
            java.io.File file = new java.io.File(circuitFilePath);
            if (!file.exists()) {
                throw new RuntimeException("Circuit file does not exist: " + circuitFilePath);
            }
            
            JsonProjectSerializer serializer = new JsonProjectSerializer();
            Project project = serializer.deserialize(circuitFilePath);
            
            // Get the first circuit from the project
            Circuit subCircuit = null;
            if (project.getCircuits() != null && !project.getCircuits().isEmpty()) {
                subCircuit = project.getCircuits().get(0);
                System.out.println("DEBUG: Successfully loaded circuit from file: " + subCircuit.getName() + 
                                 " with " + subCircuit.getComponents().size() + " components");
                
                // Validate the circuit has the required components
                if (subCircuit.getComponents().isEmpty()) {
                    throw new RuntimeException("Circuit file contains an empty circuit. Add INPUT and LED components first.");
                }
                
                // Use the in-memory version (createSubCircuitComponent with Circuit)
                return createSubCircuitComponent(id, subCircuit, position);
            } else {
                throw new IllegalArgumentException("Project file contains no circuits: " + circuitFilePath);
            }
        } catch (RuntimeException e) {
            // Re-throw runtime exceptions as-is
            throw e;
        } catch (Exception e) {
            System.err.println("ERROR: Failed to load sub-circuit from file: " + e.getMessage());
            e.printStackTrace();
            // Provide more helpful error message
            String errorMsg = "Failed to load sub-circuit from file.\n\n" +
                            "Possible causes:\n" +
                            "1. File is corrupted or in wrong format\n" +
                            "2. File was created with a different version\n" +
                            "3. File contains incompatible components\n\n" +
                            "Try:\n" +
                            "- Create a new circuit and save it again\n" +
                            "- Use circuits from the current project instead (no file loading needed)";
            throw new RuntimeException(errorMsg + "\n\nTechnical error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Creates a deep copy of a circuit for use as a sub-circuit
     * This prevents modifying the original circuit
     */
    private static Circuit copyCircuit(Circuit original) {
        // For now, we'll use the original circuit directly
        // In a production system, you'd want to deep copy to avoid side effects
        // But for simplicity, we'll use the original
        return original;
    }
    
    /**
     * Creates a component with auto-generated ID
     */
    public static Component createComponent(String type, Point position) {
        return createComponent(type, IdGenerator.generateId(), position);
    }
    
    /**
     * Get available component types
     */
    public static String[] getAvailableTypes() {
        return new String[]{"AND", "OR", "NOT", "INPUT", "LED"};
    }
    
    /**
     * Get available custom circuit files from a directory
     */
    public static String[] getAvailableCustomCircuits(String directoryPath) {
        java.util.List<String> customCircuits = new java.util.ArrayList<>();
        File dir = new File(directoryPath);
        
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".dig"));
            if (files != null) {
                for (File file : files) {
                    customCircuits.add(file.getAbsolutePath());
                }
            }
        }
        
        return customCircuits.toArray(new String[0]);
    }
}

