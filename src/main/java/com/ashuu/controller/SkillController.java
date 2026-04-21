package com.ashuu.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ashuu.model.FileData;
import com.ashuu.model.Skill;
import com.ashuu.repository.SkillRepository;
import com.ashuu.service.FileStorageService;

@RestController
@RequestMapping("/api/skills")
public class SkillController {

	private final SkillRepository skillRepository;
	private final FileStorageService fileStorageService;

	public SkillController(SkillRepository skillRepository, FileStorageService fileStorageService) {
		this.skillRepository = skillRepository;
		this.fileStorageService = fileStorageService;
    }

	// Get Skills
	@GetMapping
	public List<Skill> getSkills() {
		return skillRepository.findAllByOrderByDisplayOrderAsc();
    }

	// Create Skill
    @PostMapping
	public Skill createSkill(@RequestParam String name, @RequestParam MultipartFile image,
			@RequestParam String category) throws IOException {

		FileData fileData = fileStorageService.uploadFile(image, "skills");

		Skill skill = new Skill();
		skill.setName(name);
		skill.setImage(fileData.getFileUrl());
		skill.setCategory(category);
		skill.setDisplayOrder((int) skillRepository.count() + 1);

		return skillRepository.save(skill);
    }

	// Update Skill Image
	@PutMapping("/{id}/image")
	public Skill updateSkillImage(
            @PathVariable Long id,
			@RequestParam MultipartFile image, @RequestParam String category) throws IOException {

		Skill skill = skillRepository.findById(id).orElseThrow(() -> new RuntimeException("Skill not found"));

		FileData fileData = fileStorageService.uploadFile(image, "skills");

		skill.setImage(fileData.getFileUrl());

		skill.setCategory(category);

		return skillRepository.save(skill);
    }

	// Delete Skill
    @DeleteMapping("/{id}")
	public void deleteSkill(@PathVariable Long id) {
		skillRepository.deleteById(id);
    }

	// Reorder Skills
	@PutMapping("/reorder")
	public void reorderSkills(@RequestBody List<Long> skillIds) {

		for (int i = 0; i < skillIds.size(); i++) {

			Skill skill = skillRepository.findById(skillIds.get(i)).orElseThrow();

			skill.setDisplayOrder(i + 1);

			skillRepository.save(skill);
        }
    }
}