/*
 * Class Name: PhotoApiController
 * Description: REST API endpoints for polling photo metadata such as AI-generated descriptions.
 * Date Created: 2026-06-10
 */

package com.photoalbum.controller;

import com.photoalbum.service.PhotoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Lightweight REST controller used by the front-end to poll for
 * asynchronously generated photo metadata (e.g. AI descriptions).
 */
@RestController
@RequestMapping("/api/photos")
public class PhotoApiController {

    private final PhotoService photoService;

    public PhotoApiController(PhotoService photoService) {
        this.photoService = photoService;
    }

    /**
     * Returns the current AI-generated description for a photo.
     * The client polls this endpoint until {@code ready} is {@code true}.
     *
     * <pre>
     * GET /api/photos/{id}/description
     * Response: { "description": "...", "ready": true }
     *       or: { "description": null,  "ready": false }   (pending or not found)
     * </pre>
     *
     * <p>Note: {@code ready:false} covers both "description not yet generated"
     * and "photo not found". The front-end polling loop will time out
     * gracefully after ~2 minutes if the photo has been deleted.</p>
     */
    @GetMapping("/{id}/description")
    public ResponseEntity<Map<String, Object>> getDescription(@PathVariable String id) {
        // findDescriptionById returns Optional.empty() for both "photo not found"
        // and "description is null (still generating)".  We always return 200 so
        // the polling client can distinguish the timeout case on its own.
        Optional<String> descOpt = photoService.getPhotoDescription(id);

        String description = descOpt.orElse(null);
        boolean ready = description != null && !description.trim().isEmpty();

        Map<String, Object> body = new HashMap<>();
        body.put("description", description);
        body.put("ready", ready);
        return ResponseEntity.ok(body);
    }
}


