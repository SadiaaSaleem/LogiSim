package edu.uni.logisim.domain.component;

import edu.uni.logisim.domain.component.io.LedOutput;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test for LedOutput component
 */
public class LedOutputTest {
    
    @Test
    public void testLedOutputInitialState() {
        LedOutput led = new LedOutput("test1", new java.awt.Point(0, 0));
        
        // Initially should be unlit (false)
        assertFalse(led.isLit());
    }
    
    @Test
    public void testLedOutputWithHighInput() {
        LedOutput led = new LedOutput("test1", new java.awt.Point(0, 0));
        
        // Set input to high (true)
        led.getInputPort(0).setValue(true);
        led.execute();
        assertTrue(led.isLit());
    }
    
    @Test
    public void testLedOutputWithLowInput() {
        LedOutput led = new LedOutput("test1", new java.awt.Point(0, 0));
        
        // Set input to low (false)
        led.getInputPort(0).setValue(false);
        led.execute();
        assertFalse(led.isLit());
    }
    
    @Test
    public void testLedOutputToggle() {
        LedOutput led = new LedOutput("test1", new java.awt.Point(0, 0));
        
        // Start with low input
        led.getInputPort(0).setValue(false);
        led.execute();
        assertFalse(led.isLit());
        
        // Change to high input
        led.getInputPort(0).setValue(true);
        led.execute();
        assertTrue(led.isLit());
        
        // Change back to low input
        led.getInputPort(0).setValue(false);
        led.execute();
        assertFalse(led.isLit());
    }
}

