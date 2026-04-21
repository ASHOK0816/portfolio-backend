package com.ashuu.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ashuu.dto.DashboardStat;
import com.ashuu.repository.EducationRepository;
import com.ashuu.repository.MessageRepository;
import com.ashuu.repository.ProjectRepository;
import com.ashuu.repository.SkillRepository;

@Service
public class DashboardService {

	private final ProjectRepository projectRepository;
    private final SkillRepository skillRepository;
    private final MessageRepository messageRepository;
	private final EducationRepository educationRepository;


    public DashboardService(ProjectRepository projectRepository,
                            SkillRepository skillRepository,
			MessageRepository messageRepository, EducationRepository educationRepository) {
        this.projectRepository = projectRepository;
        this.skillRepository = skillRepository;
        this.messageRepository = messageRepository;
		this.educationRepository = educationRepository;
    }

    public List<DashboardStat> getDashboardStats() {
        List<DashboardStat> stats = new ArrayList<>();
		var latestEdu = educationRepository.findTopByOrderByEndYearDesc();

		stats.add(new DashboardStat("Projects", String.valueOf(projectRepository.count())));
		stats.add(new DashboardStat("Skills", String.valueOf(skillRepository.count())));
		stats.add(new DashboardStat("Messages", String.valueOf(messageRepository.count())));
		stats.add(new DashboardStat("Education", latestEdu != null ? latestEdu.getDegree() : "N/A"));

        return stats;
  }
}
