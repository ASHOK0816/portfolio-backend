package com.ashuu.controller;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@CrossOrigin("*")
public class ResumeController {

	@Autowired
	private FileStorageService fileStorageService;

	@Autowired
	private FileRepository repository;

	// ================= UPLOAD =================
	@PostMapping("/upload")
	public ResponseEntity<?> uploadResume(@RequestParam("file") MultipartFile file) throws Exception {

		FileData fileData = fileStorageService.uploadFile(file, "resume");

		FileData saved = repository.save(fileData);

		return ResponseEntity.ok(saved);
	}

	// ================= GET ALL =================
	@GetMapping
	public ResponseEntity<List<FileData>> getAllResumes() {
		return ResponseEntity.ok(repository.findByFolder("resume"));
	}

	// ================= PREVIEW (INLINE PDF) =================
	@GetMapping("/view/{id}")
	public ResponseEntity<InputStreamResource> viewResume(@PathVariable Long id) throws Exception {

		FileData fileData = repository.findById(id).orElseThrow(() -> new RuntimeException("Resume not found"));

	    if (!"resume".equals(fileData.getFolder())) {
	        throw new RuntimeException("Not a resume file");
	    }

		File file = new File(fileData.getFilePath());

		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

		return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=" + fileData.getFileName())
				.contentType(MediaType.APPLICATION_PDF).body(resource);
	}

	// ================= DOWNLOAD =================
	@GetMapping("/download/{id}")
	public ResponseEntity<InputStreamResource> downloadResume(@PathVariable Long id) throws Exception {

		FileData fileData = repository.findById(id).orElseThrow(() -> new RuntimeException("Resume not found"));

		File file = new File(fileData.getFilePath());

		InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileData.getFileName())
				.contentType(MediaType.APPLICATION_PDF).body(resource);
	}

	// ================= DELETE =================
	@DeleteMapping("/{id}")
	public ResponseEntity<?> deleteResume(@PathVariable Long id) {

		FileData fileData = repository.findById(id).orElseThrow(() -> new RuntimeException("Resume not found"));

		if (!"resume".equals(fileData.getFolder())) {
			return ResponseEntity.badRequest().body("Not a resume file");
		}

		fileStorageService.deleteFile(fileData.getFileUrl());

		return ResponseEntity.ok("Deleted successfully");
	}
}