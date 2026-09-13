package com.mess.app.controller;

import com.mess.app.dto.request.StaffAdvanceRequest;
import com.mess.app.dto.request.StaffPaymentRequest;
import com.mess.app.dto.request.StaffRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.StaffAdvanceResponse;
import com.mess.app.dto.response.StaffPaymentResponse;
import com.mess.app.dto.response.StaffResponse;
import com.mess.app.dto.response.StaffSalarySummary;
import com.mess.app.service.StaffService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class StaffController {

    private final StaffService staffService;

    // ==================== STAFF CRUD ====================

    // ✅ Create Staff (JSON - no images)
    @PostMapping("/")
    public ResponseEntity<ApiResponse<StaffResponse>> createStaff(
            @Valid @RequestBody StaffRequest request) {
        StaffResponse response = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Staff created successfully"));
    }

    // ✅ Create Staff (Multipart - with images)
    @PostMapping(value = "/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StaffResponse>> createStaffWithImages(
            @ModelAttribute @Valid StaffRequest request) {
        StaffResponse response = staffService.createStaff(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Staff created successfully"));
    }

    // ✅ Update Staff (JSON - no images) - RENAMED to updateStaffJson
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaffJson(
            @PathVariable String id,
            @Valid @RequestBody StaffRequest request) {
        StaffResponse response = staffService.updateStaff(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Staff updated successfully"));
    }

    // ✅ Update Staff (Multipart - with images) - RENAMED to updateStaffWithImages
    @PutMapping(value = "/{id}/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StaffResponse>> updateStaffWithImages(
            @PathVariable String id,
            @ModelAttribute @Valid StaffRequest request) {
        StaffResponse response = staffService.updateStaff(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Staff updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffById(@PathVariable String id) {
        StaffResponse response = staffService.getStaffById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Staff retrieved successfully"));
    }

    @GetMapping("/staff-id/{staffId}")
    public ResponseEntity<ApiResponse<StaffResponse>> getStaffByStaffId(@PathVariable String staffId) {
        StaffResponse response = staffService.getStaffByStaffId(staffId);
        return ResponseEntity.ok(ApiResponse.success(response, "Staff retrieved successfully"));
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getAllStaff() {
        List<StaffResponse> responses = staffService.getAllStaff();
        return ResponseEntity.ok(ApiResponse.success(responses, "Staff retrieved successfully"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getActiveStaff() {
        List<StaffResponse> responses = staffService.getActiveStaff();
        return ResponseEntity.ok(ApiResponse.success(responses, "Active staff retrieved successfully"));
    }

    @GetMapping("/position/{position}")
    public ResponseEntity<ApiResponse<List<StaffResponse>>> getStaffByPosition(@PathVariable String position) {
        List<StaffResponse> responses = staffService.getStaffByPosition(position);
        return ResponseEntity.ok(ApiResponse.success(responses, "Staff retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStaff(@PathVariable String id) {
        staffService.deleteStaff(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Staff deleted successfully"));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleStaffStatus(@PathVariable String id) {
        staffService.toggleStaffStatus(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Staff status toggled successfully"));
    }

    // ==================== STAFF PAYMENTS ====================

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<StaffPaymentResponse>> createPayment(
            @Valid @RequestBody StaffPaymentRequest request) {
        StaffPaymentResponse response = staffService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Payment created successfully"));
    }

    @GetMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<StaffPaymentResponse>> getPaymentById(@PathVariable String id) {
        StaffPaymentResponse response = staffService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment retrieved successfully"));
    }

    @GetMapping("/payments/staff/{staffId}")
    public ResponseEntity<ApiResponse<List<StaffPaymentResponse>>> getPaymentsByStaff(
            @PathVariable String staffId) {
        List<StaffPaymentResponse> responses = staffService.getPaymentsByStaff(staffId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Payments retrieved successfully"));
    }

    @GetMapping("/payments/staff/{staffId}/year/{year}")
    public ResponseEntity<ApiResponse<List<StaffPaymentResponse>>> getPaymentsByStaffAndYear(
            @PathVariable String staffId,
            @PathVariable int year) {
        List<StaffPaymentResponse> responses = staffService.getPaymentsByStaffAndYear(staffId, year);
        return ResponseEntity.ok(ApiResponse.success(responses, "Payments retrieved successfully"));
    }

    @GetMapping("/payments/range")
    public ResponseEntity<ApiResponse<List<StaffPaymentResponse>>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<StaffPaymentResponse> responses = staffService.getPaymentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Payments retrieved successfully"));
    }

    @GetMapping("/payments/total")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = staffService.getTotalPaymentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(total, "Total payments retrieved successfully"));
    }

    @DeleteMapping("/payments/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePayment(@PathVariable String id) {
        staffService.deletePayment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Payment deleted successfully"));
    }

    // ==================== STAFF ADVANCES ====================

    @PostMapping("/advances")
    public ResponseEntity<ApiResponse<StaffAdvanceResponse>> createAdvance(
            @Valid @RequestBody StaffAdvanceRequest request) {
        StaffAdvanceResponse response = staffService.createAdvance(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Advance created successfully"));
    }

    @PutMapping("/advances/{id}")
    public ResponseEntity<ApiResponse<StaffAdvanceResponse>> updateAdvance(
            @PathVariable String id,
            @Valid @RequestBody StaffAdvanceRequest request) {
        StaffAdvanceResponse response = staffService.updateAdvance(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Advance updated successfully"));
    }

    @GetMapping("/advances/{id}")
    public ResponseEntity<ApiResponse<StaffAdvanceResponse>> getAdvanceById(@PathVariable String id) {
        StaffAdvanceResponse response = staffService.getAdvanceById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Advance retrieved successfully"));
    }

    @GetMapping("/advances")
    public ResponseEntity<ApiResponse<List<StaffAdvanceResponse>>> getAllAdvances() {
        List<StaffAdvanceResponse> responses = staffService.getAllAdvances();
        return ResponseEntity.ok(ApiResponse.success(responses, "All advances retrieved successfully"));
    }

    @PatchMapping("/advances/{id}/approve")
    public ResponseEntity<ApiResponse<StaffAdvanceResponse>> approveAdvance(
            @PathVariable String id) {
        StaffAdvanceResponse response = staffService.approveAdvance(id, null);
        return ResponseEntity.ok(ApiResponse.success(response, "Advance approved successfully"));
    }

    @GetMapping("/advances/staff/{staffId}")
    public ResponseEntity<ApiResponse<List<StaffAdvanceResponse>>> getAdvancesByStaff(
            @PathVariable String staffId) {
        List<StaffAdvanceResponse> responses = staffService.getAdvancesByStaff(staffId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Advances retrieved successfully"));
    }

    @GetMapping("/advances/pending")
    public ResponseEntity<ApiResponse<List<StaffAdvanceResponse>>> getPendingAdvances() {
        List<StaffAdvanceResponse> responses = staffService.getPendingAdvances();
        return ResponseEntity.ok(ApiResponse.success(responses, "Pending advances retrieved successfully"));
    }

    @PatchMapping("/advances/{id}/reject")
    public ResponseEntity<ApiResponse<StaffAdvanceResponse>> rejectAdvance(@PathVariable String id) {
        StaffAdvanceResponse response = staffService.rejectAdvance(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Advance rejected successfully"));
    }

    @PatchMapping("/advances/{id}/repay")
    public ResponseEntity<ApiResponse<StaffAdvanceResponse>> repayAdvance(
            @PathVariable String id,
            @RequestParam BigDecimal repaymentAmount) {
        StaffAdvanceResponse response = staffService.repayAdvance(id, repaymentAmount);
        return ResponseEntity.ok(ApiResponse.success(response, "Advance repaid successfully"));
    }

    @GetMapping("/advances/outstanding/{staffId}")
    public ResponseEntity<ApiResponse<BigDecimal>> getOutstandingAdvanceForStaff(
            @PathVariable String staffId) {
        BigDecimal outstanding = staffService.getOutstandingAdvanceForStaff(staffId);
        return ResponseEntity.ok(ApiResponse.success(outstanding, "Outstanding advance retrieved successfully"));
    }

    @DeleteMapping("/advances/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAdvance(@PathVariable String id) {
        staffService.deleteAdvance(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Advance deleted successfully"));
    }

    // ==================== REPORTS & SUMMARY ====================

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<List<StaffSalarySummary>>> getSalarySummary() {
        List<StaffSalarySummary> summaries = staffService.getSalarySummary();
        return ResponseEntity.ok(ApiResponse.success(summaries, "Salary summary retrieved successfully"));
    }

    @GetMapping("/summary/{staffId}")
    public ResponseEntity<ApiResponse<StaffSalarySummary>> getStaffSalarySummary(
            @PathVariable String staffId) {
        StaffSalarySummary summary = staffService.getStaffSalarySummary(staffId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Staff salary summary retrieved successfully"));
    }

    @GetMapping("/report/monthly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMonthlySalaryReport(
            @RequestParam int month,
            @RequestParam int year) {
        Map<String, Object> report = staffService.getMonthlySalaryReport(month, year);
        return ResponseEntity.ok(ApiResponse.success(report, "Monthly salary report retrieved successfully"));
    }

    @GetMapping("/report/yearly")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getYearlySalaryReport(
            @RequestParam int year) {
        Map<String, Object> report = staffService.getYearlySalaryReport(year);
        return ResponseEntity.ok(ApiResponse.success(report, "Yearly salary report retrieved successfully"));
    }

    // ==================== IMAGE MANAGEMENT ====================

    /**
     * Upload one or more staff images (multipart field "images").
     * setAsPrimary=true makes the uploaded image the profile picture.
     */
    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StaffResponse>> uploadStaffImages(
            @PathVariable String id,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(defaultValue = "false") boolean setAsPrimary) {
        StaffResponse response = staffService.uploadStaffImages(id, images, setAsPrimary);
        return ResponseEntity.ok(ApiResponse.success(response, "Images uploaded successfully"));
    }

    @PatchMapping("/{id}/primary-image")
    public ResponseEntity<ApiResponse<StaffResponse>> setPrimaryImage(
            @PathVariable String id,
            @RequestParam String imageUrl) {
        StaffResponse response = staffService.setPrimaryImage(id, imageUrl);
        return ResponseEntity.ok(ApiResponse.success(response, "Primary image set successfully"));
    }

    @DeleteMapping("/{id}/images")
    public ResponseEntity<ApiResponse<StaffResponse>> removeStaffImage(
            @PathVariable String id,
            @RequestParam String imageUrl) {
        StaffResponse response = staffService.removeStaffImage(id, imageUrl);
        return ResponseEntity.ok(ApiResponse.success(response, "Image removed successfully"));
    }

    // ==================== GENERATE STAFF ID ====================

    @GetMapping("/generate-id")
    public ResponseEntity<ApiResponse<String>> generateStaffId() {
        String staffId = staffService.generateStaffId();
        return ResponseEntity.ok(ApiResponse.success(staffId, "Staff ID generated successfully"));
    }
}