package org.rocs.asa.utils.security.configuration.fcm.configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

/**
 * {@code FcmConfiguration} is responsible for initializing the Firebase SDK
 * for the application. It loads the service account credentials from the classpath
 * and ensures that Firebase is only initialized once.
 */
@Configuration
public class FcmConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(FcmConfiguration.class);

    /**
     * Initializes Firebase when the Spring context is fully loaded.
     * <p>
     * This method performs the following:
     * <ul>
     *     <li>Checks if Firebase is already initialized.</li>
     *     <li>Loads the service account JSON from the classpath.</li>
     *     <li>Initializes FirebaseApp with credentials and project ID.</li>
     *     <li>Logs initialization details for monitoring.</li>
     * </ul>
     * <p>
     * Throws a {@code RuntimeException} if initialization fails or the service
     * account file is missing.
     */
    @PostConstruct
    public void initialize() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                LOGGER.info("Initializing Firebase...");

                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");

                if (!resource.exists()) {
                    LOGGER.error("firebase-service-account.json not found in classpath");
                    throw new RuntimeException("firebase-service-account.json not found");
                }

                LOGGER.info("Loading Firebase service account from: {}", resource.getFilename());

                GoogleCredentials credentials = GoogleCredentials.fromStream(resource.getInputStream())
                        .createScoped("https://www.googleapis.com/auth/firebase.messaging");

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .setProjectId("appointment-notification-cc54d")
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
            throw new RuntimeException("Failed to initialize Firebase", e);
        } catch (Exception e) {
            LOGGER.error("Failed to initialize Firebase - General Error: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }
}
