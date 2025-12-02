package edu.uni.logisim.domain.component;

import edu.uni.logisim.domain.component.logic.NotGate;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for NotGate component
 */
public class NotGateTest {
    
    @Test
    public void testNotGateLogic() {
        NotGate gate = new NotGate("test1", new java.awt.Point(0, 0));
        
        // Test: NOT false = true
        gate.getInputPort(0).setValue(false);
        gate.execute();
        assertTrue(gate.getOutputPort(0).getValue());
        
        // Test: NOT true = false
        gate.getInputPort(0).setValue(true);
        gate.execute();
        assertFalse(gate.getOutputPort(0).getValue());
    }
}

