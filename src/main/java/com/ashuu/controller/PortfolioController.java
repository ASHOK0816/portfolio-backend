package com.ashuu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ashuu.model.Project;
import com.ashuu.repository.ProjectRepository;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final ProjectRepository repo;

    public PortfolioController(ProjectRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/projects")
    public List<Project> getProjects() {
        return repo.findAll();
    }
}

