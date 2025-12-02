package edu.uni.logisim.export;

import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.component.Component;
import edu.uni.logisim.domain.connector.Connector;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Handles the export of circuit diagrams (export to PNG/JPEG)
 */
public class DiagramExporter {
    
    /**
     * Export circuit diagram to PNG format
     */
    public void exportToPNG(Circuit circuit, String filePath) throws IOException {
        exportToImage(circuit, filePath, "PNG");
    }
    
    /**
     * Export circuit diagram to JPEG format
     */
    public void exportToJPEG(Circuit circuit, String filePath) throws IOException {
        exportToImage(circuit, filePath, "JPEG");
    }
    
    private void exportToImage(Circuit circuit, String filePath, String format) throws IOException {
        // Calculate bounds
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        
        for (Component component : circuit.getComponents()) {
            Point pos = component.getPosition();
            if (pos != null) {
                minX = Math.min(minX, pos.x);
                minY = Math.min(minY, pos.y);
                maxX = Math.max(maxX, pos.x + 60);
                maxY = Math.max(maxY, pos.y + 40);
            }
        }
        
        int width = Math.max(800, maxX - minX + 100);
        int height = Math.max(600, maxY - minY + 100);
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        // White background
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        
        // Enable anti-aliasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw components
        g.setColor(Color.BLACK);
        for (Component component : circuit.getComponents()) {
            drawComponent(g, component, minX, minY);
        }
        
        // Draw connectors
        for (Connector connector : circuit.getConnectors()) {
            drawConnector(g, connector, minX, minY);
        }
        
        g.dispose();
        
        // Write to file
        File outputFile = new File(filePath);
        ImageIO.write(image, format, outputFile);
    }
    
    private void drawComponent(Graphics2D g, Component component, int offsetX, int offsetY) {
        Point pos = component.getPosition();
        if (pos == null) return;
        
        int x = pos.x - offsetX + 50;
        int y = pos.y - offsetY + 50;
        
        // Draw component rectangle
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(x, y, 60, 40);
        g.setColor(Color.BLACK);
        g.drawRect(x, y, 60, 40);
        
        // Draw component name
        g.drawString(component.getName(), x + 5, y + 25);
        
        // Draw ports
        g.setColor(Color.RED);
        for (edu.uni.logisim.domain.connector.Port port : component.getInputPorts()) {
            Point portPos = port.getPosition();
            if (portPos != null) {
                int px = portPos.x - offsetX + 50;
                int py = portPos.y - offsetY + 50;
                g.fillOval(px - 3, py - 3, 6, 6);
            }
        }
        
        g.setColor(Color.BLUE);
        for (edu.uni.logisim.domain.connector.Port port : component.getOutputPorts()) {
            Point portPos = port.getPosition();
            if (portPos != null) {
                int px = portPos.x - offsetX + 50;
                int py = portPos.y - offsetY + 50;
                g.fillOval(px - 3, py - 3, 6, 6);
            }
        }
    }
    
    private void drawConnector(Graphics2D g, Connector connector, int offsetX, int offsetY) {
        Point start = connector.getStartPosition();
        Point end = connector.getEndPosition();
        
        if (start == null || end == null) return;
        
        int x1 = start.x - offsetX + 50;
        int y1 = start.y - offsetY + 50;
        int x2 = end.x - offsetX + 50;
        int y2 = end.y - offsetY + 50;
        
        g.setColor(connector.getValue() ? Color.GREEN : Color.BLACK);
        g.setStroke(new BasicStroke(2));
        g.drawLine(x1, y1, x2, y2);
    }
}

