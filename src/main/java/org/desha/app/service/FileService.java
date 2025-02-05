package org.desha.app.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@ApplicationScoped
public class FileService {

    public Uni<File> getFile(String uploadDirectory, String fileName) {
        return Uni.createFrom().item(() -> {
            Path filePath = Paths.get(uploadDirectory, fileName);

            if (!Files.exists(filePath)) {
                log.warn("Requested file not found: {}", filePath);
                throw new RuntimeException("File not found: " + fileName);
            }

            return filePath.toFile();
        });
    }

    public Uni<String> uploadFile(String uploadDirectory, FileUpload file) {
        // Sauvegarde du fichier
        return Uni.createFrom().item(() -> {
            final String fileName = UUID.randomUUID() + "_" + file.fileName();
            Path destination = Paths.get(uploadDirectory + fileName);

            try {
                File uploadDir = new File(uploadDirectory);
                if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                    throw new IOException("Failed to create upload directory: " + uploadDirectory);
                }

                Files.move(file.uploadedFile(), destination);
                log.info("File uploaded successfully: {}", fileName);
                return fileName;
            } catch (IOException e) {
                log.error("File upload failed: {}", e.getMessage());
                throw new RuntimeException("File upload failed", e);
            }
        });
    }
}
