package org.desha.app.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.desha.app.exception.FileNotFoundException;
import org.desha.app.exception.FileUploadException;
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

    private static final String RESOURCES_FOLDER = "src/main/resources/";

    public Uni<File> getFile(String uploadDirectory, String fileName) {
        return Uni.createFrom().item(() -> {
            Path filePath = Paths.get(RESOURCES_FOLDER, uploadDirectory, fileName);

            if (!Files.exists(filePath)) {
                log.warn("Requested file not found: {}", filePath);
                throw new FileNotFoundException("Fichier introuvable: " + fileName);
            }

            return filePath.toFile();
        });
    }

    public Uni<String> uploadFile(String uploadDirectory, FileUpload file) {
        // Sauvegarde du fichier
        return Uni.createFrom().item(() -> {
            final String fileName = UUID.randomUUID() + "_" + file.fileName();
            Path destination = Paths.get(RESOURCES_FOLDER, uploadDirectory, fileName);

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
                throw new FileUploadException("Erreur lors de l'upload du fichier " + fileName);
            }
        });
    }

    public void deleteFile(String folder, String fileName) throws IOException {
        log.info("Suppression du fichier {}", fileName);
        FileUtils.forceDelete(Paths.get(RESOURCES_FOLDER, folder, fileName).toFile());
    }
}
