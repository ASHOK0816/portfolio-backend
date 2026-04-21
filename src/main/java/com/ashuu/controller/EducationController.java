package com.ashuu.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ashuu.model.Education;
import com.ashuu.repository.EducationRepository;

@RestController
@RequestMapping("/api/education")
public class EducationController {

	@Autowired
	private EducationRepository educationRepository;

	// GET all education records
	@GetMapping
	public List<Education> getAllEducation() {
		return educationRepository.findAll();
    }

	// GET single education record
	@GetMapping("/{id}")
	public Education getEducationById(@PathVariable Long id) {
		return educationRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Education not found with id " + id));
    }

	// POST (Add education)
    @PostMapping
	public Education addEducation(@RequestBody Education edu) {

		// Handle pursuing logic
		if (edu.getPursuing() != null && edu.getPursuing()) {
			edu.setEndYear("Pursuing");
		}

		// Append % if gradeType is Percentage
		if ("Percentage".equalsIgnoreCase(edu.getGradeType()) && edu.getGrade() != null) {
			if (!edu.getGrade().endsWith("%")) {
				edu.setGrade(edu.getGrade() + " %");
			}
		}

		return educationRepository.save(edu);
	}

	// PUT (Update education)
	@PutMapping("/{id}")
	public Education updateEducation(@PathVariable Long id, @RequestBody Education eduDetails) {
		Education edu = educationRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Education not found with id " + id));

		edu.setDegree(eduDetails.getDegree());
		edu.setCollege(eduDetails.getCollege());
		edu.setStartYear(eduDetails.getStartYear());
		edu.setEndYear(
				eduDetails.getPursuing() != null && eduDetails.getPursuing() ? "Pursuing" : eduDetails.getEndYear());
		edu.setPursuing(eduDetails.getPursuing());
		edu.setGradeType(eduDetails.getGradeType());

		String grade = eduDetails.getGrade();
		if ("Percentage".equalsIgnoreCase(eduDetails.getGradeType()) && grade != null && !grade.endsWith("%")) {
			grade += " %";
		}
		edu.setGrade(grade);

		return educationRepository.save(edu);
    }

	// DELETE education
    @DeleteMapping("/{id}")
	public String deleteEducation(@PathVariable Long id) {
		educationRepository.deleteById(id);
		return "Education deleted successfully";
    }
}