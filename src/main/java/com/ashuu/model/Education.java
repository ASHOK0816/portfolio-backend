package com.ashuu.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String degree;
    private String college;
	private String startYear;

	private String endYear;

	private Boolean pursuing = false;

	private String gradeType = "CGPA"; // CGPA or Percentage

	private String grade;
}

