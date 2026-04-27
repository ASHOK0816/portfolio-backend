package com.ashuu.dto;

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
public class ExperienceDTO {

	private Long id;
	private String title;
	private String company;
	private String startDate;
	private String endDate;
	private String description;
}