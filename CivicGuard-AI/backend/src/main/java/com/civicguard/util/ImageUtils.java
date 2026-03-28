package com.civicguard.util;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

/**
 * Utility for image handling — saving, validating, resizing,
 * and managing complaint photo evidence files.
 */
@Component
public class ImageUtils {

    private static final Set<String> ALLOWED_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/webp"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    /**
     * Save an uploaded image with a unique filename.
     * Creates date-based subdirectories for organization.
     *
     * @param file      The uploaded multipart file
     * @param baseDir   Base upload directory
     * @return          Full path to the saved file
     */
    public String saveImage(MultipartFile file, String baseDir) throws IOException {
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
            throw new IOException("Invalid image type. Allowed: JPEG, PNG, WebP");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IOException("Image too large. Maximum size: 10MB");
        }

        // Create date-based subdirectory: uploads/2026/03/28/
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadPath = Paths.get(baseDir, dateDir);
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = "CG_" + UUID.randomUUID().toString().substring(0, 12) +
            "_" + System.currentTimeMillis() + "." + extension;

        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Generate thumbnail for dashboard previews
        createThumbnail(filePath.toString(), uploadPath.resolve("thumb_" + filename).toString());

        return filePath.toString();
    }

    /**
     * Create a 300px thumbnail for dashboard and list views.
     */
    public void createThumbnail(String sourcePath, String thumbPath) {
        try {
            Thumbnails.of(sourcePath)
                .size(300, 300)
                .outputQuality(0.8)
                .toFile(thumbPath);
        } catch (IOException e) {
            System.err.println("Thumbnail creation failed: " + e.getMessage());
        }
    }

    /**
     * Get file extension from filename.
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "jpg";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Validate if an image file exists at the given path.
     */
    public boolean imageExists(String path) {
        return path != null && Files.exists(Paths.get(path));
    }

    /**
     * Delete an image file (for cleanup/GDPR compliance).
     */
    public boolean deleteImage(String path) {
        try {
            return Files.deleteIfExists(Paths.get(path));
        } catch (IOException e) {
            System.err.println("Failed to delete image: " + e.getMessage());
            return false;
        }
    }
}
