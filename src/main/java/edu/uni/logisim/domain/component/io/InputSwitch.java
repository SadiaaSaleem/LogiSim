package java.edu.uni.logisim.domain.component.io;

import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.connector.Port;
import edu.uni.logisim.domain.connector.PortType;

import java.awt.*;

/**
 * Input switch component - user-controlled input source.
 * 
 * <p>This component provides a way for users to manually set input values
 * for circuit simulation. The switch can be toggled between high (true/logic 1)
 * and low (false/logic 0) states.
 * 
 * <p>When used in sub-circuits, InputSwitch components become input ports
 * that can be connected from parent circuits.
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class InputSwitch extends Component {
    private boolean state; // Current switch state
    
    /**
     * Creates a new input switch component.
     * 
     * @param id the unique identifier for this component
     * @param position the position of the component on the canvas
     */
    public InputSwitch(String id, Point position) {
        super(id, "Input", position);
        this.state = false;
    }
    
    /**
     * Initializes the input switch with 1 output port.
     */
    @Override
    protected void initializePorts() {
        // Input switch has 1 output
        outputPorts.add(new Port("out", PortType.OUTPUT, new Point(0, 0)));
        updatePortPositions();
    }
    
    /**
     * Executes the input switch logic.
     * Outputs the current switch state value.
     */
    @Override
    public void execute() {
        // Output reflects the switch state
        if (outputPorts.size() >= 1) {
            outputPorts.get(0).setValue(state);
        }
    }
    
    /**
     * Toggles the switch state between true and false.
     */
    public void toggle() {
        this.state = !this.state;
        execute();
    }
    
    /**
     * Sets the switch state to the specified value.
     * 
     * @param state true for high signal (logic 1), false for low signal (logic 0)
     */
    public void setState(boolean state) {
        this.state = state;
        execute();
    }
    
    /**
     * Gets the current switch state.
     * 
     * @return true for high signal (logic 1), false for low signal (logic 0)
     */
    public boolean getState() {
        return state;
    }
}

