package com.ashuu.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardResponse {

	private long totalProjects;
	private long totalSkills;
	private long totalMessages;

	private EducationDTO latestEducation;
	private ExperienceDTO latestExperience;
	private ResumeDTO resume;

	private List<ExperienceDTO> experiences; // for timeline UI
}