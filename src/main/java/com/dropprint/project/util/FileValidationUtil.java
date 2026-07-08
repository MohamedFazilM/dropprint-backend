package com.dropprint.project.util;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public class FileValidationUtil {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");
    private static final List<String> ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty.");
        }

        // 1. Validate File Size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the limit of 5 MB.");
        }

        // 2. Validate MIME Type
        String mimeType = file.getContentType();
        if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new IllegalArgumentException("Unsupported file type. Only JPG, JPEG, PNG, and WEBP are allowed.");
        }

        // 3. Validate File Extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid file name. Extension is missing.");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported file extension. Only JPG, JPEG, PNG, and WEBP are allowed.");
        }
    }

    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unnamed";
        }
        // Keep only alphanumeric characters, dots, underscores and dashes
        String cleanName = filename.replaceAll("[^a-zA-Z0-9._-]", "_");
        // Remove duplicate dots to prevent path traversal issues
        while (cleanName.contains("..")) {
            cleanName = cleanName.replace("..", ".");
        }
        return cleanName;
    }
}
