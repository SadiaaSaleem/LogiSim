package edu.uni.logisim.domain.circuit;

import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.connector.Connector;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents a single digital circuit containing components and connectors.
 * A circuit can be used as a standalone design or as a sub-circuit module
 * within another circuit.
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class Circuit {
    private static final Logger logger = Logger.getLogger(Circuit.class.getName());
    private String id;
    private String name;
    private List<Component> components;
    private List<Connector> connectors;
    
    /**
     * No-arg constructor for XStream deserialization.
     * CRITICAL: Always initializes lists to ensure they're never null.
     */
    public Circuit() {
        this.components = new ArrayList<>();
        this.connectors = new ArrayList<>();
    }
    
    /**
     * Creates a new circuit with the specified ID and name.
     * 
     * @param id the unique identifier for this circuit
     * @param name the name of the circuit
     */
    public Circuit(String id, String name) {
        this.id = id;
        this.name = name;
        this.components = new ArrayList<>();
        this.connectors = new ArrayList<>();
    }
    
    /**
     * Gets the unique identifier of this circuit.
     * 
     * @return the circuit ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the name of this circuit.
     * 
     * @return the circuit name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the unique identifier of this circuit.
     * 
     * @param id the new circuit ID
     */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Sets the name of this circuit.
     * 
     * @param name the new circuit name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the list of components in this circuit.
     * CRITICAL: Always returns a non-null list (defensive programming).
     * 
     * @return a list of components (may be empty, never null)
     */
    public List<Component> getComponents() {
        // CRITICAL: Always return a non-null list (defensive programming)
        if (components == null) {
            components = new ArrayList<>();
            logger.warning("Circuit.components was null, initializing empty list");
        }
        return components;
    }
    
    /**
     * Sets the list of components for this circuit.
     * Used by XStream during deserialization.
     * 
     * @param components the list of components (will be initialized if null)
     */
    public void setComponents(List<Component> components) {
        // Allow XStream to set the list directly
        this.components = components;
        if (this.components == null) {
            this.components = new ArrayList<>();
        }
    }
    
    /**
     * Adds a component to this circuit.
     * The component will only be added if it is not null and not already in the circuit.
     * 
     * @param component the component to add
     */
    public void addComponent(Component component) {
        // Ensure list is initialized
        if (components == null) {
            components = new ArrayList<>();
        }
        if (component != null && !components.contains(component)) {
            components.add(component);
        }
    }
    
    /**
     * Removes a component from this circuit.
     * Also removes all connectors connected to this component.
     * 
     * @param component the component to remove
     */
    public void removeComponent(Component component) {
        components.remove(component);
        // Remove all connectors connected to this component
        connectors.removeIf(connector -> 
            connector.getSourceComponent() == component || 
            connector.getSinkComponent() == component);
    }
    
    /**
     * Gets the list of connectors (wires) in this circuit.
     * CRITICAL: Always returns a non-null list (defensive programming).
     * 
     * @return a list of connectors (may be empty, never null)
     */
    public List<Connector> getConnectors() {
        // CRITICAL: Always return a non-null list (defensive programming)
        if (connectors == null) {
            connectors = new ArrayList<>();
            logger.warning("Circuit.connectors was null, initializing empty list");
        }
        return connectors;
    }
    
    /**
     * Sets the list of connectors for this circuit.
     * Used by XStream during deserialization.
     * 
     * @param connectors the list of connectors (will be initialized if null)
     */
    public void setConnectors(List<Connector> connectors) {
        // Allow XStream to set the list directly
        this.connectors = connectors;
        if (this.connectors == null) {
            this.connectors = new ArrayList<>();
        }
    }
    
    /**
     * Adds a connector (wire) to this circuit.
     * The connector will only be added if it is not null and not already in the circuit.
     * 
     * @param connector the connector to add
     */
    public void addConnector(Connector connector) {
        // Ensure list is initialized
        if (connectors == null) {
            connectors = new ArrayList<>();
        }
        if (connector != null && !connectors.contains(connector)) {
            connectors.add(connector);
        }
    }
    
    /**
     * Removes a connector from this circuit.
     * 
     * @param connector the connector to remove
     */
    public void removeConnector(Connector connector) {
        connectors.remove(connector);
    }
    
    /**
     * Finds a component by its unique identifier.
     * 
     * @param id the component ID to search for
     * @return the component with the matching ID, or null if not found
     */
    public Component getComponentById(String id) {
        return components.stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Finds a connector by its unique identifier.
     * 
     * @param id the connector ID to search for
     * @return the connector with the matching ID, or null if not found
     */
    public Connector getConnectorById(String id) {
        return connectors.stream()
            .filter(c -> c.getId().equals(id))
            .findFirst()
            .orElse(null);
    }
}

