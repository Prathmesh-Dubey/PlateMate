package com.mess.app.service;

import com.mess.app.dto.request.DepartmentRequest;
import com.mess.app.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {

    DepartmentResponse createDepartment(DepartmentRequest request);

    DepartmentResponse updateDepartment(String id, DepartmentRequest request);

    DepartmentResponse getDepartmentById(String id);

    DepartmentResponse getDepartmentByCode(String code);

    List<DepartmentResponse> getAllDepartments();

    List<DepartmentResponse> getActiveDepartments();

    DepartmentResponse toggleActive(String id);

    void deleteDepartment(String id);
}