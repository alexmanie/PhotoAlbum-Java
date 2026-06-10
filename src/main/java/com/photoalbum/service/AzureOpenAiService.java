/*
 * Class Name: AzureOpenAiService
 * Description: Calls Azure OpenAI vision API to generate descriptions for uploaded images.
 * Date Created: 2026-06-10
 */

package com.photoalbum.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service that integrates with Azure OpenAI to generate natural-language
 * descriptions of photos using a vision-capable model (e.g. GPT-4o).
 */
@Service
public class AzureOpenAiService {

    private static final Logger logger = LoggerFactory.getLogger(AzureOpenAiService.class);

    private final RestTemplate restTemplate;
    private final String endpoint;
    private final String apiKey;
    private final String deploymentName;
    private final String apiVersion;
    private final boolean enabled;

    public AzureOpenAiService(
            @Value("${azure.openai.endpoint:}") String endpoint,
            @Value("${azure.openai.api-key:}") String apiKey,
            @Value("${azure.openai.deployment-name:gpt-4o}") String deploymentName,
            @Value("${azure.openai.api-version:2024-02-15-preview}") String apiVersion,
            @Value("${azure.openai.enabled:false}") boolean enabled) {
        this.endpoint = endpoint;
        this.apiKey = apiKey;
        this.deploymentName = deploymentName;
        this.apiVersion = apiVersion;
        this.enabled = enabled;
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sends the image to Azure OpenAI and returns a natural-language description.
     *
     * @param imageData Raw image bytes
     * @param mimeType  MIME type such as image/jpeg
     * @return Description string, or {@code null} if unavailable / not configured
     */
    @SuppressWarnings("unchecked")
    public String describeImage(byte[] imageData, String mimeType) {
        if (!enabled || endpoint == null || endpoint.isEmpty() || apiKey == null || apiKey.isEmpty()) {
            logger.info("Azure OpenAI is not configured; skipping description generation");
            return null;
        }

        try {
            String base64Image = Base64.getEncoder().encodeToString(imageData);
            String imageDataUrl = "data:" + mimeType + ";base64," + base64Image;

            String url = endpoint.replaceAll("/$", "")
                    + "/openai/deployments/" + deploymentName
                    + "/chat/completions?api-version=" + apiVersion;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            // Build the vision message content
            Map<String, Object> textPart = new LinkedHashMap<>();
            textPart.put("type", "text");
            textPart.put("text", "Please provide a brief, descriptive caption for this image in 1-2 sentences.");

            Map<String, Object> imageUrlObj = new LinkedHashMap<>();
            imageUrlObj.put("url", imageDataUrl);
            imageUrlObj.put("detail", "low");

            Map<String, Object> imagePart = new LinkedHashMap<>();
            imagePart.put("type", "image_url");
            imagePart.put("image_url", imageUrlObj);

            Map<String, Object> message = new LinkedHashMap<>();
            message.put("role", "user");
            message.put("content", Arrays.asList(textPart, imagePart));

            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("messages", Collections.singletonList(message));
            requestBody.put("max_tokens", 300);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<Map<String, Object>> choices =
                        (List<Map<String, Object>>) response.getBody().get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> choiceMsg = (Map<String, Object>) choices.get(0).get("message");
                    if (choiceMsg != null) {
                        return (String) choiceMsg.get("content");
                    }
                }
            }

        } catch (Exception ex) {
            logger.error("Error calling Azure OpenAI for image description", ex);
        }
        return null;
    }
}

