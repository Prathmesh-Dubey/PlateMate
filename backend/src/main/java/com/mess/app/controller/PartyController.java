package com.mess.app.controller;

import com.mess.app.dto.request.PartyPaymentRequest;
import com.mess.app.dto.request.PartyRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.PartyResponse;
import com.mess.app.dto.response.PartySummaryResponse;
import com.mess.app.service.PartyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/parties")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class PartyController {

    private final PartyService partyService;

    // ==================== PARTY CRUD ====================

    @PostMapping("/")
    public ResponseEntity<ApiResponse<PartyResponse>> createParty(
            @Valid @RequestBody PartyRequest request) {
        PartyResponse response = partyService.createParty(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Party created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PartyResponse>> updateParty(
            @PathVariable String id,
            @Valid @RequestBody PartyRequest request) {
        PartyResponse response = partyService.updateParty(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Party updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PartyResponse>> getPartyById(@PathVariable String id) {
        PartyResponse response = partyService.getPartyById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Party retrieved successfully"));
    }

    @GetMapping("/party-id/{partyId}")
    public ResponseEntity<ApiResponse<PartyResponse>> getPartyByPartyId(@PathVariable String partyId) {
        PartyResponse response = partyService.getPartyByPartyId(partyId);
        return ResponseEntity.ok(ApiResponse.success(response, "Party retrieved successfully"));
    }

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<PartyResponse>>> getAllParties() {
        List<PartyResponse> responses = partyService.getAllParties();
        return ResponseEntity.ok(ApiResponse.success(responses, "Parties retrieved successfully"));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<PartyResponse>>> getPartiesByDate(@PathVariable String date) {
        LocalDate eventDate = LocalDate.parse(date);
        List<PartyResponse> responses = partyService.getPartiesByDate(eventDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Parties retrieved successfully"));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<PartyResponse>>> getPartiesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PartyResponse> responses = partyService.getPartiesByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Parties retrieved successfully"));
    }

    @GetMapping("/payment-status/{status}")
    public ResponseEntity<ApiResponse<List<PartyResponse>>> getPartiesByPaymentStatus(
            @PathVariable String status) {
        List<PartyResponse> responses = partyService.getPartiesByPaymentStatus(status);
        return ResponseEntity.ok(ApiResponse.success(responses, "Parties retrieved successfully"));
    }

    // ==================== NEW: DEPARTMENT FILTER ====================

    @GetMapping("/department/{departmentCode}")
    public ResponseEntity<ApiResponse<List<PartyResponse>>> getPartiesByDepartment(
            @PathVariable String departmentCode) {
        List<PartyResponse> responses = partyService.getPartiesByDepartment(departmentCode);
        return ResponseEntity.ok(ApiResponse.success(responses, "Parties retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteParty(@PathVariable String id) {
        partyService.deleteParty(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Party deleted successfully"));
    }

    // ==================== PARTY PAYMENTS ====================

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<PartyResponse>> makePayment(
            @Valid @RequestBody PartyPaymentRequest request) {
        PartyResponse response = partyService.makePayment(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment made successfully"));
    }

    @PatchMapping("/{id}/mark-paid")
    public ResponseEntity<ApiResponse<PartyResponse>> markAsPaid(@PathVariable String id) {
        PartyResponse response = partyService.markAsPaid(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Party marked as paid successfully"));
    }

    @PatchMapping("/{id}/payment-status")
    public ResponseEntity<ApiResponse<PartyResponse>> updatePaymentStatus(
            @PathVariable String id,
            @RequestParam String status) {
        PartyResponse response = partyService.updatePaymentStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(response, "Payment status updated successfully"));
    }

    // ==================== PARTY SUMMARY & REPORTS ====================

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<PartySummaryResponse>> getPartySummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        PartySummaryResponse response = partyService.getPartySummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Party summary retrieved successfully"));
    }

    @GetMapping("/total-billing")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalPartyBilling(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        BigDecimal total = partyService.getTotalPartyBilling(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(total, "Total party billing retrieved successfully"));
    }

    @GetMapping("/outstanding")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalOutstandingAmount() {
        BigDecimal outstanding = partyService.getTotalOutstandingAmount();
        return ResponseEntity.ok(ApiResponse.success(outstanding, "Total outstanding amount retrieved successfully"));
    }

    @GetMapping("/monthly-billing")
    public ResponseEntity<ApiResponse<Map<String, BigDecimal>>> getMonthlyPartyBilling(
            @RequestParam int year) {
        Map<String, BigDecimal> monthlyBilling = partyService.getMonthlyPartyBilling(year);
        return ResponseEntity.ok(ApiResponse.success(monthlyBilling, "Monthly party billing retrieved successfully"));
    }

    @GetMapping("/revenue-report")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPartyRevenueReport(
            @RequestParam int year) {
        Map<String, Object> report = partyService.getPartyRevenueReport(year);
        return ResponseEntity.ok(ApiResponse.success(report, "Party revenue report retrieved successfully"));
    }

    // ==================== UTILITY ====================

    @GetMapping("/generate-id")
    public ResponseEntity<ApiResponse<String>> generatePartyId() {
        String partyId = partyService.generatePartyId();
        return ResponseEntity.ok(ApiResponse.success(partyId, "Party ID generated successfully"));
    }

    @GetMapping("/generate-invoice")
    public ResponseEntity<ApiResponse<String>> generateInvoiceNumber() {
        String invoiceNumber = partyService.generateInvoiceNumber();
        return ResponseEntity.ok(ApiResponse.success(invoiceNumber, "Invoice number generated successfully"));
    }
}