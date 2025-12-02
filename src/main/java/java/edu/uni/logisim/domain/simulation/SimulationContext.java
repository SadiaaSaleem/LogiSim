package edu.uni.logisim.domain.simulation;

import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.connector.Connector;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the state and execution of the circuit simulation
 */
public class SimulationContext {
    private Circuit circuit;
    private boolean running;
    private List<SimulationListener> listeners;
    
    public SimulationContext(Circuit circuit) {
        this.circuit = circuit;
        this.running = false;
        this.listeners = new ArrayList<>();
    }
    
    public Circuit getCircuit() {
        return circuit;
    }
    
    public void setCircuit(Circuit circuit) {
        this.circuit = circuit;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    /**
     * Execute one simulation step
     */
    public void step() {
        if (circuit == null) {
            return;
        }
        
        // Execute all components first to update their outputs based on current inputs
        for (Component component : circuit.getComponents()) {
            component.execute();
        }
        
        // Then propagate signals through connectors (this updates input ports of connected components)
        for (Connector connector : circuit.getConnectors()) {
            connector.propagate();
        }
        
        // Execute components again after propagation to update outputs with new input values
        for (Component component : circuit.getComponents()) {
            component.execute();
        }
        
        // Notify listeners to update UI
        notifyListeners();
    }
    
    /**
     * Start the simulation
     */
    public void start() {
        running = true;
        notifyListeners();
    }
    
    /**
     * Stop the simulation
     */
    public void stop() {
        running = false;
        notifyListeners();
    }
    
    /**
     * Reset the simulation to initial state.
     * Resets all component states, port values, and connector values to their defaults.
     */
    public void reset() {
        if (circuit != null) {
            // Reset all component states
            for (Component component : circuit.getComponents()) {
                // Reset InputSwitch components to low (false)
                if (component instanceof edu.uni.logisim.domain.component.io.InputSwitch) {
                    ((edu.uni.logisim.domain.component.io.InputSwitch) component).setState(false);
                }
                
                // Reset all port values to false
                if (component.getInputPorts() != null) {
                    for (edu.uni.logisim.domain.connector.Port port : component.getInputPorts()) {
                        port.setValue(false);
                    }
                }
                if (component.getOutputPorts() != null) {
                    for (edu.uni.logisim.domain.connector.Port port : component.getOutputPorts()) {
                        port.setValue(false);
                    }
                }
            }
            
            // Reset all connector values
            for (edu.uni.logisim.domain.connector.Connector connector : circuit.getConnectors()) {
                connector.setValue(false);
            }
            
            // Execute all components once to propagate reset state
            for (Component component : circuit.getComponents()) {
                component.execute();
            }
        }
        notifyListeners();
    }
    
    public void addListener(SimulationListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    
    public void removeListener(SimulationListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners() {
        for (SimulationListener listener : listeners) {
            listener.onSimulationUpdate(this);
        }
    }
    
    /**
     * Interface for simulation event listeners
     */
    public interface SimulationListener {
        void onSimulationUpdate(SimulationContext context);
    }
}

