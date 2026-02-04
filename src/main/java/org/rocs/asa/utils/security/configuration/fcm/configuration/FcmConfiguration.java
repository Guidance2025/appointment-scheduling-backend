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

/**
 * {@code FcmConfiguration} is responsible for initializing the Firebase SDK
 * for the application. It loads the service account credentials from either
 * environment variables (production) or classpath (development).
 */
@Configuration
public class FcmConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmConfiguration.class);

    @Value("${firebase.service-account-file:}")
    private String firebaseConfig;

    @Value("${firebase.project-id:}")
    private String projectId;

    /**
     * Initializes Firebase when the Spring context is fully loaded.
     * <p>
     * This method performs the following:
     * <ul>
     *     <li>Checks if Firebase is already initialized.</li>
     *     <li>Loads credentials from environment variable or classpath.</li>
     *     <li>Initializes FirebaseApp with credentials and project ID.</li>
     *     <li>Logs initialization details for monitoring.</li>
     * </ul>
     * <p>
     * Throws a {@code RuntimeException} if initialization fails.
     */
    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                LOGGER.info("Initializing Firebase...");

                GoogleCredentials credentials = loadCredentials();

                // Use project ID from environment or fall back to config file
                String finalProjectId = (projectId != null && !projectId.isEmpty())
                        ? projectId
                        : "appointment-notification-cc54d";

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId(finalProjectId)
                        .build();

                FirebaseApp app = FirebaseApp.initializeApp(options);
                LOGGER.info("Backend project ID: {}", app.getOptions().getProjectId());
                LOGGER.info("Firebase application initialized successfully for project: {}", options.getProjectId());
                LOGGER.info("Firebase app name: {}", app.getName());

            } else {
                LOGGER.info("Firebase already initialized. Number of apps: {}", FirebaseApp.getApps().size());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to initialize Firebase - IO Error: {}", e.getMessage(), e);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Firebase - General Error: {}", e.getMessage(), e);
        }
    }

    /**
     * Loads Google credentials from environment variable (production) or classpath (development).
     *
     * @return GoogleCredentials instance
     * @throws IOException if credentials cannot be loaded
     */
    /**
     * Loads Google credentials from environment variable (production) or classpath (development).
     *
     * @return GoogleCredentials instance
     * @throws IOException if credentials cannot be loaded
     */
    private GoogleCredentials loadCredentials() throws IOException {
        InputStream credentialsStream;

        // Try to load from environment variable first (production)
        if (firebaseConfig != null && !firebaseConfig.isEmpty()) {
            LOGGER.info("Loading Firebase credentials from environment variable");
            credentialsStream = new ByteArrayInputStream(firebaseConfig.getBytes(StandardCharsets.UTF_8));
        }
        // Fall back to classpath resource (development)
        else {
            LOGGER.info("Loading Firebase credentials from classpath");
            ClassPathResource resource = new ClassPathResource("firebase-service-account.json");

            if (!resource.exists()) {
                LOGGER.error("firebase-service-account.json not found in classpath and FIREBASE_CONFIG not set");
                throw new RuntimeException("Firebase credentials not found. Set FIREBASE_CONFIG environment variable or add firebase-service-account.json to classpath");
            }

            LOGGER.info("Loading Firebase service account from: {}", resource.getFilename());
            credentialsStream = resource.getInputStream();
        }

        // Remove the createScoped() call - Firebase Admin SDK handles scopes automatically
        return GoogleCredentials.fromStream(credentialsStream);
    }
}
