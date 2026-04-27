package com.ashuu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ashuu.model.Experience;
import com.ashuu.service.ExperienceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/experience")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ExperienceController {

	private final ExperienceService service;

	@PostMapping
	public Experience create(@RequestBody Experience experience) {
		return service.createExperience(experience);
	}

	@GetMapping
	public List<Experience> getAll() {
		return service.getAllExperiences();
	}

	@GetMapping("/{id}")
	public Experience getById(@PathVariable Long id) {
		return service.getExperienceById(id);
	}

	@PutMapping("/{id}")
	public Experience update(@PathVariable Long id, @RequestBody Experience experience) {
		return service.updateExperience(id, experience);
	}

	@DeleteMapping("/{id}")
	public String delete(@PathVariable Long id) {
		service.deleteExperience(id);
		return "Deleted successfully";
	}
}
