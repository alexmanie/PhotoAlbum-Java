/*
    Class Name: HomeController
    Description: Handles gallery display and photo upload requests.
    Date Created: 2026-06-10
*/

package com.photoalbum.controller;

import com.photoalbum.model.Photo;
import com.photoalbum.model.UploadResult;
import com.photoalbum.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for the main photo gallery page with upload functionality
 */
@RestController
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    private final PhotoService photoService;
    private final boolean aiEnabled;

    public HomeController(PhotoService photoService,
                          @Value("${azure.openai.enabled:false}") boolean aiEnabled) {
        this.photoService = photoService;
        this.aiEnabled = aiEnabled;
    }

    @GetMapping("/api/photos")
    public ResponseEntity<Map<String, Object>> getPhotos() {
        Map<String, Object> response = new HashMap<String, Object>();

        try {
            List<Photo> photos = photoService.getAllPhotos();
            List<Map<String, Object>> photoDtos = new ArrayList<Map<String, Object>>();

            for (Photo photo : photos) {
                photoDtos.add(toPhotoMetadata(photo));
            }

            response.put("photos", photoDtos);
            response.put("aiEnabled", aiEnabled);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            logger.error("Error loading photos", ex);
            response.put("photos", new ArrayList<Map<String, Object>>());
            response.put("aiEnabled", aiEnabled);
            response.put("error", "Failed to load photos");
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Handler for POST requests - uploads one or more photo files
     */
    @PostMapping({"/upload", "/api/photos/upload"})
    public ResponseEntity<Map<String, Object>> uploadPhotos(@RequestParam("files") List<MultipartFile> files) {
        Map<String, Object> response = new HashMap<String, Object>();
        List<Map<String, Object>> uploadedPhotos = new ArrayList<Map<String, Object>>();
        List<Map<String, Object>> failedUploads = new ArrayList<Map<String, Object>>();

        if (files == null || files.isEmpty()) {
            response.put("success", false);
            response.put("error", "No files provided");
            return ResponseEntity.badRequest().body(response);
        }

        for (MultipartFile file : files) {
            UploadResult result = photoService.uploadPhoto(file);

            if (result.isSuccess()) {
                Optional<Photo> photoOpt = photoService.getPhotoById(result.getPhotoId());
                if (photoOpt.isPresent()) {
                    uploadedPhotos.add(toPhotoMetadata(photoOpt.get()));
                }
            } else {
                Map<String, Object> failedUpload = new HashMap<String, Object>();
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
}