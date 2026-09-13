package com.mess.app.repository;

import com.mess.app.entity.Candidate;
import com.mess.app.entity.DailyMeal;
import com.mess.app.entity.DailyMeal.MealType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyMealRepository extends JpaRepository<DailyMeal, String> {

        Optional<DailyMeal> findByCandidateAndMealDateAndMealType(
                        Candidate candidate, LocalDate date, MealType mealType);

        List<DailyMeal> findByCandidateAndMealDate(Candidate candidate, LocalDate date);

        List<DailyMeal> findByMealDate(LocalDate date);

        List<DailyMeal> findByCandidateOrderByMealDateDesc(Candidate candidate);

        List<DailyMeal> findByCandidateAndMealDateBetween(
                        Candidate candidate, LocalDate startDate, LocalDate endDate);

        List<DailyMeal> findByMealTypeAndMealDate(MealType mealType, LocalDate date);

        // ADD THIS MISSING METHOD - Count meals for candidate in date range
        @Query("SELECT COUNT(d) FROM DailyMeal d WHERE d.candidate = :candidate " +
                        "AND d.mealDate BETWEEN :startDate AND :endDate AND d.isTaken = true")
        int countMealsForCandidate(@Param("candidate") Candidate candidate,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // ADD THIS MISSING METHOD - Get meal type count
        @Query("SELECT d.mealType, COUNT(d) FROM DailyMeal d " +
                        "WHERE d.mealDate BETWEEN :startDate AND :endDate AND d.isTaken = true " +
                        "GROUP BY d.mealType")
        List<Object[]> getMealTypeCount(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        // ADD THIS MISSING METHOD - Count meals served
        @Query("SELECT COUNT(d) FROM DailyMeal d " +
                        "WHERE d.mealDate BETWEEN :startDate AND :endDate AND d.isTaken = true")
        long countMealsServed(@Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT COUNT(d) FROM DailyMeal d WHERE d.candidate = :candidate " +
                        "AND d.mealDate = :date AND d.isTaken = true")
        long countMealsTakenByCandidateOnDate(
                        @Param("candidate") Candidate candidate,
                        @Param("date") LocalDate date);

        @Query("SELECT d.mealType, COUNT(d) FROM DailyMeal d " +
                        "WHERE d.mealDate = CURRENT_DATE AND d.isTaken = true GROUP BY d.mealType")
        List<Object[]> getTodayMealCountByType();

        @Query("SELECT d.mealDate, d.mealType, COUNT(d) FROM DailyMeal d " +
                        "WHERE d.mealDate BETWEEN :startDate AND :endDate AND d.isTaken = true " +
                        "GROUP BY d.mealDate, d.mealType ORDER BY d.mealDate DESC")
        List<Object[]> getMealSummaryByDateRange(
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        boolean existsByCandidateAndMealDateAndMealType(
                        Candidate candidate, LocalDate date, MealType mealType);

        @Query("SELECT COUNT(d) FROM DailyMeal d WHERE d.candidate = :candidate " +
                        "AND d.mealDate BETWEEN :startDate AND :endDate AND d.isTaken = true")
        long countMealsInDateRange(
                        @Param("candidate") Candidate candidate,
                        @Param("startDate") LocalDate startDate,
                        @Param("endDate") LocalDate endDate);

        @Query("SELECT DISTINCT d.candidate FROM DailyMeal d " +
                        "WHERE d.mealDate = :date AND d.mealType = :mealType AND d.isTaken = false")
        List<Candidate> findCandidatesMissedMeal(
                        @Param("date") LocalDate date,
                        @Param("mealType") MealType mealType);
}