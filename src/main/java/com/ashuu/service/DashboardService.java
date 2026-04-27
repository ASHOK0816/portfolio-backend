package com.ashuu.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ashuu.dto.DashboardResponse;
import com.ashuu.dto.EducationDTO;
import com.ashuu.dto.ExperienceDTO;
import com.ashuu.dto.ResumeDTO;
import com.ashuu.repository.EducationRepository;
import com.ashuu.repository.ExperienceRepository;
import com.ashuu.repository.MessageRepository;
import com.ashuu.repository.ProjectRepository;
import com.ashuu.repository.ResumeRepository;
import com.ashuu.repository.SkillRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DashboardService {

	private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final MessageRepository messageRepository;
	private final EducationRepository educationRepository;
	private final ExperienceRepository experienceRepository;
	private final ResumeRepository resumeRepository;

	public DashboardResponse getDashboard() {

		var latestEdu = educationRepository.findTopByOrderByEndYearDesc();
		var latestExp = experienceRepository.findTopByOrderByEndDateDesc();
		var resume = resumeRepository.findTopByOrderByUploadedAtDesc();

		List<ExperienceDTO> experienceList = experienceRepository.findAll().stream()
				.map(exp -> ExperienceDTO.builder().id(exp.getId()).title(exp.getTitle()).company(exp.getCompany())
						.startDate(exp.getStartDate()).endDate(exp.getEndDate()).description(exp.getDescription())
						.build())
				.collect(Collectors.toList());

		return DashboardResponse.builder().totalProjects(projectRepository.count()).totalSkills(skillRepository.count())
				.totalMessages(messageRepository.count())

				.latestEducation(
						latestEdu != null
								? EducationDTO.builder().degree(latestEdu.getDegree()).college(latestEdu.getCollege())
										.endYear(latestEdu.getEndYear()).build()
								: null)

				.latestExperience(
						latestExp != null
								? ExperienceDTO.builder().id(latestExp.getId()).title(latestExp.getTitle())
										.company(latestExp.getCompany()).startDate(latestExp.getStartDate())
										.endDate(latestExp.getEndDate()).description(latestExp.getDescription()).build()
								: null)

				.resume(resume != null
						? ResumeDTO.builder().fileName(resume.getFileName()).url(resume.getFilePath()).build()
						: null)

				.experiences(experienceList).build();
	}
}