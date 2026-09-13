package com.mess.app.controller;

import com.mess.app.dto.request.MenuItemRequest;
import com.mess.app.dto.request.MenuRequest;
import com.mess.app.dto.response.ApiResponse;
import com.mess.app.dto.response.MenuResponse;
import com.mess.app.dto.response.WeeklyMenuResponse;
import com.mess.app.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class MenuController {

    private final MenuService menuService;

    // ==================== MENU CRUD ====================

    @PostMapping("/")
    public ResponseEntity<ApiResponse<MenuResponse>> createMenu(
            @Valid @RequestBody MenuRequest request) {
        MenuResponse response = menuService.createMenu(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Menu created successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenu(
            @PathVariable String id,
            @Valid @RequestBody MenuRequest request) {
        MenuResponse response = menuService.updateMenu(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Menu updated successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuResponse>> getMenuById(@PathVariable String id) {
        MenuResponse response = menuService.getMenuById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Menu retrieved successfully"));
    }

    @GetMapping("/date/{date}/meal/{mealType}")
    public ResponseEntity<ApiResponse<MenuResponse>> getMenuByDateAndMealType(
            @PathVariable String date,
            @PathVariable String mealType) {
        LocalDate menuDate = LocalDate.parse(date);
        MenuResponse response = menuService.getMenuByDateAndMealType(menuDate, mealType);
        return ResponseEntity.ok(ApiResponse.success(response, "Menu retrieved successfully"));
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenusByDate(
            @PathVariable String date) {
        LocalDate menuDate = LocalDate.parse(date);
        List<MenuResponse> responses = menuService.getMenusByDate(menuDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Menus retrieved successfully"));
    }

    @GetMapping("/range")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenusByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<MenuResponse> responses = menuService.getMenusByDateRange(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(responses, "Menus retrieved successfully"));
    }

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getTodayMenu() {
        List<MenuResponse> responses = menuService.getTodayMenu();
        return ResponseEntity.ok(ApiResponse.success(responses, "Today's menu retrieved successfully"));
    }

    @GetMapping("/weekly")
    public ResponseEntity<ApiResponse<WeeklyMenuResponse>> getWeeklyMenu(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        WeeklyMenuResponse response = menuService.getWeeklyMenu(startDate);
        return ResponseEntity.ok(ApiResponse.success(response, "Weekly menu retrieved successfully"));
    }

    @GetMapping("/monthly")
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMonthlyMenu(
            @RequestParam int month,
            @RequestParam int year) {
        List<MenuResponse> responses = menuService.getMonthlyMenu(month, year);
        return ResponseEntity.ok(ApiResponse.success(responses, "Monthly menu retrieved successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMenu(@PathVariable String id) {
        menuService.deleteMenu(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Menu deleted successfully"));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<Void>> toggleMenuStatus(@PathVariable String id) {
        menuService.toggleMenuStatus(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Menu status toggled successfully"));
    }

    // ==================== MENU ITEMS ====================

    @PostMapping("/{menuId}/items")
    public ResponseEntity<ApiResponse<MenuResponse>> addMenuItem(
            @PathVariable String menuId,
            @Valid @RequestBody MenuItemRequest request) {
        MenuResponse response = menuService.addMenuItem(menuId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Menu item added successfully"));
    }

    @PutMapping("/{menuId}/items/{itemId}")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenuItem(
            @PathVariable String menuId,
            @PathVariable String itemId,
            @Valid @RequestBody MenuItemRequest request) {
        MenuResponse response = menuService.updateMenuItem(menuId, itemId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Menu item updated successfully"));
    }

    @DeleteMapping("/{menuId}/items/{itemId}")
    public ResponseEntity<ApiResponse<MenuResponse>> removeMenuItem(
            @PathVariable String menuId,
            @PathVariable String itemId) {
        MenuResponse response = menuService.removeMenuItem(menuId, itemId);
        return ResponseEntity.ok(ApiResponse.success(response, "Menu item removed successfully"));
    }

    @PatchMapping("/{menuId}/items/{itemId}/availability")
    public ResponseEntity<ApiResponse<MenuResponse>> updateMenuItemAvailability(
            @PathVariable String menuId,
            @PathVariable String itemId,
            @RequestParam boolean available) {
        MenuResponse response = menuService.updateMenuItemAvailability(menuId, itemId, available);
        return ResponseEntity.ok(ApiResponse.success(response, "Menu item availability updated successfully"));
    }

    // ==================== REPORTS & SUMMARY ====================

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMenuSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Map<String, Object> summary = menuService.getMenuSummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Menu summary retrieved successfully"));
    }

    @GetMapping("/popular-items")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getPopularMenuItems(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<Map<String, Object>> popularItems = menuService.getPopularMenuItems(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(popularItems, "Popular menu items retrieved successfully"));
    }

    @GetMapping("/weekly-plan")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getWeeklyMenuPlan(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {
        Map<String, Object> plan = menuService.getWeeklyMenuPlan(startDate);
        return ResponseEntity.ok(ApiResponse.success(plan, "Weekly menu plan retrieved successfully"));
    }

    // ==================== COPY MENU ====================

    @PostMapping("/copy")
    public ResponseEntity<ApiResponse<MenuResponse>> copyMenu(
            @RequestParam String sourceMenuId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate,
            @RequestParam String mealType) {
        MenuResponse response = menuService.copyMenu(sourceMenuId, targetDate, mealType);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Menu copied successfully"));
    }

    @PostMapping("/copy-weekly")
    public ResponseEntity<ApiResponse<Void>> copyWeeklyMenu(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate sourceWeekStart,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetWeekStart) {
        menuService.copyWeeklyMenu(sourceWeekStart, targetWeekStart);
        return ResponseEntity.ok(ApiResponse.success(null, "Weekly menu copied successfully"));
    }
}