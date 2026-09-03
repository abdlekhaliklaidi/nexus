package buy01.media_service.controller;

import buy01.media_service.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("api/media")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;
    private static final Path UPLOAD_DIR = Paths.get("/app/uploads").toAbsolutePath().normalize();

    /**
     * PUBLIC ENDPOINT: Registration Avatar Upload
     * Unauthenticated users use this during sign-up.
     */
    @PostMapping(
            value = "/avatars/public",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> uploadPublicAvatar(
            @RequestPart("avatar") MultipartFile avatar
    ) {
        try {
            String avatarUrl = mediaService.uploadSingleAvatar(avatar);
            return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload avatar: " + e.getMessage()));
        }
    }

    /**
     * PROTECTED ENDPOINT: Multiple Images Upload (For Products/Sellers)
     */
    @PostMapping(
            value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public List<String> uploadImages(
            @RequestPart("images") MultipartFile[] images
    ) {
        return mediaService.upload(images);
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String id
    ) throws IOException {

        Path path = UPLOAD_DIR.resolve(id).normalize();

        // Prevent Path Traversal attacks
        if (!Files.exists(path) || !path.startsWith(UPLOAD_DIR) || Files.isDirectory(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(path);

        String contentType = Files.probeContentType(path);
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/images/{id}/download")
    public ResponseEntity<Resource> downloadImage(
            @PathVariable String id
    ) throws IOException {

        Path path = UPLOAD_DIR.resolve(id).normalize();

        if (!Files.exists(path) || !path.startsWith(UPLOAD_DIR) || Files.isDirectory(path)) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(path);

        // Sanitize header filename output
        String safeFilename = path.getFileName().toString();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFilename + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/images/{id}")
    public ResponseEntity<Void> deleteImage(@PathVariable String id) {
        boolean deleted = mediaService.deleteImage(id);
        if (!deleted) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}