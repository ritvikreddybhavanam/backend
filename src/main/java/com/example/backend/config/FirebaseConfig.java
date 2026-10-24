
package com.example.backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initializeFirebase() throws Exception {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        String credentialsPath =
                System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

        InputStream serviceAccount = null;

        if (credentialsPath != null && !credentialsPath.isBlank()) {
            Path path = Path.of(credentialsPath);

            if (Files.isRegularFile(path)) {
                serviceAccount = Files.newInputStream(path);
            }
        } else {
            serviceAccount = getClass()
                    .getClassLoader()
                    .getResourceAsStream("firebase-service-account.json");
        }

        if (serviceAccount == null) {
            System.out.println(
                    "[WARN] Firebase credentials not found. "
                    + "Firebase Admin features are disabled."
            );
            return;
        }

        try (InputStream credentials = serviceAccount) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(
                            GoogleCredentials.fromStream(credentials)
                    )
                    .build();

            FirebaseApp.initializeApp(options);
            System.out.println("[INFO] Firebase Admin initialized.");
        }
    }
}
