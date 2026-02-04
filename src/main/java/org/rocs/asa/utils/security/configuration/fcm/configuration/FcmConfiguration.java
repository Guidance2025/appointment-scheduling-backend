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
                LOGGER.info("Config length: {}", firebaseConfig != null ? firebaseConfig.length() : 0);
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
                LOGGER.info("Firebase already initialized. Number of apps: {}", FirebaseApp.getApps().size());
            }
        } catch (IOException e) {
            LOGGER.error("==============================================");
            LOGGER.error("✗ Failed to initialize Firebase - IO Error");
            LOGGER.error("✗ Error: {}", e.getMessage(), e);
            LOGGER.error("==============================================");
            throw new RuntimeException("Failed to initialize Firebase", e);
        } catch (Exception e) {
            LOGGER.error("==============================================");
            LOGGER.error("✗ Failed to initialize Firebase - General Error");
            LOGGER.error("✗ Error: {}", e.getMessage(), e);
            LOGGER.error("==============================================");
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    private GoogleCredentials loadCredentials() throws IOException {
        InputStream credentialsStream;

        if (firebaseConfig != null && !firebaseConfig.isEmpty()) {
            LOGGER.info("Loading Firebase credentials from environment variable");
            LOGGER.info("Config starts with: {}", firebaseConfig.substring(0, Math.min(50, firebaseConfig.length())));
            credentialsStream = new ByteArrayInputStream(firebaseConfig.getBytes(StandardCharsets.UTF_8));
        } else {
            LOGGER.info("Loading Firebase credentials from classpath");
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");

            if (!resource.exists()) {
                LOGGER.error("firebase-service-account.json not found and FIREBASE_CONFIG not set");
                throw new RuntimeException("Firebase credentials not found");
            }

            LOGGER.info("Loading Firebase service account from: {}", resource.getFilename());
            credentialsStream = resource.getInputStream();
        }

        return GoogleCredentials.fromStream(credentialsStream);
    }
}
