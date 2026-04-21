package com.ashuu.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ashuu.model.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {

}
