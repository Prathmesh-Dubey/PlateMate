package com.mess.app.controller;

import com.mess.app.dto.request.DepartmentRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.DepartmentResponse;
import com.mess.app.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class DepartmentController {

    private final DepartmentService departmentService;

    // ==================== CREATE ====================

    @PostMapping("/")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Department created successfully"));
    }

    // ==================== READ ====================

    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        List<DepartmentResponse> responses = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success(responses, "Departments retrieved successfully"));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getActiveDepartments() {
        List<DepartmentResponse> responses = departmentService.getActiveDepartments();
        return ResponseEntity.ok(ApiResponse.success(responses, "Active departments retrieved successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @PathVariable String id) {
        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Department retrieved successfully"));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentByCode(
            @PathVariable String code) {
        DepartmentResponse response = departmentService.getDepartmentByCode(code);
        return ResponseEntity.ok(ApiResponse.success(response, "Department retrieved successfully"));
    }

    // ==================== UPDATE ====================

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable String id,
            @Valid @RequestBody DepartmentRequest request) {
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Department updated successfully"));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<DepartmentResponse>> toggleActive(
            @PathVariable String id) {
        DepartmentResponse response = departmentService.toggleActive(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Department status toggled successfully"));
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable String id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Department deleted successfully"));
    }
}