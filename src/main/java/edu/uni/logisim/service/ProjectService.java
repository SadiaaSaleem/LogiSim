package edu.uni.logisim.service;

import edu.uni.logisim.domain.project.Project;
import edu.uni.logisim.persistence.ProjectRepository;
import java.util.List;

/**
 * Provides services related to project management.
 * 
 * <p>This service acts as a facade for project operations, delegating
 * persistence operations to the ProjectRepository.
 * 
 * <p>Operations include:
 * <ul>
 *   <li>Creating new projects</li>
 *   <li>Saving projects to disk</li>
 *   <li>Loading projects from disk</li>
 *   <li>Listing all projects</li>
 *   <li>Deleting projects</li>
 * </ul>
 * 
 * @author LogiSim Development Team
 * @version 1.0
 */
public class ProjectService {
    private ProjectRepository repository;
    
    /**
     * Creates a new ProjectService with the specified repository.
     * 
     * @param repository the repository to use for persistence operations
     */
    public ProjectService(ProjectRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Creates a new project with the specified name and path.
     * 
     * @param name the name of the project
     * @param path the file system path where the project will be stored
     * @return the newly created project
     */
    public Project createProject(String name, String path) {
        Project project = new Project(name, path);
        return project;
    }
    
    /**
     * Saves a project to disk using the repository.
     * 
     * @param project the project to save
     */
    public void saveProject(Project project) {
        repository.save(project);
    }
    
    /**
     * Loads a project from disk using the repository.
     * 
     * @param path the file system path of the project to load
     * @return the loaded project, or null if loading fails
     */
    public Project loadProject(String path) {
        return repository.load(path);
    }
    
    /**
     * Lists all projects available in the repository.
     * 
     * @return a list of all projects
     */
    public List<Project> listProjects() {
        return repository.listAll();
    }
    
    /**
     * Deletes a project from the repository.
     * 
     * @param project the project to delete
     */
    public void deleteProject(Project project) {
        repository.delete(project);
    }
}

