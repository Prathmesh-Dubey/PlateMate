package com.mess.app.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileUploadUtil {

    /** Image content types accepted for profile pictures (users, candidates, staff). */
    public static final Set<String> ALLOWED_IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/jpg", "image/png", "image/webp", "image/gif");

    /** Image file extensions accepted for profile pictures. */
    public static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");

    @Value("${storage.type:local}")
    private String storageType;

    // Local storage config
    @Value("${storage.local.upload-dir:uploads/stock-images}")
    private String localUploadDir;

    @Value("${storage.local.base-url:/uploads/stock-images/}")
    private String localBaseUrl;

    // Supabase storage config
    @Value("${supabase.storage.url:}")
    private String supabaseStorageUrl;

    @Value("${supabase.storage.api-key:}")
    private String supabaseApiKey;

    @Value("${supabase.storage.bucket-name:stock-images}")
    private String supabaseBucketName;

    /** Maximum accepted size for a profile image upload, in megabytes. */
    @Value("${app.upload.profile-image.max-size-mb:5}")
    private long profileImageMaxSizeMb;

    private final OkHttpClient okHttpClient = new OkHttpClient();

    /**
     * Upload image - supports both local and Supabase storage
     */
    public String uploadStockImage(MultipartFile file, String fileName) throws IOException {
        String extension = getFileExtension(file.getOriginalFilename());
        String newFileName = "stock/" + fileName + "_" + System.currentTimeMillis() + "." + extension;

        if ("supabase".equalsIgnoreCase(storageType)) {
            return uploadToSupabase(file, newFileName);
        } else {
            return uploadToLocal(file, newFileName);
        }
    }

    /**
     * Upload expense receipt / invoice (supports images and PDFs)
     */
    public String uploadExpenseReceipt(MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "document";
        String extension = getFileExtension(originalName);
        String safeBaseName = originalName.replaceAll("[^a-zA-Z0-9.-]", "_");
        if (safeBaseName.contains(".")) {
            safeBaseName = safeBaseName.substring(0, safeBaseName.lastIndexOf("."));
        }
        if (safeBaseName.length() > 30) {
            safeBaseName = safeBaseName.substring(0, 30);
        }
        String newFileName = "receipts/" + safeBaseName + "_" + System.currentTimeMillis() + "." + extension;

        if ("supabase".equalsIgnoreCase(storageType)) {
            return uploadToSupabase(file, newFileName);
        } else {
            return uploadToLocal(file, newFileName);
        }
    }

    // ==================== PROFILE IMAGES ====================

    /**
     * Validates that the uploaded file is an image of an accepted type and size.
     *
     * @throws IllegalArgumentException with a user-friendly message (mapped to HTTP 400)
     */
    public void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        String contentType = file.getContentType() != null ? file.getContentType().toLowerCase(Locale.ROOT) : "";
        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);

        boolean typeAllowed = ALLOWED_IMAGE_CONTENT_TYPES.contains(contentType);
        boolean extensionAllowed = ALLOWED_IMAGE_EXTENSIONS.contains(extension);
        if (!typeAllowed && !extensionAllowed) {
            throw new IllegalArgumentException(
                    "Unsupported image type. Allowed formats: JPG, JPEG, PNG, WebP, GIF");
        }

        long maxBytes = profileImageMaxSizeMb * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new IllegalArgumentException(
                    "Image is too large. Maximum size is " + profileImageMaxSizeMb + " MB");
        }
    }

    /**
     * Stores a profile image for any owner type (user, candidate, staff) using the
     * same storage mechanism as stock images (local folder or Supabase bucket).
     *
     * @param file      uploaded image
     * @param ownerType e.g. "user", "candidate", "staff"
     * @param ownerKey  a stable identifier of the owner (id / code)
     * @return the stored image reference (relative /uploads/... path for local storage, absolute URL for Supabase)
     */
    public String uploadProfileImage(MultipartFile file, String ownerType, String ownerKey) throws IOException {
        validateImageFile(file);

        String extension = getFileExtension(file.getOriginalFilename()).toLowerCase(Locale.ROOT);
        if (!ALLOWED_IMAGE_EXTENSIONS.contains(extension)) {
            // Derive the extension from the content type when the file name is unhelpful
            String contentType = file.getContentType() != null ? file.getContentType().toLowerCase(Locale.ROOT) : "";
            extension = switch (contentType) {
                case "image/png" -> "png";
                case "image/webp" -> "webp";
                case "image/gif" -> "gif";
                default -> "jpg";
            };
        }
        if ("jpeg".equals(extension)) {
            extension = "jpg";
        }

        String safeType = ownerType == null ? "profile" : ownerType.replaceAll("[^a-zA-Z0-9_-]", "_");
        String safeKey = ownerKey == null ? "unknown" : ownerKey.replaceAll("[^a-zA-Z0-9_-]", "_");
        if (safeKey.length() > 40) {
            safeKey = safeKey.substring(0, 40);
        }
        String newFileName = "profiles/" + safeType + "_" + safeKey + "_" + System.currentTimeMillis() + "." + extension;

        if ("supabase".equalsIgnoreCase(storageType)) {
            return uploadToSupabase(file, newFileName);
        } else {
            return uploadToLocal(file, newFileName);
        }
    }

    /**
     * Checks that a user supplied image reference is acceptable to persist:
     * an absolute http(s) URL or a path served by this backend (/uploads/...).
     * Temporary browser references (blob:, data:) are rejected because they
     * would not survive a page reload.
     */
    public static boolean isValidImageReference(String value) {
        if (value == null) {
            return false;
        }
        String v = value.trim();
        if (v.isEmpty() || v.length() > 2048) {
            return false;
        }
        if (v.startsWith("/uploads/")) {
            return true;
        }
        String lower = v.toLowerCase(Locale.ROOT);
        if (lower.startsWith("blob:") || lower.startsWith("data:") || lower.startsWith("javascript:")) {
            return false;
        }
        try {
            URI uri = new URI(v);
            String scheme = uri.getScheme();
            return ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    /** True when the reference points to a file stored in the local upload folder of this backend. */
    public boolean isLocallyStored(String imageUrl) {
        return imageUrl != null && imageUrl.startsWith(localBaseUrl);
    }

    /**
     * Deletes a previously stored profile image if it lives in our own storage.
     * Never throws - a failed cleanup must not break the profile update.
     */
    public void deleteStoredImageQuietly(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        try {
            if (imageUrl.contains("supabase.co") && "supabase".equalsIgnoreCase(storageType)) {
                deleteFromSupabase(imageUrl);
            } else if (isLocallyStored(imageUrl)) {
                deleteFromLocal(imageUrl);
            }
        } catch (Exception e) {
            log.warn("Could not delete old image {}: {}", imageUrl, e.getMessage());
        }
    }

    /**
     * Upload to local file system
     */
    private String uploadToLocal(MultipartFile file, String fileName) throws IOException {
        Path uploadPath = Paths.get(localUploadDir);
        Path filePath = uploadPath.resolve(fileName);

        if (filePath.getParent() != null && !Files.exists(filePath.getParent())) {
            Files.createDirectories(filePath.getParent());
        }

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        log.info("File uploaded to local storage: {}", filePath.toString());
        return localBaseUrl + fileName;
    }

    /**
     * Upload to Supabase cloud storage
     */
    private String uploadToSupabase(MultipartFile file, String fileName) throws IOException {
        if (supabaseStorageUrl.isEmpty() || supabaseApiKey.isEmpty()) {
            log.warn("Supabase credentials not configured, falling back to local storage");
            return uploadToLocal(file, fileName);
        }

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(file.getBytes(), MediaType.parse(file.getContentType())))
                .build();

        Request request = new Request.Builder()
                .url(supabaseStorageUrl + "/object/" + supabaseBucketName + "/" + fileName)
                .addHeader("Authorization", "Bearer " + supabaseApiKey)
                .post(requestBody)
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("Supabase upload failed: {}, falling back to local storage", response.message());
                return uploadToLocal(file, fileName);
            }
            String imageUrl = supabaseStorageUrl + "/object/public/" + supabaseBucketName + "/" + fileName;
            log.info("File uploaded to Supabase: {}", imageUrl);
            return imageUrl;
        }
    }

    /**
     * Delete image - supports both local and Supabase
     */
    public void deleteStockImage(String imageUrl) throws IOException {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        if ("supabase".equalsIgnoreCase(storageType) && imageUrl.contains("supabase.co")) {
            deleteFromSupabase(imageUrl);
        } else {
            deleteFromLocal(imageUrl);
        }
    }

    /**
     * Delete from local file system. Resolves the path relative to the configured
     * base URL so files stored in sub folders (stock/, receipts/, profiles/) are found.
     */
    private void deleteFromLocal(String imageUrl) throws IOException {
        Path filePath = resolveLocalPath(imageUrl);
        if (filePath == null) {
            return;
        }

        if (Files.exists(filePath)) {
            Files.delete(filePath);
            log.info("File deleted from local storage: {}", filePath.toString());
        }
    }

    /**
     * Maps a stored reference back to the file on disk, refusing anything that
     * would escape the upload directory.
     */
    private Path resolveLocalPath(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty() || imageUrl.startsWith("http")) {
            return null;
        }
        String relative = imageUrl.startsWith(localBaseUrl)
                ? imageUrl.substring(localBaseUrl.length())
                : imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        Path uploadRoot = Paths.get(localUploadDir).toAbsolutePath().normalize();
        Path candidate = uploadRoot.resolve(relative).normalize();
        if (!candidate.startsWith(uploadRoot)) {
            log.warn("Refusing to resolve path outside upload directory: {}", imageUrl);
            return null;
        }
        return candidate;
    }

    /**
     * Delete from Supabase cloud storage
     */
    private void deleteFromSupabase(String imageUrl) throws IOException {
        // Extract file path from URL
        String filePath = imageUrl.replace(supabaseStorageUrl + "/object/public/" + supabaseBucketName + "/", "");

        Request request = new Request.Builder()
                .url(supabaseStorageUrl + "/object/" + supabaseBucketName + "/" + filePath)
                .addHeader("Authorization", "Bearer " + supabaseApiKey)
                .delete()
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.warn("Failed to delete from Supabase: {}", response.message());
            } else {
                log.info("File deleted from Supabase: {}", filePath);
            }
        }
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "jpg";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Get absolute file path for local storage
     */
    public String getAbsolutePath(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        if (imageUrl.contains("supabase.co")) {
            return imageUrl; // Cloud URL
        }

        Path path = resolveLocalPath(imageUrl);
        return path != null ? path.toString() : null;
    }

    /**
     * Check if file exists
     */
    public boolean fileExists(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return false;
        }

        if (imageUrl.contains("supabase.co")) {
            return true; // Assume cloud file exists
        }

        Path path = resolveLocalPath(imageUrl);
        return path != null && Files.exists(path);
    }
}
