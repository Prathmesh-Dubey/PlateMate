package com.mess.app.controller;

import com.mess.app.dto.request.StockItemRequest;
import com.mess.app.dto.request.StockTransactionRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.StockItemResponse;
import com.mess.app.dto.response.StockSummaryResponse;
import com.mess.app.dto.response.StockTransactionResponse;
import com.mess.app.service.StockService;
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

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class StockController {

    private final StockService stockService;

    // ==================== Stock Item CRUD ====================

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<StockItemResponse>> createStockItem(
            @Valid @RequestBody StockItemRequest request) {
        StockItemResponse response = stockService.createStockItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock item created successfully"));
    }

    // ✅ CREATE - Multipart endpoint (with images) - DIFFERENT PATH
    @PostMapping(value = "/items/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StockItemResponse>> createStockItemWithImages(
            @ModelAttribute @Valid StockItemRequest request) {
        StockItemResponse response = stockService.createStockItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock item created successfully"));
    }

    // ✅ UPDATE - JSON endpoint (no images)
    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<StockItemResponse>> updateStockItem(
            @PathVariable String id,
            @Valid @RequestBody StockItemRequest request) {
        StockItemResponse response = stockService.updateStockItem(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock item updated successfully"));
    }

    // ✅ UPDATE - Multipart endpoint (with images) - DIFFERENT PATH
    @PutMapping(value = "/items/{id}/with-images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StockItemResponse>> updateStockItemWithImages(
            @PathVariable String id,
            @ModelAttribute @Valid StockItemRequest request) {
        StockItemResponse response = stockService.updateStockItem(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock item updated successfully"));
    }

    @GetMapping("/items/{id}")
    public ResponseEntity<ApiResponse<StockItemResponse>> getStockItemById(@PathVariable String id) {
        StockItemResponse response = stockService.getStockItemById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock item retrieved successfully"));
    }

    @GetMapping("/items/name/{name}")
    public ResponseEntity<ApiResponse<StockItemResponse>> getStockItemByName(@PathVariable String name) {
        StockItemResponse response = stockService.getStockItemByName(name);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock item retrieved successfully"));
    }

    @GetMapping("/items")
    public ResponseEntity<ApiResponse<List<StockItemResponse>>> getAllStockItems() {
        List<StockItemResponse> responses = stockService.getAllStockItems();
        return ResponseEntity.ok(ApiResponse.success(responses, "Stock items retrieved successfully"));
    }

    @GetMapping("/items/active")
    public ResponseEntity<ApiResponse<List<StockItemResponse>>> getActiveStockItems() {
        List<StockItemResponse> responses = stockService.getActiveStockItems();
        return ResponseEntity.ok(ApiResponse.success(responses, "Active stock items retrieved successfully"));
    }

    @GetMapping("/items/category/{category}")
    public ResponseEntity<ApiResponse<List<StockItemResponse>>> getStockItemsByCategory(
            @PathVariable String category) {
        List<StockItemResponse> responses = stockService.getStockItemsByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(responses, "Stock items retrieved successfully"));
    }

    @GetMapping("/items/low-stock")
    public ResponseEntity<ApiResponse<List<StockItemResponse>>> getLowStockItems() {
        List<StockItemResponse> responses = stockService.getLowStockItems();
        return ResponseEntity.ok(ApiResponse.success(responses, "Low stock items retrieved successfully"));
    }

    @GetMapping("/items/over-stock")
    public ResponseEntity<ApiResponse<List<StockItemResponse>>> getOverStockItems() {
        List<StockItemResponse> responses = stockService.getOverStockItems();
        return ResponseEntity.ok(ApiResponse.success(responses, "Over stock items retrieved successfully"));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStockItem(@PathVariable String id) {
        stockService.deleteStockItem(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock item deleted successfully"));
    }

    @PatchMapping("/items/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleStockItemStatus(@PathVariable String id) {
        stockService.toggleStockItemStatus(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Stock item status toggled successfully"));
    }

    // ==================== Stock Transactions ====================

    @PostMapping("/transactions")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> addStockTransaction(
            @Valid @RequestBody StockTransactionRequest request) {
        StockTransactionResponse response = stockService.addStockTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Stock transaction added successfully"));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> getStockTransactionById(
            @PathVariable String id) {
        StockTransactionResponse response = stockService.getStockTransactionById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock transaction retrieved successfully"));
    }

    @GetMapping("/transactions/item/{stockItemId}")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getStockTransactionsByItem(
            @PathVariable String stockItemId) {
        List<StockTransactionResponse> responses = stockService.getStockTransactionsByItem(stockItemId);
        return ResponseEntity.ok(ApiResponse.success(responses, "Stock transactions retrieved successfully"));
    }

    @GetMapping("/transactions/range")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getStockTransactionsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<StockTransactionResponse> responses = stockService.getStockTransactionsByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Stock transactions retrieved successfully"));
    }

    @GetMapping("/transactions/type/{type}")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getStockTransactionsByType(
            @PathVariable String type) {
        List<StockTransactionResponse> responses = stockService.getStockTransactionsByType(type);
        return ResponseEntity.ok(ApiResponse.success(responses, "Stock transactions retrieved successfully"));
    }

    // ==================== Stock Management ====================

    @PatchMapping("/items/{id}/stock")
    public ResponseEntity<ApiResponse<StockItemResponse>> updateStock(
            @PathVariable String id,
            @RequestParam BigDecimal newStock) {
        StockItemResponse response = stockService.updateStock(id, newStock);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock updated successfully"));
    }

    @PatchMapping("/items/{id}/add")
    public ResponseEntity<ApiResponse<StockItemResponse>> addToStock(
            @PathVariable String id,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String remarks) {
        StockItemResponse response = stockService.addToStock(id, quantity, reference, remarks);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock added successfully"));
    }

    @PatchMapping("/items/{id}/deduct")
    public ResponseEntity<ApiResponse<StockItemResponse>> deductFromStock(
            @PathVariable String id,
            @RequestParam BigDecimal quantity,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String remarks) {
        StockItemResponse response = stockService.deductFromStock(id, quantity, reference, remarks);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock deducted successfully"));
    }

    // ==================== Reports & Summary ====================

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<StockSummaryResponse>> getStockSummary() {
        StockSummaryResponse response = stockService.getStockSummary();
        return ResponseEntity.ok(ApiResponse.success(response, "Stock summary retrieved successfully"));
    }

    @GetMapping("/summary/category/{category}")
    public ResponseEntity<ApiResponse<StockSummaryResponse>> getStockSummaryByCategory(
            @PathVariable String category) {
        StockSummaryResponse response = stockService.getStockSummaryByCategory(category);
        return ResponseEntity.ok(ApiResponse.success(response, "Stock summary retrieved successfully"));
    }

    @GetMapping("/total-value")
    public ResponseEntity<ApiResponse<BigDecimal>> getTotalStockValue() {
        BigDecimal totalValue = stockService.getTotalStockValue();
        return ResponseEntity.ok(ApiResponse.success(totalValue, "Total stock value retrieved successfully"));
    }

    @GetMapping("/history/{stockItemId}")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getItemStockHistory(
            @PathVariable String stockItemId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<StockTransactionResponse> responses = stockService.getItemStockHistory(stockItemId, startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Stock history retrieved successfully"));
    }

    // ==================== Image Management ====================

    @PostMapping(value = "/items/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<StockItemResponse>> uploadStockImages(
            @PathVariable String id,
            @RequestParam("images") List<MultipartFile> images) {
        StockItemResponse response = stockService.uploadStockImages(id, images);
        return ResponseEntity.ok(ApiResponse.success(response, "Images uploaded successfully"));
    }

    @PatchMapping("/items/{id}/primary-image")
    public ResponseEntity<ApiResponse<StockItemResponse>> setPrimaryImage(
            @PathVariable String id,
            @RequestParam String imageUrl) {
        StockItemResponse response = stockService.setPrimaryImage(id, imageUrl);
        return ResponseEntity.ok(ApiResponse.success(response, "Primary image set successfully"));
    }

    @DeleteMapping("/items/{id}/images")
    public ResponseEntity<ApiResponse<StockItemResponse>> removeStockImage(
            @PathVariable String id,
            @RequestParam String imageUrl) {
        StockItemResponse response = stockService.removeStockImage(id, imageUrl);
        return ResponseEntity.ok(ApiResponse.success(response, "Image removed successfully"));
    }

    // ==================== Alerts ====================

    @PostMapping("/alerts/check")
    public ResponseEntity<ApiResponse<Void>> checkAndGenerateLowStockAlerts() {
        stockService.checkAndGenerateLowStockAlerts();
        return ResponseEntity.ok(ApiResponse.success(null, "Low stock alerts checked and generated"));
    }
}