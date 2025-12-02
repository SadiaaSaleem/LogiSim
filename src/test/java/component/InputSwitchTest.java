package component;

import edu.uni.logisim.domain.component.io.InputSwitch;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for InputSwitch component
 */
public class InputSwitchTest {
    
    @Test
    public void testInputSwitchInitialState() {
        InputSwitch input = new InputSwitch("test1", new java.awt.Point(0, 0));
        
        // Initially should be false (low)
        assertFalse(input.getState());
        input.execute();
        assertFalse(input.getOutputPort(0).getValue());
    }
    
    @Test
    public void testInputSwitchToggle() {
        InputSwitch input = new InputSwitch("test1", new java.awt.Point(0, 0));
        
        // Toggle from false to true
        input.toggle();
        assertTrue(input.getState());
        input.execute();
        assertTrue(input.getOutputPort(0).getValue());
        
        // Toggle from true to false
        input.toggle();
        assertFalse(input.getState());
        input.execute();
        assertFalse(input.getOutputPort(0).getValue());
    }
    
    @Test
    public void testInputSwitchSetState() {
        InputSwitch input = new InputSwitch("test1", new java.awt.Point(0, 0));
        
        // Set to true
        input.setState(true);
        assertTrue(input.getState());
        assertTrue(input.getOutputPort(0).getValue());
        
        // Set to false
        input.setState(false);
        assertFalse(input.getState());
        assertFalse(input.getOutputPort(0).getValue());
    }
}

