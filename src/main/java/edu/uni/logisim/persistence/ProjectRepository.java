package edu.uni.logisim.persistence;

import edu.uni.logisim.domain.project.Project;
import java.util.List;

/**
 * Interface for data access operations related to projects
 */
public interface ProjectRepository {
    void save(Project project);
    Project load(String path);
    void delete(Project project);
    List<Project> listAll();
}

