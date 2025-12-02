package edu.uni.logisim.domain.project;

import edu.uni.logisim.domain.circuit.Circuit;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a collection of circuits and related data.
 * A project can contain multiple circuits, where one circuit can act as a module/component
 * in another circuit (sub-circuits).
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class Project {
    private String name;
    private String path;
    private List<Circuit> circuits;
    
    /**
     * No-arg constructor for XStream deserialization.
     * Initializes an empty list of circuits.
     */
    public Project() {
        this.circuits = new ArrayList<>();
    }
    
    /**
     * Creates a new project with the specified name and path.
     * 
     * @param name the name of the project
     * @param path the file system path where the project is stored
     */
    public Project(String name, String path) {
        this.name = name;
        this.path = path;
        this.circuits = new ArrayList<>();
    }
    
    /**
     * Gets the name of the project.
     * 
     * @return the project name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name of the project.
     * 
     * @param name the new project name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the file system path of the project.
     * 
     * @return the project path
     */
    public String getPath() {
        return path;
    }
    
    /**
     * Sets the file system path of the project.
     * 
     * @param path the new project path
     */
    public void setPath(String path) {
        this.path = path;
    }
    
    /**
     * Gets the list of circuits in this project.
     * 
     * @return a list of circuits (may be empty, never null)
     */
    public List<Circuit> getCircuits() {
        return circuits;
    }
    
    /**
     * Adds a circuit to this project.
     * The circuit will only be added if it is not null and not already in the project.
     * 
     * @param circuit the circuit to add
     */
    public void addCircuit(Circuit circuit) {
        if (circuit != null && !circuits.contains(circuit)) {
            circuits.add(circuit);
        }
    }
    
    /**
     * Removes a circuit from this project.
     * 
     * @param circuit the circuit to remove
     */
    public void removeCircuit(Circuit circuit) {
        circuits.remove(circuit);
    }
    
    /**
     * Finds a circuit by its unique identifier.
     * 
     * @param id the circuit ID to search for
     * @return the circuit with the matching ID, or null if not found
     */
    public Circuit getCircuitById(String id) {
        return circuits.stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Finds a circuit by its name.
     * 
     * @param name the circuit name to search for
     * @return the circuit with the matching name, or null if not found
     */
    public Circuit getCircuitByName(String name) {
        return circuits.stream()
            .filter(c -> c.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}

