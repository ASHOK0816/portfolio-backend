package com.ashuu.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.ashuu.model.FileData;
import com.ashuu.repository.FileRepository;

@Service
public class FileStorageService {

	@Value("${file.upload.base-dir:${user.dir}/uploads}")
	private String baseDir;

	private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

	private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/gif", "image/webp");

	@Autowired
	private FileRepository repository;

	// ── Upload ────────────────────────────────────────────────────────────────
	public FileData uploadFile(MultipartFile file, String folder) throws IOException {

		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
			throw new RuntimeException("File type not allowed: " + contentType);
		}

		if (file.isEmpty() || file.getSize() == 0) {
			throw new RuntimeException("File is empty");
		}
		if (file.getSize() > MAX_FILE_SIZE) {
			throw new RuntimeException("File exceeds maximum size of 5 MB");
		}

		String safeFolder = sanitizePathSegment(folder);
		String originalName = file.getOriginalFilename();
		String safeOriginalName = (originalName == null || originalName.isBlank()) ? "file"
				: Paths.get(originalName).getFileName().toString();
		String safeFileName = sanitizePathSegment(safeOriginalName);

		Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
		Path uploadPath = basePath.resolve(safeFolder).normalize();

		if (!uploadPath.startsWith(basePath)) {
			throw new SecurityException("Path traversal detected in folder name");
		}

		Files.createDirectories(uploadPath);

		String uniqueFileName = UUID.randomUUID() + "_" + safeFileName;
		Path filePath = uploadPath.resolve(uniqueFileName).normalize();

		if (!filePath.startsWith(uploadPath)) {
			throw new SecurityException("Path traversal detected in file name");
		}

		Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

		FileData fileData = new FileData();
		fileData.setFileName(uniqueFileName);
		fileData.setFileType(contentType);
		fileData.setFilePath(filePath.toString());
		fileData.setFileUrl("/uploads/" + safeFolder + "/" + uniqueFileName);
		fileData.setSize(file.getSize());

		return repository.save(fileData);
	}

	// ── Upload and replace old file (Fix 2: old image is now deleted) ─────────
	public FileData replaceFile(MultipartFile file, String folder, String oldPhotoUrl) throws IOException {

		// Upload the new file first — if it fails, old file is untouched
		FileData newFile = uploadFile(file, folder);

		// Only delete old file after new one is safely saved
		if (oldPhotoUrl != null && !oldPhotoUrl.isBlank()) {
			deleteFile(oldPhotoUrl);
		}

		return newFile;
	}

	// ── Delete ────────────────────────────────────────────────────────────────
	public boolean deleteFile(String photoUrl) {
		try {
			if (photoUrl == null || photoUrl.isBlank())
				return false;

			// Fix 4 (from previous review): strip query string e.g. ?t=123456
			String cleanUrl = photoUrl.contains("?") ? photoUrl.substring(0, photoUrl.indexOf('?')) : photoUrl;

			String prefix = "/uploads/";
			if (!cleanUrl.startsWith(prefix))
				return false;

			String relativePath = cleanUrl.substring(prefix.length());

			Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
			Path resolvedPath = basePath.resolve(relativePath).normalize();

			if (!resolvedPath.startsWith(basePath)) {
				throw new SecurityException("Path traversal detected in delete");
			}

			boolean deleted = Files.deleteIfExists(resolvedPath);

			// Also remove the DB record for this URL
			repository.findByFileUrl(cleanUrl).ifPresent(repository::delete);

			return deleted;

		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	// ── Fix 3: load as Resource so Spring manages stream lifecycle ─────────────
	public Resource loadFileAsResource(Long id) throws IOException {
		FileData fileData = getFileMetadata(id);
		Path filePath = Paths.get(fileData.getFilePath()).normalize();

		// Path traversal guard on stored path
		Path basePath = Paths.get(baseDir).toAbsolutePath().normalize();
		if (!filePath.toAbsolutePath().startsWith(basePath)) {
			throw new SecurityException("Stored file path is outside upload directory");
		}

		try {
			Resource resource = new UrlResource(filePath.toUri());
			if (!resource.exists() || !resource.isReadable()) {
				throw new RuntimeException("File not found on server: " + filePath);
			}
			return resource;
		} catch (MalformedURLException e) {
			throw new RuntimeException("Invalid file path: " + filePath, e);
		}
	}

	// ── Fix 7: metadata lookup moved out of controller ────────────────────────
	public FileData getFileMetadata(Long id) {
		return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("File not found with id: " + id));
	}

	// ── Utility ───────────────────────────────────────────────────────────────
	private String sanitizePathSegment(String segment) {
		if (segment == null || segment.isBlank())
			return "default";
		return segment.replaceAll("[^a-zA-Z0-9._\\-]", "_");
	}
}