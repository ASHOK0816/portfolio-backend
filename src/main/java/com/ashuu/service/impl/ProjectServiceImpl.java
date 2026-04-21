   package com.ashuu.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import com.ashuu.repository.ProjectRepository;
import com.ashuu.service.ProjectService;
import com.ashuu.model.Project;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository ;

    public ProjectServiceImpl(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository ;
    }

    @Override
    public List<Project> getAllProjects() {
        return projectRepository.findAll();
    }
    
    @Override
    public Project getProjectById(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Project not found with id: " + id));
    }

    @Override
    public Project addProject(Project project) {
        return projectRepository.save(project);
    }

    @Override
    public Project updateProject(Long id, Project project) {

        Project existing = projectRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Project not found"));

        if (project.getTitle() != null)
            existing.setTitle(project.getTitle());

        if (project.getTech() != null)
            existing.setTech(project.getTech());

        if (project.getDescription() != null)
            existing.setDescription(project.getDescription());

        if (project.getGithubUrl() != null)
            existing.setGithubUrl(project.getGithubUrl());

        if (project.getImageName() != null)
            existing.setImageName(project.getImageName());

        return projectRepository.save(existing);
    }

    @Override
    public void deleteProject(Long id) {
        Project project = getProjectById(id);
        projectRepository.delete(project);
    }

}