/*
    Class Name: PhotoService
    Description: Service contract for photo retrieval, upload, and navigation operations.
    Date Created: 2026-06-10
*/

package com.photoalbum.service;

import com.photoalbum.model.Photo;
import com.photoalbum.model.UploadResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for photo operations
 */
public interface PhotoService {

    /**
     * Get all photos ordered by upload date (newest first)
     * @return List of photos
     */
    List<Photo> getAllPhotos();

    /**
     * Get a specific photo by ID
     * @param id Photo ID
     * @return Photo if found, empty otherwise
     */
    Optional<Photo> getPhotoById(String id);

    /**
     * Upload a photo file
     * @param file The uploaded file
     * @return Upload result with success status and photo details or error message
     */
    UploadResult uploadPhoto(MultipartFile file);

    /**
     * Delete a photo by ID
     * @param id Photo ID
     * @return True if deleted successfully, false if not found
     */
    boolean deletePhoto(String id);

    /**
     * Get the previous photo (older) for navigation
     * @param currentPhoto The current photo
     * @return Previous photo if found, empty otherwise
     */
    Optional<Photo> getPreviousPhoto(Photo currentPhoto);

    /**
     * Get the next photo (newer) for navigation
     * @param currentPhoto The current photo
     * @return Next photo if found, empty otherwise
     */
    Optional<Photo> getNextPhoto(Photo currentPhoto);

    /**
     * Get the description of a photo by ID (efficient lookup without loading photo data)
     * @param id Photo ID
     * @return Description if found, empty otherwise
     */
    Optional<String> getPhotoDescription(String id);

    /**
     * Asynchronously generate and save an AI description for a photo.
     * Calls Azure OpenAI with the image data and persists the resulting description.
     * @param photoId    The ID of the saved photo
     * @param photoData  The raw image bytes to analyse
     * @param mimeType   MIME type of the image (e.g. image/jpeg)
     */
    void triggerDescriptionGeneration(String photoId, byte[] photoData, String mimeType);
}