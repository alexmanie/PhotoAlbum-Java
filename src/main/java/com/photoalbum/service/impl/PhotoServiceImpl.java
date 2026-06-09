package com.photoalbum.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photoalbum.model.Photo;
import com.photoalbum.model.UploadResult;
import com.photoalbum.repository.PhotoRepository;
import com.photoalbum.service.PhotoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for photo operations including upload, retrieval, and deletion
 */
@Service
@Transactional
public class PhotoServiceImpl implements PhotoService {

    private static final Logger logger = LoggerFactory.getLogger(PhotoServiceImpl.class);

    private final PhotoRepository photoRepository;
    private final ObjectMapper objectMapper;
    private final long maxFileSizeBytes;
    private final List<String> allowedMimeTypes;
    private final boolean azureOpenAiEnabled;
    private final String azureOpenAiEndpoint;
    private final String azureOpenAiDeployment;
    private final String azureOpenAiApiKey;
    private final String azureOpenAiApiVersion;
    private final RestTemplate restTemplate;

    public PhotoServiceImpl(
            PhotoRepository photoRepository,
            ObjectMapper objectMapper,
            @Value("${app.file-upload.max-file-size-bytes}") long maxFileSizeBytes,
            @Value("${app.file-upload.allowed-mime-types}") String[] allowedMimeTypes,
            @Value("${app.azure-openai.enabled:false}") boolean azureOpenAiEnabled,
            @Value("${app.azure-openai.endpoint:}") String azureOpenAiEndpoint,
            @Value("${app.azure-openai.deployment:}") String azureOpenAiDeployment,
            @Value("${app.azure-openai.api-key:}") String azureOpenAiApiKey,
            @Value("${app.azure-openai.api-version:2024-02-15-preview}") String azureOpenAiApiVersion) {
        this.photoRepository = photoRepository;
        this.objectMapper = objectMapper;
        this.maxFileSizeBytes = maxFileSizeBytes;
        this.allowedMimeTypes = Arrays.asList(allowedMimeTypes);
        this.azureOpenAiEnabled = azureOpenAiEnabled;
        this.azureOpenAiEndpoint = azureOpenAiEndpoint;
        this.azureOpenAiDeployment = azureOpenAiDeployment;
        this.azureOpenAiApiKey = azureOpenAiApiKey;
        this.azureOpenAiApiVersion = azureOpenAiApiVersion;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Get all photos ordered by upload date (newest first)
     */
    @Override
    @Transactional(readOnly = true)
    public List<Photo> getAllPhotos() {
        try {
            return photoRepository.findAllOrderByUploadedAtDesc();
        } catch (Exception ex) {
            logger.error("Error retrieving photos from database", ex);
            throw new RuntimeException("Error retrieving photos", ex);
        }
    }

    /**
     * Get a specific photo by ID
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Photo> getPhotoById(String id) {
        try {
            return photoRepository.findById(id);
        } catch (Exception ex) {
            logger.error("Error retrieving photo with ID {}", id, ex);
            throw new RuntimeException("Error retrieving photo", ex);
        }
    }

    /**
     * Upload a photo file
     */
    @Override
    public UploadResult uploadPhoto(MultipartFile file) {
        UploadResult result = new UploadResult();
        result.setFileName(file.getOriginalFilename());

        try {
            // Validate file type
            if (!allowedMimeTypes.contains(file.getContentType().toLowerCase())) {
                result.setSuccess(false);
                result.setErrorMessage("File type not supported. Please upload JPEG, PNG, GIF, or WebP images.");
                logger.warn("Upload rejected: Invalid file type {} for {}", 
                    file.getContentType(), file.getOriginalFilename());
                return result;
            }

            // Validate file size
            if (file.getSize() > maxFileSizeBytes) {
                result.setSuccess(false);
                result.setErrorMessage(String.format("File size exceeds %dMB limit.", maxFileSizeBytes / 1024 / 1024));
                logger.warn("Upload rejected: File size {} exceeds limit for {}", 
                    file.getSize(), file.getOriginalFilename());
                return result;
            }

            // Validate file length
            if (file.getSize() <= 0) {
                result.setSuccess(false);
                result.setErrorMessage("File is empty.");
                return result;
            }

            // Generate unique filename for compatibility (stored in database, not on disk)
            String extension = getFileExtension(file.getOriginalFilename());
            String storedFileName = UUID.randomUUID().toString() + extension;
            String relativePath = "/uploads/" + storedFileName; // For compatibility only

            // Extract image dimensions and read file data
            Integer width = null;
            Integer height = null;
            byte[] photoData = null;
            
            try {
                // Read file content for database storage
                photoData = file.getBytes();
                
                // Extract image dimensions from byte array
                try (ByteArrayInputStream bis = new ByteArrayInputStream(photoData)) {
                    BufferedImage image = ImageIO.read(bis);
                    if (image != null) {
                        width = image.getWidth();
                        height = image.getHeight();
                    }
                }
            } catch (IOException ex) {
                logger.error("Error reading file data for {}", file.getOriginalFilename(), ex);
                result.setSuccess(false);
                result.setErrorMessage("Error reading file data. Please try again.");
                return result;
            } catch (Exception ex) {
                logger.warn("Could not extract image dimensions for {}", file.getOriginalFilename(), ex);
                // Continue without dimensions - not critical
            }

            // Create photo entity with database BLOB storage
            Photo photo = new Photo(
                file.getOriginalFilename(),
                photoData,  // Store actual photo data in Oracle database
                storedFileName,
                relativePath, // Keep for compatibility, not used for serving
                file.getSize(),
                file.getContentType()
            );
            photo.setWidth(width);
            photo.setHeight(height);
            photo.setImageDescription(generateImageDescription(photoData, file.getContentType()));

            // Save to database (with BLOB photo data)
            try {
                photo = photoRepository.save(photo);

                result.setSuccess(true);
                result.setPhotoId(photo.getId());

                logger.info("Successfully uploaded photo {} with ID {} to Oracle database", 
                    file.getOriginalFilename(), photo.getId());
            } catch (Exception ex) {
                logger.error("Error saving photo to Oracle database for {}", file.getOriginalFilename(), ex);
                result.setSuccess(false);
                result.setErrorMessage("Error saving photo to database. Please try again.");
            }
        } catch (Exception ex) {
            logger.error("Unexpected error during photo upload for {}", file.getOriginalFilename(), ex);
            result.setSuccess(false);
            result.setErrorMessage("An unexpected error occurred. Please try again.");
        }

        return result;
    }

    /**
     * Delete a photo by ID
     */
    @Override
    public boolean deletePhoto(String id) {
        try {
            Optional<Photo> photoOpt = photoRepository.findById(id);
            if (!photoOpt.isPresent()) {
                logger.warn("Photo with ID {} not found for deletion", id);
                return false;
            }

            Photo photo = photoOpt.get();

            // Delete from Oracle database (photos stored as BLOB)
            photoRepository.delete(photo);

            logger.info("Successfully deleted photo ID {} from Oracle database", id);
            return true;
        } catch (Exception ex) {
            logger.error("Error deleting photo with ID {} from Oracle database", id, ex);
            throw new RuntimeException("Error deleting photo", ex);
        }
    }

    /**
     * Get the previous photo (older) for navigation
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Photo> getPreviousPhoto(Photo currentPhoto) {
        List<Photo> olderPhotos = photoRepository.findPhotosUploadedBefore(currentPhoto.getUploadedAt());
        return olderPhotos.isEmpty() ? Optional.<Photo>empty() : Optional.of(olderPhotos.get(0));
    }

    /**
     * Get the next photo (newer) for navigation
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Photo> getNextPhoto(Photo currentPhoto) {
        List<Photo> newerPhotos = photoRepository.findPhotosUploadedAfter(currentPhoto.getUploadedAt());
        return newerPhotos.isEmpty() ? Optional.<Photo>empty() : Optional.of(newerPhotos.get(0));
    }

    /**
     * Generate an image description using Azure OpenAI chat completions API
     */
    @Override
    public String generateImageDescription(byte[] photoData, String mimeType) {
        if (!azureOpenAiEnabled) {
            return null;
        }

        if (photoData == null || photoData.length == 0) {
            logger.warn("Skipping image description generation: empty image payload");
            return null;
        }

        if (azureOpenAiEndpoint == null || azureOpenAiEndpoint.trim().isEmpty()
                || azureOpenAiDeployment == null || azureOpenAiDeployment.trim().isEmpty()
                || azureOpenAiApiKey == null || azureOpenAiApiKey.trim().isEmpty()) {
            logger.warn("Skipping image description generation: Azure OpenAI configuration is incomplete");
            return null;
        }

        String safeMimeType = (mimeType == null || mimeType.trim().isEmpty()) ? "image/jpeg" : mimeType;
        String dataUrl = "data:" + safeMimeType + ";base64," + Base64.getEncoder().encodeToString(photoData);

        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(azureOpenAiEndpoint)
                    .pathSegment("openai", "deployments", azureOpenAiDeployment, "chat", "completions")
                    .queryParam("api-version", azureOpenAiApiVersion)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", azureOpenAiApiKey);

            String requestBody = buildImageDescriptionRequest(dataUrl);
            HttpEntity<String> request = new HttpEntity<String>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, request, String.class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                logger.warn("Azure OpenAI image description request failed with status {}", response.getStatusCodeValue());
                return null;
            }

            return extractDescriptionFromResponse(response.getBody());
        } catch (RestClientException ex) {
            logger.error("Failed to call Azure OpenAI for image description", ex);
            return null;
        } catch (IOException ex) {
            logger.error("Failed to parse Azure OpenAI response for image description", ex);
            return null;
        }
    }

    /**
     * Extract file extension from filename
     */
    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        return lastDotIndex > 0 ? filename.substring(lastDotIndex) : "";
    }

    private String buildImageDescriptionRequest(String dataUrl) throws JsonProcessingException {
        Map<String, Object> payload = new HashMap<String, Object>();
        List<Map<String, Object>> messages = new ArrayList<Map<String, Object>>();

        Map<String, Object> systemMessage = new HashMap<String, Object>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You describe images for a photo gallery using concise, factual language.");
        messages.add(systemMessage);

        Map<String, Object> textContent = new HashMap<String, Object>();
        textContent.put("type", "text");
        textContent.put("text", "Describe this image in one short sentence.");

        Map<String, Object> imageUrlObject = new HashMap<String, Object>();
        imageUrlObject.put("url", dataUrl);

        Map<String, Object> imageContent = new HashMap<String, Object>();
        imageContent.put("type", "image_url");
        imageContent.put("image_url", imageUrlObject);

        List<Map<String, Object>> userContent = new ArrayList<Map<String, Object>>();
        userContent.add(textContent);
        userContent.add(imageContent);

        Map<String, Object> userMessage = new HashMap<String, Object>();
        userMessage.put("role", "user");
        userMessage.put("content", userContent);
        messages.add(userMessage);

        payload.put("messages", messages);
        payload.put("temperature", 0.2);
        payload.put("max_tokens", 120);

        return objectMapper.writeValueAsString(payload);
    }

    private String extractDescriptionFromResponse(String responseBody) throws IOException {
        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode contentNode = root.path("choices").path(0).path("message").path("content");

        if (contentNode.isMissingNode() || contentNode.isNull()) {
            return null;
        }

        if (contentNode.isTextual()) {
            String description = contentNode.asText().trim();
            return description.isEmpty() ? null : description;
        }

        if (contentNode.isArray()) {
            StringBuilder builder = new StringBuilder();
            for (JsonNode item : contentNode) {
                JsonNode textNode = item.path("text");
                if (textNode.isTextual()) {
                    if (builder.length() > 0) {
                        builder.append(' ');
                    }
                    builder.append(textNode.asText().trim());
                }
            }
            String description = builder.toString().trim();
            return description.isEmpty() ? null : description;
        }

        return null;
    }
}
