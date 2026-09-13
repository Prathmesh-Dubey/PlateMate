package com.mess.app.service.impl;

import com.mess.app.dto.request.CollegeBillingRequest;
import com.mess.app.dto.response.CollegeBillingResponse;
import com.mess.app.dto.response.CollegeBillingSummaryResponse;
import com.mess.app.entity.CollegeBilling;
import com.mess.app.entity.CollegeBillingPaymentStatus;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.CollegeBillingRepository;
import com.mess.app.repository.DepartmentRepository;
import com.mess.app.service.CollegeBillingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollegeBillingServiceImpl implements CollegeBillingService {

    private final CollegeBillingRepository collegeBillingRepository;
    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public CollegeBillingResponse createCollegeBilling(CollegeBillingRequest request) {
        String cleanCollege = request.getCollege().trim();
        LocalDate billingDate = request.getBillingDate();
        int month = request.getMonth() != null ? request.getMonth() : (billingDate != null ? billingDate.getMonthValue() : LocalDate.now().getMonthValue());
        int year = request.getYear() != null ? request.getYear() : (billingDate != null ? billingDate.getYear() : LocalDate.now().getYear());

        if (billingDate != null) {
            month = billingDate.getMonthValue();
            year = billingDate.getYear();
        } else {
            LocalDate today = LocalDate.now();
            if (today.getMonthValue() == month && today.getYear() == year) {
                billingDate = today;
            } else {
                billingDate = LocalDate.of(year, month, 1);
            }
        }

        // Prevent duplicate records for the same College + Month + Year
        if (collegeBillingRepository.existsByCollegeIgnoreCaseAndMonthAndYear(cleanCollege, month, year)) {
            String monthName = getMonthName(month);
            throw new IllegalArgumentException(
                    "A billing record already exists for college \"" + cleanCollege + "\" for " + monthName + " " + year
            );
        }

        BigDecimal billable = request.getTotalBillableAmount();
        BigDecimal received = request.getAmountReceived();
        BigDecimal outstanding = billable.subtract(received);
        CollegeBillingPaymentStatus status = calculatePaymentStatus(billable, received);

        CollegeBilling entity = CollegeBilling.builder()
                .college(cleanCollege)
                .month(month)
                .year(year)
                .billingDate(billingDate)
                .totalStudentAttendance(request.getTotalStudentAttendance())
                .totalThalisServed(request.getTotalThalisServed())
                .ratePerThali(request.getRatePerThali())
                .totalBillableAmount(billable)
                .amountReceived(received)
                .outstandingBalance(outstanding)
                .status(status)
                .remarks(request.getRemarks() != null ? request.getRemarks().trim() : null)
                .build();

        CollegeBilling saved = collegeBillingRepository.save(entity);
        log.info("Created college billing record with ID: {} for college: {}, {}/{}",
                saved.getId(), cleanCollege, month, year);

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public CollegeBillingResponse updateCollegeBilling(String id, CollegeBillingRequest request) {
        CollegeBilling entity = collegeBillingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College billing record not found with id: " + id));

        String cleanCollege = request.getCollege().trim();
        LocalDate billingDate = request.getBillingDate();
        int month = request.getMonth() != null ? request.getMonth() : (billingDate != null ? billingDate.getMonthValue() : entity.getMonth());
        int year = request.getYear() != null ? request.getYear() : (billingDate != null ? billingDate.getYear() : entity.getYear());

        if (billingDate != null) {
            month = billingDate.getMonthValue();
            year = billingDate.getYear();
            entity.setBillingDate(billingDate);
        }

        // Check duplicate if college, month, or year changed
        if (collegeBillingRepository.existsByCollegeIgnoreCaseAndMonthAndYearAndIdNot(cleanCollege, month, year, id)) {
            String monthName = getMonthName(month);
            throw new IllegalArgumentException(
                    "Another billing record already exists for college \"" + cleanCollege + "\" for " + monthName + " " + year
            );
        }

        BigDecimal billable = request.getTotalBillableAmount();
        BigDecimal received = request.getAmountReceived();
        BigDecimal outstanding = billable.subtract(received);
        CollegeBillingPaymentStatus status = calculatePaymentStatus(billable, received);

        entity.setCollege(cleanCollege);
        entity.setMonth(month);
        entity.setYear(year);
        entity.setTotalStudentAttendance(request.getTotalStudentAttendance());
        entity.setTotalThalisServed(request.getTotalThalisServed());
        entity.setRatePerThali(request.getRatePerThali());
        entity.setTotalBillableAmount(billable);
        entity.setAmountReceived(received);
        entity.setOutstandingBalance(outstanding);
        entity.setStatus(status);
        entity.setRemarks(request.getRemarks() != null ? request.getRemarks().trim() : null);

        CollegeBilling updated = collegeBillingRepository.save(entity);
        log.info("Updated college billing record with ID: {}", updated.getId());

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeBillingResponse getCollegeBillingById(String id) {
        CollegeBilling entity = collegeBillingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("College billing record not found with id: " + id));
        return mapToResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CollegeBillingResponse> getAllCollegeBilling(String college, Integer month, Integer year) {
        String filterCollege = (college != null && !college.trim().isEmpty()) ? college.trim() : null;
        List<CollegeBilling> records = collegeBillingRepository.findWithFilters(filterCollege, month, year);
        return records.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCollegeBilling(String id) {
        if (!collegeBillingRepository.existsById(id)) {
            throw new ResourceNotFoundException("College billing record not found with id: " + id);
        }
        collegeBillingRepository.deleteById(id);
        log.info("Deleted college billing record with ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CollegeBillingSummaryResponse getSummary(String college, Integer month, Integer year) {
        String filterCollege = (college != null && !college.trim().isEmpty()) ? college.trim() : null;
        List<CollegeBilling> records = collegeBillingRepository.findWithFilters(filterCollege, month, year);

        BigDecimal totalBillable = BigDecimal.ZERO;
        BigDecimal totalReceived = BigDecimal.ZERO;
        BigDecimal totalOutstanding = BigDecimal.ZERO;
        long paidCount = 0;
        long partiallyPaidCount = 0;
        long unpaidCount = 0;

        for (CollegeBilling r : records) {
            totalBillable = totalBillable.add(r.getTotalBillableAmount() != null ? r.getTotalBillableAmount() : BigDecimal.ZERO);
            totalReceived = totalReceived.add(r.getAmountReceived() != null ? r.getAmountReceived() : BigDecimal.ZERO);
            totalOutstanding = totalOutstanding.add(r.getOutstandingBalance() != null ? r.getOutstandingBalance() : BigDecimal.ZERO);

            if (r.getStatus() == CollegeBillingPaymentStatus.PAID) {
                paidCount++;
            } else if (r.getStatus() == CollegeBillingPaymentStatus.PARTIALLY_PAID) {
                partiallyPaidCount++;
            } else {
                unpaidCount++;
            }
        }

        return CollegeBillingSummaryResponse.builder()
                .totalBillableAmount(totalBillable)
                .totalAmountReceived(totalReceived)
                .totalOutstandingBalance(totalOutstanding)
                .totalRecords(records.size())
                .paidCount(paidCount)
                .partiallyPaidCount(partiallyPaidCount)
                .unpaidCount(unpaidCount)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getColleges() {
        Set<String> colleges = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);

        // From existing college billing records
        List<String> billedColleges = collegeBillingRepository.findDistinctColleges();
        if (billedColleges != null) {
            billedColleges.stream()
                    .filter(c -> c != null && !c.trim().isEmpty())
                    .forEach(c -> colleges.add(c.trim()));
        }

        // Also incorporate any existing departments as available institutions
        try {
            departmentRepository.findAll().forEach(dept -> {
                if (dept.getName() != null && !dept.getName().trim().isEmpty()) {
                    colleges.add(dept.getName().trim());
                }
            });
        } catch (Exception e) {
            log.warn("Could not fetch departments for colleges list: {}", e.getMessage());
        }

        return new ArrayList<>(colleges);
    }

    private CollegeBillingPaymentStatus calculatePaymentStatus(BigDecimal billable, BigDecimal received) {
        if (received == null || received.compareTo(BigDecimal.ZERO) <= 0) {
            return CollegeBillingPaymentStatus.UNPAID;
        }
        if (billable != null && received.compareTo(billable) >= 0) {
            return CollegeBillingPaymentStatus.PAID;
        }
        return CollegeBillingPaymentStatus.PARTIALLY_PAID;
    }

    private String getMonthName(int month) {
        try {
            return Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        } catch (Exception e) {
            return "Month " + month;
        }
    }

    private CollegeBillingResponse mapToResponse(CollegeBilling entity) {
        LocalDate bDate = entity.getBillingDate();
        if (bDate == null) {
            bDate = entity.getCreatedAt() != null ? entity.getCreatedAt().toLocalDate() : LocalDate.of(entity.getYear(), entity.getMonth(), 1);
        }

        return CollegeBillingResponse.builder()
                .id(entity.getId())
                .college(entity.getCollege())
                .month(entity.getMonth())
                .year(entity.getYear())
                .monthName(getMonthName(entity.getMonth()))
                .billingDate(bDate)
                .totalStudentAttendance(entity.getTotalStudentAttendance())
                .totalThalisServed(entity.getTotalThalisServed())
                .ratePerThali(entity.getRatePerThali())
                .totalBillableAmount(entity.getTotalBillableAmount())
                .amountReceived(entity.getAmountReceived())
                .outstandingBalance(entity.getOutstandingBalance())
                .status(entity.getStatus())
                .remarks(entity.getRemarks())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
