package com.ashuu.service;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ashuu.model.Resume;
import com.ashuu.repository.ResumeRepository;

@Service
public class ResumeService {

	private static final String UPLOAD_DIR = "uploads/";

	@Autowired
	private ResumeRepository repository;

	public Resume uploadResume(MultipartFile file) throws IOException {

		File dir = new File(UPLOAD_DIR);
		if (!dir.exists())
			dir.mkdirs();

		String filePath = UPLOAD_DIR + file.getOriginalFilename();

		File dest = new File(filePath);
		file.transferTo(dest);

		Resume resume = new Resume(file.getOriginalFilename(), file.getContentType(), filePath);

		return repository.save(resume);
	}
}
