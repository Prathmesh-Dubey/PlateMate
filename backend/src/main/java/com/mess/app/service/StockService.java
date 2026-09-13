package com.mess.app.service;

import com.mess.app.dto.request.StockItemRequest;
import com.mess.app.dto.request.StockTransactionRequest;
import com.mess.app.dto.response.StockItemResponse;
import com.mess.app.dto.response.StockSummaryResponse;
import com.mess.app.dto.response.StockTransactionResponse;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface StockService {

    // Stock Item CRUD
    StockItemResponse createStockItem(StockItemRequest request);

    StockItemResponse updateStockItem(String id, StockItemRequest request);

    StockItemResponse getStockItemById(String id);

    StockItemResponse getStockItemByName(String name);

    List<StockItemResponse> getAllStockItems();

    List<StockItemResponse> getActiveStockItems();

    List<StockItemResponse> getStockItemsByCategory(String category);

    List<StockItemResponse> getLowStockItems();

    List<StockItemResponse> getOverStockItems();

    void deleteStockItem(String id);

    void toggleStockItemStatus(String id);

    // Stock Transactions
    StockTransactionResponse addStockTransaction(StockTransactionRequest request);

    StockTransactionResponse getStockTransactionById(String id);

    List<StockTransactionResponse> getStockTransactionsByItem(String stockItemId);

    List<StockTransactionResponse> getStockTransactionsByDateRange(LocalDate startDate, LocalDate endDate);

    List<StockTransactionResponse> getStockTransactionsByType(String transactionType);

    // Stock Management
    StockItemResponse updateStock(String id, BigDecimal newStock);

    StockItemResponse addToStock(String id, BigDecimal quantity, String reference, String remarks);

    StockItemResponse deductFromStock(String id, BigDecimal quantity, String reference, String remarks);

    // Reports & Summary
    StockSummaryResponse getStockSummary();

    StockSummaryResponse getStockSummaryByCategory(String category);

    BigDecimal getTotalStockValue();

    List<StockTransactionResponse> getItemStockHistory(String stockItemId, LocalDate startDate, LocalDate endDate);

    // Image Upload
    StockItemResponse uploadStockImages(String stockItemId, List<MultipartFile> images);

    StockItemResponse setPrimaryImage(String stockItemId, String imageUrl);

    StockItemResponse removeStockImage(String stockItemId, String imageUrl);

    // Auto Generate Alerts
    void checkAndGenerateLowStockAlerts();
}