package com.ashuu.service;

import java.util.List;

import com.ashuu.model.Experience;

public interface ExperienceService {

	Experience createExperience(Experience experience);

	List<Experience> getAllExperiences();

	Experience getExperienceById(Long id);

	Experience updateExperience(Long id, Experience experience);

	void deleteExperience(Long id);
}
