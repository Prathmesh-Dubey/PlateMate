package com.mess.app.controller;

import com.mess.app.dto.request.CollegeBillingRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.CollegeBillingResponse;
import com.mess.app.dto.response.CollegeBillingSummaryResponse;
import com.mess.app.service.CollegeBillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/college-billing")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class CollegeBillingController {

    private final CollegeBillingService collegeBillingService;

    @PostMapping
    public ResponseEntity<ApiResponse<CollegeBillingResponse>> createCollegeBilling(
            @Valid @RequestBody CollegeBillingRequest request) {
        CollegeBillingResponse response = collegeBillingService.createCollegeBilling(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "College billing record created successfully"));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CollegeBillingResponse>>> getAllCollegeBilling(
            @RequestParam(required = false) String college,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        List<CollegeBillingResponse> responses = collegeBillingService.getAllCollegeBilling(college, month, year);
        return ResponseEntity.ok(ApiResponse.success(responses, "College billing records retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CollegeBillingResponse>> getCollegeBillingById(
            @PathVariable String id) {
        CollegeBillingResponse response = collegeBillingService.getCollegeBillingById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "College billing record retrieved successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CollegeBillingResponse>> updateCollegeBilling(
            @PathVariable String id,
            @Valid @RequestBody CollegeBillingRequest request) {
        CollegeBillingResponse response = collegeBillingService.updateCollegeBilling(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "College billing record updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCollegeBilling(
            @PathVariable String id) {
        collegeBillingService.deleteCollegeBilling(id);
        return ResponseEntity.ok(ApiResponse.success(null, "College billing record deleted successfully"));
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<CollegeBillingSummaryResponse>> getSummary(
            @RequestParam(required = false) String college,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        CollegeBillingSummaryResponse response = collegeBillingService.getSummary(college, month, year);
        return ResponseEntity.ok(ApiResponse.success(response, "College billing summary retrieved successfully"));
    }

    @GetMapping("/colleges")
    public ResponseEntity<ApiResponse<List<String>>> getColleges() {
        List<String> colleges = collegeBillingService.getColleges();
        return ResponseEntity.ok(ApiResponse.success(colleges, "Colleges retrieved successfully"));
    }
}
