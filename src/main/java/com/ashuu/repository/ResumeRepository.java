package com.ashuu.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ashuu.model.Resume;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

}
