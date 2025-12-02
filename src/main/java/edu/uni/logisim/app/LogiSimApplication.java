package java.edu.uni.logisim.app;

import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.component.SubCircuitComponent;
import edu.uni.logisim.domain.component.factory.ComponentFactory;
import edu.uni.logisim.domain.project.Project;
import edu.uni.logisim.export.DiagramExporter;
import edu.uni.logisim.persistence.FileProjectRepository;
import edu.uni.logisim.persistence.ProjectRepository;
import edu.uni.logisim.service.CircuitService;
import edu.uni.logisim.service.ProjectService;
import edu.uni.logisim.service.SimulationService;
import edu.uni.logisim.service.TruthTableService;
import edu.uni.logisim.ui.MainFrame;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point for the GUI application
 */
public class LogiSimApplication {
    private static final Logger logger = Logger.getLogger(LogiSimApplication.class.getName());
    private MainFrame mainFrame;
    private ProjectService projectService;
    private CircuitService circuitService;
    private SimulationService simulationService;
    private TruthTableService truthTableService;
    private ProjectRepository repository;
    private Project currentProject;
    private Circuit currentCircuit;
    private String selectedComponentType;
    private DiagramExporter diagramExporter;
    
    // Cache for saved circuits (to avoid file loading/deserialization issues)
    // Key: project name (e.g., "HalfAdder"), Value: the Circuit object
    private java.util.Map<String, Circuit> savedCircuitsCache;
    
    // Track projects that have been opened/created in THIS SESSION ONLY
    // Only these projects should appear in Custom components (not old saved projects)
    private java.util.Set<String> sessionProjects;
    
    public LogiSimApplication() {
        // Initialize services
        repository = new FileProjectRepository("projects");
        projectService = new ProjectService(repository);
        circuitService = new CircuitService();
        simulationService = new SimulationService();
        truthTableService = new TruthTableService();
        diagramExporter = new DiagramExporter();
        
        // Initialize cache for saved circuits (in-memory storage)
        savedCircuitsCache = new java.util.HashMap<>();
        
        // Initialize session projects tracker (only projects opened/created in this session)
        sessionProjects = new java.util.HashSet<>();
        
        // Initialize UI
        SwingUtilities.invokeLater(() -> {
            mainFrame = new MainFrame(this);
            mainFrame.setVisible(true);
            // Auto-create a default project on startup if none exists
            if (currentProject == null) {
                currentProject = projectService.createProject("Untitled", "projects/Untitled");
                currentCircuit = circuitService.createCircuit("Main Circuit");
                currentProject.addCircuit(currentCircuit);
                // Add to session projects (created in this session)
                sessionProjects.add(currentProject.getName());
                mainFrame.getProjectTreePanel().updateProject(currentProject);
                mainFrame.getCircuitCanvasPanel().setCircuit(currentCircuit);
                mainFrame.getComponentPalettePanel().refreshCustomCircuits();
            }
        });
    }
    
    public void createNewProject() {
        String name = JOptionPane.showInputDialog(mainFrame, "Enter project name:", "New Project", JOptionPane.QUESTION_MESSAGE);
        if (name == null) {
            // User cancelled - don't create project
            return;
        }
        if (name.isEmpty()) {
            name = "Untitled";
        }
        currentProject = projectService.createProject(name, "projects/" + name);
        currentCircuit = circuitService.createCircuit("Main Circuit");
        currentProject.addCircuit(currentCircuit);
        // Add to session projects (created in this session - should appear in Custom components)
        sessionProjects.add(currentProject.getName());
        mainFrame.getProjectTreePanel().updateProject(currentProject);
        mainFrame.getCircuitCanvasPanel().setCircuit(currentCircuit);
        // Refresh custom circuits list after creating new project
        mainFrame.getComponentPalettePanel().refreshCustomCircuits();
        JOptionPane.showMessageDialog(mainFrame, 
            "Project '" + name + "' created successfully!\n\n" +
            "You can now:\n" +
            "1. Add components from the palette\n" +
            "2. Connect them with wires\n" +
            "3. Save the project (File → Save Project)\n" +
            "4. Use saved projects as sub-circuits in other projects",
            "Project Created", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public void openProject() {
        JFileChooser fileChooser = new JFileChooser("projects");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Circuit Files (*.dig)", "dig"));
        
        if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                currentProject = projectService.loadProject(file.getAbsolutePath());
                if (currentProject != null && !currentProject.getCircuits().isEmpty()) {
                    // Ensure project name is set correctly (extract from filename if not set)
                    String projectName = currentProject.getName();
                    if (projectName == null || projectName.isEmpty()) {
                        // Extract name from filename
                        String fileName = file.getName();
                        projectName = fileName.replace(".dig", "");
                        currentProject.setName(projectName);
                    }
                    
                    // Make final for use in lambda
                    final String finalProjectName = projectName;
                    
                    // Ensure path is set correctly
                    if (currentProject.getPath() == null || currentProject.getPath().isEmpty()) {
                        currentProject.setPath(file.getAbsolutePath());
                    }
                    
                    currentCircuit = currentProject.getCircuits().get(0);
                    
                    // CRITICAL: Ensure circuit has valid components and connectors lists
                    if (currentCircuit.getComponents() == null) {
                        logger.warning("Circuit has null components list, initializing...");
                        // This shouldn't happen, but be defensive
                    }
                    if (currentCircuit.getConnectors() == null) {
                        logger.warning("Circuit has null connectors list, initializing...");
                        // This shouldn't happen, but be defensive
                    }
                    
                    // Add to session projects (opened in this session - should appear in Custom components)
                    sessionProjects.add(finalProjectName);
                    
                    // CRITICAL: Cache the circuit when opening (so it can be reused as sub-circuit)
                    Circuit cachedCircuit = cloneCircuit(currentCircuit);
                    savedCircuitsCache.put(finalProjectName, cachedCircuit);
                    logger.fine("Loaded project '" + finalProjectName + "' and added to session");
                    
                    // CRITICAL: Initialize all SubCircuitComponents in the loaded project
                    // This sets up the circuit loader so circuits can be loaded lazily
                    initializeSubCircuitComponents(currentProject);
                    
                    // Store references for lambda
                    final Project finalProject = currentProject;
                    final Circuit finalCircuit = currentCircuit;
                    
                    // Update UI components on EDT (Event Dispatch Thread)
                    SwingUtilities.invokeLater(() -> {
                        mainFrame.getProjectTreePanel().updateProject(finalProject);
                        
                        // CRITICAL: Set the circuit on the canvas
                        mainFrame.getCircuitCanvasPanel().setCircuit(finalCircuit);
                        
                        // Force immediate repaint to ensure circuit is visible
                        mainFrame.getCircuitCanvasPanel().repaint();
                        mainFrame.getCircuitCanvasPanel().revalidate();
                        
                        // Refresh custom circuits list
                        mainFrame.getComponentPalettePanel().refreshCustomCircuits();
                        
                        // Debug: Log what was loaded
                        int componentCount = (finalCircuit.getComponents() != null) ? finalCircuit.getComponents().size() : 0;
                        int connectorCount = (finalCircuit.getConnectors() != null) ? finalCircuit.getConnectors().size() : 0;
                        logger.fine("Opened project '" + finalProjectName + 
                                  "' with circuit '" + finalCircuit.getName() + 
                                  "' containing " + componentCount + 
                                  " components and " + connectorCount + " connectors");
                    });
                } else {
                    JOptionPane.showMessageDialog(mainFrame, 
                        "Error: Project file is empty or invalid!\n\n" +
                        "The file may be corrupted or in an incompatible format.",
                        "Load Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "Error loading project: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void saveProject() {
        if (currentProject != null) {
            // If project name is "Untitled" or empty, prompt for a name (Save As)
            String projectName = currentProject.getName();
            if (projectName == null || projectName.isEmpty() || projectName.equals("Untitled")) {
                saveProjectAs();
                return;
            }
            
            try {
                // Ensure project name and path are set correctly
                if (currentProject.getPath() == null || currentProject.getPath().isEmpty()) {
                    currentProject.setPath("projects/" + projectName);
                }
                
                projectService.saveProject(currentProject);
                
                // CRITICAL: Store the circuit in memory cache when saved
                // This allows us to use it as a sub-circuit without file loading
                if (currentCircuit != null) {
                    // Add to session projects (saved in this session - should appear in Custom components)
                    sessionProjects.add(projectName);
                    // Create a deep copy of the circuit for the cache
                    // (so modifications to current circuit don't affect cached version)
                    Circuit cachedCircuit = cloneCircuit(currentCircuit);
                    savedCircuitsCache.put(projectName, cachedCircuit);
                    logger.fine("Saved project '" + projectName + "' and cached circuit");
                }
                
                // CRITICAL: Initialize all SubCircuitComponents in the saved project
                // This ensures any sub-circuits in the project can load their referenced circuits
                initializeSubCircuitComponents(currentProject);
                
                // Refresh custom circuits list
                mainFrame.getComponentPalettePanel().refreshCustomCircuits();
                
                JOptionPane.showMessageDialog(mainFrame, "Project '" + projectName + "' saved successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "Error saving project: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(mainFrame, "No project to save!", 
                "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void saveProjectAs() {
        if (currentProject != null) {
            String name = JOptionPane.showInputDialog(mainFrame, 
                "Enter project name:", "Save Project As", JOptionPane.QUESTION_MESSAGE);
            
            if (name == null) {
                // User cancelled
                return;
            }
            
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Project name cannot be empty!", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Remove any invalid characters from filename
            name = name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
            
            // Update project name and path
            currentProject.setName(name);
            currentProject.setPath("projects/" + name);
            
            // Save with new name
            try {
                projectService.saveProject(currentProject);
                
                // CRITICAL: Store the circuit in memory cache when saved
                if (currentCircuit != null) {
                    // Add to session projects (saved in this session - should appear in Custom components)
                    sessionProjects.add(name);
                    // Create a deep copy of the circuit for the cache
                    Circuit cachedCircuit = cloneCircuit(currentCircuit);
                    savedCircuitsCache.put(name, cachedCircuit);
                    logger.fine("Saved project as '" + name + "' and cached circuit");
                }
                
                // CRITICAL: Initialize all SubCircuitComponents in the saved project
                initializeSubCircuitComponents(currentProject);
                
                // Update UI
                mainFrame.getProjectTreePanel().updateProject(currentProject);
                mainFrame.getComponentPalettePanel().refreshCustomCircuits();
                
                JOptionPane.showMessageDialog(mainFrame, "Project saved as '" + name + "'!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "Error saving project: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(mainFrame, "No project to save!", 
                "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    /**
     * Create a deep copy of a circuit for caching
     * This is a simplified version - in a real app you'd want proper deep cloning
     */
    private Circuit cloneCircuit(Circuit original) {
        // For now, just return the original (we'll improve this if needed)
        // In practice, you'd want to deep clone all components and connectors
        return original;
    }
    
    /**
     * Get a saved circuit from cache (by project name)
     * This avoids file loading/deserialization issues
     */
    public Circuit getSavedCircuit(String projectName) {
        // First check cache
        Circuit cached = savedCircuitsCache.get(projectName);
        if (cached != null) {
            return cached;
        }
        
        // If not in cache, try loading from file
        try {
            String filePath = "projects/" + projectName + ".dig";
            Project project = projectService.loadProject(filePath);
            if (project != null && !project.getCircuits().isEmpty()) {
                Circuit circuit = project.getCircuits().get(0);
                // Cache it for future use
                savedCircuitsCache.put(projectName, circuit);
                return circuit;
            }
        } catch (Exception e) {
            logger.warning("Failed to load circuit for project '" + projectName + "': " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Initialize all SubCircuitComponents in a project
     * This sets up the circuit loader so circuits can be loaded lazily
     */
    private void initializeSubCircuitComponents(Project project) {
        if (project == null || project.getCircuits() == null) {
            return;
        }
        
        // Create circuit loader implementation
        SubCircuitComponent.CircuitLoader loader = new SubCircuitComponent.CircuitLoader() {
            @Override
            public Circuit loadCircuit(String projectName) {
                return getSavedCircuit(projectName);
            }
        };
        
        // Initialize all SubCircuitComponents
        for (Circuit circuit : project.getCircuits()) {
            if (circuit == null || circuit.getComponents() == null) {
                continue;
            }
            
            for (Component component : circuit.getComponents()) {
                if (component instanceof SubCircuitComponent) {
                    SubCircuitComponent subComp = (SubCircuitComponent) component;
                    subComp.setCircuitLoader(loader);
                    // Initialize (this will load the circuit lazily)
                    try {
                        subComp.initializeAfterDeserialization(loader);
                    } catch (Exception e) {
                        logger.warning("Failed to initialize SubCircuitComponent '" + 
                                     subComp.getName() + "': " + e.getMessage());
                    }
                }
            }
        }
    }
    
    public void exportDiagram(String format) {
        if (currentCircuit == null) {
            JOptionPane.showMessageDialog(mainFrame, "No circuit to export!", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        String extension = format.equals("PNG") ? "png" : "jpg";
        fileChooser.setFileFilter(new FileNameExtensionFilter(format + " Files (*." + extension + ")", extension));
        
        if (fileChooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            if (!filePath.toLowerCase().endsWith("." + extension)) {
                filePath += "." + extension;
            }
            
            try {
                if (format.equals("PNG")) {
                    diagramExporter.exportToPNG(currentCircuit, filePath);
                } else {
                    diagramExporter.exportToJPEG(currentCircuit, filePath);
                }
                JOptionPane.showMessageDialog(mainFrame, "Diagram exported successfully!", 
                    "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(mainFrame, "Error exporting diagram: " + e.getMessage(), 
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public void showTruthTable() {
        if (currentCircuit == null) {
            JOptionPane.showMessageDialog(mainFrame, "No circuit to analyze!", 
                "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            TruthTableService.TruthTable table = truthTableService.generateTruthTable(currentCircuit);
            
            if (table.getRows().isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, 
                    "Cannot generate truth table!\n\n" +
                    "The circuit must have at least one Input component and one LED component.",
                    "Analysis Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Display truth table with boolean expressions in a dialog
            StringBuilder sb = new StringBuilder();
            sb.append("Truth Table\n");
            for (int i = 0; i < 50; i++) sb.append("=");
            sb.append("\n\n");
            
            // Header
            for (String input : table.getInputColumns()) {
                sb.append(input).append("\t");
            }
            sb.append("|\t");
            for (String output : table.getOutputColumns()) {
                sb.append(output).append("\t");
            }
            sb.append("\n");
            for (int i = 0; i < 50; i++) sb.append("-");
            sb.append("\n");
            
            // Rows
            for (TruthTableService.TruthTable.Row row : table.getRows()) {
                for (Boolean input : row.getInputs()) {
                    sb.append(input ? "1" : "0").append("\t");
                }
                sb.append("|\t");
                for (Boolean output : row.getOutputs()) {
                    sb.append(output ? "1" : "0").append("\t");
                }
                sb.append("\n");
            }
            
            // Add boolean expressions for each output (Requirement 3.2 - Optional)
            sb.append("\n");
            for (int i = 0; i < 50; i++) sb.append("=");
            sb.append("\n");
            sb.append("Boolean Expressions (Sum of Products):\n");
            for (int i = 0; i < 50; i++) sb.append("-");
            sb.append("\n");
            
            for (int i = 0; i < table.getOutputColumns().size(); i++) {
                String outputName = table.getOutputColumns().get(i);
                String expression = truthTableService.deriveBooleanExpression(table, i);
                sb.append(outputName).append(" = ").append(expression).append("\n");
            }
            
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
            textArea.setEditable(false);
           
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new java.awt.Dimension(500, 400));
            
            JOptionPane.showMessageDialog(mainFrame, scrollPane, "Truth Table & Boolean Expressions", 
                JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(mainFrame, "Error generating truth table: " + e.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void selectCircuit(Circuit circuit) {
        this.currentCircuit = circuit;
        mainFrame.getCircuitCanvasPanel().setCircuit(circuit);
        if (circuit != null) {
            simulationService.initializeSimulation(circuit);
        }
    }
    
    public void setSelectedComponentType(String type) {
        this.selectedComponentType = type;
    }
    
    public String getSelectedComponentType() {
        return selectedComponentType;
    }
    
    public void addComponentToCircuit(Point position) {
        if (currentCircuit == null) {
            // Auto-create circuit if none exists
            if (currentProject == null) {
                currentProject = projectService.createProject("Untitled", "projects/Untitled");
            }
            currentCircuit = circuitService.createCircuit("Main Circuit");
            currentProject.addCircuit(currentCircuit);
            mainFrame.getProjectTreePanel().updateProject(currentProject);
            mainFrame.getCircuitCanvasPanel().setCircuit(currentCircuit);
        }
        
        if (currentCircuit != null && selectedComponentType != null) {
            try {
                Component component = null;
                
                // Handle in-memory circuits from current project (simpler, no file loading!)
                if (selectedComponentType.startsWith("CIRCUIT:")) {
                    String circuitId = selectedComponentType.substring(8);
                    // Find circuit in current project
                    if (currentProject != null) {
                        Circuit subCircuit = currentProject.getCircuitById(circuitId);
                        if (subCircuit != null && subCircuit != currentCircuit) {
                            // Use in-memory circuit - create with Circuit object
                            component = ComponentFactory.createSubCircuitComponent(
                                edu.uni.logisim.util.IdGenerator.generateId(), 
                                subCircuit, 
                                position);
                            logger.fine("Created sub-circuit from in-memory circuit: " + subCircuit.getName());
                        } else {
                            throw new RuntimeException("Circuit not found in current project: " + circuitId);
                        }
                    } else {
                        throw new RuntimeException("No current project available");
                    }
                } else if (selectedComponentType.startsWith("CUSTOM:")) {
                    // REFERENCE-BASED: Create SubCircuitComponent with project name only
                    // Circuit will be loaded lazily when needed
                    String circuitFilePath = selectedComponentType.substring(7); // Remove "CUSTOM:" prefix
                    File file = new File(circuitFilePath);
                    String projectName = file.getName().replace(".dig", ""); // Extract project name from filename
                    
                    // Create SubCircuitComponent with project name reference (not full circuit!)
                    String componentId = edu.uni.logisim.util.IdGenerator.generateId();
                    String componentName = projectName; // Use project name as component name
                    component = new SubCircuitComponent(componentId, componentName, projectName, circuitFilePath, position);
                    
                    // Set circuit loader so it can load the circuit lazily
                    SubCircuitComponent.CircuitLoader loader = new SubCircuitComponent.CircuitLoader() {
                        @Override
                        public Circuit loadCircuit(String projName) {
                            return getSavedCircuit(projName);
                        }
                    };
                    ((SubCircuitComponent) component).setCircuitLoader(loader);
                    
                    // CRITICAL: Try to load the circuit immediately to validate it
                    // This ensures getInputCount()/getOutputCount() work correctly
                    try {
                        ((SubCircuitComponent) component).getSubCircuit(); // This triggers lazy loading
                        logger.fine("Created SubCircuitComponent with project reference: " + projectName + 
                                  " (circuit loaded: " + (((SubCircuitComponent) component).getSubCircuit() != null) + ")");
                    } catch (Exception e) {
                        logger.warning("Failed to pre-load circuit for SubCircuitComponent: " + e.getMessage());
                        // Continue anyway - circuit will be loaded when needed
                    }
                } else {
                    // Standard component
                    component = ComponentFactory.createComponent(selectedComponentType, position);
                }
                
                // CRITICAL: Ensure component position is set (this updates port positions via updatePortPositions)
                component.setPosition(position);
                
                // Special handling for SubCircuitComponent - check if it has ports
                if (component instanceof SubCircuitComponent) {
                    SubCircuitComponent subCircuit =
                        (SubCircuitComponent) component;
                    
                    // Try to get the circuit to ensure it's loaded
                    Circuit loadedCircuit = subCircuit.getSubCircuit();
                    if (loadedCircuit == null) {
                        // Circuit couldn't be loaded - show error
                        String projectName = subCircuit.getProjectName();
                        JOptionPane.showMessageDialog(mainFrame, 
                            "Error: Could not load circuit for project '" + projectName + "'!\n\n" +
                            "Possible causes:\n" +
                            "1. The project file doesn't exist or was deleted\n" +
                            "2. The project file is corrupted\n" +
                            "3. The project hasn't been saved yet\n\n" +
                            "Please:\n" +
                            "- Make sure the project exists and is saved\n" +
                            "- Try opening the project first to verify it works\n" +
                            "- Create a new project if needed",
                            "Circuit Load Error", JOptionPane.ERROR_MESSAGE);
                        // Don't add the component if circuit can't be loaded
                        return;
                    }
                    
                    // Check if circuit has inputs/outputs
                    int inputCount = subCircuit.getInputCount();
                    int outputCount = subCircuit.getOutputCount();
                    if (inputCount == 0 && outputCount == 0) {
                        JOptionPane.showMessageDialog(mainFrame, 
                            "Warning: This sub-circuit has no inputs or outputs!\n\n" +
                            "To use a circuit as a sub-circuit, it must have:\n" +
                            "- At least one INPUT component (creates input ports)\n" +
                            "- At least one LED component (creates output ports)\n\n" +
                            "The sub-circuit will still be placed, but you won't be able to connect to it.",
                            "Sub-Circuit Warning", JOptionPane.WARNING_MESSAGE);
                    }
                }
            // Double-check: ensure all port positions are set
            Point compPos = component.getPosition();
            if (compPos != null) {
                // Ensure input port positions are set
                int inputIndex = 0;
                for (edu.uni.logisim.domain.connector.Port port : component.getInputPorts()) {
                    if (port.getPosition() == null) {
                        port.setPosition(new Point(compPos.x, compPos.y + 10 + inputIndex * 20));
                    }
                    inputIndex++;
                }
                // Ensure output port positions are set
                int outputIndex = 0;
                for (edu.uni.logisim.domain.connector.Port port : component.getOutputPorts()) {
                    if (port.getPosition() == null) {
                        port.setPosition(new Point(compPos.x + 60, compPos.y + 10 + outputIndex * 20));
                    }
                    outputIndex++;
                }
            }
                circuitService.addComponent(currentCircuit, component);
                mainFrame.getCircuitCanvasPanel().setCircuit(currentCircuit);
                mainFrame.getCircuitCanvasPanel().repaint();
                logger.fine("Component added successfully: " + component.getName() + 
                           " (type: " + selectedComponentType + ")");
            } catch (Exception e) {
                logger.severe("Failed to create component: " + e.getMessage());
                logger.log(Level.SEVERE, "Exception details", e);
                JOptionPane.showMessageDialog(mainFrame, 
                    "Error creating component: " + e.getMessage() + "\n\n" +
                    "If this is a custom circuit, make sure:\n" +
                    "1. The circuit file exists and is valid\n" +
                    "2. The circuit has INPUT components (for inputs)\n" +
                    "3. The circuit has LED components (for outputs)",
                    "Component Creation Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    public Circuit getCurrentCircuit() {
        return currentCircuit;
    }
    
    public Project getCurrentProject() {
        return currentProject;
    }
    
    /**
     * Get the set of project names that have been opened/created in THIS SESSION
     * Only these projects should appear in Custom components
     */
    public java.util.Set<String> getSessionProjects() {
        return sessionProjects;
    }
    
    public void startSimulation() {
        if (currentCircuit != null) {
            if (currentCircuit.getComponents().isEmpty()) {
                JOptionPane.showMessageDialog(mainFrame, "Add some components to the circuit first!", 
                    "Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            simulationService.initializeSimulation(currentCircuit);
            simulationService.setUpdateCallback(() -> {
                SwingUtilities.invokeLater(() -> {
                    mainFrame.getCircuitCanvasPanel().repaint();
                });
            });
            simulationService.startSimulation();
            mainFrame.getSimulationToolbar().updateButtons(true);
        } else {
            JOptionPane.showMessageDialog(mainFrame, "No circuit to simulate! Create a circuit first.", 
                "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void stopSimulation() {
        simulationService.stopSimulation();
        mainFrame.getSimulationToolbar().updateButtons(false);
        if (currentCircuit != null) {
            mainFrame.getCircuitCanvasPanel().repaint();
        }
    }
    
    public void stepSimulation() {
        if (currentCircuit != null) {
            simulationService.initializeSimulation(currentCircuit);
            simulationService.setUpdateCallback(() -> {
                mainFrame.getCircuitCanvasPanel().repaint();
            });
            simulationService.stepSimulation();
            mainFrame.getCircuitCanvasPanel().repaint();
        } else {
            JOptionPane.showMessageDialog(mainFrame, "No circuit to simulate! Create a circuit first.", 
                "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    public void resetSimulation() {
        simulationService.resetSimulation();
        if (currentCircuit != null) {
            mainFrame.getCircuitCanvasPanel().repaint();
        }
    }
    
    // Getters for services
    public CircuitService getCircuitService() {
        return circuitService;
    }
    
    public ProjectService getProjectService() {
        return projectService;
    }
    
    public SimulationService getSimulationService() {
        return simulationService;
    }
    
    public static void main(String[] args) {
        new LogiSimApplication();
    }
}

