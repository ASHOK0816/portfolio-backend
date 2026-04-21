package com.ashuu.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.ashuu.model.Skill;

public interface SkillService {

    Skill getSkillById(Long id);

	Skill addSkill(String name, MultipartFile image);

	Skill updateSkill(Long id, String name);

	Skill updateSkillImage(Long id, MultipartFile image);

    void deleteSkill(Long id);

    List<Skill> getAllSkills();

    Page<Skill> getSkillsWithPagination(int page, int size);

	void reorderSkills(List<Long> skillIds);
}