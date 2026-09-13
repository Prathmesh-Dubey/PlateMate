package com.mess.app.service.impl;

import com.mess.app.dto.request.CandidateRequest;
import com.mess.app.dto.response.CandidateResponse;
import com.mess.app.entity.Candidate;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.CandidateRepository;
import com.mess.app.service.CandidateService;
import com.mess.app.util.FileUploadUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CandidateServiceImpl implements CandidateService {

    private final CandidateRepository candidateRepository;
    private final FileUploadUtil fileUploadUtil;

    @Override
    @Transactional
    public CandidateResponse createCandidate(CandidateRequest request) {
        log.info("Creating new candidate: {}", request.getFullName());

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required");
        }
        if (request.getMonthlyRate() == null) {
            throw new IllegalArgumentException("Monthly rate is required");
        }

        Candidate candidate = Candidate.builder()
                .fullName(request.getFullName().trim())
                .phoneNumber(request.getPhoneNumber())
                .email(request.getEmail())
                .address(request.getAddress())
                .joiningDate(request.getJoiningDate() != null ? request.getJoiningDate() : LocalDate.now())
                .leavingDate(request.getLeavingDate())
                .monthlyRate(request.getMonthlyRate())
                .status(parseStatus(request.getStatus(), Candidate.Status.ACTIVE))
                .emergencyContact(request.getEmergencyContact())
                .emergencyPhone(request.getEmergencyPhone())
                .notes(request.getNotes())
                .profileImageUrl(normalizeImageReference(request.getProfileImageUrl()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        candidate.setCandidateId(generateCandidateId());

        Candidate saved = candidateRepository.save(candidate);
        log.info("Candidate created with ID: {}", saved.getCandidateId());

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public CandidateResponse updateCandidate(String id, CandidateRequest request) {
        log.info("Updating candidate with ID: {}", id);

        Candidate candidate = findCandidate(id);

        if (request.getFullName() != null && !request.getFullName().trim().isEmpty()) {
            candidate.setFullName(request.getFullName().trim());
        }
        candidate.setPhoneNumber(request.getPhoneNumber());
        candidate.setEmail(request.getEmail());
        candidate.setAddress(request.getAddress());
        if (request.getJoiningDate() != null) {
            candidate.setJoiningDate(request.getJoiningDate());
        }
        candidate.setLeavingDate(request.getLeavingDate());
        if (request.getMonthlyRate() != null) {
            candidate.setMonthlyRate(request.getMonthlyRate());
        }
        if (request.getStatus() != null) {
            candidate.setStatus(parseStatus(request.getStatus(), candidate.getStatus()));
        }
        candidate.setEmergencyContact(request.getEmergencyContact());
        candidate.setEmergencyPhone(request.getEmergencyPhone());
        candidate.setNotes(request.getNotes());

        // Picture is only touched when the client sends the field (null = keep, "" = clear)
        if (request.getProfileImageUrl() != null) {
            String previous = candidate.getProfileImageUrl();
            candidate.setProfileImageUrl(normalizeImageReference(request.getProfileImageUrl()));
            if (previous != null && !previous.equals(candidate.getProfileImageUrl())) {
                fileUploadUtil.deleteStoredImageQuietly(previous);
            }
        }
        candidate.setUpdatedAt(LocalDateTime.now());

        Candidate updated = candidateRepository.save(candidate);
        log.info("Candidate updated: {}", updated.getCandidateId());

        return convertToResponse(updated);
    }

    @Override
    @Transactional
    public CandidateResponse getCandidateById(String id) {
        return convertToResponse(findCandidate(id));
    }

    @Override
    @Transactional
    public CandidateResponse getCandidateByCandidateId(String candidateId) {
        Candidate candidate = candidateRepository.findByCandidateId(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + candidateId));
        return convertToResponse(candidate);
    }

    @Override
    @Transactional
    public List<CandidateResponse> getAllCandidates() {
        return candidateRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<CandidateResponse> getActiveCandidates() {
        return candidateRepository.findByStatus(Candidate.Status.ACTIVE).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<CandidateResponse> getLeftCandidates() {
        return candidateRepository.findByStatus(Candidate.Status.LEFT).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCandidate(String id) {
        log.info("Permanently deleting candidate with ID: {}", id);
        Candidate candidate = findCandidate(id);

        candidateRepository.delete(candidate);
        candidateRepository.flush();
        fileUploadUtil.deleteStoredImageQuietly(candidate.getProfileImageUrl());

        log.info("Candidate permanently deleted: {} ({})", candidate.getFullName(), candidate.getCandidateId());
    }

    @Override
    @Transactional
    public long getActiveCount() {
        return candidateRepository.countByStatus(Candidate.Status.ACTIVE);
    }

    @Override
    @Transactional
    public long getLeftCount() {
        return candidateRepository.countByStatus(Candidate.Status.LEFT);
    }

    @Override
    @Transactional
    public long getTotalCount() {
        return candidateRepository.count();
    }

    @Override
    @Transactional
    public CandidateResponse markAsLeft(String id, LocalDate leavingDate) {
        log.info("Marking candidate as left: {}", id);

        Candidate candidate = findCandidate(id);

        candidate.setStatus(Candidate.Status.LEFT);
        candidate.setLeavingDate(leavingDate != null ? leavingDate : LocalDate.now());
        candidate.setUpdatedAt(LocalDateTime.now());

        Candidate updated = candidateRepository.save(candidate);
        log.info("Candidate marked as left: {}", updated.getCandidateId());

        return convertToResponse(updated);
    }

    // ==================== PROFILE IMAGE ====================

    @Override
    @Transactional
    public CandidateResponse uploadProfileImage(String id, MultipartFile file) {
        Candidate candidate = findCandidate(id);
        String previous = candidate.getProfileImageUrl();

        String imageUrl;
        try {
            imageUrl = fileUploadUtil.uploadProfileImage(file, "candidate", candidate.getCandidateId());
        } catch (IOException e) {
            log.error("Failed to store profile image for candidate {}", candidate.getCandidateId(), e);
            throw new RuntimeException("Failed to store profile image: " + e.getMessage());
        }

        candidate.setProfileImageUrl(imageUrl);
        candidate.setUpdatedAt(LocalDateTime.now());
        Candidate saved = candidateRepository.save(candidate);
        fileUploadUtil.deleteStoredImageQuietly(previous);
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public CandidateResponse setProfileImageUrl(String id, String imageUrl) {
        Candidate candidate = findCandidate(id);
        String previous = candidate.getProfileImageUrl();
        candidate.setProfileImageUrl(normalizeImageReference(imageUrl));
        candidate.setUpdatedAt(LocalDateTime.now());
        Candidate saved = candidateRepository.save(candidate);
        if (previous != null && !previous.equals(saved.getProfileImageUrl())) {
            fileUploadUtil.deleteStoredImageQuietly(previous);
        }
        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public CandidateResponse removeProfileImage(String id) {
        return setProfileImageUrl(id, "");
    }

    // ==================== HELPERS ====================

    private Candidate findCandidate(String id) {
        return candidateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found with ID: " + id));
    }

    private static Candidate.Status parseStatus(String value, Candidate.Status fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        try {
            return Candidate.Status.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status '" + value + "'. Allowed values: ACTIVE, INACTIVE, LEFT");
        }
    }

    /** Empty -> null (clears the picture); otherwise must be an http(s) URL or an /uploads/ path. */
    private static String normalizeImageReference(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (!FileUploadUtil.isValidImageReference(trimmed)) {
            throw new IllegalArgumentException("Profile image must be a valid http(s) image URL");
        }
        return trimmed;
    }

    private String generateCandidateId() {
        int max = 0;
        for (Candidate existing : candidateRepository.findAll()) {
            String code = existing.getCandidateId();
            if (code != null && code.startsWith("CAD")) {
                try {
                    max = Math.max(max, Integer.parseInt(code.substring(3)));
                } catch (NumberFormatException ignored) {
                    // non numeric legacy ids are skipped
                }
            }
        }
        return String.format("CAD%02d", max + 1);
    }

    private CandidateResponse convertToResponse(Candidate candidate) {
        return CandidateResponse.builder()
                .id(candidate.getId().toString())
                .candidateId(candidate.getCandidateId())
                .fullName(candidate.getFullName())
                .phoneNumber(candidate.getPhoneNumber())
                .email(candidate.getEmail())
                .address(candidate.getAddress())
                .joiningDate(candidate.getJoiningDate())
                .leavingDate(candidate.getLeavingDate())
                .monthlyRate(candidate.getMonthlyRate())
                .status(candidate.getStatus() != null ? candidate.getStatus().name() : null)
                .emergencyContact(candidate.getEmergencyContact())
                .emergencyPhone(candidate.getEmergencyPhone())
                .notes(candidate.getNotes())
                .profileImageUrl(candidate.getProfileImageUrl())
                .createdAt(candidate.getCreatedAt())
                .updatedAt(candidate.getUpdatedAt())
                .build();
    }
}
