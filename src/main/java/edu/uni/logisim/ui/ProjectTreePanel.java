package edu.uni.logisim.ui;

import edu.uni.logisim.app.LogiSimApplication;
import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.project.Project;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;

/**
 * A UI component for displaying the project's hierarchical structure.
 * 
 * <p>This panel displays a tree view of the current project, showing:
 * <ul>
 *   <li>The project name as the root node</li>
 *   <li>All circuits within the project as child nodes</li>
 * </ul>
 * 
 * <p>Users can:
 * <ul>
 *   <li>View the project structure at a glance</li>
 *   <li>Select circuits from the tree to switch between them</li>
 *   <li>See which circuit is currently active (highlighted)</li>
 * </ul>
 * 
 * <p>The tree automatically updates when:
 * <ul>
 *   <li>A new project is opened</li>
 *   <li>A new circuit is created</li>
 *   <li>The current circuit changes</li>
 * </ul>
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class ProjectTreePanel extends JPanel {
    private JTree projectTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private LogiSimApplication application;
    
    public ProjectTreePanel(LogiSimApplication application) {
        this.application = application;
        initializeUI();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Project"));
        
        rootNode = new DefaultMutableTreeNode("Project");
        treeModel = new DefaultTreeModel(rootNode);
        projectTree = new JTree(treeModel);
        projectTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        projectTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) projectTree.getLastSelectedPathComponent();
            if (node != null && node.getUserObject() instanceof Circuit) {
                application.selectCircuit((Circuit) node.getUserObject());
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(projectTree);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void updateProject(Project project) {
        rootNode.removeAllChildren();
        if (project != null) {
            DefaultMutableTreeNode projectNode = new DefaultMutableTreeNode(project.getName());
            rootNode.add(projectNode);
            
            for (Circuit circuit : project.getCircuits()) {
                DefaultMutableTreeNode circuitNode = new DefaultMutableTreeNode(circuit);
                projectNode.add(circuitNode);
            }
        }
        treeModel.reload();
    }
}

