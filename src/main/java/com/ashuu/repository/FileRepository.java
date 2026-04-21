package com.ashuu.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ashuu.model.FileData;

public interface FileRepository extends JpaRepository<FileData, Long> {

	Optional<FileData> findByFolder(String folder);

	Optional<FileData> findByFileUrl(String cleanUrl);
}
