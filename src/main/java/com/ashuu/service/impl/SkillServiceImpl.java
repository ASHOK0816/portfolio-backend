package com.ashuu.service.impl;

import java.io.File;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.ashuu.model.FileData;
import com.ashuu.model.Skill;
import com.ashuu.repository.SkillRepository;
import com.ashuu.service.FileStorageService;
import com.ashuu.service.SkillService;

@Service
@Transactional
public class SkillServiceImpl implements SkillService {

    private final SkillRepository repository;
	private final FileStorageService fileStorageService;

	public SkillServiceImpl(SkillRepository repository, FileStorageService fileStorageService) {
        this.repository = repository;
		this.fileStorageService = fileStorageService;
    }

	// Get Skill by ID
    @Override
    public Skill getSkillById(Long id) {
        return repository.findById(id)
				.orElseThrow(() -> new RuntimeException("Skill not found with id: " + id));
    }

	// Add Skill
    @Override
	public Skill addSkill(String name, MultipartFile image) {

		if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Skill name cannot be empty");
        }

		if (image == null || image.isEmpty()) {
			throw new RuntimeException("Skill image cannot be empty");
		}

		try {

			FileData fileData = fileStorageService.uploadFile(image, "skills");

			Skill skill = new Skill();
			skill.setName(name);
			skill.setImage(fileData.getFilePath());
			skill.setDisplayOrder((int) repository.count() + 1);

			return repository.save(skill);

		} catch (Exception e) {
			throw new RuntimeException("Skill image upload failed: " + e.getMessage());
		}
    }

	// Update Skill Name
    @Override
	public Skill updateSkill(Long id, String name) {

        Skill existing = getSkillById(id);

		if (name == null || name.trim().isEmpty()) {
			throw new RuntimeException("Skill name cannot be empty");
        }

		existing.setName(name);

		return repository.save(existing);
	}

	// Upload / Update Skill Image
	@Override
	public Skill updateSkillImage(Long id, MultipartFile image) {

		Skill skill = getSkillById(id);

		if (image == null || image.isEmpty()) {
			throw new RuntimeException("Image file is empty");
		}

		try {

			// delete old image if exists
			if (skill.getImage() != null) {
				File oldFile = new File(skill.getImage());
				if (oldFile.exists()) {
					oldFile.delete();
				}
			}

			// upload new image
			FileData fileData = fileStorageService.uploadFile(image, "skills");

			skill.setImage(fileData.getFilePath());

			return repository.save(skill);

		} catch (Exception e) {
			throw new RuntimeException("Image upload failed: " + e.getMessage());
        }
    }

	// Delete Skill
    @Override
    public void deleteSkill(Long id) {

        Skill existing = getSkillById(id);

		if (existing.getImage() != null) {
			File file = new File(existing.getImage());
			if (file.exists()) {
				file.delete();
			}
		}

        repository.delete(existing);
    }

	// Get All Skills
    @Override
    public List<Skill> getAllSkills() {
		return repository.findAll(Sort.by(Sort.Direction.ASC, "displayOrder"));
    }

	// Pagination
    @Override
    public Page<Skill> getSkillsWithPagination(int page, int size) {
        return repository.findAll(
				PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "displayOrder"))
        );
    }

	// Reorder Skills
	@Override
	public void reorderSkills(List<Long> skillIds) {

		List<Skill> skills = repository.findAllById(skillIds);

		for (int i = 0; i < skills.size(); i++) {
			skills.get(i).setDisplayOrder(i + 1);
		}

		repository.saveAll(skills);
		}

}