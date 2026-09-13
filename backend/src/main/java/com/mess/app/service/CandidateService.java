package com.mess.app.service;

import com.mess.app.dto.request.CandidateRequest;
import com.mess.app.dto.response.CandidateResponse;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

public interface CandidateService {

    CandidateResponse createCandidate(CandidateRequest request);
    CandidateResponse updateCandidate(String id, CandidateRequest request);  // ✅ String
    CandidateResponse getCandidateById(String id);  // ✅ String
    CandidateResponse getCandidateByCandidateId(String candidateId);
    List<CandidateResponse> getAllCandidates();
    List<CandidateResponse> getActiveCandidates();
    List<CandidateResponse> getLeftCandidates();
    void deleteCandidate(String id);  // ✅ String
    long getActiveCount();
    long getLeftCount();
    long getTotalCount();
    CandidateResponse markAsLeft(String id, LocalDate leavingDate);  // ✅ String

    // Profile image
    CandidateResponse uploadProfileImage(String id, MultipartFile file);
    CandidateResponse setProfileImageUrl(String id, String imageUrl);
    CandidateResponse removeProfileImage(String id);
}
