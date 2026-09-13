package com.mess.app.service.impl;

import com.mess.app.dto.request.MenuItemRequest;
import com.mess.app.dto.request.MenuRequest;
import com.mess.app.dto.response.MenuResponse;
import com.mess.app.dto.response.WeeklyMenuResponse;
import com.mess.app.entity.Candidate;
import com.mess.app.entity.Menu;
import com.mess.app.entity.Menu.MealType;
import com.mess.app.entity.MenuItem;
import com.mess.app.exception.ResourceNotFoundException;
import com.mess.app.repository.CandidateRepository;
import com.mess.app.repository.MenuItemRepository;
import com.mess.app.repository.MenuRepository;
import com.mess.app.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuServiceImpl implements MenuService {

    private final MenuRepository menuRepository;
    private final MenuItemRepository menuItemRepository;
    private final CandidateRepository candidateRepository;

    // ==================== MENU CRUD ====================

    @Override
    @Transactional
    public MenuResponse createMenu(MenuRequest request) {
        log.info("Creating new menu for date: {}, meal type: {}",
                request.getMenuDate(), request.getMealType());

        // Check if menu already exists for this date and meal type
        Optional<Menu> existing = menuRepository.findByMenuDateAndMealType(
                request.getMenuDate(), MealType.valueOf(request.getMealType().toUpperCase()));

        if (existing.isPresent()) {
            throw new RuntimeException("Menu already exists for this date and meal type");
        }

        Candidate createdBy = null;
        if (request.getCreatedByCandidateId() != null) {
            createdBy = candidateRepository.findById(request.getCreatedByCandidateId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Candidate not found with ID: " + request.getCreatedByCandidateId()));
        }

        Menu menu = Menu.builder()
                .menuDate(request.getMenuDate() != null ? request.getMenuDate() : LocalDate.now())
                .mealType(MealType.valueOf(request.getMealType().toUpperCase()))
                .menuName(request.getMenuName())
                .description(request.getDescription())
                .isActive(true)
                .isSpecial(request.isSpecial())
                .specialRemarks(request.getSpecialRemarks())
                .createdBy(createdBy)
                .items(new ArrayList<>())
                .build();

        // Add menu items
        if (request.getItems() != null) {
            for (MenuRequest.MenuItemRequest itemReq : request.getItems()) {
                MenuItem item = MenuItem.builder()
                        .menu(menu)
                        .itemName(itemReq.getItemName())
                        .description(itemReq.getDescription())
                        .price(itemReq.getPrice())
                        .category(itemReq.getCategory())
                        .isVegetarian(itemReq.isVegetarian())
                        .isAvailable(itemReq.isAvailable())
                        .preparationTime(itemReq.getPreparationTime())
                        .servingSize(itemReq.getServingSize())
                        .imageUrl(itemReq.getImageUrl())
                        .displayOrder(itemReq.getDisplayOrder() != null ? itemReq.getDisplayOrder() : 0)
                        .remarks(itemReq.getRemarks())
                        .build();
                menu.getItems().add(item);
            }
        }

        Menu saved = menuRepository.save(menu);
        log.info("Menu created with ID: {}", saved.getId());

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public MenuResponse updateMenu(String id, MenuRequest request) {
        log.info("Updating menu with ID: {}", id);

        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + id));

        menu.setMenuDate(request.getMenuDate());
        menu.setMealType(MealType.valueOf(request.getMealType().toUpperCase()));
        menu.setMenuName(request.getMenuName());
        menu.setDescription(request.getDescription());
        menu.setSpecial(request.isSpecial());
        menu.setSpecialRemarks(request.getSpecialRemarks());

        // Update items - delete existing and add new
        menuItemRepository.deleteByMenuId(menu.getId());

        List<MenuItem> items = new ArrayList<>();
        if (request.getItems() != null) {
            for (MenuRequest.MenuItemRequest itemReq : request.getItems()) {
                MenuItem item = MenuItem.builder()
                        .menu(menu)
                        .itemName(itemReq.getItemName())
                        .description(itemReq.getDescription())
                        .price(itemReq.getPrice())
                        .category(itemReq.getCategory())
                        .isVegetarian(itemReq.isVegetarian())
                        .isAvailable(itemReq.isAvailable())
                        .preparationTime(itemReq.getPreparationTime())
                        .servingSize(itemReq.getServingSize())
                        .imageUrl(itemReq.getImageUrl())
                        .displayOrder(itemReq.getDisplayOrder() != null ? itemReq.getDisplayOrder() : 0)
                        .remarks(itemReq.getRemarks())
                        .build();
                items.add(item);
            }
        }
        menu.setItems(items);

        Menu saved = menuRepository.save(menu);
        log.info("Menu updated successfully");

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public MenuResponse getMenuById(String id) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + id));
        return convertToResponse(menu);
    }

    @Override
    @Transactional
    public MenuResponse getMenuByDateAndMealType(LocalDate date, String mealType) {
        Menu menu = menuRepository.findByMenuDateAndMealType(
                date, MealType.valueOf(mealType.toUpperCase()))
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Menu not found for date %s and meal type %s", date, mealType)));
        return convertToResponse(menu);
    }

    @Override
    @Transactional
    public List<MenuResponse> getMenusByDate(LocalDate date) {
        return menuRepository.findByMenuDate(date).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<MenuResponse> getMenusByDateRange(LocalDate startDate, LocalDate endDate) {
        return menuRepository.findByMenuDateBetween(startDate, endDate).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public List<MenuResponse> getTodayMenu() {
        return menuRepository.findTodayActiveMenu().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public WeeklyMenuResponse getWeeklyMenu(LocalDate startDate) {
        log.info("Getting weekly menu starting from: {}", startDate);

        LocalDate weekStart = startDate.with(DayOfWeek.MONDAY);
        LocalDate weekEnd = weekStart.plusDays(6);

        List<Menu> menus = menuRepository.findWeeklyMenu(weekStart, weekEnd);

        Map<LocalDate, WeeklyMenuResponse.DayMenu> dailyMenu = new LinkedHashMap<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = weekStart.plusDays(i);
            String dayName = date.getDayOfWeek().name();

            List<MenuResponse> dayMeals = menus.stream()
                    .filter(m -> m.getMenuDate().equals(date))
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            dailyMenu.put(date, WeeklyMenuResponse.DayMenu.builder()
                    .date(date)
                    .dayName(dayName)
                    .meals(dayMeals)
                    .build());
        }

        return WeeklyMenuResponse.builder()
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .dailyMenu(dailyMenu)
                .build();
    }

    @Override
    @Transactional
    public List<MenuResponse> getMonthlyMenu(int month, int year) {
        return menuRepository.findMonthlyMenu(month, year).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteMenu(String id) {
        log.info("Deleting menu with ID: {}", id);
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + id));
        menuItemRepository.deleteByMenuId(id);
        menuRepository.delete(menu);
        log.info("Menu deleted successfully");
    }

    @Override
    @Transactional
    public void toggleMenuStatus(String id) {
        log.info("Toggling menu status for ID: {}", id);
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + id));
        menu.setActive(!menu.isActive());
        menuRepository.save(menu);
        log.info("Menu status toggled to: {}", menu.isActive());
    }

    // ==================== MENU ITEMS ====================

    @Override
    @Transactional
    public MenuResponse addMenuItem(String menuId, MenuItemRequest request) {
        log.info("Adding item to menu: {}", menuId);

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + menuId));

        MenuItem item = MenuItem.builder()
                .menu(menu)
                .itemName(request.getItemName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(request.getCategory())
                .isVegetarian(request.isVegetarian())
                .isAvailable(request.isAvailable())
                .preparationTime(request.getPreparationTime())
                .servingSize(request.getServingSize())
                .imageUrl(request.getImageUrl())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : menu.getItems().size())
                .remarks(request.getRemarks())
                .build();

        menuItemRepository.save(item);
        log.info("Item added to menu successfully");

        return convertToResponse(menu);
    }

    @Override
    @Transactional
    public MenuResponse updateMenuItem(String menuId, String itemId, MenuItemRequest request) {
        log.info("Updating menu item: {}", itemId);

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + itemId));

        if (!item.getMenu().getId().equals(menuId)) {
            throw new RuntimeException("Item does not belong to this menu");
        }

        item.setItemName(request.getItemName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        item.setCategory(request.getCategory());
        item.setVegetarian(request.isVegetarian());
        item.setAvailable(request.isAvailable());
        item.setPreparationTime(request.getPreparationTime());
        item.setServingSize(request.getServingSize());
        item.setImageUrl(request.getImageUrl());
        item.setDisplayOrder(request.getDisplayOrder());
        item.setRemarks(request.getRemarks());

        menuItemRepository.save(item);
        log.info("Menu item updated successfully");

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + menuId));
        return convertToResponse(menu);
    }

    @Override
    @Transactional
    public MenuResponse removeMenuItem(String menuId, String itemId) {
        log.info("Removing menu item: {}", itemId);

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + itemId));

        if (!item.getMenu().getId().equals(menuId)) {
            throw new RuntimeException("Item does not belong to this menu");
        }

        menuItemRepository.delete(item);
        log.info("Menu item removed successfully");

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + menuId));
        return convertToResponse(menu);
    }

    @Override
    @Transactional
    public MenuResponse updateMenuItemAvailability(String menuId, String itemId, boolean available) {
        log.info("Updating availability for menu item: {} to {}", itemId, available);

        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with ID: " + itemId));

        if (!item.getMenu().getId().equals(menuId)) {
            throw new RuntimeException("Item does not belong to this menu");
        }

        item.setAvailable(available);
        menuItemRepository.save(item);
        log.info("Menu item availability updated successfully");

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new ResourceNotFoundException("Menu not found with ID: " + menuId));
        return convertToResponse(menu);
    }

    // ==================== REPORTS & SUMMARY ====================

    @Override
    @Transactional
    public Map<String, Object> getMenuSummary(LocalDate startDate, LocalDate endDate) {
        Map<String, Object> summary = new HashMap<>();

        List<Menu> menus = menuRepository.findByMenuDateBetween(startDate, endDate);

        int totalMenus = menus.size();
        int totalBreakfast = (int) menus.stream()
                .filter(m -> m.getMealType() == MealType.BREAKFAST).count();
        int totalLunch = (int) menus.stream()
                .filter(m -> m.getMealType() == MealType.LUNCH).count();
        int totalDinner = (int) menus.stream()
                .filter(m -> m.getMealType() == MealType.DINNER).count();

        long totalItems = menus.stream()
                .mapToLong(m -> m.getItems().size()).sum();

        Map<String, Long> categoryWiseCount = menus.stream()
                .flatMap(m -> m.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getCategory() != null ? item.getCategory() : "Uncategorized",
                        Collectors.counting()));

        BigDecimal averagePrice = menus.stream()
                .flatMap(m -> m.getItems().stream())
                .map(MenuItem::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(totalItems > 0 ? totalItems : 1), 2, BigDecimal.ROUND_HALF_UP);

        List<MenuResponse> recentMenus = menus.stream()
                .sorted((m1, m2) -> m2.getMenuDate().compareTo(m1.getMenuDate()))
                .limit(5)
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        summary.put("totalMenus", totalMenus);
        summary.put("totalBreakfast", totalBreakfast);
        summary.put("totalLunch", totalLunch);
        summary.put("totalDinner", totalDinner);
        summary.put("totalItems", totalItems);
        summary.put("averagePricePerItem", averagePrice);
        summary.put("categoryWiseCount", categoryWiseCount);
        summary.put("recentMenus", recentMenus);
        summary.put("startDate", startDate);
        summary.put("endDate", endDate);

        return summary;
    }

    @Override
    @Transactional
    public List<Map<String, Object>> getPopularMenuItems(LocalDate startDate, LocalDate endDate) {
        List<MenuItem> items = menuItemRepository.findItemsByDateRange(startDate, endDate);

        Map<String, Long> itemFrequency = items.stream()
                .collect(Collectors.groupingBy(
                        MenuItem::getItemName,
                        Collectors.counting()));

        return itemFrequency.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(entry -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("itemName", entry.getKey());
                    result.put("frequency", entry.getValue());

                    // Get average price for this item
                    OptionalDouble avgPrice = items.stream()
                            .filter(i -> i.getItemName().equals(entry.getKey()))
                            .mapToDouble(i -> i.getPrice().doubleValue())
                            .average();
                    result.put("averagePrice", avgPrice.orElse(0.0));

                    return result;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> getWeeklyMenuPlan(LocalDate startDate) {
        Map<String, Object> plan = new HashMap<>();

        LocalDate weekStart = startDate.with(DayOfWeek.MONDAY);

        List<Menu> menus = menuRepository.findWeeklyMenu(weekStart, weekStart.plusDays(6));

        Map<String, List<MenuResponse>> mealTypeMap = new HashMap<>();
        mealTypeMap.put("BREAKFAST", new ArrayList<>());
        mealTypeMap.put("LUNCH", new ArrayList<>());
        mealTypeMap.put("DINNER", new ArrayList<>());

        for (Menu menu : menus) {
            mealTypeMap.get(menu.getMealType().name()).add(convertToResponse(menu));
        }

        plan.put("weekStart", weekStart);
        plan.put("weekEnd", weekStart.plusDays(6));
        plan.put("breakfast", mealTypeMap.get("BREAKFAST"));
        plan.put("lunch", mealTypeMap.get("LUNCH"));
        plan.put("dinner", mealTypeMap.get("DINNER"));
        plan.put("totalMenus", menus.size());

        return plan;
    }

    // ==================== COPY MENU ====================

    @Override
    @Transactional
    public MenuResponse copyMenu(String sourceMenuId, LocalDate targetDate, String mealType) {
        log.info("Copying menu from {} to date: {}", sourceMenuId, targetDate);

        Menu sourceMenu = menuRepository.findById(sourceMenuId)
                .orElseThrow(() -> new ResourceNotFoundException("Source menu not found with ID: " + sourceMenuId));

        // Check if target already exists
        Optional<Menu> existing = menuRepository.findByMenuDateAndMealType(
                targetDate, MealType.valueOf(mealType.toUpperCase()));

        if (existing.isPresent()) {
            throw new RuntimeException("Menu already exists for this date and meal type");
        }

        Menu newMenu = Menu.builder()
                .menuDate(targetDate)
                .mealType(MealType.valueOf(mealType.toUpperCase()))
                .menuName(sourceMenu.getMenuName() + " (Copied)")
                .description(sourceMenu.getDescription())
                .isActive(true)
                .isSpecial(sourceMenu.isSpecial())
                .specialRemarks(sourceMenu.getSpecialRemarks())
                .createdBy(sourceMenu.getCreatedBy())
                .items(new ArrayList<>())
                .build();

        // Copy items
        for (MenuItem sourceItem : sourceMenu.getItems()) {
            MenuItem newItem = MenuItem.builder()
                    .menu(newMenu)
                    .itemName(sourceItem.getItemName())
                    .description(sourceItem.getDescription())
                    .price(sourceItem.getPrice())
                    .category(sourceItem.getCategory())
                    .isVegetarian(sourceItem.isVegetarian())
                    .isAvailable(sourceItem.isAvailable())
                    .preparationTime(sourceItem.getPreparationTime())
                    .servingSize(sourceItem.getServingSize())
                    .imageUrl(sourceItem.getImageUrl())
                    .displayOrder(sourceItem.getDisplayOrder())
                    .remarks(sourceItem.getRemarks())
                    .build();
            newMenu.getItems().add(newItem);
        }

        Menu saved = menuRepository.save(newMenu);
        log.info("Menu copied successfully with ID: {}", saved.getId());

        return convertToResponse(saved);
    }

    @Override
    @Transactional
    public void copyWeeklyMenu(LocalDate sourceWeekStart, LocalDate targetWeekStart) {
        log.info("Copying weekly menu from {} to {}", sourceWeekStart, targetWeekStart);

        LocalDate sourceStart = sourceWeekStart.with(DayOfWeek.MONDAY);
        LocalDate targetStart = targetWeekStart.with(DayOfWeek.MONDAY);

        for (int i = 0; i < 7; i++) {
            LocalDate sourceDate = sourceStart.plusDays(i);
            LocalDate targetDate = targetStart.plusDays(i);

            List<Menu> sourceMenus = menuRepository.findByMenuDate(sourceDate);

            for (Menu sourceMenu : sourceMenus) {
                try {
                    copyMenu(sourceMenu.getId(), targetDate, sourceMenu.getMealType().name());
                } catch (Exception e) {
                    log.error("Failed to copy menu for date: {}, meal: {}", targetDate, sourceMenu.getMealType(), e);
                }
            }
        }

        log.info("Weekly menu copied successfully");
    }

    // ==================== HELPER METHODS ====================

    private MenuResponse convertToResponse(Menu menu) {
        List<MenuResponse.MenuItemResponse> items = menu.getItems().stream()
                .sorted(Comparator.comparing(MenuItem::getDisplayOrder))
                .map(item -> MenuResponse.MenuItemResponse.builder()
                        .id(item.getId())
                        .itemName(item.getItemName())
                        .description(item.getDescription())
                        .price(item.getPrice())
                        .category(item.getCategory())
                        .isVegetarian(item.isVegetarian())
                        .isAvailable(item.isAvailable())
                        .preparationTime(item.getPreparationTime())
                        .servingSize(item.getServingSize())
                        .imageUrl(item.getImageUrl())
                        .displayOrder(item.getDisplayOrder())
                        .remarks(item.getRemarks())
                        .createdAt(item.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        return MenuResponse.builder()
                .id(menu.getId())
                .menuDate(menu.getMenuDate())
                .mealType(menu.getMealType().name())
                .menuName(menu.getMenuName())
                .description(menu.getDescription())
                .isActive(menu.isActive())
                .isSpecial(menu.isSpecial())
                .specialRemarks(menu.getSpecialRemarks())
                .createdBy(menu.getCreatedBy() != null
                        ? menu.getCreatedBy().getFullName() + " (" + menu.getCreatedBy().getCandidateId() + ")"
                        : null)
                .items(items)
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .build();
    }
}