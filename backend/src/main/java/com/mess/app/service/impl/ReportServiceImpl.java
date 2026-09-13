package com.mess.app.service.impl;

import com.mess.app.dto.request.ReportRequest;
import com.mess.app.dto.response.ComprehensiveReportResponse;
import com.mess.app.entity.*;
import com.mess.app.repository.*;
import com.mess.app.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final CandidateRepository candidateRepository;
    private final DailyAttendanceRepository attendanceRepository;
    private final DailyMealRepository dailyMealRepository;
    private final ExpenseRepository expenseRepository;
    private final StockItemRepository stockItemRepository;
    private final StaffRepository staffRepository;
    private final StaffPaymentRepository staffPaymentRepository;
    private final StaffAdvanceRepository staffAdvanceRepository;
    private final FeeCollectionRepository feeCollectionRepository;
    private final PartyRepository partyRepository;
    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final MonthlyBillingRepository monthlyBillingRepository;

    // ==================== COMPREHENSIVE REPORTS ====================

    @Override
    @Transactional
    public ComprehensiveReportResponse generateComprehensiveReport(ReportRequest request) {
        log.info("Generating comprehensive report of type: {}", request.getReportType());

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (request.getReportType().equalsIgnoreCase("MONTHLY") && request.getMonth() != null) {
            startDate = LocalDate.of(request.getYear(), request.getMonth(), 1);
            endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        } else if (request.getReportType().equalsIgnoreCase("YEARLY") && request.getYear() != null) {
            startDate = LocalDate.of(request.getYear(), 1, 1);
            endDate = LocalDate.of(request.getYear(), 12, 31);
        }

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
            endDate = LocalDate.now();
        }

        return generateFullReport(startDate, endDate);
    }

    @Override
    @Transactional
    public ComprehensiveReportResponse generateMonthlyReport(int month, int year) {
        log.info("Generating monthly report for {}/{}", month, year);
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return generateFullReport(startDate, endDate);
    }

    @Override
    @Transactional
    public ComprehensiveReportResponse generateYearlyReport(int year) {
        log.info("Generating yearly report for {}", year);
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return generateFullReport(startDate, endDate);
    }

    @Override
    @Transactional
    public ComprehensiveReportResponse generateCustomReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating custom report from {} to {}", startDate, endDate);
        return generateFullReport(startDate, endDate);
    }

    private ComprehensiveReportResponse generateFullReport(LocalDate startDate, LocalDate endDate) {
        log.info("Generating full report from {} to {}", startDate, endDate);

        return ComprehensiveReportResponse.builder()
                .reportType("CUSTOM")
                .reportDate(LocalDate.now())
                .startDate(startDate)
                .endDate(endDate)
                .generatedAt(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                .summary(generateSummary(startDate, endDate))
                .candidateReport(generateCandidateReportData(startDate, endDate))
                .attendanceReport(generateAttendanceReportData(startDate, endDate))
                .expenseReport(generateExpenseReportData(startDate, endDate))
                .stockReport(generateStockReportData())
                .staffReport(generateStaffReportData(startDate, endDate))
                .billingReport(generateBillingReportData(startDate, endDate))
                .partyReport(generatePartyReportData(startDate, endDate))
                .menuReport(generateMenuReportData(startDate, endDate))
                .profitLossReport(generateProfitLossReportData(startDate, endDate))
                .build();
    }

    // ==================== SUMMARY ====================

    private ComprehensiveReportResponse.Summary generateSummary(LocalDate startDate, LocalDate endDate) {
        log.info("Generating summary for report");

        long totalCandidates = candidateRepository.count();
        long activeCandidates = candidateRepository.countByStatus(Candidate.Status.ACTIVE);
        long totalStaff = staffRepository.count();
        long activeStaff = staffRepository.countByStatus(Staff.StaffStatus.ACTIVE);
        long totalParties = partyRepository.countPartiesByDateRange(startDate, endDate);

        // Total Income from Fee Collections
        BigDecimal totalIncome = feeCollectionRepository.getTotalFeesByDateRange(startDate, endDate);
        if (totalIncome == null)
            totalIncome = BigDecimal.ZERO;

        // Party Income
        BigDecimal partyIncome = partyRepository.getTotalPartyBilling(startDate, endDate);
        if (partyIncome == null)
            partyIncome = BigDecimal.ZERO;
        totalIncome = totalIncome.add(partyIncome);

        // Total Expenses
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByDateRange(startDate, endDate);
        if (totalExpenses == null)
            totalExpenses = BigDecimal.ZERO;

        // Add staff salary expenses
        BigDecimal staffSalaryExpenses = staffPaymentRepository.getTotalPaymentsByDateRange(startDate, endDate);
        if (staffSalaryExpenses != null) {
            totalExpenses = totalExpenses.add(staffSalaryExpenses);
        }

        BigDecimal netProfit = totalIncome.subtract(totalExpenses);

        // Stock Value
        BigDecimal totalStockValue = stockItemRepository.getTotalStockValue();
        if (totalStockValue == null)
            totalStockValue = BigDecimal.ZERO;

        // Pending Fees
        BigDecimal pendingFees = feeCollectionRepository.getTotalPendingFees();
        if (pendingFees == null)
            pendingFees = BigDecimal.ZERO;

        // Total Meals Served
        long totalMealsServed = dailyMealRepository.countMealsServed(startDate, endDate);

        return ComprehensiveReportResponse.Summary.builder()
                .totalCandidates((int) totalCandidates)
                .activeCandidates((int) activeCandidates)
                .totalStaff((int) totalStaff)
                .activeStaff((int) activeStaff)
                .totalParties((int) totalParties)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .totalStockValue(totalStockValue)
                .pendingFees(pendingFees)
                .totalMealsServed((int) totalMealsServed)
                .totalPartyRevenue(partyIncome)
                .build();
    }

    // ==================== CANDIDATE REPORT ====================

    private ComprehensiveReportResponse.CandidateReport generateCandidateReportData(
            LocalDate startDate, LocalDate endDate) {

        log.info("Generating candidate report data");

        List<Candidate> candidates = candidateRepository.findAll();
        List<ComprehensiveReportResponse.CandidateReport.CandidateDetail> details = new ArrayList<>();

        for (Candidate candidate : candidates) {
            // Count present days
            int daysPresent = 0;
            try {
                daysPresent = attendanceRepository.countPresentDaysForCandidate(candidate, startDate, endDate);
            } catch (Exception e) {
                log.warn("Could not get attendance for candidate: {}", candidate.getCandidateId());
            }

            // Count meals
            int totalMeals = 0;
            try {
                totalMeals = dailyMealRepository.countMealsForCandidate(candidate, startDate, endDate);
            } catch (Exception e) {
                log.warn("Could not get meals for candidate: {}", candidate.getCandidateId());
            }

            // Fees paid
            BigDecimal feesPaid = feeCollectionRepository.getFeesPaidByCandidate(candidate, startDate, endDate);
            if (feesPaid == null)
                feesPaid = BigDecimal.ZERO;

            BigDecimal pendingFees = candidate.getMonthlyRate().subtract(feesPaid);
            if (pendingFees.compareTo(BigDecimal.ZERO) < 0)
                pendingFees = BigDecimal.ZERO;

            details.add(ComprehensiveReportResponse.CandidateReport.CandidateDetail.builder()
                    .candidateId(candidate.getCandidateId())
                    .name(candidate.getFullName())
                    .phone(candidate.getPhoneNumber())
                    .status(candidate.getStatus().name())
                    .joiningDate(candidate.getJoiningDate())
                    .monthlyRate(candidate.getMonthlyRate())
                    .daysPresent(daysPresent)
                    .totalMeals(totalMeals)
                    .feesPaid(feesPaid)
                    .pendingFees(pendingFees)
                    .build());
        }

        Map<String, Integer> monthlyJoining = new LinkedHashMap<>();
        try {
            List<Object[]> joiningData = candidateRepository.getMonthlyJoining(startDate.getYear());
            for (Object[] data : joiningData) {
                int month = ((Number) data[0]).intValue();
                long count = (Long) data[1];
                monthlyJoining.put(LocalDate.of(startDate.getYear(), month, 1).getMonth().name(), (int) count);
            }
        } catch (Exception e) {
            log.warn("Could not get monthly joining data");
        }

        return ComprehensiveReportResponse.CandidateReport.builder()
                .totalCandidates(candidates.size())
                .activeCandidates((int) candidateRepository.countByStatus(Candidate.Status.ACTIVE))
                .leftCandidates((int) candidateRepository.countByStatus(Candidate.Status.LEFT))
                .details(details)
                .monthlyJoining(monthlyJoining)
                .build();
    }

    // ==================== ATTENDANCE REPORT ====================

    private ComprehensiveReportResponse.AttendanceReport generateAttendanceReportData(
            LocalDate startDate, LocalDate endDate) {

        log.info("Generating attendance report data");

        List<ComprehensiveReportResponse.AttendanceReport.AttendanceDetail> details = new ArrayList<>();
        Map<String, Integer> dailyAttendance = new LinkedHashMap<>();

        try {
            List<Object[]> dailyData = attendanceRepository.getDailyAttendanceCount(startDate, endDate);
            long totalCandidates = candidateRepository.countByStatus(Candidate.Status.ACTIVE);

            for (Object[] data : dailyData) {
                LocalDate date = (LocalDate) data[0];
                long present = (Long) data[1];
                double percentage = totalCandidates > 0 ? (double) present / totalCandidates * 100 : 0.0;

                details.add(ComprehensiveReportResponse.AttendanceReport.AttendanceDetail.builder()
                        .date(date.toString())
                        .present((int) present)
                        .absent((int) (totalCandidates - present))
                        .percentage(Math.round(percentage * 100.0) / 100.0)
                        .build());

                dailyAttendance.put(date.toString(), (int) present);
            }
        } catch (Exception e) {
            log.warn("Could not get daily attendance data: {}", e.getMessage());
        }

        // Meal type count
        Map<String, Integer> mealTypeCount = new HashMap<>();
        try {
            List<Object[]> mealData = dailyMealRepository.getMealTypeCount(startDate, endDate);
            for (Object[] data : mealData) {
                String mealType = (String) data[0];
                long count = (Long) data[1];
                mealTypeCount.put(mealType, (int) count);
            }
        } catch (Exception e) {
            log.warn("Could not get meal type count: {}", e.getMessage());
        }

        long totalDays = 0;
        try {
            totalDays = attendanceRepository.getTotalAttendanceDays(startDate, endDate);
        } catch (Exception e) {
            log.warn("Could not get total attendance days: {}", e.getMessage());
        }

        int totalCandidates = (int) candidateRepository.countByStatus(Candidate.Status.ACTIVE);

        return ComprehensiveReportResponse.AttendanceReport.builder()
                .totalDays((int) totalDays)
                .totalCandidates(totalCandidates)
                .totalAttendance(dailyAttendance.values().stream().mapToInt(Integer::intValue).sum())
                .averageAttendance(
                        totalDays > 0
                                ? Math.round(
                                        (double) dailyAttendance.values().stream().mapToInt(Integer::intValue).sum()
                                                / totalDays * 100.0)
                                        / 100.0
                                : 0.0)
                .dailyAttendance(dailyAttendance)
                .mealTypeCount(mealTypeCount)
                .details(details)
                .build();
    }

    // ==================== EXPENSE REPORT ====================

    private ComprehensiveReportResponse.ExpenseReport generateExpenseReportData(
            LocalDate startDate, LocalDate endDate) {

        log.info("Generating expense report data");

        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByDateRange(startDate, endDate);
        if (totalExpenses == null)
            totalExpenses = BigDecimal.ZERO;

        List<Expense> expenses = new ArrayList<>();
        try {
            expenses = expenseRepository.findByExpenseDateBetween(startDate, endDate);
        } catch (Exception e) {
            log.warn("Could not get expenses: {}", e.getMessage());
        }

        Map<String, BigDecimal> categoryWise = new LinkedHashMap<>();
        try {
            List<Object[]> categoryData = expenseRepository.getCategoryWiseExpenses(startDate, endDate);
            for (Object[] data : categoryData) {
                categoryWise.put((String) data[0], (BigDecimal) data[1]);
            }
        } catch (Exception e) {
            log.warn("Could not get category wise expenses: {}", e.getMessage());
        }

        Map<String, BigDecimal> vendorWise = new LinkedHashMap<>();
        try {
            List<Object[]> vendorData = expenseRepository.getVendorWiseExpenses(startDate, endDate);
            for (Object[] data : vendorData) {
                vendorWise.put((String) data[0], (BigDecimal) data[1]);
            }
        } catch (Exception e) {
            log.warn("Could not get vendor wise expenses: {}", e.getMessage());
        }

        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate) + 1;
        BigDecimal averageDailyExpense = totalDays > 0
                ? totalExpenses.divide(BigDecimal.valueOf(totalDays), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<ComprehensiveReportResponse.ExpenseReport.ExpenseDetail> details = expenses.stream()
                .map(e -> ComprehensiveReportResponse.ExpenseReport.ExpenseDetail.builder()
                        .date(e.getExpenseDate().toString())
                        .item(e.getItemName())
                        .category(e.getCategory())
                        .amount(e.getTotalAmount())
                        .vendor(e.getVendorName())
                        .build())
                .collect(Collectors.toList());

        return ComprehensiveReportResponse.ExpenseReport.builder()
                .totalExpenses(totalExpenses)
                .averageDailyExpense(averageDailyExpense)
                .categoryWise(categoryWise)
                .vendorWise(vendorWise)
                .details(details)
                .build();
    }

    // ==================== STOCK REPORT ====================

    private ComprehensiveReportResponse.StockReport generateStockReportData() {
        log.info("Generating stock report data");

        List<StockItem> items = new ArrayList<>();
        try {
            items = stockItemRepository.findByActiveTrue();
        } catch (Exception e) {
            log.warn("Could not get stock items: {}", e.getMessage());
        }

        Map<String, Integer> categoryWiseCount = new LinkedHashMap<>();
        Map<String, BigDecimal> categoryWiseValue = new LinkedHashMap<>();

        List<ComprehensiveReportResponse.StockReport.StockDetail> details = new ArrayList<>();
        BigDecimal totalStockValue = BigDecimal.ZERO;
        int lowStockCount = 0;
        int overStockCount = 0;

        for (StockItem item : items) {
            BigDecimal unitPrice = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
            BigDecimal stockValue = item.getCurrentStock().multiply(unitPrice);
            totalStockValue = totalStockValue.add(stockValue);

            String status = "NORMAL";
            if (item.getCurrentStock().compareTo(item.getMinimumStockLevel()) < 0) {
                status = "LOW";
                lowStockCount++;
            } else if (item.getCurrentStock().compareTo(item.getMaximumStockLevel()) > 0) {
                status = "OVER";
                overStockCount++;
            }

            details.add(ComprehensiveReportResponse.StockReport.StockDetail.builder()
                    .itemName(item.getItemName())
                    .category(item.getCategory())
                    .currentStock(item.getCurrentStock())
                    .minStock(item.getMinimumStockLevel())
                    .maxStock(item.getMaximumStockLevel())
                    .unitPrice(unitPrice)
                    .stockValue(stockValue)
                    .status(status)
                    .build());

            String category = item.getCategory() != null ? item.getCategory() : "Uncategorized";
            categoryWiseCount.put(category, categoryWiseCount.getOrDefault(category, 0) + 1);
            categoryWiseValue.put(category, categoryWiseValue.getOrDefault(category, BigDecimal.ZERO).add(stockValue));
        }

        return ComprehensiveReportResponse.StockReport.builder()
                .totalItems(items.size())
                .lowStockItems(lowStockCount)
                .overStockItems(overStockCount)
                .totalStockValue(totalStockValue)
                .categoryWiseCount(categoryWiseCount)
                .categoryWiseValue(categoryWiseValue)
                .details(details)
                .build();
    }

    // ==================== STAFF REPORT ====================

    private ComprehensiveReportResponse.StaffReport generateStaffReportData(
            LocalDate startDate, LocalDate endDate) {

        log.info("Generating staff report data");

        List<Staff> staffList = new ArrayList<>();
        try {
            staffList = staffRepository.findAll();
        } catch (Exception e) {
            log.warn("Could not get staff list: {}", e.getMessage());
        }

        List<ComprehensiveReportResponse.StaffReport.StaffDetail> details = new ArrayList<>();

        BigDecimal totalSalaryPaid = BigDecimal.ZERO;
        BigDecimal totalBonus = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalAdvance = BigDecimal.ZERO;

        for (Staff staff : staffList) {
            List<StaffPayment> payments = new ArrayList<>();
            try {
                payments = staffPaymentRepository.findByStaffAndPaymentDateBetween(staff, startDate, endDate);
            } catch (Exception e) {
                log.warn("Could not get payments for staff: {}", staff.getStaffId());
            }

            BigDecimal paid = payments.stream()
                    .map(StaffPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal bonus = payments.stream()
                    .map(p -> p.getBonus() != null ? p.getBonus() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal deductions = payments.stream()
                    .map(p -> p.getDeductions() != null ? p.getDeductions() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal advance = BigDecimal.ZERO;
            try {
                BigDecimal adv = staffAdvanceRepository.getOutstandingAdvanceForStaff(staff);
                if (adv != null)
                    advance = adv;
            } catch (Exception e) {
                log.warn("Could not get advance for staff: {}", staff.getStaffId());
            }

            totalSalaryPaid = totalSalaryPaid.add(paid);
            totalBonus = totalBonus.add(bonus);
            totalDeductions = totalDeductions.add(deductions);
            totalAdvance = totalAdvance.add(advance);

            details.add(ComprehensiveReportResponse.StaffReport.StaffDetail.builder()
                    .staffId(staff.getStaffId())
                    .name(staff.getFullName())
                    .position(staff.getPosition())
                    .baseSalary(staff.getBaseSalary())
                    .totalPaid(paid)
                    .bonus(bonus)
                    .deductions(deductions)
                    .advance(advance)
                    .status(staff.getStatus().name())
                    .build());
        }

        return ComprehensiveReportResponse.StaffReport.builder()
                .totalStaff(staffList.size())
                .activeStaff((int) staffRepository.countByStatus(Staff.StaffStatus.ACTIVE))
                .totalSalaryPaid(totalSalaryPaid)
                .totalBonus(totalBonus)
                .totalDeductions(totalDeductions)
                .totalAdvance(totalAdvance)
                .details(details)
                .build();
    }

    // ==================== BILLING REPORT ====================

    private ComprehensiveReportResponse.BillingReport generateBillingReportData(
            LocalDate startDate, LocalDate endDate) {

        log.info("Generating billing report data");

        int month = startDate.getMonthValue();
        int year = startDate.getYear();

        List<Candidate> candidates = new ArrayList<>();
        try {
            candidates = candidateRepository.findByStatus(Candidate.Status.ACTIVE);
        } catch (Exception e) {
            log.warn("Could not get active candidates: {}", e.getMessage());
        }

        List<ComprehensiveReportResponse.BillingReport.BillingDetail> details = new ArrayList<>();

        BigDecimal totalCollected = BigDecimal.ZERO;
        BigDecimal totalPending = BigDecimal.ZERO;
        int paidCount = 0;

        for (Candidate candidate : candidates) {
            BigDecimal monthlyRate = candidate.getMonthlyRate();
            BigDecimal paid = feeCollectionRepository.getFeesPaidByCandidateForMonth(candidate, month, year);
            if (paid == null)
                paid = BigDecimal.ZERO;

            BigDecimal pending = monthlyRate.subtract(paid);
            if (pending.compareTo(BigDecimal.ZERO) < 0)
                pending = BigDecimal.ZERO;

            totalCollected = totalCollected.add(paid);
            totalPending = totalPending.add(pending);

            String status = "PENDING";
            if (pending.compareTo(BigDecimal.ZERO) == 0) {
                status = "PAID";
                paidCount++;
            } else if (paid.compareTo(BigDecimal.ZERO) > 0 && pending.compareTo(BigDecimal.ZERO) > 0) {
                status = "PARTIAL";
            }

            details.add(ComprehensiveReportResponse.BillingReport.BillingDetail.builder()
                    .candidateId(candidate.getCandidateId())
                    .name(candidate.getFullName())
                    .monthlyRate(monthlyRate)
                    .paid(paid)
                    .pending(pending)
                    .status(status)
                    .build());
        }

        int totalCandidates = candidates.size();
        int pendingCandidates = totalCandidates - paidCount;

        BigDecimal totalExpected = candidates.stream()
                .map(Candidate::getMonthlyRate)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal collectionPercentage = totalExpected.compareTo(BigDecimal.ZERO) > 0
                ? totalCollected.divide(totalExpected, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ComprehensiveReportResponse.BillingReport.builder()
                .totalCollected(totalCollected)
                .totalPending(totalPending)
                .paidCandidates(paidCount)
                .pendingCandidates(pendingCandidates)
                .collectionPercentage(collectionPercentage)
                .details(details)
                .build();
    }

    // ==================== PARTY REPORT ====================

    private ComprehensiveReportResponse.PartyReport generatePartyReportData(
            LocalDate startDate, LocalDate endDate) {

        log.info("Generating party report data");

        List<Party> parties = new ArrayList<>();
        try {
            parties = partyRepository.findByEventDateBetween(startDate, endDate);
        } catch (Exception e) {
            log.warn("Could not get parties: {}", e.getMessage());
        }

        Map<String, Integer> mealTypeCount = new HashMap<>();
        List<ComprehensiveReportResponse.PartyReport.PartyDetail> details = new ArrayList<>();

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal collected = BigDecimal.ZERO;
        BigDecimal pending = BigDecimal.ZERO;
        int totalPeople = 0;

        for (Party party : parties) {
            totalRevenue = totalRevenue.add(party.getTotalBill());
            collected = collected.add(party.getPaidAmount() != null ? party.getPaidAmount() : BigDecimal.ZERO);
            pending = pending.add(party.getPendingAmount() != null ? party.getPendingAmount() : BigDecimal.ZERO);
            totalPeople += party.getNumberOfPeople();

            String mealType = party.getMealType();
            mealTypeCount.put(mealType, mealTypeCount.getOrDefault(mealType, 0) + 1);

            details.add(ComprehensiveReportResponse.PartyReport.PartyDetail.builder()
                    .partyId(party.getPartyId())
                    .partyName(party.getPartyName())
                    .eventDate(party.getEventDate())
                    .people(party.getNumberOfPeople())
                    .mealType(mealType)
                    .totalBill(party.getTotalBill())
                    .paymentStatus(party.getPaymentStatus().name())
                    .paidAmount(party.getPaidAmount())
                    .pendingAmount(party.getPendingAmount())
                    .build());
        }

        return ComprehensiveReportResponse.PartyReport.builder()
                .totalParties(parties.size())
                .totalPeople(totalPeople)
                .totalRevenue(totalRevenue)
                .collected(collected)
                .pending(pending)
                .mealTypeCount(mealTypeCount)
                .details(details)
                .build();
    }

    // ==================== MENU REPORT ====================

    private ComprehensiveReportResponse.MenuReport generateMenuReportData(
            LocalDate startDate, LocalDate endDate) {

        log.info("Generating menu report data");

        List<Menu> menus = new ArrayList<>();
        try {
            menus = menuRepository.findByMenuDateBetween(startDate, endDate);
        } catch (Exception e) {
            log.warn("Could not get menus: {}", e.getMessage());
        }

        Map<String, Integer> mealTypeCount = new HashMap<>();
        Map<String, Integer> categoryWiseCount = new HashMap<>();
        List<ComprehensiveReportResponse.MenuReport.MenuDetail> details = new ArrayList<>();

        int totalItems = 0;

        for (Menu menu : menus) {
            String mealType = menu.getMealType().name();
            mealTypeCount.put(mealType, mealTypeCount.getOrDefault(mealType, 0) + 1);

            List<MenuItem> items = new ArrayList<>();
            try {
                items = menuItemRepository.findByMenuId(menu.getId());
            } catch (Exception e) {
                log.warn("Could not get items for menu: {}", menu.getId());
            }
            totalItems += items.size();

            BigDecimal totalPrice = items.stream()
                    .map(MenuItem::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            for (MenuItem item : items) {
                String category = item.getCategory() != null ? item.getCategory() : "Uncategorized";
                categoryWiseCount.put(category, categoryWiseCount.getOrDefault(category, 0) + 1);
            }

            details.add(ComprehensiveReportResponse.MenuReport.MenuDetail.builder()
                    .date(menu.getMenuDate().toString())
                    .mealType(mealType)
                    .menuName(menu.getMenuName())
                    .items(items.stream().map(MenuItem::getItemName).collect(Collectors.toList()))
                    .totalPrice(totalPrice)
                    .build());
        }

        return ComprehensiveReportResponse.MenuReport.builder()
                .totalMenus(menus.size())
                .totalItems(totalItems)
                .mealTypeCount(mealTypeCount)
                .categoryWiseCount(categoryWiseCount)
                .details(details)
                .build();
    }

    // ==================== PROFIT & LOSS REPORT ====================

    private ComprehensiveReportResponse.ProfitLossReport generateProfitLossReportData(
            LocalDate startDate, LocalDate endDate) {

        log.info("Generating profit & loss report data");

        // Income breakdown
        BigDecimal messFeesIncome = feeCollectionRepository.getCollectedFeesForMonth(
                startDate.getMonthValue(), startDate.getYear());
        if (messFeesIncome == null)
            messFeesIncome = BigDecimal.ZERO;

        BigDecimal partyIncome = partyRepository.getTotalPartyBilling(startDate, endDate);
        if (partyIncome == null)
            partyIncome = BigDecimal.ZERO;

        BigDecimal otherIncome = BigDecimal.ZERO;

        Map<String, BigDecimal> incomeBreakdown = new LinkedHashMap<>();
        incomeBreakdown.put("Mess Fees", messFeesIncome);
        incomeBreakdown.put("Party Income", partyIncome);
        incomeBreakdown.put("Other Income", otherIncome);

        // Expense breakdown
        Map<String, BigDecimal> expenseBreakdown = new LinkedHashMap<>();
        try {
            List<Object[]> categoryData = expenseRepository.getCategoryWiseExpenses(startDate, endDate);
            for (Object[] data : categoryData) {
                expenseBreakdown.put((String) data[0], (BigDecimal) data[1]);
            }
        } catch (Exception e) {
            log.warn("Could not get category wise expenses: {}", e.getMessage());
        }

        BigDecimal totalIncome = messFeesIncome.add(partyIncome).add(otherIncome);
        BigDecimal totalExpenses = expenseRepository.getTotalExpensesByDateRange(startDate, endDate);
        if (totalExpenses == null)
            totalExpenses = BigDecimal.ZERO;

        // Add staff salary expenses
        BigDecimal staffSalaryExpenses = staffPaymentRepository.getTotalPaymentsByDateRange(startDate, endDate);
        if (staffSalaryExpenses != null) {
            totalExpenses = totalExpenses.add(staffSalaryExpenses);
        }

        BigDecimal netProfit = totalIncome.subtract(totalExpenses);
        BigDecimal profitMargin = totalIncome.compareTo(BigDecimal.ZERO) > 0
                ? netProfit.divide(totalIncome, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Monthly data
        List<ComprehensiveReportResponse.ProfitLossReport.MonthlyProfitLoss> monthlyData = new ArrayList<>();
        int year = startDate.getYear();
        int endMonth = endDate.getMonthValue();

        for (int month = startDate.getMonthValue(); month <= endMonth; month++) {
            LocalDate monthStart = LocalDate.of(year, month, 1);
            LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());

            BigDecimal monthIncome = feeCollectionRepository.getCollectedFeesForMonth(month, year);
            if (monthIncome == null)
                monthIncome = BigDecimal.ZERO;

            BigDecimal monthExpense = expenseRepository.getTotalExpensesByDateRange(monthStart, monthEnd);
            if (monthExpense == null)
                monthExpense = BigDecimal.ZERO;

            BigDecimal monthProfit = monthIncome.subtract(monthExpense);
            BigDecimal monthMargin = monthIncome.compareTo(BigDecimal.ZERO) > 0
                    ? monthProfit.divide(monthIncome, 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            monthlyData.add(ComprehensiveReportResponse.ProfitLossReport.MonthlyProfitLoss.builder()
                    .month(monthStart.getMonth().name())
                    .income(monthIncome)
                    .expenses(monthExpense)
                    .profit(monthProfit)
                    .margin(monthMargin)
                    .build());
        }

        return ComprehensiveReportResponse.ProfitLossReport.builder()
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .netProfit(netProfit)
                .profitMargin(profitMargin)
                .incomeBreakdown(incomeBreakdown)
                .expenseBreakdown(expenseBreakdown)
                .monthlyData(monthlyData)
                .build();
    }

    // ==================== MODULE SPECIFIC REPORTS ====================

    @Override
    @Transactional
    public Map<String, Object> getCandidateReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("candidateReport", generateCandidateReportData(startDate, endDate));
        return report;
    }

    @Override
    @Transactional
    public Map<String, Object> getAttendanceReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("attendanceReport", generateAttendanceReportData(startDate, endDate));
        return report;
    }

    @Override
    @Transactional
    public Map<String, Object> getExpenseReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("expenseReport", generateExpenseReportData(startDate, endDate));
        return report;
    }

    @Override
    @Transactional
    public Map<String, Object> getStockReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("stockReport", generateStockReportData());
        return report;
    }

    @Override
    @Transactional
    public Map<String, Object> getStaffReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("staffReport", generateStaffReportData(startDate, endDate));
        return report;
    }

    @Override
    @Transactional
    public Map<String, Object> getBillingReport(int month, int year) {
        Map<String, Object> report = new HashMap<>();
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        report.put("billingReport", generateBillingReportData(startDate, endDate));
        return report;
    }

    @Override
    @Transactional
    public Map<String, Object> getPartyReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("partyReport", generatePartyReportData(startDate, endDate));
        return report;
    }

    @Override
    @Transactional
    public Map<String, Object> getMenuReport(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> report = new HashMap<>();
        report.put("menuReport", generateMenuReportData(startDate, endDate));
        return report;
    }

    @Override
    @Transactional
    public Map<String, Object> getProfitLossReport(int year) {
        Map<String, Object> report = new HashMap<>();
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        report.put("profitLossReport", generateProfitLossReportData(startDate, endDate));
        return report;
    }

    // ==================== EXPORT METHODS ====================

    @Override
    @Transactional
    public byte[] exportReportAsPDF(ComprehensiveReportResponse report) {
        log.info("Exporting report as PDF");
        // Implementation using iText or JasperReports
        // Placeholder - will be implemented with actual PDF generation
        return new byte[0];
    }

    @Override
    @Transactional
    public byte[] exportReportAsExcel(ComprehensiveReportResponse report) {
        log.info("Exporting report as Excel");
        // Implementation using Apache POI
        // Placeholder - will be implemented with actual Excel generation
        return new byte[0];
    }

    @Override
    @Transactional
    public String exportReportAsCSV(ComprehensiveReportResponse report) {
        log.info("Exporting report as CSV");
        // Implementation for CSV export
        // Placeholder - will be implemented with actual CSV generation
        return "";
    }
}