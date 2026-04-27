package com.ashuu.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ashuu.model.Experience;

public interface ExperienceRepository extends JpaRepository<Experience, Long> {

	Experience findTopByOrderByEndDateDesc();
}
