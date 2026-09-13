package com.mess.app.service.impl;

import com.mess.app.dto.request.PartyPaymentRequest;
import com.mess.app.dto.request.PartyRequest;
import com.mess.app.dto.response.PartyResponse;
import com.mess.app.dto.response.PartySummaryResponse;
import com.mess.app.entity.Candidate;
import com.mess.app.entity.Department;
import com.mess.app.entity.Party;
import com.mess.app.entity.Party.PaymentStatus;
import com.mess.app.entity.PartyExtraItem;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.CandidateRepository;
import com.mess.app.repository.DepartmentRepository;
import com.mess.app.repository.PartyExtraItemRepository;
import com.mess.app.repository.PartyRepository;
import com.mess.app.service.PartyService;
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
public class PartyServiceImpl implements PartyService {

    private final PartyRepository partyRepository;
    private final PartyExtraItemRepository partyExtraItemRepository;
    private final CandidateRepository candidateRepository;
    private final DepartmentRepository departmentRepository;

    // ==================== PARTY CRUD ====================

    @Override
    @Transactional
    public PartyResponse createParty(PartyRequest request) {
        log.info("Creating new party: {}", request.getPartyName());

        Candidate enteredBy = null;
        if (request.getEnteredByCandidateId() != null) {
            enteredBy = candidateRepository.findById(request.getEnteredByCandidateId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Candidate not found with ID: " + request.getEnteredByCandidateId()));
        }

        Department department = resolveDepartment(request.getDepartmentId(), request.getDepartmentCode());

        BigDecimal totalThaliAmount = BigDecimal.valueOf(request.getNumberOfPeople())
                .multiply(request.getThaliRate());

        BigDecimal extraItemsAmount = BigDecimal.ZERO;
        List<PartyExtraItem> extraItemList = new ArrayList<>();

        if (request.getExtraItems() != null) {
            for (PartyRequest.ExtraItemRequest extraReq : request.getExtraItems()) {
                BigDecimal total = BigDecimal.valueOf(extraReq.getQuantity())
                        .multiply(extraReq.getRate());
                extraItemsAmount = extraItemsAmount.add(total);

                PartyExtraItem extraItem = PartyExtraItem.builder()
                        .itemName(extraReq.getItemName())
                        .quantity(extraReq.getQuantity())
                        .rate(extraReq.getRate())
                        .totalAmount(total)
                        .unit(extraReq.getUnit())
                        .build();
                extraItemList.add(extraItem);
            }
        }

        BigDecimal totalBill = totalThaliAmount.add(extraItemsAmount);

        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getDiscount() != null && request.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            if ("PERCENTAGE".equalsIgnoreCase(request.getDiscountType())) {
                discountAmount = totalBill.multiply(request.getDiscount())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                discountAmount = request.getDiscount();
            }
            totalBill = totalBill.subtract(discountAmount);
        }

        BigDecimal paidAmount = request.getPaidAmount() != null ? request.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal pendingAmount = totalBill.subtract(paidAmount);

        PaymentStatus paymentStatus;
        if (paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            paymentStatus = PaymentStatus.PENDING;
        } else if (paidAmount.compareTo(totalBill) >= 0) {
            paymentStatus = PaymentStatus.PAID;
            pendingAmount = BigDecimal.ZERO;
        } else {
            paymentStatus = PaymentStatus.PARTIAL;
        }

        Party party = Party.builder()
                .partyId(generatePartyId())
                .partyName(request.getPartyName())
                .contactPerson(request.getContactPerson())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .eventDate(request.getEventDate() != null ? request.getEventDate() : LocalDate.now())
                .eventTime(request.getEventTime())
                .partyType(request.getPartyType())
                .numberOfPeople(request.getNumberOfPeople())
                .mealType(request.getMealType())
                .thaliRate(request.getThaliRate())
                .totalThaliAmount(totalThaliAmount)
                .extraItemsAmount(extraItemsAmount)
                .discount(discountAmount)
                .discountType(request.getDiscountType())
                .totalBill(totalBill)
                .paymentStatus(paymentStatus)
                .paidAmount(paidAmount)
                .pendingAmount(pendingAmount)
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .invoiceNumber(generateInvoiceNumber())
                .specialRequests(request.getSpecialRequests())
                .remarks(request.getRemarks())
                .departmentId(department != null ? department.getId() : null)
                .departmentCode(department != null ? department.getCode() : null)
                .departmentName(request.getDepartmentName())
                .additionalDepartments(request.getAdditionalDepartments())
                .enteredBy(enteredBy)
                .extraItemList(extraItemList)
                .build();

        for (PartyExtraItem extraItem : extraItemList) {
            extraItem.setParty(party);
        }

        Party saved = partyRepository.save(party);
        log.info("Party created with ID: {}", saved.getPartyId());

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public PartyResponse updateParty(String id, PartyRequest request) {
        log.info("Updating party with ID: {}", id);

        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with ID: " + id));

        party.setPartyName(request.getPartyName());
        party.setContactPerson(request.getContactPerson());
        party.setPhoneNumber(request.getPhoneNumber());
        party.setEmail(request.getEmail());
        party.setAddress(request.getAddress());
        party.setEventDate(request.getEventDate());
        party.setEventTime(request.getEventTime());
        party.setPartyType(request.getPartyType());
        party.setNumberOfPeople(request.getNumberOfPeople());
        party.setMealType(request.getMealType());
        party.setThaliRate(request.getThaliRate());
        party.setDiscount(request.getDiscount());
        party.setDiscountType(request.getDiscountType());
        party.setSpecialRequests(request.getSpecialRequests());
        party.setRemarks(request.getRemarks());

        Department department = resolveDepartment(request.getDepartmentId(), request.getDepartmentCode());
        party.setDepartmentId(department != null ? department.getId() : null);
        party.setDepartmentCode(department != null ? department.getCode() : null);
        party.setDepartmentName(request.getDepartmentName());
        party.setAdditionalDepartments(request.getAdditionalDepartments());

        BigDecimal totalThaliAmount = BigDecimal.valueOf(request.getNumberOfPeople())
                .multiply(request.getThaliRate());

        partyExtraItemRepository.deleteByPartyId(party.getId());

        BigDecimal extraItemsAmount = BigDecimal.ZERO;
        List<PartyExtraItem> extraItemList = new ArrayList<>();

        if (request.getExtraItems() != null) {
            for (PartyRequest.ExtraItemRequest extraReq : request.getExtraItems()) {
                BigDecimal total = BigDecimal.valueOf(extraReq.getQuantity())
                        .multiply(extraReq.getRate());
                extraItemsAmount = extraItemsAmount.add(total);

                PartyExtraItem extraItem = PartyExtraItem.builder()
                        .party(party)
                        .itemName(extraReq.getItemName())
                        .quantity(extraReq.getQuantity())
                        .rate(extraReq.getRate())
                        .totalAmount(total)
                        .unit(extraReq.getUnit())
                        .build();
                extraItemList.add(extraItem);
            }
        }

        party.setTotalThaliAmount(totalThaliAmount);
        party.setExtraItemsAmount(extraItemsAmount);

        BigDecimal totalBill = totalThaliAmount.add(extraItemsAmount);
        BigDecimal discountAmount = BigDecimal.ZERO;
        if (request.getDiscount() != null && request.getDiscount().compareTo(BigDecimal.ZERO) > 0) {
            if ("PERCENTAGE".equalsIgnoreCase(request.getDiscountType())) {
                discountAmount = totalBill.multiply(request.getDiscount())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            } else {
                discountAmount = request.getDiscount();
            }
            totalBill = totalBill.subtract(discountAmount);
        }
        party.setTotalBill(totalBill);

        if (request.getPaymentStatus() != null) {
            party.setPaymentStatus(PaymentStatus.valueOf(request.getPaymentStatus().toUpperCase()));
        }

        Party saved = partyRepository.save(party);
        log.info("Party updated successfully");

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public PartyResponse getPartyById(String id) {
        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with ID: " + id));
        return convertToResponse(party);
    }

    @Override
    @Transactional
    public PartyResponse getPartyByPartyId(String partyId) {
        Party party = partyRepository.findByPartyId(partyId)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with ID: " + partyId));
        return convertToResponse(party);
    }

    @Override
    @Transactional
    public List<PartyResponse> getAllParties() {
        return partyRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PartyResponse> getPartiesByDate(LocalDate date) {
        return partyRepository.findByEventDate(date).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PartyResponse> getPartiesByDateRange(LocalDate startDate, LocalDate endDate) {
        return partyRepository.findByEventDateBetween(startDate, endDate).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PartyResponse> getPartiesByPaymentStatus(String status) {
        PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
        return partyRepository.findByPaymentStatus(paymentStatus).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<PartyResponse> getPartiesByDepartment(String departmentCode) {
        if (departmentCode == null || departmentCode.isBlank()) {
            return Collections.emptyList();
        }
        String normalized = departmentCode.trim().toUpperCase();
        return partyRepository.findByDepartmentCode(normalized).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteParty(String id) {
        log.info("Deleting party with ID: {}", id);
        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with ID: " + id));
        partyExtraItemRepository.deleteByPartyId(id);
        partyRepository.delete(party);
        log.info("Party deleted successfully");
    }

    // ==================== PARTY PAYMENTS ====================

    @Override
    @Transactional
    public PartyResponse makePayment(PartyPaymentRequest request) {
        log.info("Making payment for party: {}", request.getPartyId());

        Party party = partyRepository.findById(request.getPartyId())
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with ID: " + request.getPartyId()));

        BigDecimal amount = request.getAmount();
        BigDecimal currentPaid = party.getPaidAmount() != null ? party.getPaidAmount() : BigDecimal.ZERO;
        BigDecimal newPaid = currentPaid.add(amount);

        if (newPaid.compareTo(party.getTotalBill()) > 0) {
            throw new RuntimeException("Payment amount exceeds total bill");
        }

        BigDecimal newPending = party.getTotalBill().subtract(newPaid);

        party.setPaidAmount(newPaid);
        party.setPendingAmount(newPending);

        if (newPaid.compareTo(party.getTotalBill()) >= 0) {
            party.setPaymentStatus(PaymentStatus.PAID);
            party.setPendingAmount(BigDecimal.ZERO);
        } else {
            party.setPaymentStatus(PaymentStatus.PARTIAL);
        }

        if (request.getPaymentMethod() != null) {
            party.setPaymentMethod(request.getPaymentMethod());
        }
        if (request.getTransactionId() != null) {
            party.setTransactionId(request.getTransactionId());
        }
        if (request.getRemarks() != null) {
            party.setRemarks(request.getRemarks());
        }

        Party saved = partyRepository.save(party);
        log.info("Payment made successfully for party: {}", party.getPartyId());

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public PartyResponse markAsPaid(String id) {
        log.info("Marking party as paid: {}", id);

        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with ID: " + id));

        party.setPaymentStatus(PaymentStatus.PAID);
        party.setPaidAmount(party.getTotalBill());
        party.setPendingAmount(BigDecimal.ZERO);

        Party saved = partyRepository.save(party);
        log.info("Party marked as paid: {}", party.getPartyId());

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public PartyResponse updatePaymentStatus(String id, String status) {
        log.info("Updating payment status for party: {} to {}", id, status);

        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Party not found with ID: " + id));

        party.setPaymentStatus(PaymentStatus.valueOf(status.toUpperCase()));

        Party saved = partyRepository.save(party);
        log.info("Payment status updated for party: {}", party.getPartyId());

        return convertToResponse(saved);
    }

    // ==================== PARTY SUMMARY & REPORTS ====================

    @Override
    @Transactional
    public PartySummaryResponse getPartySummary(LocalDate startDate, LocalDate endDate) {
        log.info("Generating party summary from {} to {}", startDate, endDate);

        List<Party> parties = partyRepository.findByEventDateBetween(startDate, endDate);
        int totalParties = parties.size();

        BigDecimal totalBilling = partyRepository.getTotalPartyBilling(startDate, endDate);
        if (totalBilling == null)
            totalBilling = BigDecimal.ZERO;

        BigDecimal totalPaid = partyRepository.getTotalPaidBilling(startDate, endDate);
        if (totalPaid == null)
            totalPaid = BigDecimal.ZERO;

        BigDecimal totalPending = totalBilling.subtract(totalPaid);
        BigDecimal totalOutstanding = partyRepository.getTotalOutstandingAmount();
        if (totalOutstanding == null)
            totalOutstanding = BigDecimal.ZERO;

        Map<String, Integer> mealTypeWiseCount = new HashMap<>();
        Map<String, BigDecimal> mealTypeWiseRevenue = new HashMap<>();

        List<Object[]> countResults = partyRepository.getMealTypeWiseCount(startDate, endDate);
        for (Object[] result : countResults) {
            String mealType = (String) result[0];
            Long count = (Long) result[1];
            mealTypeWiseCount.put(mealType, count.intValue());
        }

        List<Object[]> revenueResults = partyRepository.getMealTypeWiseRevenue(startDate, endDate);
        for (Object[] result : revenueResults) {
            String mealType = (String) result[0];
            BigDecimal revenue = (BigDecimal) result[1];
            mealTypeWiseRevenue.put(mealType, revenue);
        }

        List<PartyResponse> recentParties = parties.stream()
                .sorted((p1, p2) -> p2.getEventDate().compareTo(p1.getEventDate()))
                .limit(10)
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return PartySummaryResponse.builder()
                .totalParties(totalParties)
                .totalBilling(totalBilling)
                .totalPaid(totalPaid)
                .totalPending(totalPending)
                .totalOutstanding(totalOutstanding)
                .mealTypeWiseCount(mealTypeWiseCount)
                .mealTypeWiseRevenue(mealTypeWiseRevenue)
                .recentParties(recentParties)
                .build();
    }

    @Override
    @Transactional
    public BigDecimal getTotalPartyBilling(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = partyRepository.getTotalPartyBilling(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public BigDecimal getTotalOutstandingAmount() {
        BigDecimal outstanding = partyRepository.getTotalOutstandingAmount();
        return outstanding != null ? outstanding : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public Map<String, BigDecimal> getMonthlyPartyBilling(int year) {
        Map<String, BigDecimal> monthlyBilling = new LinkedHashMap<>();
        List<Object[]> results = partyRepository.getMonthlyPartyBilling(year);
        for (Object[] result : results) {
            int month = ((Number) result[0]).intValue();
            BigDecimal amount = (BigDecimal) result[1];
            String monthName = LocalDate.of(year, month, 1).getMonth().name();
            monthlyBilling.put(monthName, amount);
        }
        return monthlyBilling;
    }

    @Override
    @Transactional
    public Map<String, Object> getPartyRevenueReport(int year) {
        Map<String, Object> report = new HashMap<>();

        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);

        List<Party> parties = partyRepository.findByEventDateBetween(startDate, endDate);

        BigDecimal totalRevenue = parties.stream()
                .map(Party::getTotalBill)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollected = parties.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PAID)
                .map(Party::getTotalBill)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPending = parties.stream()
                .filter(p -> p.getPaymentStatus() == PaymentStatus.PENDING ||
                        p.getPaymentStatus() == PaymentStatus.PARTIAL)
                .map(Party::getPendingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> monthlyRevenue = getMonthlyPartyBilling(year);
        List<Object[]> monthlyCounts = partyRepository.getMonthlyPartyCount(year);

        Map<String, Integer> monthlyCountMap = new LinkedHashMap<>();
        for (Object[] result : monthlyCounts) {
            int month = ((Number) result[0]).intValue();
            Long count = (Long) result[1];
            String monthName = LocalDate.of(year, month, 1).getMonth().name();
            monthlyCountMap.put(monthName, count.intValue());
        }

        report.put("year", year);
        report.put("totalParties", parties.size());
        report.put("totalPeople", partyRepository.getTotalPeopleByDateRange(startDate, endDate));
        report.put("totalRevenue", totalRevenue);
        report.put("totalCollected", totalCollected);
        report.put("totalPending", totalPending);
        report.put("monthlyRevenue", monthlyRevenue);
        report.put("monthlyCount", monthlyCountMap);
        report.put("averageBill", partyRepository.getAverageBillByDateRange(startDate, endDate));
        report.put("collectionRate",
                totalRevenue.compareTo(BigDecimal.ZERO) > 0
                        ? totalCollected.divide(totalRevenue, 4, RoundingMode.HALF_UP)
                                .multiply(BigDecimal.valueOf(100))
                                .setScale(2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO);

        return report;
    }

    // ==================== HELPER METHODS ====================

    @Override
    @Transactional
    public String generatePartyId() {
        String lastId = partyRepository.findTopByOrderByPartyIdDesc()
                .map(Party::getPartyId)
                .orElse("PRT00");

        int number = Integer.parseInt(lastId.substring(3));
        int nextNumber = number + 1;

        return String.format("PRT%02d", nextNumber);
    }

    @Override
    @Transactional
    public String generateInvoiceNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = String.format("%04d", new Random().nextInt(10000));
        return "INV-PRT-" + date + "-" + random;
    }

    // ==================== DEPARTMENT RESOLVER ====================

    private Department resolveDepartment(String departmentId, String departmentCode) {
        if (departmentId != null && !departmentId.isBlank()) {
            return departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with ID: " + departmentId));
        }
        if (departmentCode != null && !departmentCode.isBlank()) {
            String normalized = departmentCode.trim().toUpperCase();
            return departmentRepository.findByCodeIgnoreCase(normalized)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with code: " + departmentCode));
        }
        return null;
    }

    // ==================== MAPPER ====================

    private PartyResponse convertToResponse(Party party) {
        List<PartyResponse.ExtraItemResponse> extraItems = party.getExtraItemList().stream()
                .map(item -> PartyResponse.ExtraItemResponse.builder()
                        .id(item.getId())
                        .itemName(item.getItemName())
                        .quantity(item.getQuantity())
                        .rate(item.getRate())
                        .totalAmount(item.getTotalAmount())
                        .unit(item.getUnit())
                        .build())
                .collect(Collectors.toList());

        return PartyResponse.builder()
                .id(party.getId())
                .partyId(party.getPartyId())
                .invoiceNumber(party.getInvoiceNumber())
                .partyName(party.getPartyName())
                .contactPerson(party.getContactPerson())
                .phoneNumber(party.getPhoneNumber())
                .email(party.getEmail())
                .address(party.getAddress())
                .eventDate(party.getEventDate())
                .eventTime(party.getEventTime())
                .partyType(party.getPartyType())
                .numberOfPeople(party.getNumberOfPeople())
                .mealType(party.getMealType())
                .thaliRate(party.getThaliRate())
                .totalThaliAmount(party.getTotalThaliAmount())
                .extraItemsAmount(party.getExtraItemsAmount())
                .discount(party.getDiscount())
                .discountType(party.getDiscountType())
                .totalBill(party.getTotalBill())
                .paymentStatus(party.getPaymentStatus() != null ? party.getPaymentStatus().name() : null)
                .paidAmount(party.getPaidAmount())
                .pendingAmount(party.getPendingAmount())
                .paymentMethod(party.getPaymentMethod())
                .transactionId(party.getTransactionId())
                .specialRequests(party.getSpecialRequests())
                .remarks(party.getRemarks())
                .departmentId(party.getDepartmentId())
                .departmentCode(party.getDepartmentCode())
                .departmentName(party.getDepartmentName())
                .additionalDepartments(party.getAdditionalDepartments())
                .enteredByCandidateId(party.getEnteredBy() != null ? party.getEnteredBy().getCandidateId() : null)
                .enteredByName(party.getEnteredBy() != null ? party.getEnteredBy().getFullName() : null)
                .extraItems(extraItems)
                .createdAt(party.getCreatedAt())
                .updatedAt(party.getUpdatedAt())
                .build();
    }
}