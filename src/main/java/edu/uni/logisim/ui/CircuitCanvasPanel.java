package edu.uni.logisim.ui;

import edu.uni.logisim.app.LogiSimApplication;
import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.connector.Connector;
import edu.uni.logisim.domain.connector.Port;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;

/**
 * The primary drawing area where users design and view circuits.
 * 
 * <p>This panel provides the main interactive canvas for:
 * <ul>
 *   <li><b>Placing Components:</b> Click to place selected components from the palette</li>
 *   <li><b>Moving Components:</b> Click and drag components to reposition them</li>
 *   <li><b>Connecting Components:</b> Click on output ports and drag to input ports to create wires</li>
 *   <li><b>Selecting Components:</b> Click on components to select them (for deletion or inspection)</li>
 *   <li><b>Deleting Components:</b> Press DELETE key to remove selected components</li>
 *   <li><b>Visual Feedback:</b> Components show their current signal states (green = high, black = low)</li>
 * </ul>
 * 
 * <p>The canvas supports:
 * <ul>
 *   <li>Mouse interactions for component placement and connection</li>
 *   <li>Keyboard shortcuts (DELETE key for component removal)</li>
 *   <li>Real-time visualization of signal propagation during simulation</li>
 *   <li>Grid-based layout for clean circuit organization</li>
 * </ul>
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class CircuitCanvasPanel extends JPanel implements MouseListener, MouseMotionListener, KeyListener {
    private LogiSimApplication application;
    private Circuit currentCircuit;
    private Component selectedComponent;
    private Connector connectorBeingDrawn;
    private Port sourcePort;
    private Point lastMousePosition;
    
    // Component dragging state
    private boolean isDraggingComponent;
    private Component draggingComponent;
    private Point dragOffset; // Offset from component top-left corner to mouse position
    private boolean hasDragged; // Track if mouse actually moved during drag

    public CircuitCanvasPanel(LogiSimApplication application) {
        this.application = application;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(null);
        setBorder(BorderFactory.createTitledBorder("Circuit Canvas"));
        setBackground(new Color(250, 250, 250)); // Slightly lighter background
        setFocusable(true);
        requestFocusInWindow();
        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);
        
        // Make sure panel can receive focus for keyboard events
        setFocusTraversalKeysEnabled(false);
    }

    public void setCircuit(Circuit circuit) {
        this.currentCircuit = circuit;
        this.selectedComponent = null;
        repaint();
    }

    public Circuit getCurrentCircuit() {
        return currentCircuit;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw grid background for better visual alignment
        drawGrid(g2d);

        if (currentCircuit == null) {
            return;
        }

        // Draw connectors first (so they appear behind components)
        // CRITICAL: Always draw connectors, even if list is empty (to ensure they show up)
        List<Connector> connectors = currentCircuit.getConnectors();
        if (connectors != null) {
            if (connectors.size() > 0) {
                System.out.println("DEBUG: Drawing " + connectors.size() + " connector(s)");
            }
            for (Connector connector : connectors) {
                if (connector != null) {
                    drawConnector(g2d, connector);
                }
            }
        }

        // Draw components on top of wires
        for (Component component : currentCircuit.getComponents()) {
            drawComponent(g2d, component);
        }

        // Draw connector being drawn (while dragging wire)
        if (sourcePort != null && lastMousePosition != null) {
            Point start = sourcePort.getPosition();
            if (start == null) {
                // Try to get position from component
                Component sourceComp = currentCircuit.getComponents().stream()
                        .filter(c -> c.getOutputPorts().contains(sourcePort))
                        .findFirst().orElse(null);
                if (sourceComp != null && sourceComp.getPosition() != null) {
                    Point compPos = sourceComp.getPosition();
                    start = new Point(compPos.x + 60, compPos.y + 20);
                }
            }
            if (start != null) {
                g2d.setColor(Color.GRAY);
                float[] dashPattern = {5, 5};
                g2d.setStroke(new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0, dashPattern, 0));
                g2d.drawLine(start.x, start.y, lastMousePosition.x, lastMousePosition.y);
            }
        }
    }

    /**
     * Draws a subtle grid background for better visual alignment
     */
    private void drawGrid(Graphics2D g) {
        Color gridColor = new Color(230, 230, 230); // Very light gray
        g.setColor(gridColor);
        g.setStroke(new BasicStroke(1));
        
        int gridSize = 20; // Grid spacing
        int width = getWidth();
        int height = getHeight();
        
        // Draw vertical lines
        for (int x = 0; x < width; x += gridSize) {
            g.drawLine(x, 0, x, height);
        }
        
        // Draw horizontal lines
        for (int y = 0; y < height; y += gridSize) {
            g.drawLine(0, y, width, y);
        }
    }

    private void drawComponent(Graphics2D g, Component component) {
        Point pos = component.getPosition();
        if (pos == null) return;

        // CRITICAL: Always update port positions from component position to keep them in sync
        component.setPosition(pos); // This calls updatePortPositions()

        // Enhanced component styling with rounded corners and shadows
        Color bgColor;
        Color borderColor = new Color(80, 80, 80); // Dark gray border
        
        if (component == selectedComponent) {
            bgColor = new Color(255, 255, 200); // Light yellow for selection
            borderColor = new Color(255, 200, 0); // Gold border for selected
        } else {
            bgColor = new Color(245, 245, 250); // Light blue-gray
        }
        
        // Special handling for LED - show if it's lit
        if (component instanceof edu.uni.logisim.domain.component.io.LedOutput) {
            edu.uni.logisim.domain.component.io.LedOutput led = (edu.uni.logisim.domain.component.io.LedOutput) component;
            if (led.isLit()) {
                bgColor = new Color(100, 255, 100); // Bright green when lit
                borderColor = new Color(0, 200, 0); // Dark green border
            } else {
                bgColor = new Color(220, 220, 220); // Gray when off
            }
        }
        
        // Draw shadow effect
        g.setColor(new Color(0, 0, 0, 30)); // Semi-transparent black
        g.fillRoundRect(pos.x + 2, pos.y + 2, 60, 40, 5, 5);
        
        // Draw component with rounded corners
        g.setColor(bgColor);
        g.fillRoundRect(pos.x, pos.y, 60, 40, 5, 5);
        
        // Draw border
        g.setColor(borderColor);
        g.setStroke(new BasicStroke(2));
        g.drawRoundRect(pos.x, pos.y, 60, 40, 5, 5);

        // Draw component name with better font and centering
        String displayName = component.getName();
        if (displayName.length() > 8) {
            displayName = displayName.substring(0, 8) + "...";
        }
        g.setColor(Color.BLACK);
        g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
        FontMetrics fm = g.getFontMetrics();
        int textWidth = fm.stringWidth(displayName);
        int textX = pos.x + (60 - textWidth) / 2; // Center text
        g.drawString(displayName, textX, pos.y + 25);
        
        // Special display for SubCircuitComponent - show input/output count
        if (component instanceof edu.uni.logisim.domain.component.SubCircuitComponent) {
            edu.uni.logisim.domain.component.SubCircuitComponent subCircuit = 
                (edu.uni.logisim.domain.component.SubCircuitComponent) component;
            String info = subCircuit.getInputCount() + "in/" + subCircuit.getOutputCount() + "out";
            g.setFont(g.getFont().deriveFont(8f));
            g.drawString(info, pos.x + 5, pos.y + 38);
            g.setFont(g.getFont().deriveFont(12f));
        }

        // Draw input ports (red, on left side) - enhanced styling
        int inputIndex = 0;
        for (Port port : component.getInputPorts()) {
            Point portPos = port.getPosition();
            // Port position should always be set now (from updatePortPositions)
            if (portPos == null) {
                portPos = new Point(pos.x, pos.y + 10 + inputIndex * 20);
                port.setPosition(portPos);
            }
            // Enhanced port drawing with gradient effect
            Color portColor = port.getValue() ? new Color(255, 100, 100) : new Color(200, 50, 50);
            g.setColor(portColor);
            g.fillOval(portPos.x - 6, portPos.y - 6, 12, 12);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawOval(portPos.x - 6, portPos.y - 6, 12, 12);
            inputIndex++;
        }

        // Draw output ports (blue, on right side) - enhanced styling
        int outputIndex = 0;
        for (Port port : component.getOutputPorts()) {
            Point portPos = port.getPosition();
            // Port position should always be set now (from updatePortPositions)
            if (portPos == null) {
                portPos = new Point(pos.x + 60, pos.y + 10 + outputIndex * 20);
                port.setPosition(portPos);
            }
            // Enhanced port drawing with gradient effect
            Color portColor = port.getValue() ? new Color(100, 150, 255) : new Color(50, 100, 200);
            g.setColor(portColor);
            g.fillOval(portPos.x - 6, portPos.y - 6, 12, 12);
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            g.drawOval(portPos.x - 6, portPos.y - 6, 12, 12);
            outputIndex++;
        }
    }

    private void drawConnector(Graphics2D g, Connector connector) {
        if (connector == null) return;

        // Get source and sink components - MUST have these
        Component sourceComp = connector.getSourceComponent();
        Component sinkComp = connector.getSinkComponent();
        
        if (sourceComp == null || sinkComp == null) {
            return; // Can't draw without components
        }

        Point sourcePos = sourceComp.getPosition();
        Point sinkPos = sinkComp.getPosition();
        
        if (sourcePos == null || sinkPos == null) {
            return; // Can't draw without component positions
        }

        // CRITICAL: Always get positions from ports (which are updated when components are drawn)
        // This ensures wires stay connected to the correct port positions
        Point start = null;
        Point end = null;
        
        // Get source port (output port - right side of component)
        Port sourcePort = connector.getSourcePort();
        if (sourcePort != null) {
            start = sourcePort.getPosition();
            // If port position is null, calculate it and update both port and connector
            if (start == null) {
                start = new Point(sourcePos.x + 60, sourcePos.y + 20);
                sourcePort.setPosition(start);
                connector.setStartPosition(start);
            } else {
                // Update connector's stored position to match port (in case component moved)
                connector.setStartPosition(new Point(start));
            }
        }
        
        // Get sink port (input port - left side of component)
        Port sinkPort = connector.getSinkPort();
        if (sinkPort != null) {
            end = sinkPort.getPosition();
            // If port position is null, calculate it and update both port and connector
            if (end == null) {
                end = new Point(sinkPos.x, sinkPos.y + 20);
                sinkPort.setPosition(end);
                connector.setEndPosition(end);
            } else {
                // Update connector's stored position to match port (in case component moved)
                connector.setEndPosition(new Point(end));
            }
        }

        // Now we MUST have valid positions - draw the wire with enhanced styling
        if (start != null && end != null) {
            // Draw wire with appropriate color and thickness based on signal value
            if (connector.getValue()) {
                g.setColor(new Color(0, 200, 0)); // Bright green for high signal
                g.setStroke(new BasicStroke(3.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            } else {
                g.setColor(new Color(60, 60, 60)); // Dark gray for low signal
                g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            }
            g.drawLine(start.x, start.y, end.x, end.y);
        }
    }

    private Port findPortAt(Point point) {
        if (currentCircuit == null) {
            currentCircuit = application.getCurrentCircuit();
        }
        if (currentCircuit == null) return null;

        // Increased tolerance for easier clicking (15 pixels)
        int tolerance = 15;
        Port closestPort = null;
        double closestDistance = Double.MAX_VALUE;

        for (Component component : currentCircuit.getComponents()) {
            // Check output ports first (blue ports on right side)
            for (Port port : component.getOutputPorts()) {
                Point portPos = port.getPosition();
                if (portPos != null) {
                    double distance = Math.sqrt(Math.pow(portPos.x - point.x, 2) + Math.pow(portPos.y - point.y, 2));
                    if (distance < tolerance && distance < closestDistance) {
                        closestPort = port;
                        closestDistance = distance;
                    }
                }
            }
            // Check input ports (red ports on left side)
            for (Port port : component.getInputPorts()) {
                Point portPos = port.getPosition();
                if (portPos != null) {
                    double distance = Math.sqrt(Math.pow(portPos.x - point.x, 2) + Math.pow(portPos.y - point.y, 2));
                    if (distance < tolerance && distance < closestDistance) {
                        closestPort = port;
                        closestDistance = distance;
                    }
                }
            }
        }
        return closestPort;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Ensure panel has focus for keyboard events
        requestFocusInWindow();
        
        // If we just finished dragging, don't process click (to avoid double-selection or resetting position)
        if (hasDragged) {
            System.out.println("DEBUG: Ignoring mouseClicked because component was dragged");
            hasDragged = false; // Reset flag
            return;
        }

        // Update currentCircuit reference
        currentCircuit = application.getCurrentCircuit();

        if (currentCircuit == null) {
            // If no circuit, create one automatically
            application.createNewProject();
            currentCircuit = application.getCurrentCircuit();
            if (currentCircuit == null) return;
        }

        // CRITICAL: FIRST check if clicking on an existing component or port
        // This MUST take priority over placing new components to prevent overlap
        Component clicked = findComponentAtPoint(e.getPoint());
        Port port = findPortAt(e.getPoint());

        // If clicking on a port, don't place new component or select component
        if (port != null) {
            selectedComponent = null;
            repaint();
            return;
        }

        // CRITICAL: If clicking on an existing component, ALWAYS select it (never place new one)
        // This prevents overlapping even when a component type is selected
        if (clicked != null) {
            selectedComponent = clicked;
            // If it's an input switch, toggle it and run simulation
            if (clicked instanceof edu.uni.logisim.domain.component.io.InputSwitch) {
                ((edu.uni.logisim.domain.component.io.InputSwitch) clicked).toggle();
                // CRITICAL: Always run simulation step when input is toggled to update circuit
                // Initialize simulation if needed
                if (currentCircuit != null) {
                    application.getSimulationService().initializeSimulation(currentCircuit);
                }
                application.getSimulationService().stepSimulation();
                // Force repaint to show updated LED states and wire colors
                repaint();
            }
            repaint();
            return; // IMPORTANT: Return here to prevent placing new component
        }

        // Only place new component if clicking on EMPTY space AND component type is selected
        String selectedType = application.getSelectedComponentType();
        if (selectedType != null) {
            // Place new component at click location
            Point position = new Point(e.getX() - 30, e.getY() - 20);
            application.addComponentToCircuit(position);
            repaint();
        } else {
            // No component selected and clicking empty space - deselect any selected component
            selectedComponent = null;
            repaint();
        }
    }

    /**
     * Find component at a specific point (more accurate than service method)
     */
    private Component findComponentAtPoint(Point point) {
        if (currentCircuit == null) return null;

        // Check each component to see if point is within its bounds
        for (Component component : currentCircuit.getComponents()) {
            Point pos = component.getPosition();
            if (pos != null) {
                // Components are 60x40 pixels
                if (point.x >= pos.x && point.x <= pos.x + 60 &&
                        point.y >= pos.y && point.y <= pos.y + 40) {
                    return component;
                }
            }
        }
        return null;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastMousePosition = e.getPoint();
        currentCircuit = application.getCurrentCircuit();
        
        // Reset dragging state
        isDraggingComponent = false;
        draggingComponent = null;
        dragOffset = null;
        hasDragged = false;
        
        // Check if clicking on a port to start connection
        // IMPORTANT: Allow wire drawing even if component type is selected (ports take priority)
        if (currentCircuit != null) {
            Port port = findPortAt(e.getPoint());
            if (port != null) {
                System.out.println("DEBUG: Port found at click: " + port.getType() + " - starting wire connection");
                if (port.getType() == edu.uni.logisim.domain.connector.PortType.OUTPUT) {
                    // Start wire from output port - this takes priority over component placement/dragging
                    sourcePort = port;
                    Component sourceComp = currentCircuit.getComponents().stream()
                            .filter(c -> c.getOutputPorts().contains(port))
                            .findFirst().orElse(null);
                    connectorBeingDrawn = new Connector("temp", port, null, sourceComp, null);
                    System.out.println("DEBUG: Started wire from output port. Source component: " + (sourceComp != null ? sourceComp.getName() : "null"));
                    repaint(); // Show wire being drawn
                } else {
                    // Clicked on input port - don't do anything here, wait for release
                    System.out.println("DEBUG: Clicked on input port, waiting for release");
                    sourcePort = null;
                }
            } else {
                // Not clicking on a port - check if clicking on existing component to drag
                // IMPORTANT: Allow dragging even if component type is selected (clicking existing component takes priority)
                Component clicked = findComponentAtPoint(e.getPoint());
                System.out.println("DEBUG: mousePressed - clicked component: " + (clicked != null ? clicked.getName() : "null") + 
                                 " at point: " + e.getPoint());
                
                if (clicked != null) {
                    // Start dragging this component (regardless of whether component type is selected)
                    isDraggingComponent = true;
                    draggingComponent = clicked;
                    selectedComponent = clicked; // Also select it
                    
                    // Calculate offset from component top-left to mouse position
                    Point compPos = clicked.getPosition();
                    if (compPos != null) {
                        dragOffset = new Point(e.getX() - compPos.x, e.getY() - compPos.y);
                        System.out.println("DEBUG: ✓ Started dragging component: " + clicked.getName() + 
                                         " at position: " + compPos + 
                                         " with offset: " + dragOffset +
                                         " (mouse: " + e.getPoint() + ")");
                    } else {
                        System.out.println("ERROR: Component position is null! Cannot start drag.");
                        isDraggingComponent = false;
                        draggingComponent = null;
                        dragOffset = null;
                    }
                    hasDragged = false; // Reset drag flag
                    repaint();
                } else {
                    // Clicking empty space
                    String selectedType = application.getSelectedComponentType();
                    System.out.println("DEBUG: No component found at click point. Selected type: " + 
                                     (selectedType != null ? selectedType : "null"));
                    if (selectedType == null) {
                        // No component type selected - clear selection
                        System.out.println("DEBUG: Clicked empty space, clearing selection");
                        selectedComponent = null;
                    } else {
                        // Component type selected - will place new component on click (handled in mouseClicked)
                        System.out.println("DEBUG: No port clicked, component type selected - will place component on click");
                    }
                    repaint();
                }
                // Clear any wire drawing
                sourcePort = null;
                connectorBeingDrawn = null;
            }
        } else {
            System.out.println("DEBUG: No current circuit!");
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        System.out.println("DEBUG: mouseReleased called - isDraggingComponent: " + isDraggingComponent + ", hasDragged: " + hasDragged);
        
        currentCircuit = application.getCurrentCircuit();
        
        // Handle component dragging completion FIRST (before wire connection)
        if (isDraggingComponent && draggingComponent != null) {
            if (hasDragged && dragOffset != null) {
                // Finalize component position only if we actually dragged
                Point finalPos = new Point(e.getX() - dragOffset.x, e.getY() - dragOffset.y);
                draggingComponent.setPosition(finalPos); // This updates port positions
                System.out.println("DEBUG: Finished dragging component: " + draggingComponent.getName() + " to position: " + finalPos);
            }
            // Clear dragging state
            isDraggingComponent = false;
            draggingComponent = null;
            dragOffset = null;
            hasDragged = false;
            repaint();
            return; // Don't process wire connection if we were dragging
        }
        
        System.out.println("DEBUG: sourcePort = " + (sourcePort != null ? "SET" : "NULL") + ", currentCircuit = " + (currentCircuit != null ? "SET" : "NULL"));
        
        // IMPORTANT: If we have a sourcePort, we're drawing a wire - this takes priority over component placement
        // Only skip if we DON'T have a sourcePort AND a component type is selected (then we'd place a component)
        if (sourcePort == null) {
            String selectedType = application.getSelectedComponentType();
            if (selectedType != null) {
                System.out.println("DEBUG: No wire being drawn and component type selected - will handle in mouseClicked");
                connectorBeingDrawn = null;
                return;
            }
        }

        if (sourcePort != null && currentCircuit != null) {
            Port targetPort = findPortAt(e.getPoint());
            System.out.println("DEBUG: Target port found: " + (targetPort != null ? targetPort.getType() : "NULL"));
            
            if (targetPort != null && targetPort.getType() == edu.uni.logisim.domain.connector.PortType.INPUT) {
                System.out.println("DEBUG: Valid target port (INPUT), creating connection...");
                
                // Create connection from output to input
                Component source = currentCircuit.getComponents().stream()
                        .filter(c -> c.getOutputPorts().contains(sourcePort))
                        .findFirst().orElse(null);
                Component sink = currentCircuit.getComponents().stream()
                        .filter(c -> c.getInputPorts().contains(targetPort))
                        .findFirst().orElse(null);

                System.out.println("DEBUG: Source component: " + (source != null ? source.getName() : "NULL"));
                System.out.println("DEBUG: Sink component: " + (sink != null ? sink.getName() : "NULL"));

                if (source != null && sink != null && source != sink) {
                    // CRITICAL: ALWAYS ensure port positions are set BEFORE creating connector
                    Point startPos = null;
                    Point endPos = null;

                    if (source.getPosition() != null) {
                        Point sourcePos = source.getPosition();
                        // Calculate output port position (right side of component)
                        startPos = new Point(sourcePos.x + 60, sourcePos.y + 20);
                        sourcePort.setPosition(startPos);
                        System.out.println("DEBUG: Start position set: " + startPos);
                    }

                    if (sink.getPosition() != null) {
                        Point sinkPos = sink.getPosition();
                        // Calculate input port position (left side of component)
                        endPos = new Point(sinkPos.x, sinkPos.y + 20);
                        targetPort.setPosition(endPos);
                        System.out.println("DEBUG: End position set: " + endPos);
                    }

                    // Only create connector if we have valid positions
                    if (startPos != null && endPos != null) {
                        System.out.println("DEBUG: Calling connectComponents...");
                        Connector newConnector = application.getCircuitService().connectComponents(
                                currentCircuit, source, sourcePort, sink, targetPort);
                        
                        if (newConnector != null) {
                            System.out.println("DEBUG: Connector created successfully!");
                            
                            // ALWAYS set positions explicitly on connector - CRITICAL for drawing
                            newConnector.setStartPosition(new Point(startPos));
                            newConnector.setEndPosition(new Point(endPos));
                            
                            // Also ensure ports have positions set
                            sourcePort.setPosition(new Point(startPos));
                            targetPort.setPosition(new Point(endPos));

                            // Double-check connector was added to circuit
                            if (!currentCircuit.getConnectors().contains(newConnector)) {
                                currentCircuit.addConnector(newConnector);
                                System.out.println("DEBUG: Connector manually added to circuit");
                            }

                            System.out.println("DEBUG: Total connectors in circuit: " + currentCircuit.getConnectors().size());

                            // CRITICAL: Force immediate repaint to show the new wire
                            SwingUtilities.invokeLater(() -> {
                                System.out.println("DEBUG: Repainting canvas...");
                                repaint();
                            });
                        } else {
                            // Connection failed - might be duplicate
                            System.out.println("ERROR: Failed to create connector - connectComponents returned null");
                        }
                    } else {
                        System.out.println("ERROR: Invalid positions - startPos: " + startPos + ", endPos: " + endPos);
                    }
                } else {
                    System.out.println("ERROR: Invalid components - source: " + source + ", sink: " + sink);
                }
            } else {
                System.out.println("DEBUG: No valid target port (not INPUT or null)");
            }
        } else {
            System.out.println("DEBUG: Cannot create connection - sourcePort or currentCircuit is null");
        }
        
        // Clear wire drawing state
        connectorBeingDrawn = null;
        sourcePort = null;
        
        repaint();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        lastMousePosition = e.getPoint();
        
        // Handle component dragging
        if (isDraggingComponent && draggingComponent != null && dragOffset != null) {
            // Mark that we've actually dragged (moved the mouse)
            hasDragged = true;
            
            // Calculate new component position based on mouse position and offset
            Point newPosition = new Point(e.getX() - dragOffset.x, e.getY() - dragOffset.y);
            
            // Update component position (this will also update port positions via setPosition)
            draggingComponent.setPosition(newPosition);
            
            System.out.println("DEBUG: Dragging component to position: " + newPosition);
            
            // Force repaint to show component in new position
            repaint();
        }
        
        // Handle wire drawing while dragging
        if (connectorBeingDrawn != null) {
            repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        lastMousePosition = e.getPoint();
        
        // Don't change cursor if we're dragging
        if (isDraggingComponent) {
            setCursor(new Cursor(Cursor.MOVE_CURSOR));
            return;
        }
        
        // Update cursor when over ports to indicate you can connect
        Port port = findPortAt(e.getPoint());
        if (port != null) {
            if (port.getType() == edu.uni.logisim.domain.connector.PortType.OUTPUT) {
                setCursor(new Cursor(Cursor.HAND_CURSOR)); // Hand cursor for output ports
            } else {
                setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR)); // Crosshair for input ports
            }
        } else {
            // Check if over a component (to show it can be dragged)
            Component comp = findComponentAtPoint(e.getPoint());
            if (comp != null && application.getSelectedComponentType() == null) {
                setCursor(new Cursor(Cursor.MOVE_CURSOR)); // Move cursor when over component
            } else {
                setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        requestFocusInWindow(); // Get focus when mouse enters
    }

    @Override
    public void mouseExited(MouseEvent e) {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) {
            // Update current circuit reference
            currentCircuit = application.getCurrentCircuit();

            if (selectedComponent != null && currentCircuit != null) {
                // Remove component (this also removes connected wires)
                application.getCircuitService().removeComponent(currentCircuit, selectedComponent);
                selectedComponent = null;
                // Force repaint to update display
                repaint();
            } else if (currentCircuit != null) {
                // If no component selected, try to find one at last mouse position
                if (lastMousePosition != null) {
                    Component comp = application.getCircuitService().findComponentAt(
                            currentCircuit, lastMousePosition.x, lastMousePosition.y);
                    if (comp != null) {
                        application.getCircuitService().removeComponent(currentCircuit, comp);
                        repaint();
                    }
                }
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {}

    @Override
    public void keyTyped(KeyEvent e) {}
}

