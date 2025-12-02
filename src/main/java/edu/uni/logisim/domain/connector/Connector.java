package java.edu.uni.logisim.domain.connector;

import edu.uni.logisim.domain.component.Component;

import java.awt.*;

/**
 * Represents a wire or connection between component ports.
 * Connectors transmit signals from a source component's output port
 * to a sink component's input port.
 * 
 * <p>Connectors are responsible for:
 * <ul>
 *   <li>Transmitting output values from source to sink components</li>
 *   <li>Visual representation with different colors based on signal state</li>
 *   <li>Maintaining connection information (source/sink ports and components)</li>
 * </ul>
 * 
 * <p>Connector colors:
 * <ul>
 *   <li>Green: High signal (logic 1, true)</li>
 *   <li>Black: Low signal (logic 0, false)</li>
 * </ul>
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class Connector {
    private String id;
    private Color color;
    private Point startPosition;
    private Point endPosition;
    private Port sourcePort;
    private Port sinkPort;
    private Component sourceComponent;
    private Component sinkComponent;
    private boolean value; // Current signal value
    
    /**
     * No-arg constructor for XStream deserialization.
     * Initializes the connector with default color (black) and value (false).
     */
    public Connector() {
        this.color = Color.BLACK; // Default color
        this.value = false;
    }
    
    /**
     * Creates a new connector connecting a source port to a sink port.
     * 
     * @param id the unique identifier for this connector
     * @param sourcePort the output port of the source component
     * @param sinkPort the input port of the sink component
     * @param sourceComponent the component that outputs the signal
     * @param sinkComponent the component that receives the signal
     */
    public Connector(String id, Port sourcePort, Port sinkPort,
                     Component sourceComponent, Component sinkComponent) {
        this.id = id;
        this.sourcePort = sourcePort;
        this.sinkPort = sinkPort;
        this.sourceComponent = sourceComponent;
        this.sinkComponent = sinkComponent;
        this.color = Color.BLACK; // Default color
        this.value = false;
    }
    
    /**
     * Gets the unique identifier of this connector.
     * 
     * @return the connector ID
     */
    public String getId() {
        return id;
    }
    
    /**
     * Gets the color of this connector.
     * Color changes based on signal value (green for high, black for low).
     * 
     * @return the connector color
     */
    public Color getColor() {
        return color;
    }
    
    /**
     * Sets the color of this connector.
     * 
     * @param color the new connector color
     */
    public void setColor(Color color) {
        this.color = color;
    }
    
    /**
     * Gets the start position of this connector (source port position).
     * 
     * @return the start position, or source port position if startPosition is null
     */
    public Point getStartPosition() {
        return startPosition != null ? startPosition : sourcePort.getPosition();
    }
    
    /**
     * Sets the start position of this connector.
     * 
     * @param startPosition the new start position
     */
    public void setStartPosition(Point startPosition) {
        this.startPosition = startPosition;
    }
    
    /**
     * Gets the end position of this connector (sink port position).
     * 
     * @return the end position, or sink port position if endPosition is null
     */
    public Point getEndPosition() {
        return endPosition != null ? endPosition : sinkPort.getPosition();
    }
    
    /**
     * Sets the end position of this connector.
     * 
     * @param endPosition the new end position
     */
    public void setEndPosition(Point endPosition) {
        this.endPosition = endPosition;
    }
    
    /**
     * Gets the source port (output port) of this connector.
     * 
     * @return the source port
     */
    public Port getSourcePort() {
        return sourcePort;
    }
    
    /**
     * Gets the sink port (input port) of this connector.
     * 
     * @return the sink port
     */
    public Port getSinkPort() {
        return sinkPort;
    }
    
    /**
     * Gets the source component (component that outputs the signal).
     * 
     * @return the source component
     */
    public Component getSourceComponent() {
        return sourceComponent;
    }
    
    /**
     * Gets the sink component (component that receives the signal).
     * 
     * @return the sink component
     */
    public Component getSinkComponent() {
        return sinkComponent;
    }
    
    /**
     * Gets the current signal value transmitted by this connector.
     * 
     * @return true for high signal (logic 1), false for low signal (logic 0)
     */
    public boolean getValue() {
        return value;
    }
    
    /**
     * Sets the current signal value transmitted by this connector.
     * 
     * @param value true for high signal (logic 1), false for low signal (logic 0)
     */
    public void setValue(boolean value) {
        this.value = value;
    }
    
    /**
     * Propagates the signal from the source port to the sink port.
     * This method reads the value from the source port, updates the connector's
     * value, and sets the sink port's value accordingly.
     */
    public void propagate() {
        if (sourcePort != null && sinkPort != null) {
            boolean sourceValue = sourcePort.getValue();
            this.value = sourceValue;
            sinkPort.setValue(sourceValue);
        }
    }
}

