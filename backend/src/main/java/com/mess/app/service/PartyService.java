package com.mess.app.service;

import com.mess.app.dto.request.PartyPaymentRequest;
import com.mess.app.dto.request.PartyRequest;
import com.mess.app.dto.response.PartyResponse;
import com.mess.app.dto.response.PartySummaryResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PartyService {

    // Party CRUD
    PartyResponse createParty(PartyRequest request);

    PartyResponse updateParty(String id, PartyRequest request);

    PartyResponse getPartyById(String id);

    PartyResponse getPartyByPartyId(String partyId);

    List<PartyResponse> getAllParties();

    List<PartyResponse> getPartiesByDate(LocalDate date);

    List<PartyResponse> getPartiesByDateRange(LocalDate startDate, LocalDate endDate);

    List<PartyResponse> getPartiesByPaymentStatus(String status);

    // ==================== DEPARTMENT (NEW) ====================
    List<PartyResponse> getPartiesByDepartment(String departmentCode);

    void deleteParty(String id);

    // Party Payments
    PartyResponse makePayment(PartyPaymentRequest request);

    PartyResponse markAsPaid(String id);

    PartyResponse updatePaymentStatus(String id, String status);

    // Party Summary & Reports
    PartySummaryResponse getPartySummary(LocalDate startDate, LocalDate endDate);

    BigDecimal getTotalPartyBilling(LocalDate startDate, LocalDate endDate);

    BigDecimal getTotalOutstandingAmount();

    Map<String, BigDecimal> getMonthlyPartyBilling(int year);

    Map<String, Object> getPartyRevenueReport(int year);

    // Auto Generate Party ID
    String generatePartyId();

    String generateInvoiceNumber();
}