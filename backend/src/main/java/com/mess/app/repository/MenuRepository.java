package com.mess.app.repository;

import com.mess.app.entity.Menu;
import com.mess.app.entity.Menu.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<Menu, String> {

    Optional<Menu> findByMenuDateAndMealType(LocalDate date, MealType mealType);

    List<Menu> findByMenuDate(LocalDate date);

    List<Menu> findByMenuDateBetween(LocalDate startDate, LocalDate endDate);

    List<Menu> findByMealType(MealType mealType);

    List<Menu> findByIsSpecialTrue();

    List<Menu> findByIsActiveTrue();

    @Query("SELECT m FROM Menu m WHERE m.menuDate = CURRENT_DATE")
    List<Menu> findTodayMenu();

    @Query("SELECT m FROM Menu m WHERE m.menuDate = CURRENT_DATE AND m.isActive = true")
    List<Menu> findTodayActiveMenu();

    @Query("SELECT m FROM Menu m WHERE m.menuDate BETWEEN :startDate AND :endDate ORDER BY m.menuDate ASC, m.mealType ASC")
    List<Menu> findWeeklyMenu(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM Menu m WHERE EXTRACT(MONTH FROM m.menuDate) = :month AND EXTRACT(YEAR FROM m.menuDate) = :year")
    List<Menu> findMonthlyMenu(@Param("month") int month, @Param("year") int year);

    @Query("SELECT m FROM Menu m WHERE m.menuDate >= :date ORDER BY m.menuDate ASC")
    List<Menu> findUpcomingMenu(@Param("date") LocalDate date);

    @Query("SELECT COUNT(m) FROM Menu m WHERE m.menuDate = :date")
    long countMenuByDate(@Param("date") LocalDate date);

    @Query("SELECT m.menuDate, COUNT(m) FROM Menu m WHERE m.menuDate BETWEEN :startDate AND :endDate GROUP BY m.menuDate")
    List<Object[]> getDailyMenuCount(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM Menu m WHERE m.menuDate = :date AND m.isActive = true ORDER BY m.mealType ASC")
    List<Menu> findActiveMenuByDate(@Param("date") LocalDate date);
}