package project;

import edu.uni.logisim.domain.circuit.Circuit;
import edu.uni.logisim.domain.component.logic.AndGate;
import edu.uni.logisim.domain.component.io.InputSwitch;
import edu.uni.logisim.domain.component.io.LedOutput;
import edu.uni.logisim.domain.project.Project;
import edu.uni.logisim.persistence.FileProjectRepository;
import edu.uni.logisim.persistence.ProjectRepository;
import edu.uni.logisim.service.ProjectService;
import org.junit.Test;
import static org.junit.Assert.*;
import java.awt.Point;
import java.io.File;

/**
 * Test for Project save and load operations
 */
public class ProjectSaveLoadTest {
    
    @Test
    public void testSaveAndLoadProject() {
        // Create a test project
        String projectName = "TestProject";
        String projectPath = "test_projects/TestProject.dig";
        
        Project project = new Project(projectName, projectPath);
        
        // Create a circuit with some components
        Circuit circuit = new Circuit("circuit1", "Test Circuit");
        circuit.addComponent(new InputSwitch("input1", new Point(0, 0)));
        circuit.addComponent(new AndGate("and1", new Point(100, 0)));
        circuit.addComponent(new LedOutput("led1", new Point(200, 0)));
        
        project.addCircuit(circuit);
        
        // Save project
        ProjectRepository repository = new FileProjectRepository("test_projects");
        ProjectService service = new ProjectService(repository);
        
        try {
            service.saveProject(project);
            
            // Verify file was created
            File projectFile = new File(projectPath);
            assertTrue("Project file should exist", projectFile.exists());
            
            // Load project
            Project loadedProject = service.loadProject(projectPath);
            
            assertNotNull("Loaded project should not be null", loadedProject);
            assertEquals("Project name should match", projectName, loadedProject.getName());
            assertEquals("Project path should match", projectPath, loadedProject.getPath());
            assertNotNull("Project circuits should not be null", loadedProject.getCircuits());
            assertEquals("Project should have 1 circuit", 1, loadedProject.getCircuits().size());
            
            Circuit loadedCircuit = loadedProject.getCircuits().get(0);
            assertNotNull("Loaded circuit should not be null", loadedCircuit);
            assertEquals("Circuit name should match", "Test Circuit", loadedCircuit.getName());
            assertNotNull("Circuit components should not be null", loadedCircuit.getComponents());
            assertEquals("Circuit should have 3 components", 3, loadedCircuit.getComponents().size());
            
        } finally {
            // Cleanup
            File projectFile = new File(projectPath);
            if (projectFile.exists()) {
                projectFile.delete();
            }
        }
    }
    
    @Test
    public void testLoadNonExistentProject() {
        ProjectRepository repository = new FileProjectRepository("test_projects");
        ProjectService service = new ProjectService(repository);
        
        Project loadedProject = service.loadProject("test_projects/NonExistent.dig");
        
        // Should return null or throw exception - depends on implementation
        // For now, we'll just verify it doesn't crash and returns null
        assertNull("Loading non-existent project should return null", loadedProject);
    }
}

