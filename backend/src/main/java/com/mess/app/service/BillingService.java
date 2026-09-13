package com.mess.app.service;

import com.mess.app.dto.request.BulkFeeCollectionRequest;
import com.mess.app.dto.request.FeeCollectionRequest;
import com.mess.app.dto.response.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface BillingService {

    // Fee Collection
    FeeCollectionResponse collectFee(FeeCollectionRequest request);

    FeeCollectionResponse getFeeCollectionById(String id);

    List<FeeCollectionResponse> getFeesByCandidate(String candidateId);

    List<FeeCollectionResponse> getFeesByMonth(int month, int year);

    FeeSummaryResponse getFeeSummary(int month, int year);

    void bulkCollectFees(BulkFeeCollectionRequest request);

    void deleteFeeCollection(String id);

    // Monthly Billing snapshots (monthly_billing table)
    MonthlyBillingResponse generateMonthlyBilling(int month, int year);

    MonthlyBillingResponse getMonthlyBilling(int month, int year);

    List<MonthlyBillingResponse> getYearlyBilling(int year);

    YearlyFinancialReport getYearlyFinancialReport(int year);

    // Dashboard & Reports
    Map<String, Object> getDashboardData();

    /**
     * Live financial summary for the Billing page dashboard boxes.
     *
     * @param scope MONTH (default), YEAR or ALL
     * @param month 1-12, defaults to the current month (MONTH scope only)
     * @param year  defaults to the current year (MONTH / YEAR scope)
     */
    BillingSummaryResponse getBillingSummary(String scope, Integer month, Integer year);

    /** Live monthly Profit & Loss for a calendar year, computed from the transactional tables. */
    MonthlyProfitLossResponse getMonthlyProfitLoss(int year);

    Map<String, Object> getIncomeExpenseComparison(int year);

    BigDecimal getCurrentMonthProfit();

    BigDecimal getCurrentYearProfit();

    // Auto Generation
    void generateMonthlyBillingForAll();
}
