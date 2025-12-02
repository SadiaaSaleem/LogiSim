package edu.uni.logisim.domain.connector;

import java.awt.Point;

/**
 * Represents an input or output point on a component.
 * Ports are connection points where connectors (wires) can be attached
 * to transmit signals between components.
 * 
 * <p>Each port has:
 * <ul>
 *   <li>A unique identifier</li>
 *   <li>A type (INPUT or OUTPUT)</li>
 *   <li>A position on the canvas</li>
 *   <li>A current signal value (true = high/logic 1, false = low/logic 0)</li>
 * </ul>
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class Port {
    private String id;
    private PortType type;
    private Point position;
    private boolean value; // Current signal value (true = high, false = low)
    
    /**
     * No-arg constructor for XStream deserialization.
     * Initializes the port with a default value of false (low signal).
     */
    public Port() {
        this.value = false;
    }
    
    /**
     * Creates a new port with the specified ID, type, and position.
     * 
     * @param id the unique identifier for this port
     * @param type the port type (INPUT or OUTPUT)
     * @param position the position of the port on the canvas
     */
    public Port(String id, PortType type, Point position) {
        this.id = id;
        this.type = type;
        this.position = position;
        this.value = false;
    }
    
    /**
     * Gets the unique identifier of this port.
     * 
     * @return the port ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the type of this port (INPUT or OUTPUT).
     * 
     * @return the port type
     */
    public PortType getType() {
        return type;
    }
    
    /**
     * Gets the position of this port on the canvas.
     * 
     * @return the port position
     */
    public Point getPosition() {
        return position;
    }
    
    /**
     * Sets the position of this port on the canvas.
     * 
     * @param position the new port position
     */
    public void setPosition(Point position) {
        this.position = position;
    }
    
    /**
     * Gets the current signal value of this port.
     * 
     * @return true for high signal (logic 1), false for low signal (logic 0)
     */
    public boolean getValue() {
        return value;
    }
    
    /**
     * Sets the current signal value of this port.
     * 
     * @param value true for high signal (logic 1), false for low signal (logic 0)
     */
    public void setValue(boolean value) {
        this.value = value;
    }
}

