package edu.uni.logisim.domain.component;

import edu.uni.logisim.domain.component.logic.OrGate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for OrGate component
 */
public class OrGateTest {
    
    @Test
    public void testOrGateLogic() {
        OrGate gate = new OrGate("test1", new java.awt.Point(0, 0));
        
        // Test: false OR false = false
        gate.getInputPort(0).setValue(false);
        gate.getInputPort(1).setValue(false);
        gate.execute();
        assertFalse(gate.getOutputPort(0).getValue());
        
        // Test: false OR true = true
        gate.getInputPort(0).setValue(false);
        gate.getInputPort(1).setValue(true);
        gate.execute();
        assertTrue(gate.getOutputPort(0).getValue());
        
        // Test: true OR false = true
        gate.getInputPort(0).setValue(true);
        gate.getInputPort(1).setValue(false);
        gate.execute();
        assertTrue(gate.getOutputPort(0).getValue());
        
        // Test: true OR true = true
        gate.getInputPort(0).setValue(true);
        gate.getInputPort(1).setValue(true);
        gate.execute();
        assertTrue(gate.getOutputPort(0).getValue());
    }
}

