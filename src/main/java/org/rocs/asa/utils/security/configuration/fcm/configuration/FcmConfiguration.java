package org.rocs.asa.utils.security.configuration.fcm.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FcmConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmConfiguration.class);

    @Value("${firebase.service-account-file:}")
    private String firebaseConfig;

    @Value("${firebase.project-id:}")
    private String projectId;

    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                LOGGER.info("==============================================");
                LOGGER.info("Initializing Firebase...");
                LOGGER.info("Project ID from env: {}", projectId);
                LOGGER.info("Config source: {}", getConfigSource());
                LOGGER.info("==============================================");

                GoogleCredentials credentials = loadCredentials();

                String finalProjectId = (projectId != null && !projectId.isEmpty())
                        ? projectId
                        : "appointment-notification-cc54d";

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId(finalProjectId)
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);

                LOGGER.info("==============================================");
                LOGGER.info("✓ Firebase initialized successfully!");
                LOGGER.info("✓ Backend project ID: {}", app.getOptions().getProjectId());
                LOGGER.info("✓ Firebase app name: {}", app.getName());
                LOGGER.info("==============================================");

            } else {
                LOGGER.info("Firebase already initialized");
            }
        } catch (Exception e) {
            LOGGER.error("==============================================");
            LOGGER.error("✗ Failed to initialize Firebase");
            LOGGER.error("✗ Error: {}", e.getMessage(), e);
            LOGGER.error("==============================================");
            // Make Firebase optional - don't crash the app
            LOGGER.warn("⚠ Application will continue without Firebase push notifications");
        }
    }

    private String getConfigSource() {
        if (firebaseConfig == null || firebaseConfig.isEmpty()) {
            return "classpath";
        }
        File configFile = new File(firebaseConfig);
        if (configFile.exists() && configFile.isFile()) {
            return "file: " + firebaseConfig;
        }
        return "environment variable (length: " + firebaseConfig.length() + ")";
    }

    private GoogleCredentials loadCredentials() throws IOException {
        InputStream credentialsStream = null;

        try {
            if (firebaseConfig != null && !firebaseConfig.isEmpty()) {
                firebaseConfig = firebaseConfig.trim();
                File configFile = new File(firebaseConfig);

                // Check if it's a file path
                if (configFile.exists() && configFile.isFile()) {
                    LOGGER.info("Loading Firebase credentials from file: {}", firebaseConfig);
                    credentialsStream = new FileInputStream(configFile);
                } else {
                    // Try to decode from Base64
                    try {
                        LOGGER.info("Attempting to decode Base64 Firebase credentials");
                        byte[] decodedBytes = java.util.Base64.getDecoder().decode(firebaseConfig);
                        String jsonString = new String(decodedBytes, StandardCharsets.UTF_8).trim();

                        LOGGER.info("Decoded content length: {} bytes", jsonString.length());
                        LOGGER.info("Content starts with: {}", jsonString.substring(0, Math.min(100, jsonString.length())));

                        // Validate JSON structure
                        if (!jsonString.startsWith("{")) {
                            throw new RuntimeException("Decoded content is not valid JSON (doesn't start with '{')");
                        }

                        credentialsStream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));

                    } catch (IllegalArgumentException e) {
                        // Not Base64, try treating as direct JSON
                        LOGGER.info("Not Base64-encoded, treating as direct JSON string");
                        String trimmed = firebaseConfig.trim();

                        LOGGER.info("Direct JSON content length: {} bytes", trimmed.length());
                        LOGGER.info("Content starts with: {}", trimmed.substring(0, Math.min(100, trimmed.length())));

                        if (!trimmed.startsWith("{")) {
                            throw new RuntimeException("Config is not valid JSON (doesn't start with '{')");
                        }

                        credentialsStream = new ByteArrayInputStream(trimmed.getBytes(StandardCharsets.UTF_8));
                    }
                }
            } else {
                // Fall back to classpath
                LOGGER.info("Loading Firebase credentials from classpath");
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");

                if (!resource.exists()) {
                    throw new RuntimeException("Firebase credentials not found in classpath and FIREBASE_CONFIG not set");
                }

                credentialsStream = resource.getInputStream();
            }

            GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream);
            LOGGER.info("✓ Successfully loaded Google credentials");
            return credentials;

        } catch (Exception e) {
            LOGGER.error("Failed to load credentials: {}", e.getMessage());
            throw new IOException("Failed to load Firebase credentials", e);
        } finally {
            if (credentialsStream != null) {
                try {
                    credentialsStream.close();
                } catch (IOException e) {
                    LOGGER.warn("Failed to close credentials stream", e);
                }
            }
        }
    }
}