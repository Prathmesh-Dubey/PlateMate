package com.mess.app.service;

import com.mess.app.dto.request.StaffAdvanceRequest;
import com.mess.app.dto.request.StaffPaymentRequest;
import com.mess.app.dto.request.StaffRequest;
import com.mess.app.dto.response.StaffAdvanceResponse;
import com.mess.app.dto.response.StaffPaymentResponse;
import com.mess.app.dto.response.StaffResponse;
import com.mess.app.dto.response.StaffSalarySummary;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StaffService {

    // Staff CRUD
    StaffResponse createStaff(StaffRequest request);

    StaffResponse updateStaff(String id, StaffRequest request);

    StaffResponse getStaffById(String id);

    StaffResponse getStaffByStaffId(String staffId);

    List<StaffResponse> getAllStaff();

    List<StaffResponse> getActiveStaff();

    List<StaffResponse> getStaffByPosition(String position);

    void deleteStaff(String id);

    void toggleStaffStatus(String id);

    // Staff Payments
    StaffPaymentResponse createPayment(StaffPaymentRequest request);

    StaffPaymentResponse getPaymentById(String id);

    List<StaffPaymentResponse> getPaymentsByStaff(String staffId);

    List<StaffPaymentResponse> getPaymentsByStaffAndYear(String staffId, int year);

    List<StaffPaymentResponse> getPaymentsByDateRange(LocalDate startDate, LocalDate endDate);

    BigDecimal getTotalPaymentsByDateRange(LocalDate startDate, LocalDate endDate);

    void deletePayment(String id);

    // Staff Advances
    StaffAdvanceResponse createAdvance(StaffAdvanceRequest request);

    StaffAdvanceResponse updateAdvance(String id, StaffAdvanceRequest request);

    StaffAdvanceResponse getAdvanceById(String id);

    List<StaffAdvanceResponse> getAdvancesByStaff(String staffId);

    List<StaffAdvanceResponse> getPendingAdvances();

    StaffAdvanceResponse approveAdvance(String id, String approvedByCandidateId);

    StaffAdvanceResponse rejectAdvance(String id);

    StaffAdvanceResponse repayAdvance(String id, BigDecimal repaymentAmount);

    BigDecimal getOutstandingAdvanceForStaff(String staffId);

    void deleteAdvance(String id);

    // Reports & Summary
    List<StaffSalarySummary> getSalarySummary();

    StaffSalarySummary getStaffSalarySummary(String staffId);

    Map<String, Object> getMonthlySalaryReport(int month, int year);

    Map<String, Object> getYearlySalaryReport(int year);

    // Image Management

    /**
     * Uploads one or more images for the staff member.
     *
     * @param setAsPrimary when true the last uploaded image becomes the profile (primary) image
     */
    StaffResponse uploadStaffImages(String staffId, List<MultipartFile> images, boolean setAsPrimary);

    /**
     * Sets the profile (primary) image. Accepts an already uploaded image of this staff
     * member or an external http(s) image URL, which is added to the image list.
     */
    StaffResponse setPrimaryImage(String staffId, String imageUrl);

    StaffResponse removeStaffImage(String staffId, String imageUrl);

    // Auto Generate Staff ID
    String generateStaffId();

    List<StaffAdvanceResponse> getAllAdvances();
}
