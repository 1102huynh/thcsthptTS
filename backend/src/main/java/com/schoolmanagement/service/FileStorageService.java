package com.schoolmanagement.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * Local-filesystem storage for {@link com.schoolmanagement.entity.DocumentAttachment}
 * files, per IMPLEMENTATION_PLAN.md 3.9 (chose filesystem over MinIO — this
 * app deploys as a single self-hosted instance, same reasoning as the
 * in-memory admission rate limiter in 3.7; MinIO would add real
 * infrastructure for a scaling need that doesn't exist yet).
 *
 * <p>Every stored filename is server-generated ({@link UUID}), never derived
 * from the client-supplied original filename — accepting a client filename
 * as-is (or embedding it in a path) is a classic path-traversal vector
 * ("../../etc/passwd") and a collision risk. The original filename is kept
 * only as {@link com.schoolmanagement.entity.DocumentAttachment#getFileName()}
 * metadata, never touched by the filesystem layer.
 */
@Service
public class FileStorageService {

    @Value("${app.uploads.dir}")
    private String uploadsDir;

    private Path root;

    @PostConstruct
    void init() {
        try {
            root = Paths.get(uploadsDir).toAbsolutePath().normalize();
            Files.createDirectories(root);
        } catch (IOException ex) {
            throw new IllegalStateException("Could not create upload directory: " + uploadsDir, ex);
        }
    }

    /** @return the generated stored filename (UUID + original extension, lower-cased). */
    public String store(MultipartFile file) {
        String extension = extensionOf(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);

        try {
            Path target = root.resolve(storedFileName).normalize();
            // Belt-and-braces: storedFileName is our own UUID, so this can't
            // actually escape `root`, but a resolved path that ends up outside
            // the upload directory is refused outright rather than trusted.
            if (!target.startsWith(root)) {
                throw new IllegalStateException("Resolved upload path escaped the upload directory");
            }
            Files.copy(file.getInputStream(), target);
            return storedFileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to store uploaded file", ex);
        }
    }

    public Resource load(String storedFileName) {
        try {
            Path file = root.resolve(storedFileName).normalize();
            if (!file.startsWith(root)) {
                throw new IllegalStateException("Resolved download path escaped the upload directory");
            }
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new IllegalStateException("Stored file is missing or unreadable: " + storedFileName);
            }
            return resource;
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("Failed to load stored file: " + storedFileName, ex);
        }
    }

    public void delete(String storedFileName) {
        try {
            Path file = root.resolve(storedFileName).normalize();
            if (!file.startsWith(root)) {
                throw new IllegalStateException("Resolved delete path escaped the upload directory");
            }
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to delete stored file: " + storedFileName, ex);
        }
    }

    private String extensionOf(String originalFilename) {
        if (originalFilename == null) {
            return "";
        }
        int dot = originalFilename.lastIndexOf('.');
        if (dot < 0 || dot == originalFilename.length() - 1) {
            return "";
        }
        return originalFilename.substring(dot + 1).toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
