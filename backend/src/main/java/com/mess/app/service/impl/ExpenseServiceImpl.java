package com.mess.app.service.impl;

import com.mess.app.dto.request.ExpenseRequest;
import com.mess.app.dto.response.ExpenseResponse;
import com.mess.app.dto.response.ExpenseSummaryResponse;
import com.mess.app.entity.Candidate;
import com.mess.app.entity.Expense;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.CandidateRepository;
import com.mess.app.repository.ExpenseRepository;
import com.mess.app.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

        private final ExpenseRepository expenseRepository;
        private final CandidateRepository candidateRepository;

        @Override
        @Transactional
        public ExpenseResponse createExpense(ExpenseRequest request) {
                log.info("Creating new expense: {}", request.getItemName());

                Candidate enteredBy = null;
                if (request.getEnteredByCandidateId() != null) {
                        enteredBy = candidateRepository.findById(request.getEnteredByCandidateId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Candidate not found with ID: "
                                                                        + request.getEnteredByCandidateId()));
                }

                BigDecimal totalAmount = request.getQuantity().multiply(request.getRate());

                Expense expense = Expense.builder()
                                .itemName(request.getItemName())
                                .quantity(request.getQuantity())
                                .unit(request.getUnit())
                                .rate(request.getRate())
                                .totalAmount(totalAmount)
                                .category(request.getCategory())
                                .subCategory(request.getSubCategory())
                                .vendorName(request.getVendorName())
                                .vendorPhone(request.getVendorPhone())
                                .invoiceNumber(request.getInvoiceNumber())
                                .expenseDate(request.getExpenseDate() != null ? request.getExpenseDate()
                                                : LocalDate.now())
                                .paymentMethod(request.getPaymentMethod())
                                .paymentStatus(request.getPaymentStatus() != null ? request.getPaymentStatus() : "PAID")
                                .attachmentUrl(request.getAttachmentUrl())
                                .attachmentName(request.getAttachmentName())
                                .remarks(request.getRemarks())
                                .enteredBy(enteredBy)
                                .build();

                Expense saved = expenseRepository.save(expense);
                log.info("Expense created with ID: {}", saved.getId());

                return convertToResponse(saved);
        }

        @Override
        @Transactional
        public ExpenseResponse updateExpense(String id, ExpenseRequest request) {
                log.info("Updating expense with ID: {}", id);

                Expense expense = expenseRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));

                expense.setItemName(request.getItemName());
                expense.setQuantity(request.getQuantity());
                expense.setUnit(request.getUnit());
                expense.setRate(request.getRate());
                expense.setTotalAmount(request.getQuantity().multiply(request.getRate()));
                expense.setCategory(request.getCategory());
                expense.setSubCategory(request.getSubCategory());
                expense.setVendorName(request.getVendorName());
                expense.setVendorPhone(request.getVendorPhone());
                expense.setInvoiceNumber(request.getInvoiceNumber());
                expense.setExpenseDate(request.getExpenseDate());
                expense.setPaymentMethod(request.getPaymentMethod());
                expense.setPaymentStatus(request.getPaymentStatus());
                expense.setAttachmentUrl(request.getAttachmentUrl());
                expense.setAttachmentName(request.getAttachmentName());
                expense.setRemarks(request.getRemarks());

                if (request.getEnteredByCandidateId() != null) {
                        Candidate enteredBy = candidateRepository.findById(request.getEnteredByCandidateId())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Candidate not found with ID: "
                                                                        + request.getEnteredByCandidateId()));
                        expense.setEnteredBy(enteredBy);
                }

                Expense updated = expenseRepository.save(expense);
                log.info("Expense updated successfully");

                return convertToResponse(updated);
        }

        @Override
        @Transactional
        public ExpenseResponse getExpenseById(String id) {
                Expense expense = expenseRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
                return convertToResponse(expense);
        }

        @Override
        @Transactional
        public List<ExpenseResponse> getExpensesByDate(LocalDate date) {
                return expenseRepository.findByExpenseDate(date).stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public List<ExpenseResponse> getExpensesByDateRange(LocalDate startDate, LocalDate endDate) {
                return expenseRepository.findByExpenseDateBetween(startDate, endDate).stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public List<ExpenseResponse> getExpensesByCategory(String category) {
                return expenseRepository.findByCategory(category).stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public List<ExpenseResponse> getExpensesByCategoryAndDateRange(
                        String category, LocalDate startDate, LocalDate endDate) {
                return expenseRepository.findByCategoryAndExpenseDateBetween(category, startDate, endDate).stream()
                                .map(this::convertToResponse)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public void deleteExpense(String id) {
                log.info("Deleting expense with ID: {}", id);
                Expense expense = expenseRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with ID: " + id));
                expenseRepository.delete(expense);
                log.info("Expense deleted successfully");
        }

        @Override
        @Transactional
        public BigDecimal getTotalExpenses(LocalDate startDate, LocalDate endDate) {
                BigDecimal total = expenseRepository.getTotalExpensesByDateRange(startDate, endDate);
                return total != null ? total : BigDecimal.ZERO;
        }

        @Override
        @Transactional
        public BigDecimal getTodayTotalExpenses() {
                BigDecimal total = expenseRepository.getTodayTotalExpenses();
                return total != null ? total : BigDecimal.ZERO;
        }

        @Override
        @Transactional
        public ExpenseSummaryResponse getExpenseSummary(LocalDate startDate, LocalDate endDate) {
                log.info("Generating expense summary from {} to {}", startDate, endDate);

                BigDecimal totalExpenses = getTotalExpenses(startDate, endDate);

                Double avgDaily = expenseRepository.getAverageDailyExpense(startDate, endDate);
                BigDecimal averageDailyExpense = avgDaily != null
                                ? BigDecimal.valueOf(avgDaily).setScale(2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;

                List<Expense> expenses = expenseRepository.findByExpenseDateBetween(startDate, endDate);
                int totalExpenseCount = expenses.size();

                Map<String, BigDecimal> categoryWise = getCategoryWiseExpenses(startDate, endDate);

                Map<String, BigDecimal> vendorWise = new HashMap<>();
                List<Object[]> vendorResults = expenseRepository.getVendorWiseExpenses(startDate, endDate);
                for (Object[] result : vendorResults) {
                        vendorWise.put((String) result[0], (BigDecimal) result[1]);
                }

                Map<String, BigDecimal> paymentMethodWise = new HashMap<>();
                List<Object[]> paymentResults = expenseRepository.getExpensesByPaymentMethod(startDate, endDate);
                for (Object[] result : paymentResults) {
                        paymentMethodWise.put((String) result[0], (BigDecimal) result[1]);
                }

                List<ExpenseSummaryResponse.DailyExpense> dailyExpenses = new ArrayList<>();
                List<Object[]> dailyResults = expenseRepository.getDailyExpensesForMonth(startDate, endDate);
                for (Object[] result : dailyResults) {
                        dailyExpenses.add(ExpenseSummaryResponse.DailyExpense.builder()
                                        .date((LocalDate) result[0])
                                        .amount((BigDecimal) result[1])
                                        .build());
                }

                List<ExpenseSummaryResponse.TopExpenseItem> topItems = new ArrayList<>();
                List<Object[]> topResults = expenseRepository.getTopExpenseItems(startDate, endDate);
                for (Object[] result : topResults) {
                        topItems.add(ExpenseSummaryResponse.TopExpenseItem.builder()
                                        .itemName((String) result[0])
                                        .totalAmount((BigDecimal) result[1])
                                        .build());
                }

                return ExpenseSummaryResponse.builder()
                                .startDate(startDate)
                                .endDate(endDate)
                                .totalExpenses(totalExpenses)
                                .averageDailyExpense(averageDailyExpense)
                                .totalExpenseCount(totalExpenseCount)
                                .categoryWiseExpenses(categoryWise)
                                .vendorWiseExpenses(vendorWise)
                                .paymentMethodWiseExpenses(paymentMethodWise)
                                .dailyExpenses(dailyExpenses)
                                .topExpenseItems(topItems)
                                .build();
        }

        @Override
        @Transactional
        public Map<String, BigDecimal> getCategoryWiseExpenses(LocalDate startDate, LocalDate endDate) {
                Map<String, BigDecimal> categoryWise = new LinkedHashMap<>();
                List<Object[]> results = expenseRepository.getCategoryWiseExpenses(startDate, endDate);
                for (Object[] result : results) {
                        categoryWise.put((String) result[0], (BigDecimal) result[1]);
                }
                return categoryWise;
        }

        @Override
        @Transactional
        public Map<String, BigDecimal> getMonthlyExpensesForYear(int year) {
                Map<String, BigDecimal> monthlyExpenses = new LinkedHashMap<>();
                List<Object[]> results = expenseRepository.getMonthlyExpensesForYear(year);
                for (Object[] result : results) {
                        int month = ((Number) result[0]).intValue();
                        BigDecimal amount = (BigDecimal) result[1];
                        String monthName = LocalDate.of(year, month, 1).getMonth().name();
                        monthlyExpenses.put(monthName, amount);
                }
                return monthlyExpenses;
        }

        @Override
        @Transactional
        public List<Map<String, Object>> getDailyExpensesForMonth(int month, int year) {
                LocalDate startDate = LocalDate.of(year, month, 1);
                LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

                List<Map<String, Object>> dailyExpenses = new ArrayList<>();
                List<Object[]> results = expenseRepository.getDailyExpensesForMonth(startDate, endDate);

                Map<LocalDate, BigDecimal> expenseMap = new HashMap<>();
                for (Object[] result : results) {
                        expenseMap.put((LocalDate) result[0], (BigDecimal) result[1]);
                }

                for (int day = 1; day <= endDate.getDayOfMonth(); day++) {
                        LocalDate date = LocalDate.of(year, month, day);
                        Map<String, Object> dailyData = new HashMap<>();
                        dailyData.put("date", date);
                        dailyData.put("amount", expenseMap.getOrDefault(date, BigDecimal.ZERO));
                        dailyExpenses.add(dailyData);
                }

                return dailyExpenses;
        }

        // Helper method to convert Entity to Response DTO
        private ExpenseResponse convertToResponse(Expense expense) {
                return ExpenseResponse.builder()
                                .id(expense.getId())
                                .itemName(expense.getItemName())
                                .quantity(expense.getQuantity())
                                .unit(expense.getUnit())
                                .rate(expense.getRate())
                                .totalAmount(expense.getTotalAmount())
                                .category(expense.getCategory())
                                .subCategory(expense.getSubCategory())
                                .vendorName(expense.getVendorName())
                                .vendorPhone(expense.getVendorPhone())
                                .invoiceNumber(expense.getInvoiceNumber())
                                .expenseDate(expense.getExpenseDate())
                                .paymentMethod(expense.getPaymentMethod())
                                .paymentStatus(expense.getPaymentStatus())
                                .attachmentUrl(expense.getAttachmentUrl())
                                .attachmentName(expense.getAttachmentName())
                                .remarks(expense.getRemarks())
                                .enteredBy(expense.getEnteredBy() != null
                                                ? expense.getEnteredBy().getFullName() + " ("
                                                                + expense.getEnteredBy().getCandidateId() + ")"
                                                : null)
                                .createdAt(expense.getCreatedAt())
                                .updatedAt(expense.getUpdatedAt())
                                .build();
        }
}