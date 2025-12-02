package edu.uni.logisim.persistence;

import edu.uni.logisim.domain.project.Project;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of ProjectRepository for file-based storage
 */
public class FileProjectRepository implements ProjectRepository {
    private String baseDirectory;
    
    public FileProjectRepository(String baseDirectory) {
        this.baseDirectory = baseDirectory;
        // Ensure directory exists
        File dir = new File(baseDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    @Override
    public void save(Project project) {
        JsonProjectSerializer serializer = new JsonProjectSerializer();
        String filePath = baseDirectory + File.separator + project.getName() + ".dig";
        serializer.serialize(project, filePath);
    }
    
    @Override
    public Project load(String path) {
        JsonProjectSerializer serializer = new JsonProjectSerializer();
        return serializer.deserialize(path);
    }
    
    @Override
    public void delete(Project project) {
        String filePath = baseDirectory + File.separator + project.getName() + ".dig";
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
    }
    
    @Override
    public List<Project> listAll() {
        List<Project> projects = new ArrayList<>();
        File dir = new File(baseDirectory);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(".dig"));
            if (files != null) {
                for (File file : files) {
                    try {
                        Project project = load(file.getAbsolutePath());
                        projects.add(project);
                    } catch (Exception e) {
                        // Skip files that can't be loaded
                    }
                }
            }
        }
        return projects;
    }
}

