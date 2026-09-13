package com.mess.app.service.impl;

import com.mess.app.dto.request.BulkMealRequest;
import com.mess.app.dto.request.DailyMealRequest;
import com.mess.app.dto.response.CandidateMealSummary;
import com.mess.app.dto.response.DailyMealResponse;
import com.mess.app.dto.response.MealSummaryResponse;
import com.mess.app.entity.Candidate;
import com.mess.app.entity.DailyMeal;
import com.mess.app.entity.DailyMeal.MealType;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.CandidateRepository;
import com.mess.app.repository.DailyMealRepository;
import com.mess.app.service.DailyMealService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DailyMealServiceImpl implements DailyMealService {

        private final DailyMealRepository mealRepository;
        private final CandidateRepository candidateRepository;

        @Override
        @Transactional
        public DailyMealResponse markMeal(DailyMealRequest request) {
                log.info("Marking meal for candidate: {} on date: {} for meal: {}",
                                request.getCandidateId(), request.getMealDate(), request.getMealType());

                Candidate candidate = candidateRepository.findById(request.getCandidateId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Candidate not found with ID: " + request.getCandidateId()));

                LocalDate mealDate = request.getMealDate() != null ? request.getMealDate() : LocalDate.now();
                MealType mealType = MealType.valueOf(request.getMealType().toUpperCase());

                if (mealRepository.existsByCandidateAndMealDateAndMealType(candidate, mealDate, mealType)) {
                        throw new RuntimeException("Meal already marked for this candidate on this date");
                }

                DailyMeal meal = DailyMeal.builder()
                                .candidate(candidate)
                                .mealDate(mealDate)
                                .mealType(mealType)
                                .isTaken(request.isTaken())
                                .mealPreference(request.getMealPreference())
                                .remarks(request.getRemarks())
                                .build();

                DailyMeal saved = mealRepository.save(meal);
                log.info("Meal marked successfully for candidate: {}", candidate.getCandidateId());

                return convertToResponse(saved);
        }

        @Override
        @Transactional
        public DailyMealResponse updateMeal(String id, DailyMealRequest request) {
                log.info("Updating meal with ID: {}", id);

                DailyMeal meal = mealRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with ID: " + id));

                if (request.isTaken() != meal.isTaken()) {
                        meal.setTaken(request.isTaken());
                }
                if (request.getMealPreference() != null) {
                        meal.setMealPreference(request.getMealPreference());
                }
                if (request.getRemarks() != null) {
                        meal.setRemarks(request.getRemarks());
                }

                DailyMeal updated = mealRepository.save(meal);
                log.info("Meal updated successfully");

                return convertToResponse(updated);
        }

        @Override
        @Transactional 
        public DailyMealResponse getMealById(String id) {
                DailyMeal meal = mealRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with ID: " + id));
                return convertToResponse(meal);
        }

        @Override
        @Transactional 
        public List<DailyMealResponse> getMealsByDate(LocalDate date) {
                return mealRepository.findByMealDate(date).stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional 
        public List<DailyMealResponse> getMealsByCandidate(String candidateId) {
                Candidate candidate = candidateRepository.findById(candidateId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Candidate not found with ID: " + candidateId));

                return mealRepository.findByCandidateOrderByMealDateDesc(candidate).stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional 
        public List<DailyMealResponse> getMealsByCandidateAndDate(String candidateId, LocalDate date) {
                Candidate candidate = candidateRepository.findById(candidateId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Candidate not found with ID: " + candidateId));

                return mealRepository.findByCandidateAndMealDate(candidate, date).stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional 
        public List<DailyMealResponse> getMealsByCandidateAndDateRange(
                        String candidateId, LocalDate startDate, LocalDate endDate) {

                Candidate candidate = candidateRepository.findById(candidateId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Candidate not found with ID: " + candidateId));

                return mealRepository.findByCandidateAndMealDateBetween(candidate, startDate, endDate)
                                .stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional 
        public List<DailyMealResponse> getMealsByTypeAndDate(String mealType, LocalDate date) {
                MealType type = MealType.valueOf(mealType.toUpperCase());
                return mealRepository.findByMealTypeAndMealDate(type, date).stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void bulkMarkMeals(BulkMealRequest request) {
                log.info("Bulk marking meals for date: {}, meal type: {}",
                                request.getMealDate(), request.getMealType());

                LocalDate mealDate = request.getMealDate() != null ? request.getMealDate() : LocalDate.now();
                MealType mealType = MealType.valueOf(request.getMealType().toUpperCase());

                if (request.getCandidateIds() != null) {
                        for (String candidateId : request.getCandidateIds()) {
                                try {
                                        Candidate candidate = candidateRepository.findById(candidateId)
                                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                                        "Candidate not found: " + candidateId));

                                        if (!mealRepository.existsByCandidateAndMealDateAndMealType(
                                                        candidate, mealDate, mealType)) {
                                                DailyMeal meal = DailyMeal.builder()
                                                                .candidate(candidate)
                                                                .mealDate(mealDate)
                                                                .mealType(mealType)
                                                                .isTaken(true)
                                                                .mealPreference(
                                                                                request.getMealPreference() != null
                                                                                                ? request.getMealPreference()
                                                                                                : "VEG")
                                                                .build();
                                                mealRepository.save(meal);
                                        }
                                } catch (Exception e) {
                                        log.error("Error marking meal for candidate: {}", candidateId, e);
                                }
                        }
                }

                log.info("Bulk meal marking completed");
        }

        @Override
        @Transactional
        public void deleteMeal(String id) {
                log.info("Deleting meal with ID: {}", id);
                DailyMeal meal = mealRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Meal not found with ID: " + id));
                mealRepository.delete(meal);
                log.info("Meal deleted successfully");
        }

        @Override
        @Transactional 
        public MealSummaryResponse getDailyMealSummary(LocalDate date) {
                LocalDate summaryDate = date != null ? date : LocalDate.now();

                List<Candidate> activeCandidates = candidateRepository.findByStatus(Candidate.Status.ACTIVE);
                int totalCandidates = activeCandidates.size();

                List<DailyMeal> meals = mealRepository.findByMealDate(summaryDate);

                Map<String, Integer> mealCount = new HashMap<>();
                Map<String, Double> mealPercentage = new HashMap<>();

                for (MealType type : MealType.values()) {
                        long count = meals.stream()
                                        .filter(m -> m.getMealType() == type && m.isTaken())
                                        .count();
                        mealCount.put(type.name(), (int) count);
                        mealPercentage.put(type.name(),
                                        totalCandidates > 0 ? (double) count / totalCandidates * 100 : 0.0);
                }

                int totalMealsServed = meals.stream()
                                .filter(DailyMeal::isTaken)
                                .mapToInt(m -> 1)
                                .sum();

                return MealSummaryResponse.builder()
                                .date(summaryDate)
                                .totalCandidates(totalCandidates)
                                .mealCount(mealCount)
                                .mealPercentage(mealPercentage)
                                .totalMealsServed(totalMealsServed)
                                .build();
        }

        @Override
        @Transactional 
        public Map<String, Long> getTodayMealStats() {
                LocalDate today = LocalDate.now();
                List<Object[]> results = mealRepository.getTodayMealCountByType();

                Map<String, Long> stats = new HashMap<>();
                for (Object[] result : results) {
                        MealType type = (MealType) result[0];
                        Long count = (Long) result[1];
                        stats.put(type.name(), count);
                }

                long total = stats.values().stream().mapToLong(Long::longValue).sum();
                stats.put("TOTAL", total);

                return stats;
        }

        @Override
        @Transactional 
        public CandidateMealSummary getCandidateMealSummary(String candidateId, int month, int year) {
                Candidate candidate = candidateRepository.findById(candidateId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Candidate not found with ID: " + candidateId));

                LocalDate startDate = LocalDate.of(year, month, 1);
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

                List<DailyMeal> meals = mealRepository.findByCandidateAndMealDateBetween(
                                candidate, startDate, endDate);

                int breakfastCount = (int) meals.stream()
                                .filter(m -> m.getMealType() == MealType.BREAKFAST && m.isTaken())
                                .count();
                int lunchCount = (int) meals.stream()
                                .filter(m -> m.getMealType() == MealType.LUNCH && m.isTaken())
                                .count();
                int dinnerCount = (int) meals.stream()
                                .filter(m -> m.getMealType() == MealType.DINNER && m.isTaken())
                                .count();
                int totalMeals = breakfastCount + lunchCount + dinnerCount;

                int totalDays = endDate.getDayOfMonth();
                double averageMealsPerDay = totalDays > 0 ? (double) totalMeals / totalDays : 0.0;

                return CandidateMealSummary.builder()
                                .candidateId(candidate.getCandidateId())
                                .candidateName(candidate.getFullName())
                                .totalDays(totalDays)
                                .breakfastCount(breakfastCount)
                                .lunchCount(lunchCount)
                                .dinnerCount(dinnerCount)
                                .totalMeals(totalMeals)
                                .averageMealsPerDay(Math.round(averageMealsPerDay * 100.0) / 100.0)
                                .build();
        }

        @Override
        @Transactional 
        public List<Candidate> getCandidatesMissedMeal(LocalDate date, String mealType) {
                MealType type = MealType.valueOf(mealType.toUpperCase());
                return mealRepository.findCandidatesMissedMeal(date, type);
        }

        // Helper method to convert Entity to Response DTO
        private DailyMealResponse convertToResponse(DailyMeal meal) {
                return DailyMealResponse.builder()
                                .id(meal.getId())
                                .candidateId(meal.getCandidate().getCandidateId())
                                .candidateName(meal.getCandidate().getFullName())
                                .mealDate(meal.getMealDate())
                                .mealType(meal.getMealType().name())
                                .isTaken(meal.isTaken())
                                .mealPreference(meal.getMealPreference())
                                .remarks(meal.getRemarks())
                                .createdAt(meal.getCreatedAt())
                                .updatedAt(meal.getUpdatedAt())
                                .build();
        }
}