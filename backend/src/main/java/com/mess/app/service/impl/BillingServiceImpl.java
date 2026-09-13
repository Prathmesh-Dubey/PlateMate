package com.mess.app.service.impl;

import com.mess.app.dto.request.BulkFeeCollectionRequest;
import com.mess.app.dto.request.FeeCollectionRequest;
import com.mess.app.dto.response.*;
import com.mess.app.entity.*;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.*;
import com.mess.app.service.BillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingServiceImpl implements BillingService {

    private final FeeCollectionRepository feeCollectionRepository;
    private final MonthlyBillingRepository monthlyBillingRepository;
    private final CandidateRepository candidateRepository;
    private final StaffRepository staffRepository;
    private final ExpenseRepository expenseRepository;
    private final StaffPaymentRepository staffPaymentRepository;
    private final PartyRepository partyRepository;
    private final CollegeBillingRepository collegeBillingRepository;

    /** Year range used for the ALL-time scope. */
    private static final int ALL_TIME_FROM_YEAR = 2000;
    private static final int ALL_TIME_TO_YEAR = 2100;

    /**
     * Documentation of the financial model used by the Billing dashboard and the
     * monthly Profit &amp; Loss chart. This is returned to the client as "basis".
     */
    private static final String PL_BASIS =
            "Revenue = mess fees collected (fee_collections, isPaid, attributed to the fee month) "
            + "+ party bills (parties.totalBill by event date) "
            + "+ college billable amount (college_billing by billing month). "
            + "Expenses = expenses table (all categories, by expense date) "
            + "+ staff salary paid (staff_payments.amount by salary month). "
            + "Stock purchase transactions and staff advances are intentionally excluded to avoid double counting.";

    // ==================== FEE COLLECTION ====================

    @Override
    @Transactional
    public FeeCollectionResponse collectFee(FeeCollectionRequest request) {
        log.info("Collecting fee for candidate: {}", request.getCandidateId());

        Candidate candidate = candidateRepository.findById(request.getCandidateId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found with ID: " + request.getCandidateId()));

        Candidate collectedBy = null;
        if (request.getCollectedByCandidateId() != null) {
            collectedBy = candidateRepository.findById(request.getCollectedByCandidateId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Candidate not found with ID: " + request.getCollectedByCandidateId()));
        }

        Optional<FeeCollection> existing = feeCollectionRepository
                .findByCandidateAndPaymentMonthAndPaymentYear(
                        candidate, request.getPaymentMonth(), request.getPaymentYear());

        if (existing.isPresent()) {
            throw new RuntimeException("Fee already collected for this month");
        }

        FeeCollection fee = FeeCollection.builder()
                .candidate(candidate)
                .collectionDate(request.getCollectionDate() != null ? request.getCollectionDate() : LocalDate.now())
                .amount(request.getAmount())
                .paymentMonth(request.getPaymentMonth())
                .paymentYear(request.getPaymentYear())
                .paymentMonthNumber(request.getPaymentMonthNumber())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .receiptNumber(generateReceiptNumber())
                .isPaid(true)
                .remarks(request.getRemarks())
                .collectedBy(collectedBy)
                .build();

        FeeCollection saved = feeCollectionRepository.save(fee);
        log.info("Fee collected successfully with receipt: {}", saved.getReceiptNumber());

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public FeeCollectionResponse getFeeCollectionById(String id) {
        FeeCollection fee = feeCollectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fee collection not found with ID: " + id));
        return convertToResponse(fee);
    }

    @Override
    @Transactional
    public List<FeeCollectionResponse> getFeesByCandidate(String candidateId) {
        Candidate candidate = candidateRepository.findById(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Candidate not found with ID: " + candidateId));

        return feeCollectionRepository.findByCandidateOrderByCollectionDateDesc(candidate)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<FeeCollectionResponse> getFeesByMonth(int month, int year) {
        return feeCollectionRepository.findByPaymentMonthNumberAndPaymentYear(month, year)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FeeSummaryResponse getFeeSummary(int month, int year) {
        log.info("Generating fee summary for {}/{}", month, year);

        List<Candidate> activeCandidates = candidateRepository.findByStatus(Candidate.Status.ACTIVE);
        int totalCandidates = activeCandidates.size();

        BigDecimal totalExpectedFees = activeCandidates.stream()
                .map(Candidate::getMonthlyRate)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollectedFees = nz(feeCollectionRepository.getCollectedFeesForMonth(month, year));

        long paidCandidatesCount = feeCollectionRepository.getPaidCandidatesCountForMonth(month, year);
        int paidCandidates = (int) paidCandidatesCount;
        int pendingCandidates = totalCandidates - paidCandidates;

        BigDecimal pendingAmount = totalExpectedFees.subtract(totalCollectedFees);

        BigDecimal collectionPercentage = percentage(totalCollectedFees, totalExpectedFees);

        List<FeeSummaryResponse.CandidateFeeDetail> candidateFees = new ArrayList<>();

        for (Candidate candidate : activeCandidates) {
            Optional<FeeCollection> feeOpt = feeCollectionRepository
                    .findByCandidateAndPaymentMonthAndPaymentYear(
                            candidate,
                            LocalDate.of(year, month, 1).getMonth().name().toUpperCase() + "_" + year,
                            year);

            boolean isPaid = feeOpt.isPresent() && feeOpt.get().isPaid();
            BigDecimal paidAmount = isPaid ? feeOpt.get().getAmount() : BigDecimal.ZERO;
            BigDecimal pending = nz(candidate.getMonthlyRate()).subtract(paidAmount);

            candidateFees.add(FeeSummaryResponse.CandidateFeeDetail.builder()
                    .candidateId(candidate.getCandidateId())
                    .candidateName(candidate.getFullName())
                    .monthlyRate(candidate.getMonthlyRate())
                    .paidAmount(paidAmount)
                    .pendingAmount(pending.compareTo(BigDecimal.ZERO) > 0 ? pending : BigDecimal.ZERO)
                    .isPaid(isPaid)
                    .paymentDate(isPaid ? feeOpt.get().getCollectionDate().toString() : null)
                    .build());
        }

        return FeeSummaryResponse.builder()
                .month(month)
                .year(year)
                .monthName(LocalDate.of(year, month, 1).getMonth().name())
                .totalCandidates(totalCandidates)
                .paidCandidates(paidCandidates)
                .pendingCandidates(pendingCandidates)
                .totalExpectedFees(totalExpectedFees)
                .totalCollectedFees(totalCollectedFees)
                .pendingAmount(pendingAmount)
                .collectionPercentage(collectionPercentage)
                .candidateFees(candidateFees)
                .build();
    }

    @Override
    @Transactional
    public void bulkCollectFees(BulkFeeCollectionRequest request) {
        log.info("Bulk collecting fees for month: {}/{}",
                request.getPaymentMonthNumber(), request.getPaymentYear());

        Candidate collectedBy = null;
        if (request.getCollectedByCandidateId() != null) {
            collectedBy = candidateRepository.findById(request.getCollectedByCandidateId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Candidate not found with ID: " + request.getCollectedByCandidateId()));
        }

        int successCount = 0;
        int failCount = 0;

        for (BulkFeeCollectionRequest.CandidateFee candidateFee : request.getCandidateFees()) {
            try {
                Candidate candidate = candidateRepository.findById(candidateFee.getCandidateId())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Candidate not found with ID: " + candidateFee.getCandidateId()));

                Optional<FeeCollection> existing = feeCollectionRepository
                        .findByCandidateAndPaymentMonthAndPaymentYear(
                                candidate, request.getPaymentMonth(), request.getPaymentYear());

                if (existing.isPresent()) {
                    log.warn("Fee already collected for candidate: {}", candidate.getCandidateId());
                    failCount++;
                    continue;
                }

                FeeCollection fee = FeeCollection.builder()
                        .candidate(candidate)
                        .collectionDate(
                                request.getCollectionDate() != null ? request.getCollectionDate() : LocalDate.now())
                        .amount(candidateFee.getAmount())
                        .paymentMonth(request.getPaymentMonth())
                        .paymentYear(request.getPaymentYear())
                        .paymentMonthNumber(request.getPaymentMonthNumber())
                        .paymentMethod(request.getPaymentMethod())
                        .receiptNumber(generateReceiptNumber())
                        .isPaid(true)
                        .remarks(candidateFee.getRemarks())
                        .collectedBy(collectedBy)
                        .build();

                feeCollectionRepository.save(fee);
                successCount++;
            } catch (Exception e) {
                log.error("Failed to collect fee for candidate: {}", candidateFee.getCandidateId(), e);
                failCount++;
            }
        }

        log.info("Bulk fee collection completed. Success: {}, Failed: {}", successCount, failCount);
    }

    @Override
    @Transactional
    public void deleteFeeCollection(String id) {
        log.info("Deleting fee collection with ID: {}", id);
        FeeCollection fee = feeCollectionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fee collection not found with ID: " + id));
        feeCollectionRepository.delete(fee);
        log.info("Fee collection deleted successfully");
    }

    // ==================== MONTHLY BILLING SNAPSHOTS ====================

    @Override
    @Transactional
    public MonthlyBillingResponse generateMonthlyBilling(int month, int year) {
        log.info("Generating monthly billing for {}/{}", month, year);

        Optional<MonthlyBilling> existing = monthlyBillingRepository.findByMonthAndYear(month, year);
        if (existing.isPresent()) {
            throw new RuntimeException("Billing already generated for this month");
        }

        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // Same live figures as the Billing dashboard, frozen into the snapshot table.
        MonthAggregate agg = loadAggregates(year, year).getOrDefault(YearMonth.of(year, month), new MonthAggregate());

        BigDecimal messFeesIncome = agg.messFees;
        BigDecimal partyIncome = agg.partyBilled;          // previously hard-coded to zero
        BigDecimal otherIncome = agg.collegeBilled;        // college billing receivable
        BigDecimal totalIncome = messFeesIncome.add(partyIncome).add(otherIncome);

        BigDecimal stockExpenses = agg.expenses;           // expenses table (all categories)
        BigDecimal staffSalaryExpenses = agg.staffSalaryPaid;
        BigDecimal otherExpenses = BigDecimal.ZERO;
        BigDecimal totalExpenses = stockExpenses.add(staffSalaryExpenses).add(otherExpenses);

        BigDecimal netProfitLoss = totalIncome.subtract(totalExpenses);
        BigDecimal profit = netProfitLoss.compareTo(BigDecimal.ZERO) > 0 ? netProfitLoss : BigDecimal.ZERO;
        BigDecimal loss = netProfitLoss.compareTo(BigDecimal.ZERO) < 0 ? netProfitLoss.abs() : BigDecimal.ZERO;
        BigDecimal profitMarginPercentage = percentage(netProfitLoss, totalIncome);

        int totalCandidates = (int) candidateRepository.countByStatus(Candidate.Status.ACTIVE);
        int totalStaff = (int) staffRepository.countByStatus(Staff.StaffStatus.ACTIVE);
        int totalParties = (int) agg.partyCount;

        MonthlyBilling billing = MonthlyBilling.builder()
                .month(month)
                .year(year)
                .monthName(LocalDate.of(year, month, 1).getMonth().name())
                .monthStartDate(startDate)
                .monthEndDate(endDate)
                .messFeesIncome(messFeesIncome)
                .partyIncome(partyIncome)
                .otherIncome(otherIncome)
                .totalIncome(totalIncome)
                .stockExpenses(stockExpenses)
                .staffSalaryExpenses(staffSalaryExpenses)
                .otherExpenses(otherExpenses)
                .totalExpenses(totalExpenses)
                .profit(profit)
                .loss(loss)
                .netProfitLoss(netProfitLoss)
                .profitMarginPercentage(profitMarginPercentage)
                .totalCandidates(totalCandidates)
                .totalStaff(totalStaff)
                .totalParties(totalParties)
                .isGenerated(true)
                .generatedDate(LocalDate.now())
                .build();

        MonthlyBilling saved = monthlyBillingRepository.save(billing);
        log.info("Monthly billing generated for {}/{}", month, year);

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public MonthlyBillingResponse getMonthlyBilling(int month, int year) {
        MonthlyBilling billing = monthlyBillingRepository.findByMonthAndYear(month, year)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Billing not found for %d/%d", month, year)));
        return convertToResponse(billing);
    }

    @Override
    @Transactional
    public List<MonthlyBillingResponse> getYearlyBilling(int year) {
        return monthlyBillingRepository.findByYearOrderByMonthAsc(year)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public YearlyFinancialReport getYearlyFinancialReport(int year) {
        log.info("Generating yearly financial report for {}", year);

        List<MonthlyBilling> billings = monthlyBillingRepository.findByYearOrderByMonthAsc(year);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;
        int profitableMonths = 0;
        int lossMonths = 0;

        Map<String, BigDecimal> incomeBreakdown = new HashMap<>();
        Map<String, BigDecimal> expenseBreakdown = new HashMap<>();

        for (MonthlyBilling billing : billings) {
            totalIncome = totalIncome.add(billing.getTotalIncome());
            totalExpenses = totalExpenses.add(billing.getTotalExpenses());

            if (billing.getProfit().compareTo(BigDecimal.ZERO) > 0) {
                totalProfit = totalProfit.add(billing.getProfit());
                profitableMonths++;
            }
            if (billing.getLoss().compareTo(BigDecimal.ZERO) > 0) {
                totalLoss = totalLoss.add(billing.getLoss());
                lossMonths++;
            }

            incomeBreakdown.merge("Mess Fees", billing.getMessFeesIncome(), BigDecimal::add);
            incomeBreakdown.merge("Party Income", billing.getPartyIncome(), BigDecimal::add);
            incomeBreakdown.merge("Other Income", billing.getOtherIncome(), BigDecimal::add);

            expenseBreakdown.merge("Stock Expenses", billing.getStockExpenses(), BigDecimal::add);
            expenseBreakdown.merge("Staff Salary", billing.getStaffSalaryExpenses(), BigDecimal::add);
            expenseBreakdown.merge("Other Expenses", billing.getOtherExpenses(), BigDecimal::add);
        }

        BigDecimal netProfitLoss = totalIncome.subtract(totalExpenses);
        BigDecimal averageMonthlyProfit = billings.isEmpty() ? BigDecimal.ZERO
                : netProfitLoss.divide(BigDecimal.valueOf(billings.size()), 2, RoundingMode.HALF_UP);

        List<MonthlyBillingResponse> monthlyData = billings.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return YearlyFinancialReport.builder()
                .year(year)
                .totalIncome(totalIncome)
                .totalExpenses(totalExpenses)
                .totalProfit(totalProfit)
                .totalLoss(totalLoss)
                .netProfitLoss(netProfitLoss)
                .averageMonthlyProfit(averageMonthlyProfit)
                .profitableMonths(profitableMonths)
                .lossMonths(lossMonths)
                .monthlyData(monthlyData)
                .incomeBreakdown(incomeBreakdown)
                .expenseBreakdown(expenseBreakdown)
                .build();
    }

    // ==================== LIVE BILLING DASHBOARD ====================

    /**
     * Calculation of every dashboard box (all figures are live database aggregates):
     *
     * <pre>
     *  1. Billable Amount        = SUM(college_billing.total_billable_amount) for the period
     *                               (revenue billed to colleges; received / outstanding shown alongside)
     *  2. Total Staff Salary     = SUM(staff.base_salary) WHERE status = ACTIVE  (monthly payroll commitment)
     *                               + SUM(staff_payments.amount) for the period  (actually disbursed)
     *  3. Total Expenses         = SUM(expenses.total_amount) for the period, all categories
     *                               (VEGETABLES, GROCERY, MILK_DAIRY, GAS_CYLINDER, UTILITIES, MAINTENANCE, MISCELLANEOUS).
     *                               Both the "Day-to-day Expenses" page and the "Total Expenses" page write to this table.
     *  4. Today's Expense        = SUM(expenses.total_amount) WHERE expense_date = today (server date)
     *  5. Combined Expenses      = Total Expenses (3) + staff salary paid in the period.
     *                               Expenses are counted once; stock purchase transactions and staff advances
     *                               are NOT added (they would duplicate expense rows / are loans, not costs).
     *  6. Mess Fees Revenue      = SUM(fee_collections.amount) WHERE is_paid = true, by fee month
     *  7. Outstanding Amount     = college outstanding (billed - received) + party pending (bill - received)
     *                               with collection rate = received / billed of those receivables
     *  8. Party / Catering       = SUM(parties.total_bill) by event date, with received / pending
     * </pre>
     */
    @Override
    @Transactional(readOnly = true)
    public BillingSummaryResponse getBillingSummary(String scopeParam, Integer monthParam, Integer yearParam) {
        LocalDate today = LocalDate.now();
        String scope = scopeParam == null ? "MONTH" : scopeParam.trim().toUpperCase();
        if (!scope.equals("MONTH") && !scope.equals("YEAR") && !scope.equals("ALL")) {
            throw new IllegalArgumentException("scope must be one of MONTH, YEAR, ALL");
        }

        int year = yearParam != null ? yearParam : today.getYear();
        int month = monthParam != null ? monthParam : today.getMonthValue();
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("month must be between 1 and 12");
        }
        if (year < ALL_TIME_FROM_YEAR || year > ALL_TIME_TO_YEAR) {
            throw new IllegalArgumentException("year must be between 2000 and 2100");
        }

        int fromYear;
        int toYear;
        LocalDate periodStart;
        LocalDate periodEnd;
        String periodLabel;
        switch (scope) {
            case "YEAR" -> {
                fromYear = year;
                toYear = year;
                periodStart = LocalDate.of(year, 1, 1);
                periodEnd = LocalDate.of(year, 12, 31);
                periodLabel = "Year " + year;
            }
            case "ALL" -> {
                fromYear = ALL_TIME_FROM_YEAR;
                toYear = ALL_TIME_TO_YEAR;
                periodStart = LocalDate.of(ALL_TIME_FROM_YEAR, 1, 1);
                periodEnd = LocalDate.of(ALL_TIME_TO_YEAR, 12, 31);
                periodLabel = "All time";
            }
            default -> {
                fromYear = year;
                toYear = year;
                periodStart = LocalDate.of(year, month, 1);
                periodEnd = periodStart.withDayOfMonth(periodStart.lengthOfMonth());
                periodLabel = monthName(month) + " " + year;
            }
        }

        Map<YearMonth, MonthAggregate> aggregates = loadAggregates(fromYear, toYear);
        MonthAggregate period = new MonthAggregate();
        for (Map.Entry<YearMonth, MonthAggregate> entry : aggregates.entrySet()) {
            YearMonth ym = entry.getKey();
            boolean inScope = switch (scope) {
                case "YEAR" -> ym.getYear() == year;
                case "ALL" -> true;
                default -> ym.getYear() == year && ym.getMonthValue() == month;
            };
            if (inScope) {
                period.add(entry.getValue());
            }
        }

        // Expense category breakdown for the period (LinkedHashMap keeps the DESC order of the query)
        Map<String, BigDecimal> categoryBreakdown = new LinkedHashMap<>();
        for (Object[] row : expenseRepository.getCategoryWiseExpenses(periodStart, periodEnd)) {
            String category = row[0] != null ? row[0].toString() : "UNCATEGORIZED";
            categoryBreakdown.merge(category, money(row[1]), BigDecimal::add);
        }

        // Staff payroll commitment (current roster)
        BigDecimal monthlyPayroll = money(staffRepository.getTotalActiveBaseSalary());
        long activeStaff = staffRepository.countByStatus(Staff.StaffStatus.ACTIVE);

        // Today's day-to-day expense (always the current server date, independent of the scope)
        BigDecimal todayAmount = money(expenseRepository.getTotalExpensesByDateRange(today, today));
        long todayCount = expenseRepository.countByExpenseDate(today);

        // Mess fees expected / pending only make sense for a single month
        BigDecimal expectedMonthly = null;
        BigDecimal feesPending = null;
        BigDecimal feeCollectionRate = null;
        if (scope.equals("MONTH")) {
            expectedMonthly = money(candidateRepository.getTotalActiveMonthlyRate());
            feesPending = expectedMonthly.subtract(period.messFees).max(BigDecimal.ZERO);
            feeCollectionRate = percentage(period.messFees, expectedMonthly);
        }

        // Receivables outstanding
        BigDecimal collegeOutstanding = period.collegeBilled.subtract(period.collegeReceived).max(BigDecimal.ZERO);
        BigDecimal partyPending = period.partyBilled.subtract(period.partyReceived).max(BigDecimal.ZERO);
        BigDecimal billedReceivables = period.collegeBilled.add(period.partyBilled);
        BigDecimal receivedReceivables = period.collegeReceived.add(period.partyReceived);

        // Profit & loss for the period (same formula as the monthly chart)
        BigDecimal totalRevenue = period.messFees.add(period.partyBilled).add(period.collegeBilled);
        BigDecimal totalExpenses = period.expenses.add(period.staffSalaryPaid);
        BigDecimal combinedExpenses = totalExpenses;

        return BillingSummaryResponse.builder()
                .scope(scope)
                .month(scope.equals("MONTH") ? month : null)
                .year(scope.equals("ALL") ? null : year)
                .periodLabel(periodLabel)
                .periodStart(periodStart)
                .periodEnd(periodEnd)
                .generatedAt(LocalDateTime.now())
                .collegeBilling(BillingSummaryResponse.CollegeBillingBox.builder()
                        .billableAmount(scale(period.collegeBilled))
                        .amountReceived(scale(period.collegeReceived))
                        .outstandingBalance(scale(collegeOutstanding))
                        .recordCount(period.collegeCount)
                        .build())
                .staffSalary(BillingSummaryResponse.StaffSalaryBox.builder()
                        .monthlyPayroll(scale(monthlyPayroll))
                        .activeStaffCount(activeStaff)
                        .paidInPeriod(scale(period.staffSalaryPaid))
                        .paymentCount(period.staffPaymentCount)
                        .build())
                .expenses(BillingSummaryResponse.ExpenseBox.builder()
                        .total(scale(period.expenses))
                        .count(period.expenseCount)
                        .categoryBreakdown(categoryBreakdown)
                        .build())
                .todayExpense(BillingSummaryResponse.TodayExpenseBox.builder()
                        .date(today)
                        .amount(scale(todayAmount))
                        .count(todayCount)
                        .build())
                .combinedExpenses(BillingSummaryResponse.CombinedExpenseBox.builder()
                        .total(scale(combinedExpenses))
                        .expenses(scale(period.expenses))
                        .staffSalaryPaid(scale(period.staffSalaryPaid))
                        .formula("Expenses (all categories, counted once) + Staff salary paid")
                        .build())
                .messFees(BillingSummaryResponse.MessFeesBox.builder()
                        .collected(scale(period.messFees))
                        .collectionCount(period.messFeeCount)
                        .expectedMonthly(expectedMonthly != null ? scale(expectedMonthly) : null)
                        .pending(feesPending != null ? scale(feesPending) : null)
                        .collectionRate(feeCollectionRate)
                        .build())
                .outstanding(BillingSummaryResponse.OutstandingBox.builder()
                        .total(scale(collegeOutstanding.add(partyPending)))
                        .collegeOutstanding(scale(collegeOutstanding))
                        .partyPending(scale(partyPending))
                        .collectionRate(billedReceivables.compareTo(BigDecimal.ZERO) > 0
                                ? percentage(receivedReceivables, billedReceivables) : null)
                        .totalBilledReceivables(scale(billedReceivables))
                        .totalReceivedReceivables(scale(receivedReceivables))
                        .build())
                .partyIncome(BillingSummaryResponse.PartyIncomeBox.builder()
                        .totalBilled(scale(period.partyBilled))
                        .received(scale(period.partyReceived))
                        .pending(scale(partyPending))
                        .partyCount(period.partyCount)
                        .build())
                .profitLoss(BillingSummaryResponse.ProfitLossBox.builder()
                        .totalRevenue(scale(totalRevenue))
                        .totalExpenses(scale(totalExpenses))
                        .netProfitLoss(scale(totalRevenue.subtract(totalExpenses)))
                        .messFeesRevenue(scale(period.messFees))
                        .partyIncome(scale(period.partyBilled))
                        .collegeBillingIncome(scale(period.collegeBilled))
                        .expenses(scale(period.expenses))
                        .staffSalary(scale(period.staffSalaryPaid))
                        .build())
                .build();
    }

    /**
     * Monthly Profit &amp; Loss computed live for every month of the year:
     * revenue = mess fees + party income + college billing, expenses = expenses table + staff salary paid.
     */
    @Override
    @Transactional(readOnly = true)
    public MonthlyProfitLossResponse getMonthlyProfitLoss(int year) {
        if (year < ALL_TIME_FROM_YEAR || year > ALL_TIME_TO_YEAR) {
            throw new IllegalArgumentException("year must be between 2000 and 2100");
        }

        Map<YearMonth, MonthAggregate> aggregates = loadAggregates(year, year);
        List<MonthlyProfitLossResponse.MonthEntry> months = new ArrayList<>();
        MonthAggregate yearTotal = new MonthAggregate();
        int profitableMonths = 0;
        int lossMonths = 0;
        int monthsWithData = 0;

        for (int m = 1; m <= 12; m++) {
            MonthAggregate agg = aggregates.getOrDefault(YearMonth.of(year, m), new MonthAggregate());
            yearTotal.add(agg);

            BigDecimal income = agg.messFees.add(agg.partyBilled).add(agg.collegeBilled);
            BigDecimal expenses = agg.expenses.add(agg.staffSalaryPaid);
            BigDecimal profit = income.subtract(expenses);
            boolean hasData = agg.hasData();
            if (hasData) {
                monthsWithData++;
                if (profit.compareTo(BigDecimal.ZERO) > 0) profitableMonths++;
                else if (profit.compareTo(BigDecimal.ZERO) < 0) lossMonths++;
            }

            months.add(MonthlyProfitLossResponse.MonthEntry.builder()
                    .month(m)
                    .monthName(monthName(m))
                    .shortName(Month.of(m).getDisplayName(TextStyle.SHORT, Locale.ENGLISH))
                    .messFees(scale(agg.messFees))
                    .partyIncome(scale(agg.partyBilled))
                    .collegeBillingIncome(scale(agg.collegeBilled))
                    .income(scale(income))
                    .dayToDayExpenses(scale(agg.expenses))
                    .staffSalary(scale(agg.staffSalaryPaid))
                    .expenses(scale(expenses))
                    .profit(scale(profit))
                    .profitMargin(percentage(profit, income))
                    .hasData(hasData)
                    .build());
        }

        BigDecimal totalIncome = yearTotal.messFees.add(yearTotal.partyBilled).add(yearTotal.collegeBilled);
        BigDecimal totalExpenses = yearTotal.expenses.add(yearTotal.staffSalaryPaid);

        return MonthlyProfitLossResponse.builder()
                .year(year)
                .basis(PL_BASIS)
                .monthlyData(months)
                .totals(MonthlyProfitLossResponse.Totals.builder()
                        .messFees(scale(yearTotal.messFees))
                        .partyIncome(scale(yearTotal.partyBilled))
                        .collegeBillingIncome(scale(yearTotal.collegeBilled))
                        .income(scale(totalIncome))
                        .dayToDayExpenses(scale(yearTotal.expenses))
                        .staffSalary(scale(yearTotal.staffSalaryPaid))
                        .expenses(scale(totalExpenses))
                        .profit(scale(totalIncome.subtract(totalExpenses)))
                        .profitableMonths(profitableMonths)
                        .lossMonths(lossMonths)
                        .monthsWithData(monthsWithData)
                        .build())
                .build();
    }

    // ==================== LEGACY DASHBOARD & REPORTS ====================

    @Override
    @Transactional
    public Map<String, Object> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();

        LocalDate today = LocalDate.now();
        int currentMonth = today.getMonthValue();
        int currentYear = today.getYear();

        BigDecimal currentMonthIncome = nz(feeCollectionRepository.getCollectedFeesForMonth(currentMonth, currentYear));

        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());
        BigDecimal currentMonthExpenses = nz(expenseRepository.getTotalExpensesByDateRange(monthStart, monthEnd));

        BigDecimal currentMonthProfit = currentMonthIncome.subtract(currentMonthExpenses);

        long activeCandidates = candidateRepository.countByStatus(Candidate.Status.ACTIVE);
        long activeStaff = staffRepository.countByStatus(Staff.StaffStatus.ACTIVE);

        FeeSummaryResponse feeSummary = getFeeSummary(currentMonth, currentYear);
        BigDecimal pendingFees = feeSummary.getPendingAmount();

        BigDecimal yearIncome = nz(feeCollectionRepository.getTotalFeesForYear(currentYear));

        BigDecimal yearExpenses = nz(expenseRepository.getTotalExpensesByDateRange(
                LocalDate.of(currentYear, 1, 1), today));

        dashboard.put("currentMonthIncome", currentMonthIncome);
        dashboard.put("currentMonthExpenses", currentMonthExpenses);
        dashboard.put("currentMonthProfit", currentMonthProfit);
        dashboard.put("yearIncome", yearIncome);
        dashboard.put("yearExpenses", yearExpenses);
        dashboard.put("yearProfit", yearIncome.subtract(yearExpenses));
        dashboard.put("activeCandidates", activeCandidates);
        dashboard.put("activeStaff", activeStaff);
        dashboard.put("pendingFees", pendingFees);
        dashboard.put("pendingCandidates", feeSummary.getPendingCandidates());
        dashboard.put("collectionPercentage", feeSummary.getCollectionPercentage());
        dashboard.put("month", currentMonth);
        dashboard.put("year", currentYear);

        return dashboard;
    }

    @Override
    @Transactional
    public Map<String, Object> getIncomeExpenseComparison(int year) {
        Map<String, Object> comparison = new HashMap<>();

        List<MonthlyBilling> billings = monthlyBillingRepository.findByYearOrderByMonthAsc(year);

        List<BigDecimal> incomes = billings.stream()
                .map(MonthlyBilling::getTotalIncome)
                .collect(Collectors.toList());

        List<BigDecimal> expenses = billings.stream()
                .map(MonthlyBilling::getTotalExpenses)
                .collect(Collectors.toList());

        List<BigDecimal> profits = billings.stream()
                .map(MonthlyBilling::getNetProfitLoss)
                .collect(Collectors.toList());

        comparison.put("year", year);
        comparison.put("incomes", incomes);
        comparison.put("expenses", expenses);
        comparison.put("profits", profits);
        comparison.put("months", billings.stream()
                .map(b -> LocalDate.of(year, b.getMonth(), 1).getMonth().name())
                .collect(Collectors.toList()));

        return comparison;
    }

    @Override
    @Transactional
    public BigDecimal getCurrentMonthProfit() {
        LocalDate today = LocalDate.now();
        int month = today.getMonthValue();
        int year = today.getYear();

        Optional<MonthlyBilling> billingOpt = monthlyBillingRepository.findByMonthAndYear(month, year);
        return billingOpt.map(MonthlyBilling::getNetProfitLoss).orElse(BigDecimal.ZERO);
    }

    @Override
    @Transactional
    public BigDecimal getCurrentYearProfit() {
        int year = LocalDate.now().getYear();
        List<MonthlyBilling> billings = monthlyBillingRepository.findByYearOrderByMonthAsc(year);

        return billings.stream()
                .map(MonthlyBilling::getNetProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional
    public void generateMonthlyBillingForAll() {
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();

        for (int month = 1; month <= currentMonth; month++) {
            try {
                Optional<MonthlyBilling> existing = monthlyBillingRepository.findByMonthAndYear(month, currentYear);
                if (existing.isEmpty()) {
                    generateMonthlyBilling(month, currentYear);
                }
            } catch (Exception e) {
                log.error("Failed to generate billing for {}/{}", month, currentYear, e);
            }
        }
    }

    // ==================== AGGREGATION HELPERS ====================

    /** Per-month totals of every financial table involved in the Billing dashboard. */
    private static class MonthAggregate {
        BigDecimal messFees = BigDecimal.ZERO;
        long messFeeCount = 0;
        BigDecimal partyBilled = BigDecimal.ZERO;
        BigDecimal partyReceived = BigDecimal.ZERO;
        long partyCount = 0;
        BigDecimal collegeBilled = BigDecimal.ZERO;
        BigDecimal collegeReceived = BigDecimal.ZERO;
        BigDecimal collegeOutstanding = BigDecimal.ZERO;
        long collegeCount = 0;
        BigDecimal expenses = BigDecimal.ZERO;
        long expenseCount = 0;
        BigDecimal staffSalaryPaid = BigDecimal.ZERO;
        long staffPaymentCount = 0;

        void add(MonthAggregate other) {
            messFees = messFees.add(other.messFees);
            messFeeCount += other.messFeeCount;
            partyBilled = partyBilled.add(other.partyBilled);
            partyReceived = partyReceived.add(other.partyReceived);
            partyCount += other.partyCount;
            collegeBilled = collegeBilled.add(other.collegeBilled);
            collegeReceived = collegeReceived.add(other.collegeReceived);
            collegeOutstanding = collegeOutstanding.add(other.collegeOutstanding);
            collegeCount += other.collegeCount;
            expenses = expenses.add(other.expenses);
            expenseCount += other.expenseCount;
            staffSalaryPaid = staffSalaryPaid.add(other.staffSalaryPaid);
            staffPaymentCount += other.staffPaymentCount;
        }

        boolean hasData() {
            return messFeeCount > 0 || partyCount > 0 || collegeCount > 0 || expenseCount > 0 || staffPaymentCount > 0;
        }
    }

    /**
     * Loads one aggregate per (year, month) for the given year range using one
     * GROUP BY query per table. Months without any records are simply absent.
     */
    private Map<YearMonth, MonthAggregate> loadAggregates(int fromYear, int toYear) {
        Map<YearMonth, MonthAggregate> map = new HashMap<>();
        LocalDate start = LocalDate.of(fromYear, 1, 1);
        LocalDate end = LocalDate.of(toYear, 12, 31);

        for (Object[] row : feeCollectionRepository.sumCollectedFeesByYearMonth(fromYear, toYear)) {
            MonthAggregate agg = aggregateFor(map, row[0], row[1]);
            if (agg == null) continue;
            agg.messFees = agg.messFees.add(money(row[2]));
            agg.messFeeCount += count(row[3]);
        }

        for (Object[] row : staffPaymentRepository.sumPaymentsByYearMonth(fromYear, toYear)) {
            MonthAggregate agg = aggregateFor(map, row[0], row[1]);
            if (agg == null) continue;
            agg.staffSalaryPaid = agg.staffSalaryPaid.add(money(row[2]));
            agg.staffPaymentCount += count(row[3]);
        }

        for (Object[] row : expenseRepository.sumExpensesByYearMonth(start, end)) {
            MonthAggregate agg = aggregateFor(map, row[0], row[1]);
            if (agg == null) continue;
            agg.expenses = agg.expenses.add(money(row[2]));
            agg.expenseCount += count(row[3]);
        }

        for (Object[] row : partyRepository.sumBillingByYearMonth(start, end)) {
            MonthAggregate agg = aggregateFor(map, row[0], row[1]);
            if (agg == null) continue;
            agg.partyBilled = agg.partyBilled.add(money(row[2]));
            agg.partyReceived = agg.partyReceived.add(money(row[3]));
            agg.partyCount += count(row[4]);
        }

        for (Object[] row : collegeBillingRepository.sumByYearMonth(fromYear, toYear)) {
            MonthAggregate agg = aggregateFor(map, row[0], row[1]);
            if (agg == null) continue;
            agg.collegeBilled = agg.collegeBilled.add(money(row[2]));
            agg.collegeReceived = agg.collegeReceived.add(money(row[3]));
            agg.collegeOutstanding = agg.collegeOutstanding.add(money(row[4]));
            agg.collegeCount += count(row[5]);
        }

        return map;
    }

    private static MonthAggregate aggregateFor(Map<YearMonth, MonthAggregate> map, Object yearObj, Object monthObj) {
        if (yearObj == null || monthObj == null) return null;
        int y = ((Number) yearObj).intValue();
        int m = ((Number) monthObj).intValue();
        if (m < 1 || m > 12) return null;
        return map.computeIfAbsent(YearMonth.of(y, m), k -> new MonthAggregate());
    }

    private static BigDecimal money(Object value) {
        if (value == null) return BigDecimal.ZERO;
        if (value instanceof BigDecimal bd) return bd;
        if (value instanceof Number n) return BigDecimal.valueOf(n.doubleValue());
        return new BigDecimal(value.toString());
    }

    private static long count(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }

    private static BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private static BigDecimal scale(BigDecimal value) {
        return nz(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal percentage(BigDecimal part, BigDecimal whole) {
        if (whole == null || whole.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return nz(part).divide(whole, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static String monthName(int month) {
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
    }

    private String generateReceiptNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "RCP-" + date + "-" + random;
    }

    private FeeCollectionResponse convertToResponse(FeeCollection fee) {
        return FeeCollectionResponse.builder()
                .id(fee.getId())
                .candidateId(fee.getCandidate().getCandidateId())
                .candidateName(fee.getCandidate().getFullName())
                .collectionDate(fee.getCollectionDate())
                .amount(fee.getAmount())
                .paymentMonth(fee.getPaymentMonth())
                .paymentYear(fee.getPaymentYear())
                .paymentMonthNumber(fee.getPaymentMonthNumber())
                .paymentMethod(fee.getPaymentMethod())
                .transactionId(fee.getTransactionId())
                .receiptNumber(fee.getReceiptNumber())
                .isPaid(fee.isPaid())
                .remarks(fee.getRemarks())
                .collectedBy(fee.getCollectedBy() != null ? fee.getCollectedBy().getFullName() : null)
                .createdAt(fee.getCreatedAt())
                .build();
    }

    private MonthlyBillingResponse convertToResponse(MonthlyBilling billing) {
        return MonthlyBillingResponse.builder()
                .id(billing.getId())
                .month(billing.getMonth())
                .year(billing.getYear())
                .monthName(billing.getMonthName())
                .monthStartDate(billing.getMonthStartDate())
                .monthEndDate(billing.getMonthEndDate())
                .messFeesIncome(billing.getMessFeesIncome())
                .partyIncome(billing.getPartyIncome())
                .otherIncome(billing.getOtherIncome())
                .totalIncome(billing.getTotalIncome())
                .stockExpenses(billing.getStockExpenses())
                .staffSalaryExpenses(billing.getStaffSalaryExpenses())
                .otherExpenses(billing.getOtherExpenses())
                .totalExpenses(billing.getTotalExpenses())
                .profit(billing.getProfit())
                .loss(billing.getLoss())
                .netProfitLoss(billing.getNetProfitLoss())
                .profitMarginPercentage(billing.getProfitMarginPercentage())
                .totalCandidates(billing.getTotalCandidates())
                .totalStaff(billing.getTotalStaff())
                .totalParties(billing.getTotalParties())
                .remarks(billing.getRemarks())
                .isGenerated(billing.isGenerated())
                .generatedDate(billing.getGeneratedDate())
                .build();
    }
}
