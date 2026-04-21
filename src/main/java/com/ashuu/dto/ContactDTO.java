package com.ashuu.dto;

import lombok.Data;

@Data
public class ContactDTO {

    private Long id;
    private String name;
    private String email;
    private String message;
}

