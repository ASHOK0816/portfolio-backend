package com.ashuu.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;

    private String email;

    @Column(length = 2000)
    private String content;

    @Column(name = "is_read", nullable = false)
    private boolean read;
    
	private boolean starred = false;

	private LocalDateTime createdAt = LocalDateTime.now();

}
