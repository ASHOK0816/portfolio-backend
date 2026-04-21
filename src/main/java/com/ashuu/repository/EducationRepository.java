package com.ashuu.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ashuu.model.Education;

public interface EducationRepository extends JpaRepository<Education, Long>{

	Education findTopByOrderByEndYearDesc();

}
