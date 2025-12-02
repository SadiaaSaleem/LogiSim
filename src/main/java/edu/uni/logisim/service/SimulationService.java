package edu.uni.logisim.service;

import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.simulation.SimulationContext;
import javax.swing.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Provides services for running and controlling circuit simulations.
 * 
 * <p>This service manages the simulation lifecycle:
 * <ul>
 *   <li>Initializing simulation contexts for circuits</li>
 *   <li>Starting and stopping continuous simulations</li>
 *   <li>Executing single simulation steps</li>
 *   <li>Managing UI update callbacks during simulation</li>
 * </ul>
 * 
 * <p>The service uses a timer-based approach for continuous simulation,
 * executing simulation steps at regular intervals and updating the UI
 * after each step.
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class SimulationService {
    private SimulationContext simulationContext;
    private Timer simulationTimer;
    private Runnable updateCallback;
    
    /**
     * Creates a new SimulationService.
     */
    public SimulationService() {
    }
    
    /**
     * Sets a callback to be invoked when the simulation state changes.
     * This is typically used to update the UI after each simulation step.
     * 
     * @param callback the callback to invoke on simulation updates
     */
    public void setUpdateCallback(Runnable callback) {
        this.updateCallback = callback;
    }
    
    /**
     * Initializes a simulation context for the given circuit.
     * If a simulation is already running, it will be stopped first.
     * 
     * @param circuit the circuit to simulate
     */
    public void initializeSimulation(Circuit circuit) {
        if (simulationContext != null && simulationContext.isRunning()) {
            stopSimulation();
        }
        simulationContext = new SimulationContext(circuit);
        
        // Add listener to update UI
        simulationContext.addListener(context -> {
            if (updateCallback != null) {
                SwingUtilities.invokeLater(updateCallback);
            }
        });
    }
    
    /**
     * Gets the current simulation context.
     * 
     * @return the simulation context, or null if no simulation is initialized
     */
    public SimulationContext getSimulationContext() {
        return simulationContext;
    }
    
    /**
     * Starts continuous simulation of the current circuit.
     * The simulation will run in a loop, executing steps at regular intervals.
     */
    public void startSimulation() {
        if (simulationContext != null) {
            simulationContext.start();
            
            // Start continuous simulation loop
            if (simulationTimer != null) {
                simulationTimer.cancel();
            }
            simulationTimer = new Timer(true);
            simulationTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (simulationContext != null && simulationContext.isRunning()) {
                        simulationContext.step();
                    }
                }
            }, 0, 100); // Update every 100ms
        }
    }
    
    /**
     * Stops the current simulation if it is running.
     * Cancels the simulation timer and stops the simulation context.
     */
    public void stopSimulation() {
        if (simulationTimer != null) {
            simulationTimer.cancel();
            simulationTimer = null;
        }
        if (simulationContext != null) {
            simulationContext.stop();
        }
    }
    
    /**
     * Executes a single simulation step.
     * This is useful for step-by-step debugging or manual simulation control.
     */
    public void stepSimulation() {
        if (simulationContext != null) {
            simulationContext.step();
        }
    }
    
    /**
     * Resets the simulation to its initial state.
     * All component states are reset to their default values.
     */
    public void resetSimulation() {
        if (simulationContext != null) {
            simulationContext.reset();
        }
    }
    
    /**
     * Checks if a simulation is currently running.
     * 
     * @return true if a simulation is running, false otherwise
     */
    public boolean isRunning() {
        return simulationContext != null && simulationContext.isRunning();
    }
}

