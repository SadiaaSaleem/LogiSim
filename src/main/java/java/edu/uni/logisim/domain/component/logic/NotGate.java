package edu.uni.logisim.domain.component.logic;

import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.connector.Port;
import edu.uni.logisim.domain.connector.PortType;
import java.awt.Point;

/**
 * NOT gate component (inverter) - performs logical NOT operation.
 * 
 * <p>Truth table:
 * <pre>
 * Input | Output
 * ------|-------
 *   0   |   1
 *   1   |   0
 * </pre>
 * 
 * <p>The output is the inverse (negation) of the input.
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class NotGate extends Component {
    
    /**
     * Creates a new NOT gate component.
     * 
     * @param id the unique identifier for this component
     * @param position the position of the component on the canvas
     */
    public NotGate(String id, Point position) {
        super(id, "NOT", position);
    }
    
    /**
     * Initializes the NOT gate with 1 input port and 1 output port.
     */
    @Override
    protected void initializePorts() {
        // NOT gate has 1 input and 1 output
        inputPorts.add(new Port("in", PortType.INPUT, new Point(0, 0)));
        outputPorts.add(new Port("out", PortType.OUTPUT, new Point(0, 0)));
        updatePortPositions();
    }
    
    /**
     * Executes the NOT gate logic.
     * Outputs the inverse of the input value.
     */
    @Override
    public void execute() {
        if (inputPorts.size() >= 1 && outputPorts.size() >= 1) {
            boolean result = !inputPorts.get(0).getValue();
            outputPorts.get(0).setValue(result);
        }
    }
}

