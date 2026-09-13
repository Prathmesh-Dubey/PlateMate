package com.mess.app.repository;

import com.mess.app.entity.LowStockAlert;
import com.mess.app.entity.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LowStockAlertRepository extends JpaRepository<LowStockAlert, String> {

    List<LowStockAlert> findByIsResolvedFalse();

    List<LowStockAlert> findByIsResolvedFalseOrderByCreatedAtDesc();

    List<LowStockAlert> findByStockItem(StockItem stockItem);

    List<LowStockAlert> findByStockItemAndIsResolvedFalse(StockItem stockItem);

    @Query("SELECT COUNT(a) FROM LowStockAlert a WHERE a.isResolved = false")
    long countUnresolvedAlerts();

    @Modifying
    @Query("UPDATE LowStockAlert a SET a.isResolved = true, a.resolvedAt = CURRENT_TIMESTAMP, a.resolvedBy = :resolvedBy "
            +
            "WHERE a.stockItem = :stockItem AND a.isResolved = false")
    void resolveAlertsForStockItem(@Param("stockItem") StockItem stockItem, @Param("resolvedBy") String resolvedBy);

    @Modifying
    @Query("UPDATE LowStockAlert a SET a.isResolved = true, a.resolvedAt = CURRENT_TIMESTAMP, a.resolvedBy = :resolvedBy "
            +
            "WHERE a.id = :alertId")
    void resolveAlert(@Param("alertId") String alertId, @Param("resolvedBy") String resolvedBy);
}