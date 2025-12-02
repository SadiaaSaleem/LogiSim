package edu.uni.logisim.domain.component.io;

import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.connector.Port;
import edu.uni.logisim.domain.connector.PortType;
import java.awt.Point;

/**
 * LED output component - visual output indicator.
 * 
 * <p>This component displays the output value of a circuit.
 * The LED is lit (visualized) when the input signal is high (true/logic 1),
 * and unlit when the input signal is low (false/logic 0).
 * 
 * <p>When used in sub-circuits, LedOutput components become output ports
 * that can be connected to parent circuits.
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class LedOutput extends Component {
    private boolean lit; // Whether the LED is currently lit
    
    /**
     * Creates a new LED output component.
     * 
     * @param id the unique identifier for this component
     * @param position the position of the component on the canvas
     */
    public LedOutput(String id, Point position) {
        super(id, "LED", position);
        this.lit = false;
    }
    
    /**
     * Initializes the LED with 1 input port.
     */
    @Override
    protected void initializePorts() {
        // LED has 1 input
        inputPorts.add(new Port("in", PortType.INPUT, new Point(0, 0)));
        updatePortPositions();
    }
    
    /**
     * Executes the LED logic.
     * Updates the LED state based on the input value.
     */
    @Override
    public void execute() {
        // LED state reflects the input value
        if (inputPorts.size() >= 1) {
            lit = inputPorts.get(0).getValue();
        }
    }
    
    /**
     * Checks if the LED is currently lit.
     * 
     * @return true if the LED is lit (input is high), false otherwise
     */
    public boolean isLit() {
        return lit;
    }
}

