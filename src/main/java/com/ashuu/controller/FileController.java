package com.ashuu.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ashuu.model.FileData;
import com.ashuu.service.FileStorageService;

@RestController
@RequestMapping("/api/files")
public class FileController {

	@Autowired
	private FileStorageService fileStorageService;

	// ── Fix 7: removed direct repository injection — service handles all data
	// access

	// ── Upload ────────────────────────────────────────────────────────────────
	@PostMapping("/upload")
	public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
		try {
			FileData fileData = fileStorageService.uploadFile(file, "profile");
			return ResponseEntity.ok(fileData);
		} catch (SecurityException e) {
			// Fix 4: return proper HTTP status instead of leaking stack trace
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("Upload failed: " + e.getMessage());
		}
	}

	// ── Download ──────────────────────────────────────────────────────────────
	@GetMapping("/download/{id}")
	public ResponseEntity<?> downloadFile(@PathVariable Long id) {
		try {
			// Fix 7: delegate file lookup to service layer
			Resource resource = fileStorageService.loadFileAsResource(id);
			FileData fileData = fileStorageService.getFileMetadata(id);

			// Fix 5: encode filename to prevent header injection
			String safeFileName = fileData.getFileName().replaceAll("[\"\\\\]", "_") // strip quotes and backslashes
					.replaceAll("[\\r\\n]", ""); // strip newlines

			// Fix 3: using Resource (FileSystemResource inside service) instead
			// of raw FileInputStream — Spring closes it automatically
			return ResponseEntity.ok()
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFileName + "\"")
					.contentType(MediaType.parseMediaType(fileData.getFileType()))
					.contentLength(resource.contentLength()).body(resource);

		} catch (IllegalArgumentException e) {
			// Fix 4: 404 when file metadata not found
			return ResponseEntity.notFound().build();
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.internalServerError().body("Download failed: " + e.getMessage());
		}
	}
}