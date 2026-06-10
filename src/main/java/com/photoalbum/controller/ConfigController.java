/*
 * Class Name: ConfigController
 * Description: Exposes front-end runtime configuration flags (e.g. whether AI descriptions are enabled).
 * Date Created: 2026-06-10
 */

package com.photoalbum.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides client-side configuration to the React application so it can adapt
 * its UI (for example, hiding the AI description area when AI is disabled).
 */
@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final boolean aiEnabled;

    public ConfigController(@Value("${azure.openai.enabled:false}") boolean aiEnabled) {
        this.aiEnabled = aiEnabled;
    }

    @GetMapping
    public Map<String, Object> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("aiEnabled", aiEnabled);
        return config;
    }
}
