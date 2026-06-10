/*
    Class Name: DetailController
    Description: Handles detail view rendering and deletion for individual photos.
    Date Created: 2026-06-10
*/

package com.photoalbum.controller;

import com.photoalbum.model.Photo;
import com.photoalbum.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for displaying a single photo in full size
 */
@RestController
public class DetailController {

    private static final Logger logger = LoggerFactory.getLogger(DetailController.class);

    private final PhotoService photoService;
    private final boolean aiEnabled;

    public DetailController(PhotoService photoService,
                            @Value("${azure.openai.enabled:false}") boolean aiEnabled) {
        this.photoService = photoService;
        this.aiEnabled = aiEnabled;
    }

    @GetMapping("/api/photos/{id}")
    public ResponseEntity<Map<String, Object>> getPhotoDetail(@PathVariable String id) {
        if (id == null || id.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(errorResponse("Photo ID is required"));
        }

        try {
            Optional<Photo> photoOpt = photoService.getPhotoById(id);
            if (!photoOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }

            Photo photo = photoOpt.get();
            Optional<Photo> previousPhoto = photoService.getPreviousPhoto(photo);
            Optional<Photo> nextPhoto = photoService.getNextPhoto(photo);

            Map<String, Object> response = new HashMap<String, Object>();
            response.put("photo", toPhotoMetadata(photo));
            response.put("previousPhotoId", previousPhoto.isPresent() ? previousPhoto.get().getId() : null);
            response.put("nextPhotoId", nextPhoto.isPresent() ? nextPhoto.get().getId() : null);
            response.put("aiEnabled", aiEnabled);

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Error loading photo with ID {}", id, ex);
            return ResponseEntity.status(500).body(errorResponse("Failed to load photo"));
        }
    }

    @DeleteMapping("/api/photos/{id}")
    public ResponseEntity<Map<String, Object>> deletePhoto(@PathVariable String id) {
        Map<String, Object> response = new HashMap<String, Object>();

        try {
            boolean deleted = photoService.deletePhoto(id);
            if (deleted) {
                logger.info("Photo {} deleted successfully", id);
                response.put("success", true);
                response.put("message", "Photo deleted successfully");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Photo not found");
                return ResponseEntity.status(404).body(response);
            }
        } catch (Exception ex) {
            logger.error("Error deleting photo {}", id, ex);
            response.put("success", false);
            response.put("message", "Failed to delete photo. Please try again.");
            return ResponseEntity.status(500).body(response);
        }
    }

    private Map<String, Object> toPhotoMetadata(Photo photo) {
        Map<String, Object> photoMap = new HashMap<String, Object>();
        photoMap.put("id", photo.getId());
        photoMap.put("originalFileName", photo.getOriginalFileName());
        photoMap.put("filePath", photo.getFilePath());
        photoMap.put("uploadedAt", photo.getUploadedAt());
        photoMap.put("fileSize", photo.getFileSize());
        photoMap.put("width", photo.getWidth());
        photoMap.put("height", photo.getHeight());
        photoMap.put("mimeType", photo.getMimeType());
        photoMap.put("description", photo.getDescription());
        return photoMap;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("error", message);
        return response;
    }
}