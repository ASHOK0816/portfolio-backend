package com.ashuu.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.ashuu.model.Experience;
import com.ashuu.repository.ExperienceRepository;
import com.ashuu.service.ExperienceService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExperienceServiceImpl implements ExperienceService {

	private final ExperienceRepository repository;

	@Override
	public Experience createExperience(Experience experience) {
		return repository.save(experience);
	}

	@Override
	public List<Experience> getAllExperiences() {
		return repository.findAll();
	}

	@Override
	public Experience getExperienceById(Long id) {
		return repository.findById(id).orElseThrow(() -> new RuntimeException("Experience not found"));
	}

	@Override
	public Experience updateExperience(Long id, Experience exp) {
		Experience existing = getExperienceById(id);

		existing.setTitle(exp.getTitle());
		existing.setCompany(exp.getCompany());
		existing.setLocation(exp.getLocation());
		existing.setStartDate(exp.getStartDate());
		existing.setEndDate(exp.getEndDate());
		existing.setDescription(exp.getDescription());

		return repository.save(existing);
	}

	@Override
	public void deleteExperience(Long id) {
		repository.deleteById(id);
	}
}
