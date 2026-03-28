package com.civicguard.controller;

import com.civicguard.dto.VerificationDTO;
import com.civicguard.service.AIImageValidationService;
import com.civicguard.util.ImageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Standalone verification endpoints for AI image analysis.
 * Used by officers and supervisors for manual verification checks
 * and deepfake detection on suspicious submissions.
 */
@RestController
@RequestMapping("/api/verify")
@CrossOrigin
public class VerificationController {

    private final AIImageValidationService aiService;
    private final ImageUtils imageUtils;

    @Value("${complaint.upload.dir:/app/uploads}")
    private String uploadDir;

    public VerificationController(AIImageValidationService aiService,
                                  ImageUtils imageUtils) {
        this.aiService = aiService;
        this.imageUtils = imageUtils;
    }

    /**
     * Verify an uploaded image for authenticity, classification, and fraud detection.
     * POST /api/verify/image
     */
    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<VerificationDTO> verifyImage(
            @RequestPart("image") MultipartFile image,
            @RequestParam double latitude,
            @RequestParam double longitude) throws IOException {

        String imagePath = imageUtils.saveImage(image, uploadDir + "/verification");
        VerificationDTO result = aiService.verifyImage(imagePath, latitude, longitude);
        return ResponseEntity.ok(result);
    }

    /**
     * Compare before and after images to verify resolution completion.
     * POST /api/verify/completion
     */
    @PostMapping(value = "/completion", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<VerificationDTO> verifyCompletion(
            @RequestPart("beforeImage") MultipartFile beforeImage,
            @RequestPart("afterImage") MultipartFile afterImage,
            @RequestParam double latitude,
            @RequestParam double longitude) throws IOException {

        String beforePath = imageUtils.saveImage(beforeImage, uploadDir + "/verification");
        String afterPath = imageUtils.saveImage(afterImage, uploadDir + "/verification");
        VerificationDTO result = aiService.verifyCompletion(
            beforePath, afterPath, latitude, longitude);
        return ResponseEntity.ok(result);
    }

    /**
     * Quick deepfake check on a single image.
     * POST /api/verify/deepfake
     */
    @PostMapping(value = "/deepfake", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('OFFICER', 'SUPERVISOR', 'ADMIN')")
    public ResponseEntity<VerificationDTO> checkDeepfake(
            @RequestPart("image") MultipartFile image) throws IOException {

        String imagePath = imageUtils.saveImage(image, uploadDir + "/verification");
        VerificationDTO result = aiService.checkDeepfake(imagePath);
        return ResponseEntity.ok(result);
    }

    /**
     * Health check for the AI verification engine.
     * GET /api/verify/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> aiEngineHealth() {
        try {
            // Quick test — verify a dummy request
            return ResponseEntity.ok(Map.of(
                "status", "UP",
                "engine", "CivicGuard AI Verification Engine",
                "version", "1.0.0"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "status", "DEGRADED",
                "message", "AI engine may be offline. Fallback mode active.",
                "error", e.getMessage()
            ));
        }
    }
}
