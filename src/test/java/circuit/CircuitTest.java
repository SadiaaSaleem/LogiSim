package circuit;

import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.component.logic.AndGate;
import edu.uni.logisim.domain.component.io.InputSwitch;
import edu.uni.logisim.domain.component.io.LedOutput;
import edu.uni.logisim.domain.connector.Connector;
import edu.uni.logisim.domain.connector.Port;
import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.Point;

/**
 * Test for Circuit class operations
 */
public class CircuitTest {
    
    @Test
    public void testCircuitCreation() {
        Circuit circuit = new Circuit("circuit1", "Test Circuit");
        
        assertNotNull(circuit);
        assertEquals("circuit1", circuit.getId());
        assertEquals("Test Circuit", circuit.getName());
        assertNotNull(circuit.getComponents());
        assertNotNull(circuit.getConnectors());
        assertEquals(0, circuit.getComponents().size());
        assertEquals(0, circuit.getConnectors().size());
    }
    
    @Test
    public void testAddComponent() {
        Circuit circuit = new Circuit("circuit1", "Test Circuit");
        Component component = new AndGate("comp1", new Point(0, 0));
        
        circuit.addComponent(component);
        
        assertEquals(1, circuit.getComponents().size());
        assertTrue(circuit.getComponents().contains(component));
    }
    
    @Test
    public void testRemoveComponent() {
        Circuit circuit = new Circuit("circuit1", "Test Circuit");
        Component component = new AndGate("comp1", new Point(0, 0));
        
        circuit.addComponent(component);
        assertEquals(1, circuit.getComponents().size());
        
        circuit.removeComponent(component);
        assertEquals(0, circuit.getComponents().size());
    }
    
    @Test
    public void testGetComponentById() {
        Circuit circuit = new Circuit("circuit1", "Test Circuit");
        Component component = new AndGate("comp1", new Point(0, 0));
        
        circuit.addComponent(component);
        
        Component found = circuit.getComponentById("comp1");
        assertNotNull(found);
        assertEquals(component, found);
        
        Component notFound = circuit.getComponentById("nonexistent");
        assertNull(notFound);
    }
    
    @Test
    public void testAddConnector() {
        Circuit circuit = new Circuit("circuit1", "Test Circuit");
        InputSwitch input = new InputSwitch("input1", new Point(0, 0));
        LedOutput output = new LedOutput("output1", new Point(100, 0));
        
        circuit.addComponent(input);
        circuit.addComponent(output);
        
        Port sourcePort = input.getOutputPort(0);
        Port sinkPort = output.getInputPort(0);
        
        Connector connector = new Connector("conn1", sourcePort, sinkPort, input, output);
        circuit.addConnector(connector);
        
        assertEquals(1, circuit.getConnectors().size());
        assertTrue(circuit.getConnectors().contains(connector));
    }
    
    @Test
    public void testRemoveConnector() {
        Circuit circuit = new Circuit("circuit1", "Test Circuit");
        InputSwitch input = new InputSwitch("input1", new Point(0, 0));
        LedOutput output = new LedOutput("output1", new Point(100, 0));
        
        circuit.addComponent(input);
        circuit.addComponent(output);
        
        Port sourcePort = input.getOutputPort(0);
        Port sinkPort = output.getInputPort(0);
        
        Connector connector = new Connector("conn1", sourcePort, sinkPort, input, output);
        circuit.addConnector(connector);
        assertEquals(1, circuit.getConnectors().size());
        
        circuit.removeConnector(connector);
        assertEquals(0, circuit.getConnectors().size());
    }
    
    @Test
    public void testRemoveComponentRemovesConnectors() {
        Circuit circuit = new Circuit("circuit1", "Test Circuit");
        InputSwitch input = new InputSwitch("input1", new Point(0, 0));
        LedOutput output = new LedOutput("output1", new Point(100, 0));
        
        circuit.addComponent(input);
        circuit.addComponent(output);
        
        Port sourcePort = input.getOutputPort(0);
        Port sinkPort = output.getInputPort(0);
        
        Connector connector = new Connector("conn1", sourcePort, sinkPort, input, output);
        circuit.addConnector(connector);
        assertEquals(1, circuit.getConnectors().size());
        
        // Removing a component should also remove its connectors
        circuit.removeComponent(input);
        assertEquals(0, circuit.getComponents().size());
        assertEquals(0, circuit.getConnectors().size());
    }
}

