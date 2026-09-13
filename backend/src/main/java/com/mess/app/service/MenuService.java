package com.mess.app.service;

import com.mess.app.dto.request.MenuRequest;
import com.mess.app.dto.request.MenuItemRequest;
import com.mess.app.dto.response.MenuResponse;
import com.mess.app.dto.response.WeeklyMenuResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MenuService {

    // Menu CRUD
    MenuResponse createMenu(MenuRequest request);

    MenuResponse updateMenu(String id, MenuRequest request);

    MenuResponse getMenuById(String id);

    MenuResponse getMenuByDateAndMealType(LocalDate date, String mealType);

    List<MenuResponse> getMenusByDate(LocalDate date);

    List<MenuResponse> getMenusByDateRange(LocalDate startDate, LocalDate endDate);

    List<MenuResponse> getTodayMenu();

    WeeklyMenuResponse getWeeklyMenu(LocalDate startDate);

    List<MenuResponse> getMonthlyMenu(int month, int year);

    void deleteMenu(String id);

    void toggleMenuStatus(String id);

    // Menu Items
    MenuResponse addMenuItem(String menuId, MenuItemRequest request);

    MenuResponse updateMenuItem(String menuId, String itemId, MenuItemRequest request);

    MenuResponse removeMenuItem(String menuId, String itemId);

    MenuResponse updateMenuItemAvailability(String menuId, String itemId, boolean available);

    // Reports & Summary
    Map<String, Object> getMenuSummary(LocalDate startDate, LocalDate endDate);

    List<Map<String, Object>> getPopularMenuItems(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getWeeklyMenuPlan(LocalDate startDate);

    // Copy Menu
    MenuResponse copyMenu(String sourceMenuId, LocalDate targetDate, String mealType);

    void copyWeeklyMenu(LocalDate sourceWeekStart, LocalDate targetWeekStart);
}