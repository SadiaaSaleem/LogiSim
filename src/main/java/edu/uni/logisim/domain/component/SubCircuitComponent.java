package edu.uni.logisim.domain.component;

import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.component.io.InputSwitch;
import edu.uni.logisim.domain.component.io.LedOutput;
import edu.uni.logisim.domain.connector.Port;
import edu.uni.logisim.domain.connector.PortType;
import edu.uni.logisim.domain.simulation.SimulationContext;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Component that represents a sub-circuit (a saved circuit used as a component)
 * Uses reference-based storage: stores only the project name, loads circuit lazily when needed
 */
public class SubCircuitComponent extends Component {
    private String projectName; // Reference to the project (not the full circuit!)
    private String circuitFilePath; // Path to the .dig file (for file-based loading)
    
    // Lazy-loaded fields (loaded when needed)
    private transient Circuit subCircuit; // Loaded on demand, not serialized
    private transient List<InputSwitch> inputSwitches; // Cached after analysis
    private transient List<LedOutput> outputLeds; // Cached after analysis
    private transient SimulationContext simulationContext; // For executing the sub-circuit
    
    // Callback to load circuit (set by application)
    private transient CircuitLoader circuitLoader;
    
    /**
     * Interface for loading circuits by project name
     */
    public interface CircuitLoader {
        Circuit loadCircuit(String projectName);
    }
    
    /**
     * No-arg constructor for XStream deserialization
     * Initializes lists to prevent null pointer exceptions
     */
    public SubCircuitComponent() {
        super("", "SubCircuit", new Point(0, 0));
        this.inputSwitches = new ArrayList<>();
        this.outputLeds = new ArrayList<>();
    }
    
    /**
     * Constructor with project name (reference-based)
     * Circuit will be loaded lazily when needed
     */
    public SubCircuitComponent(String id, String name, String projectName, String circuitFilePath, Point position) {
        super(id, name != null ? name : "SubCircuit", position);
        this.projectName = projectName;
        this.circuitFilePath = circuitFilePath;
        this.inputSwitches = new ArrayList<>();
        this.outputLeds = new ArrayList<>();
        // Don't load circuit yet - lazy loading
    }
    
    /**
     * Constructor with Circuit object (for in-memory usage)
     * This is used when creating from an already-loaded circuit
     */
    public SubCircuitComponent(String id, String name, Circuit subCircuit, Point position) {
        super(id, name != null ? name : "SubCircuit", position);
        this.subCircuit = subCircuit;
        // Extract project name from circuit if possible
        if (subCircuit != null && subCircuit.getName() != null) {
            this.projectName = subCircuit.getName();
        }
        this.inputSwitches = new ArrayList<>();
        this.outputLeds = new ArrayList<>();
        analyzeSubCircuit();
    }
    
    /**
     * Set the circuit loader callback (used for lazy loading)
     */
    public void setCircuitLoader(CircuitLoader loader) {
        this.circuitLoader = loader;
    }
    
    /**
     * Load the circuit lazily (when needed)
     * This implements the reference-based architecture
     */
    private void ensureCircuitLoaded() {
        if (subCircuit != null) {
            return; // Already loaded
        }
        
        // CRITICAL: Always ensure lists are initialized, even if loading fails
        if (inputSwitches == null) {
            inputSwitches = new ArrayList<>();
        }
        if (outputLeds == null) {
            outputLeds = new ArrayList<>();
        }
        
        if (circuitLoader != null && projectName != null) {
            try {
                subCircuit = circuitLoader.loadCircuit(projectName);
                if (subCircuit != null) {
                    analyzeSubCircuit();
                    // Initialize ports if not already done
                    if (inputPorts == null) {
                        inputPorts = new ArrayList<>();
                    }
                    if (outputPorts == null) {
                        outputPorts = new ArrayList<>();
                    }
                    initializePorts();
                } else {
                    System.err.println("WARNING: Circuit loader returned null for project: " + projectName);
                }
            } catch (Exception e) {
                System.err.println("WARNING: Failed to load circuit for project '" + projectName + "': " + e.getMessage());
                e.printStackTrace();
                // Lists are already initialized above, so we can continue
            }
        } else if (circuitFilePath != null) {
            // Fallback: try to load from file path
            // This would require access to ProjectService, so we rely on circuitLoader
            System.err.println("WARNING: Cannot load circuit - no circuitLoader set and projectName: " + projectName);
        }
    }
    
    /**
     * Initialize the sub-circuit after deserialization
     * This ensures the circuit is loaded and analyzed
     */
    public void initializeAfterDeserialization(CircuitLoader loader) {
        this.circuitLoader = loader;
        if (inputSwitches == null) {
            inputSwitches = new ArrayList<>();
        }
        if (outputLeds == null) {
            outputLeds = new ArrayList<>();
        }
        // Load circuit if not already loaded
        ensureCircuitLoaded();
    }
    
    /**
     * Analyze the sub-circuit to find inputs (InputSwitch) and outputs (LedOutput)
     */
    private void analyzeSubCircuit() {
        // Ensure lists are initialized (defensive programming)
        if (inputSwitches == null) {
            inputSwitches = new ArrayList<>();
        }
        if (outputLeds == null) {
            outputLeds = new ArrayList<>();
        }
        
        if (subCircuit == null) {
            System.out.println("WARNING: SubCircuitComponent has null subCircuit");
            return;
        }
        
        List<Component> components = subCircuit.getComponents();
        if (components == null) {
            System.out.println("WARNING: SubCircuitComponent subCircuit has null components list");
            return;
        }
        
        // Find all input switches (these become input ports)
        for (Component comp : components) {
            if (comp instanceof InputSwitch) {
                inputSwitches.add((InputSwitch) comp);
            } else if (comp instanceof LedOutput) {
                outputLeds.add((LedOutput) comp);
            }
        }
        
        System.out.println("DEBUG: SubCircuitComponent analyzed - found " + inputSwitches.size() + 
                         " inputs and " + outputLeds.size() + " outputs");
    }
    
    @Override
    protected void initializePorts() {
        // Ensure circuit is loaded before analyzing
        ensureCircuitLoaded();
        
        // Clear existing ports
        if (inputPorts == null) {
            inputPorts = new ArrayList<>();
        } else {
            inputPorts.clear();
        }
        if (outputPorts == null) {
            outputPorts = new ArrayList<>();
        } else {
            outputPorts.clear();
        }
        
        // Create input ports based on input switches found in sub-circuit
        for (int i = 0; i < inputSwitches.size(); i++) {
            Port inputPort = new Port("in" + i, PortType.INPUT, new Point(0, 0));
            inputPorts.add(inputPort);
        }
        
        // Create output ports based on output LEDs found in sub-circuit
        for (int i = 0; i < outputLeds.size(); i++) {
            Port outputPort = new Port("out" + i, PortType.OUTPUT, new Point(0, 0));
            outputPorts.add(outputPort);
        }
        
        updatePortPositions();
    }
    
    @Override
    public void execute() {
        // Ensure circuit is loaded before executing
        ensureCircuitLoaded();
        if (subCircuit == null) return;
        
        // Step 1: Map input port values to input switches in sub-circuit
        for (int i = 0; i < inputPorts.size() && i < inputSwitches.size(); i++) {
            Port inputPort = inputPorts.get(i);
            InputSwitch inputSwitch = inputSwitches.get(i);
            // Set the switch state based on input port value
            inputSwitch.setState(inputPort.getValue());
        }
        
        // Step 2: Execute the sub-circuit
        // Initialize simulation context if needed
        if (simulationContext == null) {
            simulationContext = new SimulationContext(subCircuit);
        }
        
        // Run simulation step to propagate values through sub-circuit
        simulationContext.step();
        
        // Step 3: Map output LED values to output ports
        for (int i = 0; i < outputPorts.size() && i < outputLeds.size(); i++) {
            Port outputPort = outputPorts.get(i);
            LedOutput led = outputLeds.get(i);
            // Execute the LED to update its state, then get the lit state
            led.execute();
            boolean ledValue = led.isLit();
            outputPort.setValue(ledValue);
        }
    }
    
    public Circuit getSubCircuit() {
        ensureCircuitLoaded();
        return subCircuit;
    }
    
    public String getProjectName() {
        return projectName;
    }
    
    public String getCircuitFilePath() {
        return circuitFilePath;
    }
    
    public int getInputCount() {
        // Ensure lists are initialized (defensive)
        if (inputSwitches == null) {
            inputSwitches = new ArrayList<>();
        }
        // Ensure circuit is loaded and analyzed
        ensureCircuitLoaded();
        // If circuit was loaded, analyzeSubCircuit() was called in ensureCircuitLoaded()
        // If not, we still have an empty list, which is safe
        return inputSwitches.size();
    }
    
    public int getOutputCount() {
        // Ensure lists are initialized (defensive)
        if (outputLeds == null) {
            outputLeds = new ArrayList<>();
        }
        // Ensure circuit is loaded and analyzed
        ensureCircuitLoaded();
        // If circuit was loaded, analyzeSubCircuit() was called in ensureCircuitLoaded()
        // If not, we still have an empty list, which is safe
        return outputLeds.size();
    }
    
    /**
     * Update the sub-circuit (useful if the circuit file was modified)
     */
    public void updateSubCircuit(Circuit newCircuit) {
        this.subCircuit = newCircuit;
        if (inputSwitches == null) {
            inputSwitches = new ArrayList<>();
        } else {
            inputSwitches.clear();
        }
        if (outputLeds == null) {
            outputLeds = new ArrayList<>();
        } else {
            outputLeds.clear();
        }
        analyzeSubCircuit();
        
        // Reinitialize ports
        if (inputPorts == null) {
            inputPorts = new ArrayList<>();
        } else {
            inputPorts.clear();
        }
        if (outputPorts == null) {
            outputPorts = new ArrayList<>();
        } else {
            outputPorts.clear();
        }
        initializePorts();
    }
}

