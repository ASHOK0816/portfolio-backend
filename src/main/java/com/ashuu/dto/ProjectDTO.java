package com.ashuu.dto;

import lombok.Data;

@Data
public class ProjectDTO {

    private Long id;
    private String title;
    private String tech;
    private String description;
    private String githuburl;
    private String imageUrl;
}
