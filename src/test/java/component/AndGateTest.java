package component;

import edu.uni.logisim.domain.component.logic.AndGate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for AndGate component
 */
public class AndGateTest {
    
    @Test
    public void testAndGateLogic() {
        AndGate gate = new AndGate("test1", new java.awt.Point(0, 0));
        
        // Test: false AND false = false
        gate.getInputPort(0).setValue(false);
        gate.getInputPort(1).setValue(false);
        gate.execute();
        assertFalse(gate.getOutputPort(0).getValue());
        
        // Test: false AND true = false
        gate.getInputPort(0).setValue(false);
        gate.getInputPort(1).setValue(true);
        gate.execute();
        assertFalse(gate.getOutputPort(0).getValue());
        
        // Test: true AND false = false
        gate.getInputPort(0).setValue(true);
        gate.getInputPort(1).setValue(false);
        gate.execute();
        assertFalse(gate.getOutputPort(0).getValue());
        
        // Test: true AND true = true
        gate.getInputPort(0).setValue(true);
        gate.getInputPort(1).setValue(true);
        gate.execute();
        assertTrue(gate.getOutputPort(0).getValue());
    }
}

