package com.ashuu.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ashuu.model.FileData;
import com.ashuu.model.Resume;
import com.ashuu.repository.ResumeRepository;

@Service
public class ResumeService {

	@Autowired
	private ResumeRepository repository;

	@Autowired
	private FileStorageService fileStorageService;

	public Resume uploadResume(MultipartFile file) throws IOException {

		FileData uploaded = fileStorageService.uploadFile(file, "resume");

		Resume resume = new Resume();

		resume.setFileName(uploaded.getFileName());
		resume.setFileType(uploaded.getFileType());
		resume.setFilePath(uploaded.getFileUrl());

		return repository.save(resume);
	}
}
