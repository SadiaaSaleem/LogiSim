package edu.uni.logisim.domain.component;

import edu.uni.logisim.domain.connector.Port;
import edu.uni.logisim.domain.connector.PortType;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all circuit components.
 * Each component has inputs and outputs (ports) and can execute logic
 * to process input values and generate output values.
 * 
 * <p>Components are responsible for:
 * <ul>
 *   <li>Defining their input and output ports</li>
 *   <li>Executing their logic when {@link #execute()} is called</li>
 *   <li>Updating output port values based on input port values</li>
 * </ul>
 * 
 * <p>Subclasses include:
 * <ul>
 *   <li>Logic gates (AND, OR, NOT)</li>
 *   <li>Input/output components (InputSwitch, LedOutput)</li>
 *   <li>Sub-circuits (SubCircuitComponent)</li>
 * </ul>
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public abstract class Component {
    protected String id;
    protected String name;
    protected Point position;
    protected List<Port> inputPorts;
    protected List<Port> outputPorts;
    
    /**
     * Creates a new component with the specified ID, name, and position.
     * 
     * @param id the unique identifier for this component
     * @param name the name of the component
     * @param position the position of the component on the canvas
     */
    public Component(String id, String name, Point position) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.inputPorts = new ArrayList<>();
        this.outputPorts = new ArrayList<>();
        initializePorts();
    }
    
    /**
     * Initialize input and output ports for this component.
     * This method is called during construction and should create
     * the appropriate number of input and output ports based on
     * the component's type.
     */
    protected abstract void initializePorts();
    
    /**
     * Execute the component's logic and update outputs based on inputs.
     * This method reads values from input ports, performs the component's
     * logical operation, and updates the output port values accordingly.
     */
    public abstract void execute();
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public Point getPosition() {
        return position;
    }
    
    public void setPosition(Point position) {
        if (position != null) {
            this.position = new Point(position);
            updatePortPositions();
        }
    }
    
    public List<Port> getInputPorts() {
        return inputPorts;
    }
    
    public List<Port> getOutputPorts() {
        return outputPorts;
    }
    
    public Port getInputPort(int index) {
        return index < inputPorts.size() ? inputPorts.get(index) : null;
    }
    
    public Port getOutputPort(int index) {
        return index < outputPorts.size() ? outputPorts.get(index) : null;
    }
    
    /**
     * Update port positions relative to component position
     */
    protected void updatePortPositions() {
        // Default implementation - subclasses can override
        int portSpacing = 20;
        int yOffset = 10;
        
        // Update input port positions (left side)
        for (int i = 0; i < inputPorts.size(); i++) {
            Port port = inputPorts.get(i);
            port.setPosition(new Point(position.x, position.y + yOffset + i * portSpacing));
        }
        
        // Update output port positions (right side)
        int componentWidth = 60; // Default width
        for (int i = 0; i < outputPorts.size(); i++) {
            Port port = outputPorts.get(i);
            port.setPosition(new Point(position.x + componentWidth, position.y + yOffset + i * portSpacing));
        }
    }
    
    /**
     * Get the number of inputs
     */
    public int getInputCount() {
        return inputPorts.size();
    }
    
    /**
     * Get the number of outputs
     */
    public int getOutputCount() {
        return outputPorts.size();
    }
}

