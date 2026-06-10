/*
 * Class Name: PhotoApiController
 * Description: REST API endpoints for the React front-end: list, fetch, upload, delete photos and poll AI descriptions.
 * Date Created: 2026-06-10
 */

package com.photoalbum.controller;

import com.photoalbum.model.Photo;
import com.photoalbum.model.PhotoDto;
import com.photoalbum.model.UploadResult;
import com.photoalbum.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller used by the React single-page application to manage photos.
 * All endpoints return JSON and are rooted at {@code /api/photos}.
 */
@RestController
@RequestMapping("/api/photos")
public class PhotoApiController {

    private static final Logger logger = LoggerFactory.getLogger(PhotoApiController.class);

    private final PhotoService photoService;
    private final boolean aiEnabled;

    public PhotoApiController(PhotoService photoService,
                             @Value("${azure.openai.enabled:false}") boolean aiEnabled) {
        this.photoService = photoService;
        this.aiEnabled = aiEnabled;
    }

    /**
     * Returns all photos ordered by upload date (newest first).
     */
    @GetMapping
    public ResponseEntity<List<PhotoDto>> listPhotos() {
        try {
            List<PhotoDto> photos = photoService.getAllPhotos().stream()
                    .map(PhotoDto::from)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(photos);
        } catch (Exception ex) {
            logger.error("Error loading photos", ex);
            return ResponseEntity.ok(new ArrayList<>());
        }
    }

    /**
     * Returns a single photo's metadata along with previous/next navigation ids.
     */
    @GetMapping("/{id}")
    public ResponseEntity<PhotoDto> getPhoto(@PathVariable String id) {
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            Optional<Photo> photoOpt = photoService.getPhotoById(id);
            if (!photoOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Photo photo = photoOpt.get();
            PhotoDto dto = PhotoDto.from(photo);

            Optional<Photo> previousPhoto = photoService.getPreviousPhoto(photo);
            Optional<Photo> nextPhoto = photoService.getNextPhoto(photo);
            previousPhoto.ifPresent(p -> dto.setPreviousPhotoId(p.getId()));
            nextPhoto.ifPresent(p -> dto.setNextPhotoId(p.getId()));

            return ResponseEntity.ok(dto);
        } catch (Exception ex) {
            logger.error("Error loading photo with ID {}", id, ex);
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * Uploads one or more photo files and returns the created photo metadata.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadPhotos(@RequestParam("files") List<MultipartFile> files) {
        Map<String, Object> response = new HashMap<>();
        List<PhotoDto> uploadedPhotos = new ArrayList<>();
        List<Map<String, Object>> failedUploads = new ArrayList<>();

        if (files == null || files.isEmpty()) {
            response.put("success", false);
            response.put("error", "No files provided");
            return ResponseEntity.badRequest().body(response);
        }

        for (MultipartFile file : files) {
            UploadResult result = photoService.uploadPhoto(file);

            if (result.isSuccess()) {
                Optional<Photo> photoOpt = photoService.getPhotoById(result.getPhotoId());
                photoOpt.ifPresent(photo -> uploadedPhotos.add(PhotoDto.from(photo)));
            } else {
                Map<String, Object> failedUpload = new HashMap<>();
                failedUpload.put("fileName", result.getFileName());
                failedUpload.put("error", result.getErrorMessage());
                failedUploads.add(failedUpload);
            }
        }

        response.put("success", !uploadedPhotos.isEmpty());
        response.put("uploadedPhotos", uploadedPhotos);
        response.put("failedUploads", failedUploads);
        response.put("aiEnabled", aiEnabled);

        return ResponseEntity.ok(response);
    }

    /**
     * Deletes a photo by id.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deletePhoto(@PathVariable String id) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean deleted = photoService.deletePhoto(id);
            if (deleted) {
                logger.info("Photo {} deleted successfully", id);
                response.put("success", true);
                return ResponseEntity.ok(response);
            }
            response.put("success", false);
            response.put("error", "Photo not found");
            return ResponseEntity.status(404).body(response);
        } catch (Exception ex) {
            logger.error("Error deleting photo {}", id, ex);
            response.put("success", false);
            response.put("error", "Failed to delete photo");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Returns the current AI-generated description for a photo. The client polls
     * this endpoint until {@code ready} is {@code true}.
     */
    @GetMapping("/{id}/description")
    public ResponseEntity<Map<String, Object>> getDescription(@PathVariable String id) {
        Optional<String> descOpt = photoService.getPhotoDescription(id);

        String description = descOpt.orElse(null);
        boolean ready = description != null && !description.trim().isEmpty();

        Map<String, Object> body = new HashMap<>();
        body.put("description", description);
        body.put("ready", ready);
        return ResponseEntity.ok(body);
    }
}

