package com.ashuu.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ashuu.model.FileData;
import com.ashuu.repository.FileRepository;
import com.ashuu.service.FileStorageService;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private FileRepository repository;

	// Upload Resume
	@PostMapping("/upload")
	public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) throws Exception {

		// delete old resume if exists
		Optional<FileData> oldResume = repository.findByFolder("resume");

		if (oldResume.isPresent()) {
			File oldFile = new File(oldResume.get().getFilePath());
			if (oldFile.exists()) {
				oldFile.delete();
			}
			repository.delete(oldResume.get());
		}

		FileData fileData = fileStorageService.uploadFile(file, "resume");

		return ResponseEntity.ok(fileData);
	}

	// Download Resume
	@GetMapping("/download")
	public ResponseEntity<InputStreamResource> downloadResume() throws Exception {

		FileData fileData = repository.findByFolder("resume")
				.orElseThrow(() -> new RuntimeException("Resume not found"));

		File file = new File(fileData.getFilePath());

		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileData.getFileName())
				.contentType(MediaType.parseMediaType(fileData.getFileType())).body(resource);
    }
}