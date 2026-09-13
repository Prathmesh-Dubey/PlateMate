package com.mess.app.controller;

import com.mess.app.dto.request.CandidateRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.CandidateResponse;
import com.mess.app.service.CandidateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class CandidateController {

    private final CandidateService candidateService;

    @PostMapping
    public ResponseEntity<ApiResponse<CandidateResponse>> createCandidate(
            @Valid @RequestBody CandidateRequest request) {
        CandidateResponse response = candidateService.createCandidate(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Candidate created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> getAllCandidates() {
        List<CandidateResponse> candidates = candidateService.getAllCandidates();
        return ResponseEntity.ok(ApiResponse.success(candidates, "Candidates retrieved successfully"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> getActiveCandidates() {
        List<CandidateResponse> candidates = candidateService.getActiveCandidates();
        return ResponseEntity.ok(ApiResponse.success(candidates, "Active candidates retrieved successfully"));
    }

    @GetMapping("/left")
    public ResponseEntity<ApiResponse<List<CandidateResponse>>> getLeftCandidates() {
        List<CandidateResponse> candidates = candidateService.getLeftCandidates();
        return ResponseEntity.ok(ApiResponse.success(candidates, "Left candidates retrieved successfully"));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getStats() {
        Map<String, Long> stats = Map.of(
                "active", candidateService.getActiveCount(),
                "left", candidateService.getLeftCount(),
                "total", candidateService.getTotalCount());
        return ResponseEntity.ok(ApiResponse.success(stats, "Statistics retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateResponse>> getCandidateById(@PathVariable String id) {  // ✅ String
        CandidateResponse response = candidateService.getCandidateById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Candidate retrieved successfully"));
    }

    @GetMapping("/by-id/{candidateId}")
    public ResponseEntity<ApiResponse<CandidateResponse>> getCandidateByCandidateId(
            @PathVariable String candidateId) {
        CandidateResponse response = candidateService.getCandidateByCandidateId(candidateId);
        return ResponseEntity.ok(ApiResponse.success(response, "Candidate retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CandidateResponse>> updateCandidate(
            @PathVariable String id,  // ✅ String
            @Valid @RequestBody CandidateRequest request) {
        CandidateResponse response = candidateService.updateCandidate(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Candidate updated successfully"));
    }

    @PatchMapping("/{id}/mark-left")
    public ResponseEntity<ApiResponse<CandidateResponse>> markAsLeft(
            @PathVariable String id,  // ✅ String
            @RequestParam(required = false) LocalDate leavingDate) {
        CandidateResponse response = candidateService.markAsLeft(id, leavingDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Candidate marked as left successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCandidate(@PathVariable String id) {  // ✅ String
        candidateService.deleteCandidate(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Candidate deleted successfully"));
    }

    // ==================== PROFILE IMAGE ====================

    /** Upload a profile picture from the local device (multipart field "file"). */
    @PostMapping(value = "/{id}/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CandidateResponse>> uploadProfileImage(
            @PathVariable String id,
            @RequestParam("file") MultipartFile file) {
        CandidateResponse response = candidateService.uploadProfileImage(id, file);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile image uploaded successfully"));
    }

    /** Set the profile picture from an image URL (empty value clears it). */
    @PatchMapping("/{id}/profile-image")
    public ResponseEntity<ApiResponse<CandidateResponse>> setProfileImageUrl(
            @PathVariable String id,
            @RequestParam(defaultValue = "") String imageUrl) {
        CandidateResponse response = candidateService.setProfileImageUrl(id, imageUrl);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile image updated successfully"));
    }

    @DeleteMapping("/{id}/profile-image")
    public ResponseEntity<ApiResponse<CandidateResponse>> removeProfileImage(@PathVariable String id) {
        CandidateResponse response = candidateService.removeProfileImage(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile image removed successfully"));
    }
}
