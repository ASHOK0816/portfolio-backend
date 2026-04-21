package com.ashuu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ashuu.model.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {

	List<Skill> findAllByOrderByDisplayOrderAsc();

}