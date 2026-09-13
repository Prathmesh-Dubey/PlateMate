package com.mess.app.service.impl;

import com.mess.app.dto.request.StaffAdvanceRequest;
import com.mess.app.dto.request.StaffPaymentRequest;
import com.mess.app.dto.request.StaffRequest;
import com.mess.app.dto.response.StaffAdvanceResponse;
import com.mess.app.dto.response.StaffPaymentResponse;
import com.mess.app.dto.response.StaffResponse;
import com.mess.app.dto.response.StaffSalarySummary;
import com.mess.app.entity.*;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.*;
import com.mess.app.service.StaffService;
import com.mess.app.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StaffServiceImpl implements StaffService {

    private final StaffRepository staffRepository;
    private final StaffPaymentRepository staffPaymentRepository;
    private final StaffAdvanceRepository staffAdvanceRepository;
    private final CandidateRepository candidateRepository;
    private final FileUploadUtil fileUploadUtil;

    // ==================== STAFF CRUD ====================

    @Override
    @Transactional
    public List<StaffAdvanceResponse> getAllAdvances() {
        return staffAdvanceRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StaffResponse createStaff(StaffRequest request) {
        log.info("Creating new staff: {}", request.getFullName());

        validateStaffRequest(request, true);

        Staff staff = Staff.builder()
                .staffId(generateStaffId())
                .fullName(request.getFullName().trim())
                .position(request.getPosition() != null && !request.getPosition().isBlank()
                        ? request.getPosition().trim()
                        : "OTHER")
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .joiningDate(request.getJoiningDate() != null ? request.getJoiningDate() : LocalDate.now())
                .leavingDate(request.getLeavingDate())
                .baseSalary(request.getBaseSalary())
                // The React form does not send an employment type - default to FULL_TIME
                // instead of failing
                .employmentType(parseEmploymentType(request.getEmploymentType(), Staff.EmploymentType.FULL_TIME))
                .status(parseStatus(request.getStatus(), Staff.StaffStatus.ACTIVE))
                .emergencyContact(request.getEmergencyContact())
                .emergencyPhone(request.getEmergencyPhone())
                .bankName(request.getBankName())
                .bankAccountNumber(request.getBankAccountNumber())
                .ifscCode(request.getIfscCode())
                .panNumber(request.getPanNumber())
                .aadharNumber(request.getAadharNumber())
                .notes(request.getNotes())
                .imageUrls(new ArrayList<>())
                .build();

        // Handle image uploads
        handleImageUploads(request, staff);

        Staff saved = staffRepository.save(staff);
        log.info("Staff created with ID: {}", saved.getStaffId());

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public StaffResponse updateStaff(String id, StaffRequest request) {
        log.info("Updating staff with ID: {}", id);

        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + id));

        validateStaffRequest(request, false);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            staff.setFullName(request.getFullName().trim());
        }
        if (request.getPosition() != null && !request.getPosition().isBlank()) {
            staff.setPosition(request.getPosition().trim());
        }
        staff.setPhoneNumber(request.getPhoneNumber());
        staff.setEmail(request.getEmail());
        staff.setAddress(request.getAddress());
        if (request.getJoiningDate() != null) {
            staff.setJoiningDate(request.getJoiningDate());
        }
        staff.setLeavingDate(request.getLeavingDate());
        if (request.getBaseSalary() != null) {
            staff.setBaseSalary(request.getBaseSalary());
        }
        if (request.getEmploymentType() != null && !request.getEmploymentType().isBlank()) {
            staff.setEmploymentType(parseEmploymentType(request.getEmploymentType(), staff.getEmploymentType()));
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            staff.setStatus(parseStatus(request.getStatus(), staff.getStatus()));
        }
        staff.setEmergencyContact(request.getEmergencyContact());
        staff.setEmergencyPhone(request.getEmergencyPhone());
        staff.setBankName(request.getBankName());
        staff.setBankAccountNumber(request.getBankAccountNumber());
        staff.setIfscCode(request.getIfscCode());
        staff.setPanNumber(request.getPanNumber());
        staff.setAadharNumber(request.getAadharNumber());
        staff.setNotes(request.getNotes());

        // Handle new image uploads
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            handleImageUploads(request, staff);
        }

        Staff updated = staffRepository.save(staff);
        log.info("Staff updated successfully");

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public StaffResponse getStaffById(String id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + id));
        return convertToResponse(staff);
    }

    @Override
    @Transactional
    public StaffResponse getStaffByStaffId(String staffId) {
        Staff staff = staffRepository.findByStaffId(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffId));
        return convertToResponse(staff);
    }

    @Override
    @Transactional
    public List<StaffResponse> getAllStaff() {
        return staffRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StaffResponse> getActiveStaff() {
        return staffRepository.findByStatus(Staff.StaffStatus.ACTIVE).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StaffResponse> getStaffByPosition(String position) {
        return staffRepository.findByPosition(position).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteStaff(String id) {
        log.info("Hard deleting staff with ID: {}", id);

        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + id));

        // 1) Delete dependent records first (FK constraints)
        List<StaffPayment> payments = staffPaymentRepository.findByStaffOrderByPaymentDateDesc(staff);
        if (!payments.isEmpty()) {
            staffPaymentRepository.deleteAll(payments);
        }

        List<StaffAdvance> advances = staffAdvanceRepository.findByStaffOrderByAdvanceDateDesc(staff);
        if (!advances.isEmpty()) {
            staffAdvanceRepository.deleteAll(advances);
        }

        // 2) Delete any uploaded image files from disk
        if (staff.getImageUrls() != null) {
            for (String url : staff.getImageUrls()) {
                fileUploadUtil.deleteStoredImageQuietly(url);
            }
        }

        // 3) Finally delete the staff row
        staffRepository.delete(staff);

        log.info("Staff {} permanently deleted", id);
    }

    @Override
    @Transactional
    public void toggleStaffStatus(String id) {
        Staff staff = staffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + id));

        if (staff.getStatus() == Staff.StaffStatus.ACTIVE) {
            staff.setStatus(Staff.StaffStatus.ON_LEAVE);
        } else if (staff.getStatus() == Staff.StaffStatus.ON_LEAVE) {
            staff.setStatus(Staff.StaffStatus.ACTIVE);
        }
        staffRepository.save(staff);
        log.info("Staff status toggled to: {}", staff.getStatus());
    }

    // ==================== STAFF PAYMENTS ====================

    @Override
    @Transactional
    public StaffPaymentResponse createPayment(StaffPaymentRequest request) {
        log.info("Creating payment for staff: {}", request.getStaffId());

        if (request.getStaffId() == null || request.getStaffId().isBlank()) {
            throw new IllegalArgumentException("Staff ID is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + request.getStaffId()));

        Candidate enteredBy = null;
        if (request.getEnteredByCandidateId() != null) {
            enteredBy = candidateRepository.findById(request.getEnteredByCandidateId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Candidate not found with ID: " + request.getEnteredByCandidateId()));
        }

        LocalDate paymentDate = request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now();
        int paymentYear = request.getPaymentYear() > 0 ? request.getPaymentYear() : paymentDate.getYear();
        int paymentMonthNumber = request.getPaymentMonthNumber() >= 1 && request.getPaymentMonthNumber() <= 12
                ? request.getPaymentMonthNumber()
                : paymentDate.getMonthValue();
        String paymentMonth = request.getPaymentMonth() != null && !request.getPaymentMonth().isBlank()
                ? request.getPaymentMonth()
                : LocalDate.of(paymentYear, paymentMonthNumber, 1).getMonth().name();

        BigDecimal netAmount = request.getAmount()
                .add(request.getBonus() != null ? request.getBonus() : BigDecimal.ZERO)
                .subtract(request.getDeductions() != null ? request.getDeductions() : BigDecimal.ZERO);

        StaffPayment payment = StaffPayment.builder()
                .staff(staff)
                .paymentDate(paymentDate)
                .amount(request.getAmount())
                .paymentMonth(paymentMonth)
                .paymentYear(paymentYear)
                .paymentMonthNumber(paymentMonthNumber)
                .bonus(request.getBonus() != null ? request.getBonus() : BigDecimal.ZERO)
                .deductions(request.getDeductions() != null ? request.getDeductions() : BigDecimal.ZERO)
                .netAmount(netAmount)
                .paymentMethod(request.getPaymentMethod())
                .transactionId(request.getTransactionId())
                .referenceNumber(request.getReferenceNumber())
                .remarks(request.getRemarks())
                .enteredBy(enteredBy)
                .build();

        StaffPayment saved = staffPaymentRepository.save(payment);
        log.info("Payment created with ID: {}", saved.getId());

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public StaffPaymentResponse getPaymentById(String id) {
        StaffPayment payment = staffPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        return convertToResponse(payment);
    }

    @Override
    @Transactional
    public List<StaffPaymentResponse> getPaymentsByStaff(String staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffId));

        return staffPaymentRepository.findByStaffOrderByPaymentDateDesc(staff).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StaffPaymentResponse> getPaymentsByStaffAndYear(String staffId, int year) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffId));

        return staffPaymentRepository.findByStaffAndPaymentYear(staff, year).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StaffPaymentResponse> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        return staffPaymentRepository.findByPaymentDateBetween(startDate, endDate).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BigDecimal getTotalPaymentsByDateRange(LocalDate startDate, LocalDate endDate) {
        BigDecimal total = staffPaymentRepository.getTotalPaymentsByDateRange(startDate, endDate);
        return total != null ? total : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public void deletePayment(String id) {
        log.info("Deleting payment with ID: {}", id);
        StaffPayment payment = staffPaymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + id));
        staffPaymentRepository.delete(payment);
        log.info("Payment deleted successfully");
    }

    // ==================== STAFF ADVANCES ====================

    @Override
    @Transactional
    public StaffAdvanceResponse createAdvance(StaffAdvanceRequest request) {
        log.info("Creating advance for staff: {}", request.getStaffId());

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Advance amount must be greater than zero");
        }

        Staff staff = staffRepository.findById(request.getStaffId())
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + request.getStaffId()));

        Candidate enteredBy = null;
        if (request.getEnteredByCandidateId() != null) {
            enteredBy = candidateRepository.findById(request.getEnteredByCandidateId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Candidate not found with ID: " + request.getEnteredByCandidateId()));
        }

        // The Staff page issues advances as already APPROVED; honour the requested
        // status
        StaffAdvance.AdvanceStatus status = StaffAdvance.AdvanceStatus.PENDING;
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            try {
                status = StaffAdvance.AdvanceStatus.valueOf(request.getStatus().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid advance status: " + request.getStatus());
            }
        }

        StaffAdvance advance = StaffAdvance.builder()
                .staff(staff)
                .advanceDate(request.getAdvanceDate() != null ? request.getAdvanceDate() : LocalDate.now())
                .amount(request.getAmount())
                .reason(request.getReason() != null ? request.getReason() : "General advance")
                .status(status)
                .repaymentAmount(BigDecimal.ZERO)
                .remainingAmount(request.getAmount())
                .installmentMonths(request.getInstallmentMonths())
                .monthlyDeduction(request.getMonthlyDeduction())
                .remarks(request.getRemarks())
                .enteredBy(enteredBy)
                .build();

        StaffAdvance saved = staffAdvanceRepository.save(advance);
        log.info("Advance created with ID: {} ({})", saved.getId(), saved.getStatus());

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public StaffAdvanceResponse updateAdvance(String id, StaffAdvanceRequest request) {
        log.info("Updating advance with ID: {}", id);

        StaffAdvance advance = staffAdvanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance not found with ID: " + id));

        if (request.getAdvanceDate() != null) {
            advance.setAdvanceDate(request.getAdvanceDate());
        }
        if (request.getAmount() != null) {
            advance.setAmount(request.getAmount());
        }
        if (request.getReason() != null) {
            advance.setReason(request.getReason());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            advance.setStatus(StaffAdvance.AdvanceStatus.valueOf(request.getStatus().trim().toUpperCase()));
        }
        if (request.getRepaymentDate() != null) {
            advance.setRepaymentDate(request.getRepaymentDate());
        }
        if (request.getRepaymentAmount() != null) {
            advance.setRepaymentAmount(request.getRepaymentAmount());
        }
        if (request.getInstallmentMonths() > 0) {
            advance.setInstallmentMonths(request.getInstallmentMonths());
        }
        if (request.getMonthlyDeduction() != null) {
            advance.setMonthlyDeduction(request.getMonthlyDeduction());
        }
        if (request.getRemarks() != null) {
            advance.setRemarks(request.getRemarks());
        }

        BigDecimal repaid = advance.getRepaymentAmount() != null ? advance.getRepaymentAmount() : BigDecimal.ZERO;
        if (advance.getStatus() == StaffAdvance.AdvanceStatus.REPAID) {
            advance.setRepaymentAmount(advance.getAmount());
            advance.setRemainingAmount(BigDecimal.ZERO);
            if (advance.getRepaymentDate() == null) {
                advance.setRepaymentDate(LocalDate.now());
            }
        } else if (advance.getAmount() != null) {
            advance.setRemainingAmount(advance.getAmount().subtract(repaid).max(BigDecimal.ZERO));
        }

        StaffAdvance updated = staffAdvanceRepository.save(advance);
        log.info("Advance updated successfully");

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public StaffAdvanceResponse getAdvanceById(String id) {
        StaffAdvance advance = staffAdvanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance not found with ID: " + id));
        return convertToResponse(advance);
    }

    @Override
    @Transactional
    public List<StaffAdvanceResponse> getAdvancesByStaff(String staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffId));

        return staffAdvanceRepository.findByStaffOrderByAdvanceDateDesc(staff).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<StaffAdvanceResponse> getPendingAdvances() {
        return staffAdvanceRepository.findByStatus(StaffAdvance.AdvanceStatus.PENDING).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public StaffAdvanceResponse approveAdvance(String id, String approvedByCandidateId) {
        log.info("Approving advance with ID: {}", id);

        StaffAdvance advance = staffAdvanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance not found with ID: " + id));

        advance.setStatus(StaffAdvance.AdvanceStatus.APPROVED);
        if (advance.getRemainingAmount() == null) {
            BigDecimal repaid = advance.getRepaymentAmount() != null ? advance.getRepaymentAmount() : BigDecimal.ZERO;
            advance.setRemainingAmount(advance.getAmount().subtract(repaid).max(BigDecimal.ZERO));
        }

        // Only set approvedBy if a candidate ID is provided
        if (approvedByCandidateId != null && !approvedByCandidateId.isEmpty()) {
            try {
                Candidate approvedBy = candidateRepository.findById(approvedByCandidateId)
                        .orElse(null);
                advance.setApprovedBy(approvedBy);
            } catch (Exception e) {
                advance.setApprovedBy(null);
            }
        } else {
            advance.setApprovedBy(null);
        }

        StaffAdvance updated = staffAdvanceRepository.save(advance);
        log.info("Advance approved successfully");

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public StaffAdvanceResponse rejectAdvance(String id) {
        log.info("Rejecting advance with ID: {}", id);

        StaffAdvance advance = staffAdvanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance not found with ID: " + id));

        advance.setStatus(StaffAdvance.AdvanceStatus.REJECTED);

        StaffAdvance updated = staffAdvanceRepository.save(advance);
        log.info("Advance rejected successfully");

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public StaffAdvanceResponse repayAdvance(String id, BigDecimal repaymentAmount) {
        log.info("Repaying advance with ID: {}", id);

        StaffAdvance advance = staffAdvanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance not found with ID: " + id));

        if (advance.getStatus() != StaffAdvance.AdvanceStatus.APPROVED) {
            throw new RuntimeException("Only approved advances can be repaid");
        }
        if (repaymentAmount == null || repaymentAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Repayment amount must be greater than zero");
        }

        BigDecimal remaining = advance.getAmount().subtract(
                advance.getRepaymentAmount() != null ? advance.getRepaymentAmount() : BigDecimal.ZERO);

        if (repaymentAmount.compareTo(remaining) > 0) {
            throw new RuntimeException("Repayment amount exceeds remaining balance");
        }

        BigDecimal totalRepaid = (advance.getRepaymentAmount() != null ? advance.getRepaymentAmount() : BigDecimal.ZERO)
                .add(repaymentAmount);
        advance.setRepaymentAmount(totalRepaid);
        advance.setRepaymentDate(LocalDate.now());

        if (totalRepaid.compareTo(advance.getAmount()) >= 0) {
            advance.setStatus(StaffAdvance.AdvanceStatus.REPAID);
            advance.setRemainingAmount(BigDecimal.ZERO);
        } else {
            advance.setRemainingAmount(advance.getAmount().subtract(totalRepaid));
        }

        StaffAdvance updated = staffAdvanceRepository.save(advance);
        log.info("Advance repaid successfully");

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public BigDecimal getOutstandingAdvanceForStaff(String staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffId));

        BigDecimal outstanding = staffAdvanceRepository.getOutstandingAdvanceForStaff(staff);
        return outstanding != null ? outstanding : BigDecimal.ZERO;
    }

    @Override
    @Transactional
    public void deleteAdvance(String id) {
        log.info("Deleting advance with ID: {}", id);
        StaffAdvance advance = staffAdvanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Advance not found with ID: " + id));
        staffAdvanceRepository.delete(advance);
        log.info("Advance deleted successfully");
    }

    // ==================== REPORTS & SUMMARY ====================

    @Override
    @Transactional
    public List<StaffSalarySummary> getSalarySummary() {
        List<Staff> activeStaff = staffRepository.findByStatus(Staff.StaffStatus.ACTIVE);
        List<StaffSalarySummary> summaries = new ArrayList<>();

        for (Staff staff : activeStaff) {
            summaries.add(getStaffSalarySummary(staff.getId()));
        }

        return summaries;
    }

    @Override
    @Transactional
    public StaffSalarySummary getStaffSalarySummary(String staffId) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffId));

        List<StaffPayment> payments = staffPaymentRepository.findByStaffOrderByPaymentDateDesc(staff);

        BigDecimal totalPaid = payments.stream()
                .map(StaffPayment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDeductions = payments.stream()
                .map(p -> p.getDeductions() != null ? p.getDeductions() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalBonus = payments.stream()
                .map(p -> p.getBonus() != null ? p.getBonus() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal outstandingAdvance = getOutstandingAdvanceForStaff(staffId);

        int monthsWorked = 0;
        if (staff.getJoiningDate() != null) {
            LocalDate today = LocalDate.now();
            monthsWorked = (int) java.time.temporal.ChronoUnit.MONTHS.between(
                    staff.getJoiningDate().withDayOfMonth(1),
                    today.withDayOfMonth(1));
        }

        LocalDate lastPaymentDate = payments.isEmpty() ? null : payments.get(0).getPaymentDate();

        return StaffSalarySummary.builder()
                .staffId(staff.getStaffId())
                .staffName(staff.getFullName())
                .position(staff.getPosition())
                .baseSalary(staff.getBaseSalary())
                .totalPaid(totalPaid)
                .totalDeductions(totalDeductions)
                .totalBonus(totalBonus)
                .outstandingAdvance(outstandingAdvance)
                .monthsWorked(monthsWorked)
                .joiningDate(staff.getJoiningDate())
                .lastPaymentDate(lastPaymentDate)
                .build();
    }

    @Override
    @Transactional
    public Map<String, Object> getMonthlySalaryReport(int month, int year) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> staffDetails = new ArrayList<>();

        List<Staff> activeStaff = staffRepository.findByStatus(Staff.StaffStatus.ACTIVE);

        BigDecimal totalSalary = BigDecimal.ZERO;
        BigDecimal totalBonus = BigDecimal.ZERO;
        BigDecimal totalDeductions = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (Staff staff : activeStaff) {
            List<StaffPayment> payments = staffPaymentRepository.findByStaffAndPaymentMonthNumberAndPaymentYear(
                    staff, month, year);

            BigDecimal monthSalary = payments.stream()
                    .map(StaffPayment::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal monthBonus = payments.stream()
                    .map(p -> p.getBonus() != null ? p.getBonus() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal monthDeductions = payments.stream()
                    .map(p -> p.getDeductions() != null ? p.getDeductions() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal net = monthSalary.add(monthBonus).subtract(monthDeductions);

            Map<String, Object> staffData = new HashMap<>();
            staffData.put("staffId", staff.getStaffId());
            staffData.put("staffName", staff.getFullName());
            staffData.put("position", staff.getPosition());
            staffData.put("baseSalary", staff.getBaseSalary());
            staffData.put("paidAmount", monthSalary);
            staffData.put("bonus", monthBonus);
            staffData.put("deductions", monthDeductions);
            staffData.put("netAmount", net);
            staffData.put("hasPayment", !payments.isEmpty());

            staffDetails.add(staffData);

            totalSalary = totalSalary.add(monthSalary);
            totalBonus = totalBonus.add(monthBonus);
            totalDeductions = totalDeductions.add(monthDeductions);
            totalNet = totalNet.add(net);
        }

        report.put("month", month);
        report.put("year", year);
        report.put("monthName", LocalDate.of(year, month, 1).getMonth().name());
        report.put("totalStaff", activeStaff.size());
        report.put("totalSalary", totalSalary);
        report.put("totalBonus", totalBonus);
        report.put("totalDeductions", totalDeductions);
        report.put("totalNetAmount", totalNet);
        report.put("staffDetails", staffDetails);

        return report;
    }

    @Override
    @Transactional
    public Map<String, Object> getYearlySalaryReport(int year) {
        Map<String, Object> report = new HashMap<>();
        List<Map<String, Object>> monthlyData = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            Map<String, Object> monthlyReport = getMonthlySalaryReport(month, year);
            monthlyData.add(monthlyReport);
        }

        BigDecimal totalYearlySalary = monthlyData.stream()
                .map(m -> (BigDecimal) m.get("totalSalary"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalYearlyBonus = monthlyData.stream()
                .map(m -> (BigDecimal) m.get("totalBonus"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalYearlyDeductions = monthlyData.stream()
                .map(m -> (BigDecimal) m.get("totalDeductions"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalYearlyNet = monthlyData.stream()
                .map(m -> (BigDecimal) m.get("totalNetAmount"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        report.put("year", year);
        report.put("monthlyData", monthlyData);
        report.put("totalYearlySalary", totalYearlySalary);
        report.put("totalYearlyBonus", totalYearlyBonus);
        report.put("totalYearlyDeductions", totalYearlyDeductions);
        report.put("totalYearlyNetAmount", totalYearlyNet);

        return report;
    }

    // ==================== IMAGE MANAGEMENT ====================

    @Override
    @Transactional
    public StaffResponse uploadStaffImages(String staffId, List<MultipartFile> images, boolean setAsPrimary) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffId));

        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("At least one image file is required");
        }
        // Validate everything first so a bad file does not leave a partial upload
        // behind
        for (MultipartFile image : images) {
            fileUploadUtil.validateImageFile(image);
        }

        List<String> existingImages = staff.getImageUrls() != null ? new ArrayList<>(staff.getImageUrls())
                : new ArrayList<>();
        String lastUploaded = null;

        for (int i = 0; i < images.size(); i++) {
            MultipartFile image = images.get(i);
            try {
                String imageUrl = fileUploadUtil.uploadProfileImage(image, "staff",
                        staff.getStaffId() + "_" + i);
                existingImages.add(imageUrl);
                lastUploaded = imageUrl;

                if (staff.getPrimaryImageUrl() == null) {
                    staff.setPrimaryImageUrl(imageUrl);
                }
            } catch (Exception e) {
                log.error("Failed to upload image: {}", e.getMessage());
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        if (setAsPrimary && lastUploaded != null) {
            staff.setPrimaryImageUrl(lastUploaded);
        }

        staff.setImageUrls(existingImages);
        Staff updated = staffRepository.save(staff);

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public StaffResponse setPrimaryImage(String staffId, String imageUrl) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffId));

        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Image URL is required");
        }
        String url = imageUrl.trim();

        List<String> imageUrls = staff.getImageUrls() != null ? new ArrayList<>(staff.getImageUrls())
                : new ArrayList<>();
        if (!imageUrls.contains(url)) {
            // Not one of the uploaded files: accept it as an external image URL
            if (!FileUploadUtil.isValidImageReference(url)) {
                throw new IllegalArgumentException(
                        "Image must be an uploaded staff image or a valid http(s) image URL");
            }
            imageUrls.add(url);
            staff.setImageUrls(imageUrls);
        }

        staff.setPrimaryImageUrl(url);
        Staff updated = staffRepository.save(staff);

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public StaffResponse removeStaffImage(String staffId, String imageUrl) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff not found with ID: " + staffId));

        if (staff.getImageUrls() != null) {
            List<String> imageUrls = new ArrayList<>(staff.getImageUrls());
            imageUrls.remove(imageUrl);
            staff.setImageUrls(imageUrls);

            if (imageUrl.equals(staff.getPrimaryImageUrl()) && !imageUrls.isEmpty()) {
                staff.setPrimaryImageUrl(imageUrls.get(0));
            } else if (imageUrls.isEmpty()) {
                staff.setPrimaryImageUrl(null);
            }
        } else if (imageUrl.equals(staff.getPrimaryImageUrl())) {
            staff.setPrimaryImageUrl(null);
        }

        Staff updated = staffRepository.save(staff);

        fileUploadUtil.deleteStoredImageQuietly(imageUrl);

        return convertToResponse(updated);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Generates the next STFnn id from the highest numeric suffix in use. Ordering
     * by
     * the string column alone breaks after STF99 ("STF99" > "STF100"), so we
     * compare numerically.
     */
    @Override
    @Transactional
    public String generateStaffId() {
        int max = 0;
        for (Staff existing : staffRepository.findAll()) {
            String code = existing.getStaffId();
            if (code != null && code.startsWith("STF")) {
                try {
                    max = Math.max(max, Integer.parseInt(code.substring(3)));
                } catch (NumberFormatException ignored) {
                    // legacy / manual ids are skipped
                }
            }
        }
        return String.format("STF%02d", max + 1);
    }

    private void validateStaffRequest(StaffRequest request, boolean creating) {
        if (creating && (request.getFullName() == null || request.getFullName().isBlank())) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (creating && request.getBaseSalary() == null) {
            throw new IllegalArgumentException("Base salary is required");
        }
        if (request.getBaseSalary() != null && request.getBaseSalary().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base salary cannot be negative");
        }
        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()
                && !request.getPhoneNumber().trim().matches("^\\+?[0-9\\s-]{7,20}$")) {
            throw new IllegalArgumentException("Phone number format is invalid");
        }
    }

    private static Staff.EmploymentType parseEmploymentType(String value, Staff.EmploymentType fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Staff.EmploymentType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid employment type '" + value + "'. Allowed values: FULL_TIME, PART_TIME, CONTRACT");
        }
    }

    private static Staff.StaffStatus parseStatus(String value, Staff.StaffStatus fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        String normalized = value.trim().toUpperCase();
        // The React type uses INACTIVE; the closest backend state is ON_LEAVE
        if ("INACTIVE".equals(normalized)) {
            return Staff.StaffStatus.ON_LEAVE;
        }
        try {
            return Staff.StaffStatus.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid staff status '" + value + "'. Allowed values: ACTIVE, ON_LEAVE, LEFT");
        }
    }

    private void handleImageUploads(StaffRequest request, Staff staff) {
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (MultipartFile image : request.getImages()) {
                fileUploadUtil.validateImageFile(image);
            }

            List<String> imageUrls = staff.getImageUrls() != null ? new ArrayList<>(staff.getImageUrls())
                    : new ArrayList<>();
            int primaryIndex = 0;

            if (request.getPrimaryImageIndex() != null) {
                try {
                    primaryIndex = Integer.parseInt(request.getPrimaryImageIndex());
                } catch (NumberFormatException e) {
                    primaryIndex = 0;
                }
            }

            String staffKey = staff.getStaffId() != null ? staff.getStaffId()
                    : request.getFullName().replaceAll("\\s+", "_");

            for (int i = 0; i < request.getImages().size(); i++) {
                MultipartFile image = request.getImages().get(i);
                try {
                    String imageUrl = fileUploadUtil.uploadProfileImage(image, "staff", staffKey + "_" + i);
                    imageUrls.add(imageUrl);

                    if (i == primaryIndex) {
                        staff.setPrimaryImageUrl(imageUrl);
                    }
                } catch (Exception e) {
                    log.error("Failed to upload image: {}", e.getMessage());
                    throw new RuntimeException("Failed to upload image: " + e.getMessage());
                }
            }
            staff.setImageUrls(imageUrls);

            if (staff.getPrimaryImageUrl() == null && !imageUrls.isEmpty()) {
                staff.setPrimaryImageUrl(imageUrls.get(0));
            }
        }
    }

    private StaffResponse convertToResponse(Staff staff) {
        BigDecimal totalPaid = staffPaymentRepository.getTotalPaymentsForStaff(staff,
                LocalDate.of(1900, 1, 1), LocalDate.now());
        BigDecimal outstandingAdvance = staffAdvanceRepository.getOutstandingAdvanceForStaff(staff);

        return StaffResponse.builder()
                .id(staff.getId())
                .staffId(staff.getStaffId())
                .fullName(staff.getFullName())
                .position(staff.getPosition())
                .phoneNumber(staff.getPhoneNumber())
                .email(staff.getEmail())
                .address(staff.getAddress())
                .joiningDate(staff.getJoiningDate())
                .leavingDate(staff.getLeavingDate())
                .baseSalary(staff.getBaseSalary())
                .employmentType(staff.getEmploymentType() != null ? staff.getEmploymentType().name() : null)
                .status(staff.getStatus() != null ? staff.getStatus().name() : null)
                .emergencyContact(staff.getEmergencyContact())
                .emergencyPhone(staff.getEmergencyPhone())
                .bankName(staff.getBankName())
                .bankAccountNumber(staff.getBankAccountNumber())
                .ifscCode(staff.getIfscCode())
                .panNumber(staff.getPanNumber())
                .aadharNumber(staff.getAadharNumber())
                .notes(staff.getNotes())
                .imageUrls(staff.getImageUrls())
                .primaryImageUrl(staff.getPrimaryImageUrl())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .totalPaid(totalPaid != null ? totalPaid : BigDecimal.ZERO)
                .outstandingAdvance(outstandingAdvance != null ? outstandingAdvance : BigDecimal.ZERO)
                .build();
    }

    private StaffPaymentResponse convertToResponse(StaffPayment payment) {
        return StaffPaymentResponse.builder()
                .id(payment.getId())
                .staffId(payment.getStaff().getStaffId())
                .staffName(payment.getStaff().getFullName())
                .paymentDate(payment.getPaymentDate())
                .amount(payment.getAmount())
                .paymentMonth(payment.getPaymentMonth())
                .paymentYear(payment.getPaymentYear())
                .paymentMonthNumber(payment.getPaymentMonthNumber())
                .bonus(payment.getBonus())
                .deductions(payment.getDeductions())
                .netAmount(payment.getNetAmount())
                .paymentMethod(payment.getPaymentMethod())
                .transactionId(payment.getTransactionId())
                .referenceNumber(payment.getReferenceNumber())
                .remarks(payment.getRemarks())
                .enteredBy(payment.getEnteredBy() != null
                        ? payment.getEnteredBy().getFullName() + " (" + payment.getEnteredBy().getCandidateId() + ")"
                        : null)
                .createdAt(payment.getCreatedAt())
                .build();
    }

    private StaffAdvanceResponse convertToResponse(StaffAdvance advance) {
        return StaffAdvanceResponse.builder()
                .id(advance.getId())
                .staffId(advance.getStaff().getStaffId())
                .staffName(advance.getStaff().getFullName())
                .advanceDate(advance.getAdvanceDate())
                .amount(advance.getAmount())
                .reason(advance.getReason())
                .status(advance.getStatus() != null ? advance.getStatus().name() : null)
                .repaymentDate(advance.getRepaymentDate())
                .repaymentAmount(advance.getRepaymentAmount())
                .remainingAmount(advance.getRemainingAmount())
                .installmentMonths(advance.getInstallmentMonths())
                .monthlyDeduction(advance.getMonthlyDeduction())
                .remarks(advance.getRemarks())
                .approvedBy(advance.getApprovedBy() != null
                        ? advance.getApprovedBy().getFullName() + " (" + advance.getApprovedBy().getCandidateId() + ")"
                        : null)
                .enteredBy(advance.getEnteredBy() != null
                        ? advance.getEnteredBy().getFullName() + " (" + advance.getEnteredBy().getCandidateId() + ")"
                        : null)
                .createdAt(advance.getCreatedAt())
                .build();
    }
}
