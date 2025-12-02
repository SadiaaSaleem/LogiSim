package edu.uni.logisim.domain.component.logic;

import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.connector.Port;
import edu.uni.logisim.domain.connector.PortType;

import java.awt.*;

/**
 * AND gate component - performs logical AND operation.
 * 
 * <p>Truth table:
 * <pre>
 * Input1 | Input2 | Output
 * -------|--------|-------
 *   0    |   0    |   0
 *   0    |   1    |   0
 *   1    |   0    |   0
 *   1    |   1    |   1
 * </pre>
 * 
 * <p>The output is true (logic 1) only when all inputs are true.
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class AndGate extends Component {
    
    /**
     * Creates a new AND gate component.
     * 
     * @param id the unique identifier for this component
     * @param position the position of the component on the canvas
     */
    public AndGate(String id, Point position) {
        super(id, "AND", position);
    }
    
    /**
     * Initializes the AND gate with 2 input ports and 1 output port.
     */
    @Override
    protected void initializePorts() {
        // AND gate typically has 2 inputs and 1 output
        inputPorts.add(new Port("in1", PortType.INPUT, new Point(0, 0)));
        inputPorts.add(new Port("in2", PortType.INPUT, new Point(0, 0)));
        outputPorts.add(new Port("out", PortType.OUTPUT, new Point(0, 0)));
        updatePortPositions();
    }
    
    /**
     * Executes the AND gate logic.
     * Outputs true only when both inputs are true.
     */
    @Override
    public void execute() {
        if (inputPorts.size() >= 2 && outputPorts.size() >= 1) {
            boolean result = inputPorts.get(0).getValue() && inputPorts.get(1).getValue();
            outputPorts.get(0).setValue(result);
        }
    }
}

