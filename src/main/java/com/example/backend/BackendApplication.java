package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
public class BackendApplication {

    static {
        loadDotenv();
    }

    public static void main(String[] args) {
        loadDotenv();
        SpringApplication.run(BackendApplication.class, args);
    }

    private static void loadDotenv() {
        String[] candidatePaths = {".env", "backend/.env", "../.env"};
        for (String candidate : candidatePaths) {
            try {
                Path path = Paths.get(candidate).toAbsolutePath().normalize();
                if (Files.isRegularFile(path)) {
                    try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            line = line.trim();
                            if (line.isEmpty() || line.startsWith("#")) {
                                continue;
                            }
                            int eqIdx = line.indexOf('=');
                            if (eqIdx > 0) {
                                String key = line.substring(0, eqIdx).trim();
                                String val = line.substring(eqIdx + 1).trim();
                                if ((val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) ||
                                    (val.startsWith("'") && val.endsWith("'") && val.length() >= 2)) {
                                    val = val.substring(1, val.length() - 1);
                                }
                                if (System.getenv(key) == null && System.getProperty(key) == null) {
                                    System.setProperty(key, val);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
                // Ignore any file access errors during dotenv discovery
            }
        }
    }
}
