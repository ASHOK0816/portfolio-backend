package com.ashuu.service;

import java.util.List;
import com.ashuu.model.Project;

public interface ProjectService {

	List<Project> getAllProjects();
	
	Project getProjectById(Long id);

    Project addProject(Project project);

    Project updateProject(Long id, Project project);

    void deleteProject(Long id);
}