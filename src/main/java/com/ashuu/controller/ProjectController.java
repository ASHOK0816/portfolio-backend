package com.ashuu.controller;

import java.io.IOException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ashuu.model.FileData;
import com.ashuu.model.Project;
import com.ashuu.repository.ProjectRepository;
import com.ashuu.service.FileStorageService;
import com.ashuu.service.ProjectService;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

	private final ProjectService service;
    private final ProjectRepository projectRepo;
	private final FileStorageService fileStorageService;

	public ProjectController(ProjectService service, ProjectRepository projectRepo,
			FileStorageService fileStorageService) {
        this.service = service;
        this.projectRepo = projectRepo;
		this.fileStorageService = fileStorageService;
    }

	// 🔹 GET project by ID
    @GetMapping("/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getProjectById(id));
    }

	// 🔹 ADD project
	@PostMapping(consumes = "multipart/form-data")
	public ResponseEntity<Project> addProject(@RequestParam String title, @RequestParam String tech,
			@RequestParam String description, @RequestParam String githubUrl,
			@RequestParam(required = false) MultipartFile image) throws IOException {

		Project project = new Project();

		project.setTitle(title);
		project.setTech(tech);
		project.setDescription(description);
		project.setGithubUrl(githubUrl);

		if (image != null && !image.isEmpty()) {

			FileData fileData = fileStorageService.uploadFile(image, "projects");

			project.setImageName(fileData.getFileUrl());
		}

		return ResponseEntity.ok(projectRepo.save(project));
    }

    // 🔹 UPDATE project
	@PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
			@RequestParam String title, @RequestParam String tech, @RequestParam String description,
			@RequestParam String githubUrl, @RequestParam(required = false) MultipartFile image) throws IOException {

		Project project = service.getProjectById(id);

		project.setTitle(title);
		project.setTech(tech);
		project.setDescription(description);
		project.setGithubUrl(githubUrl);

		if (image != null && !image.isEmpty()) {

			// delete old image
			if (project.getImageName() != null) {
				fileStorageService.deleteFile(project.getImageName());
			}

			FileData fileData = fileStorageService.uploadFile(image, "projects");

			project.setImageName(fileData.getFileUrl());
		}

		return ResponseEntity.ok(projectRepo.save(project));
    }

	// 🔹 DELETE project
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProject(@PathVariable Long id) {

        Project project = service.getProjectById(id);

        if (project.getImageName() != null) {
			fileStorageService.deleteFile(project.getImageName());
        }

        service.deleteProject(id);

		return ResponseEntity.ok("Project deleted successfully");
    }

	// 🔹 GET all or paginated
    @GetMapping
    public ResponseEntity<?> getProjects(
            @RequestParam(required = false) Integer page,
			@RequestParam(required = false) Integer size) {

        if (page != null && size != null) {

            Page<Project> projects = projectRepo.findAll(
                    PageRequest.of(page, size, Sort.by("id").descending())
            );

            return ResponseEntity.ok(projects);
		}

		return ResponseEntity.ok(service.getAllProjects());
    }
}