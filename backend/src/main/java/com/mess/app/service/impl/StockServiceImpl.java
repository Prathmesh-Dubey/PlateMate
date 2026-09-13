package com.mess.app.service.impl;

import com.mess.app.dto.request.StockItemRequest;
import com.mess.app.dto.request.StockTransactionRequest;
import com.mess.app.dto.response.StockItemResponse;
import com.mess.app.dto.response.StockSummaryResponse;
import com.mess.app.dto.response.StockTransactionResponse;
import com.mess.app.entity.Candidate;
import com.mess.app.entity.LowStockAlert;
import com.mess.app.entity.StockItem;
import com.mess.app.entity.StockTransaction;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.CandidateRepository;
import com.mess.app.repository.LowStockAlertRepository;
import com.mess.app.repository.StockItemRepository;
import com.mess.app.repository.StockTransactionRepository;
import com.mess.app.service.StockService;
import com.mess.app.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockServiceImpl implements StockService {

    private final StockItemRepository stockItemRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final LowStockAlertRepository lowStockAlertRepository;
    private final CandidateRepository candidateRepository;
    private final FileUploadUtil fileUploadUtil;

    @Override
    @Transactional
    public StockItemResponse createStockItem(StockItemRequest request) {
        log.info("Creating new stock item: {}", request.getItemName());

        if (stockItemRepository.existsByItemNameIgnoreCase(request.getItemName())) {
            throw new RuntimeException("Stock item already exists: " + request.getItemName());
        }

        StockItem stockItem = StockItem.builder()
                .itemName(request.getItemName())
                .category(request.getCategory())
                .subCategory(request.getSubCategory())
                .unit(request.getUnit())
                .currentStock(request.getCurrentStock() != null ? request.getCurrentStock() : BigDecimal.ZERO)
                .minimumStockLevel(
                        request.getMinimumStockLevel() != null ? request.getMinimumStockLevel() : BigDecimal.TEN)
                .maximumStockLevel(request.getMaximumStockLevel() != null ? request.getMaximumStockLevel()
                        : BigDecimal.valueOf(100))
                .unitPrice(request.getUnitPrice())
                .description(request.getDescription())
                .barcode(request.getBarcode())
                .sku(request.getSku())
                .supplierName(request.getSupplierName())
                .supplierContact(request.getSupplierContact())
                .remarks(request.getRemarks())
                .active(true)
                .imageUrls(new ArrayList<>())
                .build();

        // Handle image uploads
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> imageUrls = new ArrayList<>();
            int primaryIndex = 0;

            if (request.getPrimaryImageIndex() != null) {
                try {
                    primaryIndex = Integer.parseInt(request.getPrimaryImageIndex());
                } catch (NumberFormatException e) {
                    primaryIndex = 0;
                }
            }

            for (int i = 0; i < request.getImages().size(); i++) {
                MultipartFile image = request.getImages().get(i);
                try {
                    String imageUrl = fileUploadUtil.uploadStockImage(image,
                            request.getItemName().replaceAll("\\s+", "_") + "_" + i);
                    imageUrls.add(imageUrl);

                    if (i == primaryIndex) {
                        stockItem.setPrimaryImageUrl(imageUrl);
                    }
                } catch (Exception e) {
                    log.error("Failed to upload image: {}", e.getMessage());
                }
            }
            stockItem.setImageUrls(imageUrls);

            if (stockItem.getPrimaryImageUrl() == null && !imageUrls.isEmpty()) {
                stockItem.setPrimaryImageUrl(imageUrls.get(0));
            }
        }

        StockItem saved = stockItemRepository.save(stockItem);
        log.info("Stock item created with ID: {}", saved.getId());

        if (request.getCurrentStock() != null && request.getCurrentStock().compareTo(BigDecimal.ZERO) > 0) {
            StockTransactionRequest transactionRequest = StockTransactionRequest.builder()
                    .stockItemId(saved.getId())
                    .transactionType("PURCHASE")
                    .quantity(request.getCurrentStock())
                    .unitPrice(request.getUnitPrice() != null ? request.getUnitPrice() : BigDecimal.ZERO)
                    .transactionDate(LocalDate.now())
                    .referenceType("INITIAL_STOCK")
                    .remarks("Initial stock entry")
                    .enteredByCandidateId(request.getEnteredByCandidateId())
                    .build();
            addStockTransaction(transactionRequest);
        }

        checkAndGenerateLowStockAlerts();

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public StockItemResponse updateStockItem(String id, StockItemRequest request) {
        log.info("Updating stock item with ID: {}", id);

        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with ID: " + id));

        if (request.getItemName() != null && !request.getItemName().trim().isEmpty()) {
            stockItem.setItemName(request.getItemName());
        }
        if (request.getCategory() != null) {
            stockItem.setCategory(request.getCategory());
        }
        if (request.getSubCategory() != null) {
            stockItem.setSubCategory(request.getSubCategory());
        }
        if (request.getUnit() != null) {
            stockItem.setUnit(request.getUnit());
        }
        if (request.getCurrentStock() != null) {
            stockItem.setCurrentStock(request.getCurrentStock());
        }
        if (request.getMinimumStockLevel() != null) {
            stockItem.setMinimumStockLevel(request.getMinimumStockLevel());
        } else if (stockItem.getMinimumStockLevel() == null) {
            stockItem.setMinimumStockLevel(BigDecimal.TEN);
        }
        if (request.getMaximumStockLevel() != null) {
            stockItem.setMaximumStockLevel(request.getMaximumStockLevel());
        } else if (stockItem.getMaximumStockLevel() == null) {
            stockItem.setMaximumStockLevel(BigDecimal.valueOf(100));
        }
        if (request.getUnitPrice() != null) {
            stockItem.setUnitPrice(request.getUnitPrice());
        }
        if (request.getDescription() != null) {
            stockItem.setDescription(request.getDescription());
        }
        if (request.getBarcode() != null) {
            stockItem.setBarcode(request.getBarcode());
        }
        if (request.getSku() != null) {
            stockItem.setSku(request.getSku());
        }
        if (request.getSupplierName() != null) {
            stockItem.setSupplierName(request.getSupplierName());
        }
        if (request.getSupplierContact() != null) {
            stockItem.setSupplierContact(request.getSupplierContact());
        }
        if (request.getRemarks() != null) {
            stockItem.setRemarks(request.getRemarks());
        }

        // Handle new image uploads
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            List<String> imageUrls = new ArrayList<>(
                    stockItem.getImageUrls() != null ? stockItem.getImageUrls() : new ArrayList<>());

            int primaryIndex = 0;
            if (request.getPrimaryImageIndex() != null) {
                try {
                    primaryIndex = Integer.parseInt(request.getPrimaryImageIndex());
                } catch (NumberFormatException e) {
                    primaryIndex = 0;
                }
            }

            for (int i = 0; i < request.getImages().size(); i++) {
                MultipartFile image = request.getImages().get(i);
                try {
                    String imageUrl = fileUploadUtil.uploadStockImage(image,
                            request.getItemName().replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + "_" + i);
                    imageUrls.add(imageUrl);

                    if (i == primaryIndex || (i == 0 && stockItem.getPrimaryImageUrl() == null)) {
                        stockItem.setPrimaryImageUrl(imageUrl);
                    }
                } catch (Exception e) {
                    log.error("Failed to upload image: {}", e.getMessage());
                }
            }
            stockItem.setImageUrls(imageUrls);
        }

        StockItem updated = stockItemRepository.save(stockItem);
        log.info("Stock item updated successfully");

        checkAndGenerateLowStockAlerts();

        return convertToResponse(updated);
    }

    @Override
    public StockItemResponse getStockItemById(String id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with ID: " + id));
        return convertToResponse(stockItem);
    }

    @Override
    public StockItemResponse getStockItemByName(String name) {
        StockItem stockItem = stockItemRepository.findByItemNameIgnoreCase(name)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + name));
        return convertToResponse(stockItem);
    }

    @Override
    public List<StockItemResponse> getAllStockItems() {
        return stockItemRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockItemResponse> getActiveStockItems() {
        return stockItemRepository.findByActiveTrue().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockItemResponse> getStockItemsByCategory(String category) {
        return stockItemRepository.findByCategoryAndActiveTrue(category).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockItemResponse> getLowStockItems() {
        return stockItemRepository.findLowStockItems().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockItemResponse> getOverStockItems() {
        return stockItemRepository.findOverStockItems().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteStockItem(String id) {
        log.info("Deleting stock item with ID: {}", id);
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with ID: " + id));

        // ✅ Hard delete - remove from database
        stockItemRepository.delete(stockItem);
        log.info("Stock item deleted successfully: {}", stockItem.getItemName());
    }

    @Override
    @Transactional
    public void toggleStockItemStatus(String id) {
        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with ID: " + id));
        stockItem.setActive(!stockItem.isActive());
        stockItemRepository.save(stockItem);
        log.info("Stock item status toggled to: {}", stockItem.isActive());
    }

    @Override
    @Transactional
    public StockTransactionResponse addStockTransaction(StockTransactionRequest request) {
        log.info("Adding stock transaction for item: {}", request.getStockItemId());

        StockItem stockItem = stockItemRepository.findById(request.getStockItemId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Stock item not found with ID: " + request.getStockItemId()));

        Candidate enteredBy = null;
        if (request.getEnteredByCandidateId() != null) {
            enteredBy = candidateRepository.findById(request.getEnteredByCandidateId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Candidate not found with ID: " + request.getEnteredByCandidateId()));
        }

        BigDecimal stockBefore = stockItem.getCurrentStock();
        BigDecimal quantity = request.getQuantity();
        BigDecimal unitPrice = request.getUnitPrice() != null ? request.getUnitPrice()
                : (stockItem.getUnitPrice() != null ? stockItem.getUnitPrice() : BigDecimal.ZERO);
        BigDecimal totalAmount = quantity.multiply(unitPrice);

        StockTransaction.TransactionType transactionType = StockTransaction.TransactionType
                .valueOf(request.getTransactionType().toUpperCase());

        // Calculate stock after based on transaction type
        BigDecimal stockAfter;
        switch (transactionType) {
            case PURCHASE:
            case RETURN:
                stockAfter = stockBefore.add(quantity);
                break;
            case USAGE:
            case ADJUSTMENT:
                stockAfter = stockBefore.subtract(quantity);
                break;
            default:
                stockAfter = stockBefore;
        }

        // Validate stock for usage
        if ((transactionType == StockTransaction.TransactionType.USAGE ||
                transactionType == StockTransaction.TransactionType.ADJUSTMENT) &&
                stockAfter.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Insufficient stock. Available: " + stockBefore + ", Requested: " + quantity);
        }

        // Update stock item current stock
        stockItem.setCurrentStock(stockAfter);
        if (unitPrice.compareTo(BigDecimal.ZERO) > 0) {
            stockItem.setUnitPrice(unitPrice);
        }
        stockItemRepository.save(stockItem);

        StockTransaction transaction = StockTransaction.builder()
                .stockItem(stockItem)
                .transactionType(transactionType)
                .quantity(quantity)
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .stockBefore(stockBefore)
                .stockAfter(stockAfter)
                .transactionDate(request.getTransactionDate() != null ? request.getTransactionDate() : LocalDate.now())
                .referenceNumber(request.getReferenceNumber())
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .remarks(request.getRemarks())
                .enteredBy(enteredBy)
                .build();

        StockTransaction saved = stockTransactionRepository.save(transaction);
        log.info("Stock transaction added with ID: {}", saved.getId());

        // Check for low stock alerts
        checkAndGenerateLowStockAlerts();

        return convertToResponse(saved);
    }

    @Override
    public StockTransactionResponse getStockTransactionById(String id) {
        StockTransaction transaction = stockTransactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock transaction not found with ID: " + id));
        return convertToResponse(transaction);
    }

    @Override
    public List<StockTransactionResponse> getStockTransactionsByItem(String stockItemId) {
        StockItem stockItem = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with ID: " + stockItemId));

        return stockTransactionRepository.findByStockItemOrderByTransactionDateDesc(stockItem).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockTransactionResponse> getStockTransactionsByDateRange(LocalDate startDate, LocalDate endDate) {
        return stockTransactionRepository.findByTransactionDateBetween(startDate, endDate).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<StockTransactionResponse> getStockTransactionsByType(String transactionType) {
        StockTransaction.TransactionType type = StockTransaction.TransactionType.valueOf(transactionType.toUpperCase());

        return stockTransactionRepository.findByTransactionType(type).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StockItemResponse updateStock(String id, BigDecimal newStock) {
        log.info("Updating stock for item ID: {} to new value: {}", id, newStock);

        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with ID: " + id));

        if (newStock.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("Stock cannot be negative");
        }

        BigDecimal difference = newStock.subtract(stockItem.getCurrentStock());

        stockItem.setCurrentStock(newStock);
        StockItem updated = stockItemRepository.save(stockItem);

        // Create adjustment transaction
        if (difference.compareTo(BigDecimal.ZERO) != 0) {
            StockTransactionRequest transactionRequest = StockTransactionRequest.builder()
                    .stockItemId(id)
                    .transactionType("ADJUSTMENT")
                    .quantity(difference.abs())
                    .unitPrice(stockItem.getUnitPrice() != null ? stockItem.getUnitPrice() : BigDecimal.ZERO)
                    .transactionDate(LocalDate.now())
                    .referenceType("STOCK_ADJUSTMENT")
                    .remarks(difference.compareTo(BigDecimal.ZERO) > 0 ? "Manual stock addition"
                            : "Manual stock deduction")
                    .build();
            addStockTransaction(transactionRequest);
        }

        checkAndGenerateLowStockAlerts();

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public StockItemResponse addToStock(String id, BigDecimal quantity, String reference, String remarks) {
        log.info("Adding {} to stock for item ID: {}", quantity, id);

        StockTransactionRequest request = StockTransactionRequest.builder()
                .stockItemId(id)
                .transactionType("PURCHASE")
                .quantity(quantity)
                .unitPrice(BigDecimal.ZERO)
                .transactionDate(LocalDate.now())
                .referenceType(reference != null ? reference : "MANUAL_ADD")
                .remarks(remarks != null ? remarks : "Manual stock addition")
                .build();

        addStockTransaction(request);
        return getStockItemById(id);
    }

    @Override
    @Transactional
    public StockItemResponse deductFromStock(String id, BigDecimal quantity, String reference, String remarks) {
        log.info("Deducting {} from stock for item ID: {}", quantity, id);

        StockItem stockItem = stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with ID: " + id));

        if (stockItem.getCurrentStock().compareTo(quantity) < 0) {
            throw new RuntimeException("Insufficient stock. Available: " + stockItem.getCurrentStock());
        }

        StockTransactionRequest request = StockTransactionRequest.builder()
                .stockItemId(id)
                .transactionType("USAGE")
                .quantity(quantity)
                .unitPrice(stockItem.getUnitPrice() != null ? stockItem.getUnitPrice() : BigDecimal.ZERO)
                .transactionDate(LocalDate.now())
                .referenceType(reference != null ? reference : "MANUAL_DEDUCT")
                .remarks(remarks != null ? remarks : "Manual stock deduction")
                .build();

        addStockTransaction(request);
        return getStockItemById(id);
    }

    @Override
    public StockSummaryResponse getStockSummary() {
        List<StockItem> allItems = stockItemRepository.findAll();
        List<StockItem> activeItems = allItems.stream().filter(StockItem::isActive).collect(Collectors.toList());

        int totalItems = allItems.size();
        int activeCount = (int) activeItems.stream().filter(StockItem::isActive).count();
        int lowStockCount = (int) activeItems.stream()
                .filter(item -> item.getCurrentStock().compareTo(item.getMinimumStockLevel()) < 0)
                .count();
        int overStockCount = (int) activeItems.stream()
                .filter(item -> item.getCurrentStock().compareTo(item.getMaximumStockLevel()) > 0)
                .count();

        BigDecimal totalStockValue = activeItems.stream()
                .map(item -> item.getCurrentStock().multiply(
                        item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Integer> categoryWiseCount = new HashMap<>();
        Map<String, BigDecimal> categoryWiseValue = new HashMap<>();

        for (StockItem item : activeItems) {
            String category = item.getCategory() != null ? item.getCategory() : "Uncategorized";
            categoryWiseCount.put(category, categoryWiseCount.getOrDefault(category, 0) + 1);

            BigDecimal itemValue = item.getCurrentStock().multiply(
                    item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO);
            categoryWiseValue.put(category, categoryWiseValue.getOrDefault(category, BigDecimal.ZERO).add(itemValue));
        }

        List<StockItemResponse> lowStockList = activeItems.stream()
                .filter(item -> item.getCurrentStock().compareTo(item.getMinimumStockLevel()) < 0)
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        List<StockItemResponse> overStockList = activeItems.stream()
                .filter(item -> item.getCurrentStock().compareTo(item.getMaximumStockLevel()) > 0)
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return StockSummaryResponse.builder()
                .totalItems(totalItems)
                .activeItems(activeCount)
                .lowStockItems(lowStockCount)
                .overStockItems(overStockCount)
                .totalStockValue(totalStockValue)
                .categoryWiseCount(categoryWiseCount)
                .categoryWiseValue(categoryWiseValue)
                .lowStockList(lowStockList)
                .overStockList(overStockList)
                .build();
    }

    @Override
    public StockSummaryResponse getStockSummaryByCategory(String category) {
        List<StockItem> items = stockItemRepository.findByCategoryAndActiveTrue(category);

        int lowStockCount = (int) items.stream()
                .filter(item -> item.getCurrentStock().compareTo(item.getMinimumStockLevel()) < 0)
                .count();
        int overStockCount = (int) items.stream()
                .filter(item -> item.getCurrentStock().compareTo(item.getMaximumStockLevel()) > 0)
                .count();

        BigDecimal totalStockValue = items.stream()
                .map(item -> item.getCurrentStock().multiply(
                        item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Integer> categoryWiseCount = new HashMap<>();
        categoryWiseCount.put(category, items.size());

        Map<String, BigDecimal> categoryWiseValue = new HashMap<>();
        categoryWiseValue.put(category, totalStockValue);

        List<StockItemResponse> lowStockList = items.stream()
                .filter(item -> item.getCurrentStock().compareTo(item.getMinimumStockLevel()) < 0)
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        List<StockItemResponse> overStockList = items.stream()
                .filter(item -> item.getCurrentStock().compareTo(item.getMaximumStockLevel()) > 0)
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return StockSummaryResponse.builder()
                .totalItems(items.size())
                .activeItems(items.size())
                .lowStockItems(lowStockCount)
                .overStockItems(overStockCount)
                .totalStockValue(totalStockValue)
                .categoryWiseCount(categoryWiseCount)
                .categoryWiseValue(categoryWiseValue)
                .lowStockList(lowStockList)
                .overStockList(overStockList)
                .build();
    }

    @Override
    public BigDecimal getTotalStockValue() {
        return stockItemRepository.getTotalStockValue();
    }

    @Override
    public List<StockTransactionResponse> getItemStockHistory(String stockItemId, LocalDate startDate,
            LocalDate endDate) {
        StockItem stockItem = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with ID: " + stockItemId));

        return stockTransactionRepository.findByStockItemAndTransactionDateBetween(stockItem, startDate, endDate)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StockItemResponse uploadStockImages(String stockItemId, List<MultipartFile> images) {
        StockItem stockItem = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with ID: " + stockItemId));

        List<String> existingImages = stockItem.getImageUrls() != null ? stockItem.getImageUrls() : new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            try {
                String imageUrl = fileUploadUtil.uploadStockImage(image,
                        stockItem.getItemName().replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + "_" + i);
                existingImages.add(imageUrl);
                stockItem.setPrimaryImageUrl(imageUrl);
            } catch (Exception e) {
                log.error("Failed to upload image: {}", e.getMessage());
            }
        }

        stockItem.setImageUrls(existingImages);
        StockItem updated = stockItemRepository.save(stockItem);

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public StockItemResponse setPrimaryImage(String stockItemId, String imageUrl) {
        StockItem stockItem = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with ID: " + stockItemId));

        if (stockItem.getImageUrls() == null || !stockItem.getImageUrls().contains(imageUrl)) {
            throw new RuntimeException("Image not found for this stock item");
        }

        stockItem.setPrimaryImageUrl(imageUrl);
        StockItem updated = stockItemRepository.save(stockItem);

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public StockItemResponse removeStockImage(String stockItemId, String imageUrl) {
        StockItem stockItem = stockItemRepository.findById(stockItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock item not found with ID: " + stockItemId));

        if (stockItem.getImageUrls() != null) {
            stockItem.getImageUrls().remove(imageUrl);

            if (imageUrl.equals(stockItem.getPrimaryImageUrl()) && !stockItem.getImageUrls().isEmpty()) {
                stockItem.setPrimaryImageUrl(stockItem.getImageUrls().get(0));
            } else if (stockItem.getImageUrls().isEmpty()) {
                stockItem.setPrimaryImageUrl(null);
            }
        }

        StockItem updated = stockItemRepository.save(stockItem);

        // Delete image from storage
        try {
            fileUploadUtil.deleteStockImage(imageUrl);
        } catch (Exception e) {
            log.error("Failed to delete image: {}", e.getMessage());
        }

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public void checkAndGenerateLowStockAlerts() {
        log.info("Checking for low stock items...");

        List<StockItem> lowStockItems = stockItemRepository.findLowStockItems();

        for (StockItem item : lowStockItems) {
            // Check if alert already exists and not resolved
            List<LowStockAlert> existingAlerts = lowStockAlertRepository.findByStockItem(item);
            boolean hasUnresolvedAlert = existingAlerts.stream()
                    .anyMatch(alert -> !alert.isResolved());

            if (!hasUnresolvedAlert) {
                String alertMessage = String.format(
                        "Low stock alert: %s is running low. Current stock: %s %s. Minimum required: %s %s",
                        item.getItemName(),
                        item.getCurrentStock().toString(),
                        item.getUnit(),
                        item.getMinimumStockLevel().toString(),
                        item.getUnit());

                LowStockAlert alert = LowStockAlert.builder()
                        .stockItem(item)
                        .alertMessage(alertMessage)
                        .isResolved(false)
                        .build();

                lowStockAlertRepository.save(alert);
                log.info("Low stock alert generated for: {}", item.getItemName());
            }
        }
    }

    // Helper method to convert StockItem to Response
    private StockItemResponse convertToResponse(StockItem stockItem) {
        boolean isLowStock = stockItem.isActive() &&
                stockItem.getCurrentStock().compareTo(stockItem.getMinimumStockLevel()) < 0;
        boolean isOverStock = stockItem.isActive() &&
                stockItem.getCurrentStock().compareTo(stockItem.getMaximumStockLevel()) > 0;

        return StockItemResponse.builder()
                .id(stockItem.getId())
                .itemName(stockItem.getItemName())
                .category(stockItem.getCategory())
                .subCategory(stockItem.getSubCategory())
                .unit(stockItem.getUnit())
                .currentStock(stockItem.getCurrentStock())
                .minimumStockLevel(stockItem.getMinimumStockLevel())
                .maximumStockLevel(stockItem.getMaximumStockLevel())
                .unitPrice(stockItem.getUnitPrice())
                .active(stockItem.isActive())
                .description(stockItem.getDescription())
                .imageUrls(stockItem.getImageUrls())
                .primaryImageUrl(stockItem.getPrimaryImageUrl())
                .barcode(stockItem.getBarcode())
                .sku(stockItem.getSku())
                .supplierName(stockItem.getSupplierName())
                .supplierContact(stockItem.getSupplierContact())
                .remarks(stockItem.getRemarks())
                .createdAt(stockItem.getCreatedAt())
                .updatedAt(stockItem.getUpdatedAt())
                .isLowStock(isLowStock)
                .isOverStock(isOverStock)
                .build();
    }

    // Helper method to convert StockTransaction to Response
    // Helper method to convert StockTransaction to Response
    private StockTransactionResponse convertToResponse(StockTransaction transaction) {
        return StockTransactionResponse.builder()
                .id(transaction.getId())
                .stockItemId(transaction.getStockItem().getId())
                .stockItemName(transaction.getStockItem().getItemName()) // ✅ Added
                .transactionType(transaction.getTransactionType().name())
                .quantity(transaction.getQuantity())
                .unit(transaction.getStockItem().getUnit()) // ✅ Added
                .unitPrice(transaction.getUnitPrice())
                .totalAmount(transaction.getTotalAmount())
                .stockBefore(transaction.getStockBefore())
                .stockAfter(transaction.getStockAfter())
                .transactionDate(transaction.getTransactionDate())
                .referenceNumber(transaction.getReferenceNumber())
                .referenceType(transaction.getReferenceType())
                .referenceId(transaction.getReferenceId())
                .remarks(transaction.getRemarks())
                .enteredBy(transaction.getEnteredBy() != null ? transaction.getEnteredBy().getFullName() + " (" +
                        transaction.getEnteredBy().getCandidateId() + ")" : null)
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}