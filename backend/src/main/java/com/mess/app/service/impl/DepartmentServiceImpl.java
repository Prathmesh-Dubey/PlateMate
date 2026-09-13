package com.mess.app.service.impl;

import com.mess.app.dto.request.DepartmentRequest;
import com.mess.app.dto.response.DepartmentResponse;
import com.mess.app.entity.Department;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.DepartmentRepository;
import com.mess.app.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentRepository departmentRepository;

    @Override
    @Transactional
    public DepartmentResponse createDepartment(DepartmentRequest request) {
        String code = normalizeCode(request.getCode());

        if (departmentRepository.existsByCodeIgnoreCase(code)) {
            throw new IllegalArgumentException("Department code already exists: " + code);
        }

        Department department = Department.builder()
                .code(code)
                .name(request.getName().trim())
                .headName(trimOrNull(request.getHeadName()))
                .contactEmail(trimOrNull(request.getContactEmail()))
                .contactPhone(trimOrNull(request.getContactPhone()))
                .active(request.getActive() == null || request.getActive())
                .remarks(trimOrNull(request.getRemarks()))
                .build();

        Department saved = departmentRepository.save(department);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public DepartmentResponse updateDepartment(String id, DepartmentRequest request) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));

        if (request.getCode() != null && !request.getCode().isBlank()) {
            String newCode = normalizeCode(request.getCode());
            if (!newCode.equalsIgnoreCase(department.getCode())
                    && departmentRepository.existsByCodeIgnoreCase(newCode)) {
                throw new IllegalArgumentException("Department code already exists: " + newCode);
            }
            department.setCode(newCode);
        }

        if (request.getName() != null && !request.getName().isBlank()) {
            department.setName(request.getName().trim());
        }
        if (request.getHeadName() != null) {
            department.setHeadName(trimOrNull(request.getHeadName()));
        }
        if (request.getContactEmail() != null) {
            department.setContactEmail(trimOrNull(request.getContactEmail()));
        }
        if (request.getContactPhone() != null) {
            department.setContactPhone(trimOrNull(request.getContactPhone()));
        }
        if (request.getActive() != null) {
            department.setActive(request.getActive());
        }
        if (request.getRemarks() != null) {
            department.setRemarks(trimOrNull(request.getRemarks()));
        }

        Department saved = departmentRepository.save(department);
        return toResponse(saved);
    }

    @Override
    public DepartmentResponse getDepartmentById(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        return toResponse(department);
    }

    @Override
    public DepartmentResponse getDepartmentByCode(String code) {
        Department department = departmentRepository.findByCodeIgnoreCase(normalizeCode(code))
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + code));
        return toResponse(department);
    }

    @Override
    public List<DepartmentResponse> getAllDepartments() {
        return departmentRepository.findAllByOrderByCodeAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<DepartmentResponse> getActiveDepartments() {
        return departmentRepository.findByActiveTrueOrderByCodeAsc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DepartmentResponse toggleActive(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        department.setActive(!Boolean.TRUE.equals(department.getActive()));
        Department saved = departmentRepository.save(department);
        return toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteDepartment(String id) {
        Department department = departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + id));
        departmentRepository.delete(department);
    }

    // ==================== HELPERS ====================

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase();
    }

    private String trimOrNull(String value) {
        if (value == null)
            return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private DepartmentResponse toResponse(Department d) {
        return DepartmentResponse.builder()
                .id(d.getId())
                .code(d.getCode())
                .name(d.getName())
                .headName(d.getHeadName())
                .contactEmail(d.getContactEmail())
                .contactPhone(d.getContactPhone())
                .active(d.getActive())
                .remarks(d.getRemarks())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
}