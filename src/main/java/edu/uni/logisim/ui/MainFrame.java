package edu.uni.logisim.ui;

import edu.uni.logisim.app.LogiSimApplication;
import javax.swing.*;
import java.awt.*;

/**
 * Represents the main window (frame) of the LogiSim application.
 * 
 * <p>This class orchestrates the main UI components:
 * <ul>
 *   <li>ProjectTreePanel - displays project structure and circuits</li>
 *   <li>CircuitCanvasPanel - the main drawing area for circuit design</li>
 *   <li>ComponentPalettePanel - displays available components</li>
 *   <li>SimulationToolbar - controls for simulation (start/stop/step)</li>
 * </ul>
 * 
 * <p>The frame also provides menu bars for:
 * <ul>
 *   <li>File operations (New, Open, Save, Export)</li>
 *   <li>Component selection</li>
 *   <li>Analysis tools (Truth Table)</li>
 * </ul>
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class MainFrame extends JFrame {
    private ProjectTreePanel projectTreePanel;
    private CircuitCanvasPanel circuitCanvasPanel;
    private ComponentPalettePanel componentPalettePanel;
    private SimulationToolbar simulationToolbar;
    private LogiSimApplication application;
    
    public MainFrame(LogiSimApplication application) {
        this.application = application;
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("LogiSim - Digital Circuit Simulator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        
        // Set application icon (if available) and improve appearance
        try {
            setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(getClass().getResource("/icon.png")));
        } catch (Exception e) {
            // Icon not found, continue without it
        }
        
        // Create menu bar
        createMenuBar();
        
        // Create main panels
        projectTreePanel = new ProjectTreePanel(application);
        circuitCanvasPanel = new CircuitCanvasPanel(application);
        componentPalettePanel = new ComponentPalettePanel(application);
        simulationToolbar = new SimulationToolbar(application);
        
        // Enhanced split pane styling
        JSplitPane leftSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
            projectTreePanel, circuitCanvasPanel);
        leftSplit.setDividerLocation(200);
        leftSplit.setDividerSize(8);
        leftSplit.setBorder(BorderFactory.createEmptyBorder());
        
        JSplitPane mainSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
            leftSplit, componentPalettePanel);
        mainSplit.setDividerLocation(800);
        mainSplit.setDividerSize(8);
        mainSplit.setBorder(BorderFactory.createEmptyBorder());
        
        add(simulationToolbar, BorderLayout.NORTH);
        add(mainSplit, BorderLayout.CENTER);
        
        // Create and add status bar
        JPanel statusBar = createStatusBar();
        add(statusBar, BorderLayout.SOUTH);
        
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }
    
    /**
     * Creates a status bar with helpful information
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusBar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(3, 10, 3, 10)
        ));
        statusBar.setBackground(new Color(245, 245, 250));
        
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        statusLabel.setForeground(new Color(80, 80, 80));
        statusBar.add(statusLabel);
        
        statusBar.add(Box.createHorizontalStrut(20));
        
        JLabel hintLabel = new JLabel("Tip: Click components to place, drag to move, DELETE to remove");
        hintLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 10));
        hintLabel.setForeground(new Color(120, 120, 120));
        statusBar.add(hintLabel);
        
        return statusBar;
    }
    
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newProjectItem = new JMenuItem("New Project");
        JMenuItem openProjectItem = new JMenuItem("Open Project");
        JMenuItem saveProjectItem = new JMenuItem("Save Project");
        JMenuItem saveProjectAsItem = new JMenuItem("Save Project As...");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        fileMenu.add(newProjectItem);
        fileMenu.add(openProjectItem);
        fileMenu.add(saveProjectItem);
        fileMenu.add(saveProjectAsItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // Export menu
        JMenu exportMenu = new JMenu("Export");
        JMenuItem exportPNGItem = new JMenuItem("Export as PNG");
        JMenuItem exportJPEGItem = new JMenuItem("Export as JPEG");
        exportMenu.add(exportPNGItem);
        exportMenu.add(exportJPEGItem);
        fileMenu.add(exportMenu);
        
        menuBar.add(fileMenu);
        
        // Components menu
        JMenu componentsMenu = new JMenu("Components");
        
        // Standard components submenu
        JMenu standardMenu = new JMenu("Standard");
        String[] standardTypes = edu.uni.logisim.domain.component.factory.ComponentFactory.getAvailableTypes();
        for (String type : standardTypes) {
            JMenuItem item = new JMenuItem(type);
            item.addActionListener(e -> {
                application.setSelectedComponentType(type);
                componentPalettePanel.refreshCustomCircuits(); // Refresh to update selection
            });
            standardMenu.add(item);
        }
        componentsMenu.add(standardMenu);
        
        // Custom circuits submenu
        JMenu customMenu = new JMenu("Custom");
        componentsMenu.add(customMenu);
        componentsMenu.addSeparator();
        
        // Refresh custom circuits menu item
        JMenuItem refreshCustomItem = new JMenuItem("Refresh Custom Circuits");
        refreshCustomItem.addActionListener(e -> {
            componentPalettePanel.refreshCustomCircuits();
            updateCustomCircuitsMenu(customMenu);
        });
        componentsMenu.add(refreshCustomItem);
        
        // Initialize custom circuits menu
        updateCustomCircuitsMenu(customMenu);
        
        menuBar.add(componentsMenu);
        
        // Analyse menu
        JMenu analyseMenu = new JMenu("Analyse");
        JMenuItem truthTableItem = new JMenuItem("Generate Truth Table");
        analyseMenu.add(truthTableItem);
        menuBar.add(analyseMenu);
        
        setJMenuBar(menuBar);
        
        // Add action listeners
        exitItem.addActionListener(e -> System.exit(0));
        newProjectItem.addActionListener(e -> application.createNewProject());
        openProjectItem.addActionListener(e -> application.openProject());
        saveProjectItem.addActionListener(e -> application.saveProject());
        saveProjectAsItem.addActionListener(e -> application.saveProjectAs());
        exportPNGItem.addActionListener(e -> application.exportDiagram("PNG"));
        exportJPEGItem.addActionListener(e -> application.exportDiagram("JPEG"));
        truthTableItem.addActionListener(e -> application.showTruthTable());
    }
    
    public ProjectTreePanel getProjectTreePanel() {
        return projectTreePanel;
    }
    
    public CircuitCanvasPanel getCircuitCanvasPanel() {
        return circuitCanvasPanel;
    }
    
    public ComponentPalettePanel getComponentPalettePanel() {
        return componentPalettePanel;
    }
    
    public SimulationToolbar getSimulationToolbar() {
        return simulationToolbar;
    }
    
    /**
     * Update the custom circuits menu with available .dig files
     */
    private void updateCustomCircuitsMenu(JMenu customMenu) {
        customMenu.removeAll();
        
        // ONLY show circuits from CURRENT PROJECT (not old saved files)
        // Old projects should be opened via "Open Project" menu, not shown here
        edu.uni.logisim.domain.project.Project currentProject = application.getCurrentProject();
        if (currentProject != null && currentProject.getCircuits() != null) {
            edu.uni.logisim.domain.circuit.Circuit currentCircuit = application.getCurrentCircuit();
            
            boolean hasCircuits = false;
            for (edu.uni.logisim.domain.circuit.Circuit circuit : currentProject.getCircuits()) {
                // Skip the current circuit (can't use itself as sub-circuit)
                if (circuit == currentCircuit) {
                    continue;
                }
                
                hasCircuits = true;
                String circuitName = circuit.getName();
                JMenuItem item = new JMenuItem(circuitName);
                item.addActionListener(e -> {
                    application.setSelectedComponentType("CIRCUIT:" + circuit.getId());
                    componentPalettePanel.refreshCustomCircuits(); // Refresh to update selection
                });
                customMenu.add(item);
            }
            
            if (!hasCircuits) {
                JMenuItem noCircuitsItem = new JMenuItem("No other circuits in current project");
                noCircuitsItem.setEnabled(false);
                customMenu.add(noCircuitsItem);
            }
        } else {
            JMenuItem noCircuitsItem = new JMenuItem("No project open");
            noCircuitsItem.setEnabled(false);
            customMenu.add(noCircuitsItem);
        }
    }
}

