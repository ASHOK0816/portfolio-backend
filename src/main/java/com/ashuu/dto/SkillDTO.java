package com.ashuu.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;

@Data
public class SkillDTO {
    private Long id;
    private String name;
	private MultipartFile image;
	private String category;
    
}

