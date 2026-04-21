package com.ashuu.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.ashuu.model.FileData;
import com.ashuu.model.MyProfile;
import com.ashuu.repository.ProfileRepository;
import com.ashuu.service.FileStorageService;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

	// Fix 8: single constant — change in one place only
	private static final String ADMIN_KEY = "ADMIN_PROFILE";

	private final ProfileRepository profileRepository;
	private final FileStorageService fileStorageService;

	public ProfileController(ProfileRepository profileRepository, FileStorageService fileStorageService) {
		this.profileRepository = profileRepository;
		this.fileStorageService = fileStorageService;
    }

	// ── GET ───────────────────────────────────────────────────────────────────
	@GetMapping("/admin")
	public ResponseEntity<?> getAdminProfile() {
		// Fix 3: 404 instead of 500 when profile row is missing
		return profileRepository.findByAdminKey(ADMIN_KEY).map(ResponseEntity::ok)
				.orElse(ResponseEntity.notFound().build());
    }

	// ── UPDATE ────────────────────────────────────────────────────────────────
	@PutMapping("/admin")
	public ResponseEntity<?> updateAdminProfile(@RequestBody MyProfile updatedProfile) {

		// Fix 2: reject requests where every updatable field is blank
		if (isProfileBodyEmpty(updatedProfile)) {
			return ResponseEntity.badRequest().body("Request body must contain at least one non-blank field");
		}

		return profileRepository.findByAdminKey(ADMIN_KEY).map(profile -> {
			// Fix 2: only overwrite a field if the incoming value is non-blank
			// so a partial PUT never wipes existing data
			if (isNotBlank(updatedProfile.getName()))
				profile.setName(updatedProfile.getName().trim());

			if (isNotBlank(updatedProfile.getEmail()))
				profile.setEmail(updatedProfile.getEmail().trim());

			if (isNotBlank(updatedProfile.getPhone()))
				profile.setPhone(updatedProfile.getPhone().trim());

			if (isNotBlank(updatedProfile.getLinkedin()))
				profile.setLinkedin(updatedProfile.getLinkedin().trim());

			if (isNotBlank(updatedProfile.getGithub()))
				profile.setGithub(updatedProfile.getGithub().trim());

			return ResponseEntity.ok(profileRepository.save(profile));
		})
				// Fix 3: 404 instead of 500
				.orElse(ResponseEntity.notFound().build());
    }

	// ── UPLOAD PHOTO ──────────────────────────────────────────────────────────
	@PostMapping("/admin/photo")
	@Transactional // Fix 5: if DB save fails, file write is still on disk but
					// at least the old URL isn't lost from the DB record
	public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file) {

		// Fix 4: explicit guard for missing or empty multipart field
		if (file == null || file.isEmpty()) {
			return ResponseEntity.badRequest().body("No file provided");
		}

		try {
			MyProfile profile = profileRepository.findByAdminKey(ADMIN_KEY)
					.orElseThrow(() -> new IllegalArgumentException("Profile not found"));

			String oldPhotoUrl = profile.getPhotoUrl();

			// Upload new file first — if this throws, old file is untouched
			FileData fileData = fileStorageService.uploadFile(file, "profile");
			profile.setPhotoUrl(fileData.getFileUrl());
			MyProfile saved = profileRepository.save(profile);

			// Only delete old file after DB is successfully updated
			if (oldPhotoUrl != null) {
				fileStorageService.deleteFile(oldPhotoUrl);
			}

			return ResponseEntity.ok(saved);

		} catch (IllegalArgumentException e) {
			// Fix 3: profile not found → 404
			return ResponseEntity.notFound().build();
		} catch (SecurityException e) {
			return ResponseEntity.badRequest().body(e.getMessage());
		} catch (IOException e) {
			// Fix 1: IOException caught here, not leaked via throws declaration
			return ResponseEntity.internalServerError().body("File upload failed: " + e.getMessage());
		}
	}

	// ── DELETE PHOTO ──────────────────────────────────────────────────────────
	@DeleteMapping("/admin/photo")
	public ResponseEntity<?> deletePhoto() {
		try {
			MyProfile profile = profileRepository.findByAdminKey(ADMIN_KEY)
					.orElseThrow(() -> new IllegalArgumentException("Profile not found"));

			// Fix 6: return 400 if there is no photo to delete
			if (profile.getPhotoUrl() == null) {
				return ResponseEntity.badRequest().body("No photo to delete");
			}

			fileStorageService.deleteFile(profile.getPhotoUrl());
			profile.setPhotoUrl(null);
			return ResponseEntity.ok(profileRepository.save(profile));

		} catch (IllegalArgumentException e) {
			return ResponseEntity.notFound().build();
		}
	}

	// ── Helpers ───────────────────────────────────────────────────────────────
	private boolean isNotBlank(String value) {
		return value != null && !value.isBlank();
	}

	private boolean isProfileBodyEmpty(MyProfile p) {
		return p == null || (!isNotBlank(p.getName()) && !isNotBlank(p.getEmail()) && !isNotBlank(p.getPhone())
				&& !isNotBlank(p.getLinkedin()) && !isNotBlank(p.getGithub()));
    }
}