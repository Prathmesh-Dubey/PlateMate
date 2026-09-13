package com.mess.app.repository;

import com.mess.app.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, String> {

    Optional<StockItem> findByItemNameIgnoreCase(String itemName);

    List<StockItem> findByCategory(String category);

    List<StockItem> findByCategoryAndActiveTrue(String category);

    List<StockItem> findByActiveTrue();

    List<StockItem> findByActiveTrueOrderByItemNameAsc();

    @Query("SELECT s FROM StockItem s WHERE s.active = true AND s.currentStock < s.minimumStockLevel")
    List<StockItem> findLowStockItems();

    @Query("SELECT s FROM StockItem s WHERE s.active = true AND s.currentStock > s.maximumStockLevel")
    List<StockItem> findOverStockItems();

    @Query("SELECT s FROM StockItem s WHERE s.active = true AND s.currentStock <= :threshold")
    List<StockItem> findStockBelowThreshold(@Param("threshold") BigDecimal threshold);

    boolean existsByItemNameIgnoreCase(String itemName);

    @Query("SELECT COUNT(s) FROM StockItem s WHERE s.active = true AND s.currentStock < s.minimumStockLevel")
    long countLowStockItems();

    @Query("SELECT s FROM StockItem s WHERE s.active = true ORDER BY s.currentStock ASC")
    List<StockItem> findAllOrderByStockAsc();

    @Query("SELECT DISTINCT s.category FROM StockItem s WHERE s.active = true")
    List<String> findAllCategories();

    // ADD THIS MISSING METHOD
    @Query("SELECT SUM(s.currentStock * s.unitPrice) FROM StockItem s WHERE s.active = true")
    BigDecimal getTotalStockValue();

    @Query("SELECT s.category, COUNT(s) FROM StockItem s WHERE s.active = true GROUP BY s.category")
    List<Object[]> getCategoryWiseCount();

    @Query("SELECT s.category, SUM(s.currentStock * s.unitPrice) FROM StockItem s " +
            "WHERE s.active = true GROUP BY s.category")
    List<Object[]> getCategoryWiseValue();
}