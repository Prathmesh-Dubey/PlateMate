package com.mess.app.service;

import com.mess.app.dto.request.CollegeBillingRequest;
import com.mess.app.dto.response.CollegeBillingResponse;
import com.mess.app.dto.response.CollegeBillingSummaryResponse;

import java.util.List;

public interface CollegeBillingService {

    CollegeBillingResponse createCollegeBilling(CollegeBillingRequest request);

    CollegeBillingResponse updateCollegeBilling(String id, CollegeBillingRequest request);

    CollegeBillingResponse getCollegeBillingById(String id);

    List<CollegeBillingResponse> getAllCollegeBilling(String college, Integer month, Integer year);

    void deleteCollegeBilling(String id);

    CollegeBillingSummaryResponse getSummary(String college, Integer month, Integer year);

    List<String> getColleges();
}
