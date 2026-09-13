package com.mess.app.repository;

import com.mess.app.entity.StockItem;
import com.mess.app.entity.StockTransaction;
import com.mess.app.entity.StockTransaction.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, String> {

    List<StockTransaction> findByStockItemOrderByTransactionDateDesc(StockItem stockItem);

    List<StockTransaction> findByStockItemOrderByCreatedAtDesc(StockItem stockItem);

    List<StockTransaction> findByTransactionType(TransactionType type);

    List<StockTransaction> findByTransactionDate(LocalDate date);

    List<StockTransaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);

    List<StockTransaction> findByStockItemAndTransactionDateBetween(
            StockItem stockItem, LocalDate startDate, LocalDate endDate);

    List<StockTransaction> findByReferenceTypeAndReferenceId(String referenceType, String referenceId);

    List<StockTransaction> findByStockItemAndTransactionTypeOrderByTransactionDateDesc(
            StockItem stockItem, TransactionType transactionType);

    @Query("SELECT SUM(st.quantity) FROM StockTransaction st " +
            "WHERE st.stockItem = :stockItem AND st.transactionType = 'PURCHASE' " +
            "AND st.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPurchasedQuantity(
            @Param("stockItem") StockItem stockItem,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(st.quantity) FROM StockTransaction st " +
            "WHERE st.stockItem = :stockItem AND st.transactionType = 'USAGE' " +
            "AND st.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalUsedQuantity(
            @Param("stockItem") StockItem stockItem,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(st.totalAmount) FROM StockTransaction st " +
            "WHERE st.transactionType = 'PURCHASE' " +
            "AND st.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalPurchaseAmount(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(st.totalAmount) FROM StockTransaction st " +
            "WHERE st.transactionType = 'USAGE' " +
            "AND st.transactionDate BETWEEN :startDate AND :endDate")
    BigDecimal getTotalUsageAmount(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT st.stockItem, SUM(st.quantity) FROM StockTransaction st " +
            "WHERE st.transactionType = 'USAGE' " +
            "AND st.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY st.stockItem ORDER BY SUM(st.quantity) DESC")
    List<Object[]> getTopUsedItems(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT st.stockItem, SUM(st.totalAmount) FROM StockTransaction st " +
            "WHERE st.transactionType = 'PURCHASE' " +
            "AND st.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY st.stockItem ORDER BY SUM(st.totalAmount) DESC")
    List<Object[]> getTopPurchasedItems(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT DATE(st.transactionDate), SUM(st.totalAmount) FROM StockTransaction st " +
            "WHERE st.transactionType = 'PURCHASE' " +
            "AND st.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(st.transactionDate) ORDER BY DATE(st.transactionDate)")
    List<Object[]> getDailyPurchaseSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT DATE(st.transactionDate), SUM(st.quantity) FROM StockTransaction st " +
            "WHERE st.stockItem = :stockItem " +
            "AND st.transactionType = 'USAGE' " +
            "AND st.transactionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(st.transactionDate) ORDER BY DATE(st.transactionDate)")
    List<Object[]> getDailyUsageSummary(
            @Param("stockItem") StockItem stockItem,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT EXTRACT(MONTH FROM st.transactionDate), SUM(st.totalAmount) FROM StockTransaction st " +
            "WHERE st.transactionType = 'PURCHASE' " +
            "AND EXTRACT(YEAR FROM st.transactionDate) = :year " +
            "GROUP BY EXTRACT(MONTH FROM st.transactionDate) ORDER BY EXTRACT(MONTH FROM st.transactionDate)")
    List<Object[]> getMonthlyPurchaseSummary(@Param("year") int year);

    @Query("SELECT EXTRACT(MONTH FROM st.transactionDate), SUM(st.totalAmount) FROM StockTransaction st " +
            "WHERE st.transactionType = 'USAGE' " +
            "AND EXTRACT(YEAR FROM st.transactionDate) = :year " +
            "GROUP BY EXTRACT(MONTH FROM st.transactionDate) ORDER BY EXTRACT(MONTH FROM st.transactionDate)")
    List<Object[]> getMonthlyUsageSummary(@Param("year") int year);
}