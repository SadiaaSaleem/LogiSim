package edu.uni.logisim.service;

import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.connector.Connector;
import edu.uni.logisim.domain.connector.Port;
import edu.uni.logisim.util.IdGenerator;
import java.awt.Point;

/**
 * Provides services related to circuit manipulation
 */
public class CircuitService {
    
    public Circuit createCircuit(String name) {
        String id = IdGenerator.generateId("circuit");
        return new Circuit(id, name);
    }
    
    public void addComponent(Circuit circuit, Component component) {
        circuit.addComponent(component);
    }
    
    public void removeComponent(Circuit circuit, Component component) {
        circuit.removeComponent(component);
    }
    
    public Connector connectComponents(Circuit circuit, 
                                       Component source, Port sourcePort,
                                       Component sink, Port sinkPort) {
        if (circuit == null || source == null || sink == null || sourcePort == null || sinkPort == null) {
            System.out.println("ERROR: connectComponents called with null parameter");
            return null;
        }
        
        // Check if connection already exists
        for (Connector existing : circuit.getConnectors()) {
            if (existing.getSourcePort() == sourcePort && existing.getSinkPort() == sinkPort) {
                System.out.println("Connection already exists");
                return existing; // Connection already exists
            }
        }
        
        String connectorId = IdGenerator.generateId("conn");
        Connector connector = new Connector(connectorId, sourcePort, sinkPort, source, sink);
        
        // ALWAYS set positions from ports (even if null, we'll calculate them)
        Point startPos = sourcePort.getPosition();
        Point endPos = sinkPort.getPosition();
        
        // If port positions are null, calculate from component positions
        if (startPos == null && source.getPosition() != null) {
            Point sourcePos = source.getPosition();
            startPos = new Point(sourcePos.x + 60, sourcePos.y + 20);
            sourcePort.setPosition(startPos);
        }
        if (endPos == null && sink.getPosition() != null) {
            Point sinkPos = sink.getPosition();
            endPos = new Point(sinkPos.x, sinkPos.y + 20);
            sinkPort.setPosition(endPos);
        }
        
        // Set positions on connector
        if (startPos != null) {
            connector.setStartPosition(new Point(startPos));
        }
        if (endPos != null) {
            connector.setEndPosition(new Point(endPos));
        }
        
        // CRITICAL: Add connector to circuit
        circuit.addConnector(connector);
        System.out.println("Connector created and added. Total connectors: " + circuit.getConnectors().size());
        return connector;
    }
    
    public void disconnectComponents(Circuit circuit, Connector connector) {
        circuit.removeConnector(connector);
    }
    
    public Component findComponentAt(Circuit circuit, int x, int y) {
        // Check if click is within component bounds (components are 60x40 pixels)
        for (Component component : circuit.getComponents()) {
            Point pos = component.getPosition();
            if (pos != null) {
                // Check if click is within component rectangle
                if (x >= pos.x && x <= pos.x + 60 && y >= pos.y && y <= pos.y + 40) {
                    return component;
                }
            }
        }
        return null;
    }
}

