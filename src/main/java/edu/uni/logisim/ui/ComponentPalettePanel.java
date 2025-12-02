package edu.uni.logisim.ui;

import edu.uni.logisim.app.LogiSimApplication;
import edu.uni.logisim.domain.component.factory.ComponentFactory;
import javax.swing.*;
import java.awt.*;

/**
 * A panel displaying available components for circuit design.
 * 
 * <p>This panel provides a palette of components that users can select
 * to add to their circuits. It includes:
 * <ul>
 *   <li><b>Standard Components:</b> Built-in logic gates and IO components
 *       (AND, OR, NOT, Input Switch, LED Output)</li>
 *   <li><b>Custom Components:</b> Circuits from the current project session
 *       that can be reused as sub-circuits</li>
 * </ul>
 * 
 * <p>When a component button is clicked, it becomes the selected component type,
 * and users can then place it on the circuit canvas by clicking.
 * 
 * <p><b>Note:</b> Only circuits created or opened in the current session
 * appear in the Custom section. Old projects must be opened via the
 * "Open Project" menu item.
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class ComponentPalettePanel extends JPanel {
    private LogiSimApplication application;
    private String selectedComponentType;
    
    public ComponentPalettePanel(LogiSimApplication application) {
        this.application = application;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createTitledBorder("Components"));
        setPreferredSize(new Dimension(120, 0));
        
        // Standard components section
        JLabel standardLabel = new JLabel("Standard:");
        standardLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        add(standardLabel);
        
        String[] componentTypes = ComponentFactory.getAvailableTypes();
        
        for (String type : componentTypes) {
            JButton button = createComponentButton(type);
            button.addActionListener(e -> {
                selectedComponentType = type;
                application.setSelectedComponentType(type);
                // Highlight selected button
                highlightSelectedButton(button);
            });
            add(button);
        }
        
        // Custom circuits section - ONLY show circuits from CURRENT project
        // Old saved projects should NOT appear here - use "Open Project" menu instead
        add(Box.createVerticalStrut(10));
        JLabel customLabel = new JLabel("Custom:");
        customLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 10));
        add(customLabel);
        
        // Load and display ONLY circuits from current project (no file scanning!)
        refreshCustomCircuits();
    }
    
    /**
     * Refresh the list of custom circuits - ONLY from current project
     * Old saved projects should NOT appear here - use "Open Project" menu instead
     */
    public void refreshCustomCircuits() {
        // STEP 1: Remove ALL existing custom circuit buttons (everything after "Custom:" label)
        java.awt.Component[] components = getComponents();
        java.util.List<JButton> toRemove = new java.util.ArrayList<>();
        boolean foundCustomLabel = false;
        
        // Find all buttons after "Custom:" label and mark for removal
        for (int i = 0; i < components.length; i++) {
            if (components[i] instanceof JLabel && 
                ((JLabel) components[i]).getText().equals("Custom:")) {
                foundCustomLabel = true;
            } else if (foundCustomLabel && components[i] instanceof JButton) {
                // Remove ALL buttons after "Custom:" label (they're all custom circuits)
                toRemove.add((JButton) components[i]);
            }
        }
        
        // Actually remove them
        for (JButton button : toRemove) {
            remove(button);
        }
        
        // STEP 2: ONLY add circuits from projects created/opened in THIS SESSION
        // Old projects from previous sessions should NOT appear - use "Open Project" menu instead
        java.util.Set<String> sessionProjects = application.getSessionProjects();
        edu.uni.logisim.domain.project.Project currentProject = application.getCurrentProject();
        edu.uni.logisim.domain.circuit.Circuit currentCircuit = application.getCurrentCircuit();
        String currentProjectName = currentProject != null ? currentProject.getName() : null;
        
        // Show circuits from ALL session projects (not just current)
        for (String projectName : sessionProjects) {
            if (projectName.equals(currentProjectName) && currentProject != null) {
                // Show circuits from current project (skip current circuit itself)
                if (currentProject.getCircuits() != null) {
                    for (edu.uni.logisim.domain.circuit.Circuit circuit : currentProject.getCircuits()) {
                        // Skip the current circuit (can't use itself as sub-circuit)
                        if (circuit == currentCircuit) {
                            continue;
                        }
                        
                        // Create button for this circuit from session project
                        String circuitName = circuit.getName();
                        JButton button = createComponentButton(circuitName.length() > 10 ? circuitName.substring(0, 10) : circuitName);
                        button.setToolTipText("Circuit: " + circuitName + " (ID: " + circuit.getId() + ")");
                        button.addActionListener(e -> {
                            // Use "CIRCUIT:" prefix for in-memory circuits from session projects
                            selectedComponentType = "CIRCUIT:" + circuit.getId();
                            application.setSelectedComponentType(selectedComponentType);
                            highlightSelectedButton(button);
                        });
                        add(button);
                    }
                }
            } else {
                // Show circuits from other session projects (saved projects)
                // Use "CUSTOM:" prefix with file path for reference-based loading
                String filePath = "projects/" + projectName + ".dig";
                String circuitName = projectName; // Use project name as display name
                JButton button = createComponentButton(circuitName.length() > 10 ? circuitName.substring(0, 10) : circuitName);
                button.setToolTipText("Custom Circuit: " + projectName);
                button.addActionListener(e -> {
                    // Use "CUSTOM:" prefix for saved projects (reference-based)
                    selectedComponentType = "CUSTOM:" + filePath;
                    application.setSelectedComponentType(selectedComponentType);
                    highlightSelectedButton(button);
                });
                add(button);
            }
        }
        
        // Force UI update
        revalidate();
        repaint();
    }
    
    /**
     * Creates a styled component button with modern appearance
     */
    private JButton createComponentButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(100, 32));
        button.setMaximumSize(new Dimension(100, 32));
        button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
        button.setBackground(new Color(240, 245, 250));
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (button.getBackground() != Color.YELLOW) {
                    button.setBackground(new Color(220, 235, 250));
                }
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                if (button.getBackground() != Color.YELLOW) {
                    button.setBackground(new Color(240, 245, 250));
                }
            }
        });
        
        return button;
    }
    
    private void highlightSelectedButton(JButton selectedButton) {
        // Clear all button backgrounds and restore default styling
        for (java.awt.Component comp : getComponents()) {
            if (comp instanceof JButton) {
                JButton btn = (JButton) comp;
                if (btn == selectedButton) {
                    btn.setBackground(new Color(255, 255, 150)); // Light yellow for selection
                    btn.setBorder(BorderFactory.createLoweredBevelBorder());
                } else {
                    btn.setBackground(new Color(240, 245, 250)); // Default color
                    btn.setBorder(BorderFactory.createRaisedBevelBorder());
                }
            }
        }
        for (java.awt.Component comp : getComponents()) {
            if (comp instanceof JButton) {
                comp.setBackground(null);
            }
        }
        // Highlight selected button
        if (selectedButton != null) {
            selectedButton.setBackground(Color.CYAN);
        }
    }
    
    public String getSelectedComponentType() {
        return selectedComponentType;
    }
}

