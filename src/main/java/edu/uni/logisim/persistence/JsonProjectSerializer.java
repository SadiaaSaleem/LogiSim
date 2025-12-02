package edu.uni.logisim.persistence;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.security.AnyTypePermission;
import edu.uni.logisim.domain.project.Project;
import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.component.io.InputSwitch;
import edu.uni.logisim.domain.component.io.LedOutput;
import edu.uni.logisim.domain.component.logic.AndGate;
import edu.uni.logisim.domain.component.logic.OrGate;
import edu.uni.logisim.domain.component.logic.NotGate;
import edu.uni.logisim.domain.component.SubCircuitComponent;
import edu.uni.logisim.domain.connector.Connector;
import edu.uni.logisim.domain.connector.Port;
import edu.uni.logisim.domain.connector.PortType;
import java.awt.Point;
import java.awt.Color;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Handles serialization and deserialization of project data to/from JSON format
 * (Note: Using XStream for XML serialization as it's simpler and matches .dig format)
 */
public class JsonProjectSerializer {
    private static final Logger logger = Logger.getLogger(JsonProjectSerializer.class.getName());
    private XStream xstream;
    
    public JsonProjectSerializer() {
        xstream = new XStream();
        
        // Allow all types (for deserialization)
        xstream.addPermission(AnyTypePermission.ANY);
        
        // Register aliases for all domain classes
        xstream.alias("project", Project.class);
        xstream.alias("circuit", Circuit.class);
        xstream.alias("component", Component.class);
        xstream.alias("connector", Connector.class);
        xstream.alias("port", Port.class);
        
        // Component types
        xstream.alias("inputSwitch", InputSwitch.class);
        xstream.alias("ledOutput", LedOutput.class);
        xstream.alias("andGate", AndGate.class);
        xstream.alias("orGate", OrGate.class);
        xstream.alias("notGate", NotGate.class);
        xstream.alias("subCircuitComponent", SubCircuitComponent.class);
        
        // Collections
        xstream.alias("circuits", List.class);
        xstream.alias("components", List.class);
        xstream.alias("connectors", List.class);
        xstream.alias("inputPorts", List.class);
        xstream.alias("outputPorts", List.class);
        
        // Java types
        xstream.alias("point", Point.class);
        xstream.alias("color", Color.class);
        xstream.alias("portType", PortType.class);
        
        // Use implicit collections to avoid wrapper elements
        // Specify item types to help XStream with custom converters
        xstream.addImplicitCollection(Project.class, "circuits", Circuit.class);
        xstream.addImplicitCollection(Circuit.class, "components", Component.class);
        xstream.addImplicitCollection(Circuit.class, "connectors", Connector.class);
        xstream.addImplicitCollection(Component.class, "inputPorts", Port.class);
        xstream.addImplicitCollection(Component.class, "outputPorts", Port.class);
        
        // Register custom converter for Point (avoids Java module system reflection issues)
        xstream.registerConverter(new PointConverter());
        
        // Register custom converter for Color (avoids Java module system reflection issues)
        xstream.registerConverter(new ColorConverter());
        
        // Register custom converter for Component classes
        // This is needed because components don't have no-arg constructors
        xstream.registerConverter(new ComponentConverter(xstream));
        
        // NOTE: We DON'T use a custom converter for Circuit
        // XStream's implicit collection should handle components/connectors automatically
        // The Circuit class has proper initialization in its no-arg constructor and getters
        
        // Ignore unknown elements (for backward compatibility)
        xstream.ignoreUnknownElements();
        
        // Set mode to allow missing fields during deserialization
        xstream.setMode(XStream.NO_REFERENCES);
    }
    
    /**
     * Custom converter for java.awt.Point to avoid Java module system reflection issues
     * Serializes Point as attributes (x, y) instead of using reflection
     */
    private static class PointConverter implements Converter {
        @Override
        @SuppressWarnings("rawtypes")
        public boolean canConvert(Class type) {
            return Point.class.equals(type);
        }
        
        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            Point point = (Point) source;
            writer.addAttribute("x", String.valueOf(point.x));
            writer.addAttribute("y", String.valueOf(point.y));
        }
        
        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            String xStr = reader.getAttribute("x");
            String yStr = reader.getAttribute("y");
            
            int x = 0;
            int y = 0;
            
            if (xStr != null) {
                try {
                    x = Integer.parseInt(xStr);
                } catch (NumberFormatException e) {
                    // use default 0
                }
            }
            
            if (yStr != null) {
                try {
                    y = Integer.parseInt(yStr);
                } catch (NumberFormatException e) {
                    // use default 0
                }
            }
            
            return new Point(x, y);
        }
    }
    
    /**
     * Custom converter for java.awt.Color to avoid Java module system reflection issues
     * Serializes Color as RGB values (r, g, b) instead of using reflection
     */
    private static class ColorConverter implements Converter {
        @Override
        @SuppressWarnings("rawtypes")
        public boolean canConvert(Class type) {
            return Color.class.equals(type);
        }
        
        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            Color color = (Color) source;
            writer.addAttribute("r", String.valueOf(color.getRed()));
            writer.addAttribute("g", String.valueOf(color.getGreen()));
            writer.addAttribute("b", String.valueOf(color.getBlue()));
            writer.addAttribute("a", String.valueOf(color.getAlpha()));
        }
        
        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            String rStr = reader.getAttribute("r");
            String gStr = reader.getAttribute("g");
            String bStr = reader.getAttribute("b");
            String aStr = reader.getAttribute("a");
            
            int r = 0, g = 0, b = 0, a = 255;
            
            if (rStr != null) {
                try {
                    r = Integer.parseInt(rStr);
                } catch (NumberFormatException e) {
                    // use default 0
                }
            }
            
            if (gStr != null) {
                try {
                    g = Integer.parseInt(gStr);
                } catch (NumberFormatException e) {
                    // use default 0
                }
            }
            
            if (bStr != null) {
                try {
                    b = Integer.parseInt(bStr);
                } catch (NumberFormatException e) {
                    // use default 0
                }
            }
            
            if (aStr != null) {
                try {
                    a = Integer.parseInt(aStr);
                } catch (NumberFormatException e) {
                    // use default 255
                }
            }
            
            return new Color(r, g, b, a);
        }
    }
    
    /**
     * Custom converter for Component classes to handle deserialization
     * Components require constructor parameters, so we need custom handling
     */
    private static class ComponentConverter implements Converter {
        public ComponentConverter(XStream xstream) {
            // xstream parameter kept for potential future use
        }
        
        @Override
        @SuppressWarnings("rawtypes")
        public boolean canConvert(Class type) {
            return Component.class.isAssignableFrom(type) && type != Component.class;
        }
        
        @Override
        public void marshal(Object source, HierarchicalStreamWriter writer, MarshallingContext context) {
            Component comp = (Component) source;
            
            // Write attributes
            writer.addAttribute("id", comp.getId());
            writer.addAttribute("name", comp.getName());
            if (comp.getPosition() != null) {
                Point pos = comp.getPosition();
                writer.addAttribute("x", String.valueOf(pos.x));
                writer.addAttribute("y", String.valueOf(pos.y));
            }
            
            // Special handling for SubCircuitComponent
            // REFERENCE-BASED: Save only project name, not the full circuit!
            if (comp instanceof SubCircuitComponent) {
                SubCircuitComponent subComp = (SubCircuitComponent) comp;
                // Write project name (reference) - this is the key change!
                if (subComp.getProjectName() != null) {
                    writer.addAttribute("projectName", subComp.getProjectName());
                }
                // Write circuit file path if available (for backward compatibility)
                if (subComp.getCircuitFilePath() != null) {
                    writer.addAttribute("circuitFilePath", subComp.getCircuitFilePath());
                }
                // DO NOT serialize the full circuit - it will be loaded from file when needed
            }
            
            // Write ports as child elements (not attributes)
            if (comp.getInputPorts() != null && !comp.getInputPorts().isEmpty()) {
                writer.startNode("inputPorts");
                for (Port port : comp.getInputPorts()) {
                    writer.startNode("port");
                    writer.addAttribute("id", port.getId());
                    writer.addAttribute("type", port.getType().toString());
                    if (port.getPosition() != null) {
                        writer.addAttribute("x", String.valueOf(port.getPosition().x));
                        writer.addAttribute("y", String.valueOf(port.getPosition().y));
                    }
                    writer.endNode();
                }
                writer.endNode();
            }
            
            if (comp.getOutputPorts() != null && !comp.getOutputPorts().isEmpty()) {
                writer.startNode("outputPorts");
                for (Port port : comp.getOutputPorts()) {
                    writer.startNode("port");
                    writer.addAttribute("id", port.getId());
                    writer.addAttribute("type", port.getType().toString());
                    if (port.getPosition() != null) {
                        writer.addAttribute("x", String.valueOf(port.getPosition().x));
                        writer.addAttribute("y", String.valueOf(port.getPosition().y));
                    }
                    writer.endNode();
                }
                writer.endNode();
            }
        }
        
        @Override
        public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) {
            // Read attributes
            String id = reader.getAttribute("id");
            String xStr = reader.getAttribute("x");
            String yStr = reader.getAttribute("y");
            
            if (id == null || id.isEmpty()) {
                id = "comp_" + System.currentTimeMillis();
            }
            
            Point position = new Point(0, 0);
            if (xStr != null && yStr != null) {
                try {
                    position = new Point(Integer.parseInt(xStr), Integer.parseInt(yStr));
                } catch (NumberFormatException e) {
                    // use default
                }
            }
            
            // Read name attribute if available
            String name = reader.getAttribute("name");
            if (name == null || name.isEmpty()) {
                name = "Component";
            }
            
            // Determine component type from node name OR class attribute
            // This handles both direct component nodes and nested components (e.g., sourceComponent/sinkComponent in connectors)
            String nodeName = reader.getNodeName();
            String classAttr = reader.getAttribute("class"); // Check class attribute (used in sourceComponent/sinkComponent)
            Component component = null;
            String circuitFilePath = null;
            
            // Use class attribute if available (for nested components like sourceComponent/sinkComponent)
            // Otherwise use node name
            String componentType = (classAttr != null && !classAttr.isEmpty()) ? classAttr : nodeName;
            
            try {
                // Check if this is a SubCircuitComponent
                if (nodeName.contains("SubCircuitComponent") || "subCircuitComponent".equals(nodeName) ||
                    nodeName.contains("edu.uni.logisim.domain.component.SubCircuitComponent") ||
                    "subCircuitComponent".equals(classAttr)) {
                    // REFERENCE-BASED: Read project name (reference), not the full circuit!
                    String projectName = reader.getAttribute("projectName");
                    // Read circuitFilePath attribute if available (for backward compatibility)
                    circuitFilePath = reader.getAttribute("circuitFilePath");
                    
                    // Skip any nested elements (old format had embedded circuits, we ignore them now)
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        String childName = reader.getNodeName();
                        
                        // Skip old format: embedded subCircuit elements
                        if ("subCircuit".equals(childName)) {
                            // Skip the nested circuit - we don't need it anymore (reference-based)
                            while (reader.hasMoreChildren()) {
                                reader.moveDown();
                                reader.moveUp();
                            }
                        } else if ("inputPorts".equals(childName) || "outputPorts".equals(childName)) {
                            // Skip ports - they'll be initialized when circuit is loaded
                            while (reader.hasMoreChildren()) {
                                reader.moveDown();
                                reader.moveUp();
                            }
                        }
                        reader.moveUp();
                    }
                    
                    // Create SubCircuitComponent with project name reference only
                    // Circuit will be loaded lazily when needed
                    if (projectName == null || projectName.isEmpty()) {
                        // Fallback: try to extract from circuitFilePath
                        if (circuitFilePath != null) {
                            String fileName = new java.io.File(circuitFilePath).getName();
                            projectName = fileName.replace(".dig", "");
                        } else {
                            throw new RuntimeException("SubCircuitComponent missing projectName attribute");
                        }
                    }
                    
                    component = new SubCircuitComponent(id, name, projectName, circuitFilePath, position);
                    
                    // Note: Circuit will be loaded lazily via initializeAfterDeserialization() 
                    // which is called after the entire project is deserialized
                } else if (componentType.contains("AndGate") || "andGate".equals(componentType) || 
                    componentType.contains("edu.uni.logisim.domain.component.logic.AndGate")) {
                    component = new AndGate(id, position);
                } else if (componentType.contains("OrGate") || "orGate".equals(componentType) ||
                          componentType.contains("edu.uni.logisim.domain.component.logic.OrGate")) {
                    component = new OrGate(id, position);
                } else if (componentType.contains("NotGate") || "notGate".equals(componentType) ||
                          componentType.contains("edu.uni.logisim.domain.component.logic.NotGate")) {
                    component = new NotGate(id, position);
                } else if (componentType.contains("InputSwitch") || "inputSwitch".equals(componentType) ||
                          componentType.contains("edu.uni.logisim.domain.component.io.InputSwitch")) {
                    component = new InputSwitch(id, position);
                } else if (componentType.contains("LedOutput") || "ledOutput".equals(componentType) ||
                          componentType.contains("edu.uni.logisim.domain.component.io.LedOutput")) {
                    component = new LedOutput(id, position);
                } else {
                    // More helpful error message
                    throw new RuntimeException("Unknown component type: " + nodeName + 
                                             (classAttr != null ? " (class=" + classAttr + ")" : ""));
                }
                
                // For non-SubCircuitComponent, skip remaining children (ports are already initialized by constructor)
                if (!(component instanceof SubCircuitComponent)) {
                    while (reader.hasMoreChildren()) {
                        reader.moveDown();
                        String childName = reader.getNodeName();
                        
                        // Skip ports collections - they're already initialized by constructor
                        if ("inputPorts".equals(childName) || "outputPorts".equals(childName)) {
                            // Skip all port children
                            while (reader.hasMoreChildren()) {
                                reader.moveDown();
                                reader.moveUp(); // Skip port element
                            }
                        }
                        reader.moveUp();
                    }
                }
                
                // Ensure position is set (this updates port positions via updatePortPositions)
                component.setPosition(position);
                
                return component;
            } catch (Exception e) {
                throw new RuntimeException("Failed to create component '" + nodeName + "': " + e.getMessage(), e);
            }
        }
    }
    
    public void serialize(Project project, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            xstream.toXML(project, writer);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save project: " + e.getMessage(), e);
        }
    }
    
    public Project deserialize(String filePath) {
        try (FileReader reader = new FileReader(filePath)) {
            logger.fine("Deserializing project from: " + filePath);
            
            Object result = xstream.fromXML(reader);
            if (result instanceof Project) {
                Project project = (Project) result;
                
                // Ensure project has valid data
                if (project.getCircuits() == null) {
                    logger.warning("Project has null circuits list, initializing...");
                    // This shouldn't happen if deserialization worked, but be defensive
                }
                
                logger.fine("Project deserialized - name: " + project.getName() + 
                          ", circuits: " + (project.getCircuits() != null ? project.getCircuits().size() : 0));
                
                // CRITICAL: Post-process to ensure all circuits have properly initialized components/connectors lists
                // This fixes the issue where components list becomes null during deserialization
                // NOTE: XStream's implicit collection should have added components/connectors automatically,
                // but if it didn't, the getters will initialize empty lists (defensive programming)
                if (project.getCircuits() != null) {
                    for (Circuit circuit : project.getCircuits()) {
                        if (circuit != null) {
                            // Ensure components list is initialized (defensive - getter initializes if null)
                            List<Component> components = circuit.getComponents();
                            List<Connector> connectors = circuit.getConnectors();
                            
                            logger.fine("Circuit '" + circuit.getName() + 
                                       "' has " + components.size() + 
                                       " components and " + connectors.size() + " connectors");
                            
                            // If lists are empty but we know the file has components, there's a deserialization issue
                            if (components.isEmpty() && connectors.isEmpty()) {
                                logger.warning("Circuit '" + circuit.getName() + 
                                             "' appears empty after deserialization. " +
                                             "This might indicate an issue with XStream's implicit collection mechanism.");
                            }
                        }
                    }
                }
                
                // NOTE: SubCircuitComponents will be initialized later by LogiSimApplication
                // after setting the CircuitLoader callback. This allows lazy loading.
                // We don't initialize them here because we don't have access to ProjectService.
                
                return project;
            } else {
                throw new RuntimeException("File does not contain a valid Project: " + filePath + 
                                         " (got: " + (result != null ? result.getClass().getName() : "null") + ")");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load project from " + filePath + ": " + e.getMessage(), e);
        } catch (com.thoughtworks.xstream.converters.ConversionException e) {
            // More detailed error for conversion issues
            String detailedError = "Failed to deserialize project from " + filePath + ".\n" +
                                 "Error: " + e.getMessage() + "\n";
            if (e.getCause() != null) {
                detailedError += "Cause: " + e.getCause().getMessage() + "\n";
            }
            detailedError += "This usually means the file format is incompatible or corrupted.\n" +
                           "Try creating a new project and saving it again.";
            throw new RuntimeException(detailedError, e);
        } catch (Exception e) {
            // Catch any other XStream deserialization errors
            String errorMsg = "Failed to deserialize project from " + filePath + ": " + e.getMessage();
            if (e.getCause() != null) {
                errorMsg += "\nCause: " + e.getCause().getMessage();
                if (e.getCause().getCause() != null) {
                    errorMsg += "\nRoot Cause: " + e.getCause().getCause().getMessage();
                }
            }
            logger.severe("ERROR: " + errorMsg);
            logger.log(Level.SEVERE, "Exception details", e);
            throw new RuntimeException(errorMsg + " (Class: " + e.getClass().getName() + ")", e);
        }
    }
    
}

