package com.mess.app.repository;

import com.mess.app.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate; // THIS IS THE MISSING IMPORT
import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {

    List<MenuItem> findByMenuId(String menuId);

    List<MenuItem> findByMenuIdOrderByDisplayOrderAsc(String menuId);

    List<MenuItem> findByItemNameContainingIgnoreCase(String itemName);

    List<MenuItem> findByIsVegetarianTrue();

    List<MenuItem> findByIsAvailableTrue();

    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.id = :menuId AND mi.isAvailable = true ORDER BY mi.displayOrder ASC")
    List<MenuItem> findAvailableItemsByMenu(@Param("menuId") String menuId);

    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.menuDate = CURRENT_DATE AND mi.isAvailable = true")
    List<MenuItem> findTodayAvailableItems();

    @Modifying
    @Transactional
    @Query("UPDATE MenuItem mi SET mi.isAvailable = :available WHERE mi.menu.id = :menuId")
    void updateAvailabilityByMenu(@Param("menuId") String menuId, @Param("available") boolean available);

    @Modifying
    @Transactional
    @Query("DELETE FROM MenuItem mi WHERE mi.menu.id = :menuId")
    void deleteByMenuId(@Param("menuId") String menuId);

    @Query("SELECT AVG(mi.price) FROM MenuItem mi WHERE mi.menu.id = :menuId")
    BigDecimal getAveragePriceByMenu(@Param("menuId") String menuId);

    @Query("SELECT mi.category, COUNT(mi) FROM MenuItem mi WHERE mi.menu.id = :menuId GROUP BY mi.category")
    List<Object[]> getCategoryWiseItemCount(@Param("menuId") String menuId);

    // Line 50 - Uses LocalDate
    @Query("SELECT mi FROM MenuItem mi WHERE mi.menu.menuDate BETWEEN :startDate AND :endDate")
    List<MenuItem> findItemsByDateRange(@Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}