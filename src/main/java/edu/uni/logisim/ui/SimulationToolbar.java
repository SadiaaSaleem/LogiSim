package edu.uni.logisim.ui;

import edu.uni.logisim.app.LogiSimApplication;
import javax.swing.*;
import java.awt.*;

/**
 * A toolbar providing controls for circuit simulation.
 * 
 * <p>This toolbar provides buttons for:
 * <ul>
 *   <li><b>Start:</b> Begins continuous simulation of the current circuit</li>
 *   <li><b>Stop:</b> Stops the running simulation</li>
 *   <li><b>Step:</b> Executes a single simulation step (useful for debugging)</li>
 *   <li><b>Reset:</b> Resets all component states to their initial values</li>
 * </ul>
 * 
 * <p>During simulation, signal values propagate through the circuit,
 * and components update their visual states (colors) to reflect
 * current signal values (green = high/logic 1, black = low/logic 0).
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class SimulationToolbar extends JPanel {
    private LogiSimApplication application;
    private JButton startButton;
    private JButton stopButton;
    private JButton stepButton;
    private JButton resetButton;
    
    public SimulationToolbar(LogiSimApplication application) {
        this.application = application;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        setBackground(new Color(245, 245, 250));
        
        // Enhanced button styling
        startButton = createStyledButton("▶ Start", new Color(50, 150, 50));
        stopButton = createStyledButton("■ Stop", new Color(200, 50, 50));
        stepButton = createStyledButton("⏭ Step", new Color(50, 100, 200));
        resetButton = createStyledButton("↻ Reset", new Color(200, 150, 50));
        
        startButton.setToolTipText("Start continuous simulation");
        stopButton.setToolTipText("Stop the simulation");
        stepButton.setToolTipText("Execute one simulation step");
        resetButton.setToolTipText("Reset all components to initial state");
        
        startButton.addActionListener(e -> application.startSimulation());
        stopButton.addActionListener(e -> application.stopSimulation());
        stepButton.addActionListener(e -> application.stepSimulation());
        resetButton.addActionListener(e -> application.resetSimulation());
        
        add(startButton);
        add(stopButton);
        add(stepButton);
        add(resetButton);
    }
    
    /**
     * Creates a styled button with modern appearance
     */
    private JButton createStyledButton(String text, Color bgColor) {
        JButton button = new JButton(text);
        button.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setPreferredSize(new Dimension(100, 32));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor.brighter());
            }
            
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
            }
        });
        
        return button;
    }
    
    public void updateButtons(boolean isRunning) {
        startButton.setEnabled(!isRunning);
        stopButton.setEnabled(isRunning);
    }
}

