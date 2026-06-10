/*
 * Class Name: PhotoDescriptionService
 * Description: Asynchronously generates and persists AI descriptions for uploaded photos.
 * Date Created: 2026-06-10
 */

package com.photoalbum.service;

import com.photoalbum.repository.PhotoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Handles asynchronous AI-powered description generation for photos.
 * Runs in a dedicated thread pool to avoid blocking upload responses.
 */
@Service
public class PhotoDescriptionService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoDescriptionService.class);

    private final AzureOpenAiService azureOpenAiService;
    private final PhotoRepository photoRepository;

    public PhotoDescriptionService(AzureOpenAiService azureOpenAiService,
                                   PhotoRepository photoRepository) {
        this.azureOpenAiService = azureOpenAiService;
        this.photoRepository = photoRepository;
    }

    /**
     * Asynchronously calls Azure OpenAI with the given image data, then
     * persists the returned description directly via an update query
     * (avoids reloading the BLOB from the database).
     *
     * @param photoId   ID of the already-saved Photo entity
     * @param photoData Raw image bytes (passed from the upload flow, no extra DB read needed)
     * @param mimeType  MIME type such as image/jpeg
     */
    @Async("descriptionTaskExecutor")
    public void generateAndSave(String photoId, byte[] photoData, String mimeType) {
        logger.info("Starting AI description generation for photo {}", photoId);
        try {
            // Azure OpenAI call takes 1-5 seconds, well after the upload transaction commits
            String description = azureOpenAiService.describeImage(photoData, mimeType);

            if (description != null && !description.trim().isEmpty()) {
                int updated = photoRepository.updateDescription(photoId, description.trim());
                if (updated > 0) {
                    logger.info("Saved AI description for photo {}", photoId);
                } else {
                    logger.warn("Photo {} not found when saving description", photoId);
                }
            } else {
                logger.warn("No description returned from Azure OpenAI for photo {}", photoId);
            }
        } catch (Exception ex) {
            logger.error("Error generating description for photo {}", photoId, ex);
        }
    }
}

