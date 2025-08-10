package com.greencompost.controller;

import com.greencompost.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a food item logged by a user for composting or donation
 * Also serves as a controller for food item operations
 */
public class FoodItem {
    // Static collection to store all food items
    public static final List<FoodItem> allFoodItems = new ArrayList<>();
    
    /**
     * Add a new food item for a user
     * 
     * @param user The user logging the food item
     * @param name Name of the food item
     * @param quantity Amount of food
     * @param quantityUnit Unit of measurement (kg, liters, pieces, etc.)
     * @param expiryDate When the food expires
     * @param category Food category
     * @return The created food item
     */
    public FoodItem addFoodItem(User user, String name, double quantity, String quantityUnit,
                          LocalDateTime expiryDate, FoodCategory category) {
        FoodItem item = new FoodItem(name, quantity, quantityUnit, expiryDate, category);
        user.addFoodItem(item);
        allFoodItems.add(item);
        return item;
    }

    /**
     * Find food items by category
     *
     * @param category The category to search for
     * @return List of matching food items
     */
    public static List<FoodItem> findByCategory(FoodCategory category) {
        List<FoodItem> result = new ArrayList<>();
        for (FoodItem item : allFoodItems) {
            if (item.getCategory() == category) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Find food items that are expiring soon (within 48 hours)
     * 
     * @return List of food items expiring soon
     */
    public static List<FoodItem> findExpiringSoon() {
        List<FoodItem> result = new ArrayList<>();
        for (FoodItem item : allFoodItems) {
            if (item.isExpiringSoon()) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Find food items by status
     *
     * @param status The status to search for
     * @return List of matching food items
     */
    public static List<FoodItem> findByStatus(ItemStatus status) {
        List<FoodItem> result = new ArrayList<>();
        for (FoodItem item : allFoodItems) {
            if (item.getStatus() == status) {
                result.add(item);
            }
        }
        return result;
    }

    /**
     * Get all food items in the system
     *
     * @return List of all food items
     */
    public static List<FoodItem> getAllFoodItems() {
        return new ArrayList<>(allFoodItems);
    }

    /**
     * Remove a food item from the system
     *
     * @param item The food item to remove
     */
    public static void removeFoodItem(FoodItem item) {
        allFoodItems.remove(item);
    }
    private UUID id;
    private String name;
    private double quantity;
    private String quantityUnit; // e.g., kg, liters, pieces
    private LocalDateTime expiryDate;
    private FoodCategory category;
    private ItemStatus status;
    private LocalDateTime createdAt;
    private String description;
    private User owner;

    /**
     * Food categories for classification and sorting
     */
    public enum FoodCategory {
        VEGETABLE, FRUIT, DAIRY, GRAIN, PROTEIN, LEFTOVER_MEAL, OTHER
    }

    /**
     * Possible statuses for food items
     */
    public enum ItemStatus {
        AVAILABLE, SCHEDULED_FOR_PICKUP, DONATED, COMPOSTED
    }

    /**
     * Default constructor for the controller functionality
     */
    public FoodItem() {
        // This is just for the controller functionality
    }
    
    /**
     * Constructor for creating a new food item
     *
     * @param name Name of the food item
     * @param quantity Amount of food
     * @param quantityUnit Unit of measurement (kg, liters, pieces, etc.)
     * @param expiryDate When the food expires
     * @param category Food category
     */
    public FoodItem(String name, double quantity, String quantityUnit,
                    LocalDateTime expiryDate, FoodCategory category) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.quantity = quantity;
        this.quantityUnit = quantityUnit;
        this.expiryDate = expiryDate;
        this.category = category;
        this.status = ItemStatus.AVAILABLE;
        this.createdAt = LocalDateTime.now();
        this.description = "";
    }

    /**
     * Get the unique identifier for this food item
     *
     * @return The item's UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Get the name of this food item
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Update the name of this food item
     *
     * @param name The new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the quantity of this food item
     *
     * @return The quantity
     */
    public double getQuantity() {
        return quantity;
    }

    /**
     * Update the quantity of this food item
     *
     * @param quantity The new quantity
     */
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    /**
     * Get the unit of measurement for this food item
     *
     * @return The quantity unit
     */
    public String getQuantityUnit() {
        return quantityUnit;
    }

    /**
     * Update the unit of measurement for this food item
     *
     * @param quantityUnit The new quantity unit
     */
    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    /**
     * Get the expiry date of this food item
     *
     * @return The expiry date
     */
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    /**
     * Update the expiry date of this food item
     *
     * @param expiryDate The new expiry date
     */
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

    /**
     * Get the category of this food item
     *
     * @return The food category
     */
    public FoodCategory getCategory() {
        return category;
    }

    /**
     * Update the category of this food item
     *
     * @param category The new category
     */
    public void setCategory(FoodCategory category) {
        this.category = category;
    }

    /**
     * Get the current status of this food item
     *
     * @return The item status
     */
    public ItemStatus getStatus() {
        return status;
    }

    /**
     * Update the status of this food item
     *
     * @param status The new status
     */
    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    /**
     * Get the date and time when this food item was created
     *
     * @return The creation date and time
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Get the description of this food item
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Update the description of this food item
     *
     * @param description The new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Check if this food item is expired
     *
     * @return true if expired, false otherwise
     */
    public boolean isExpired() {
        return expiryDate.isBefore(LocalDateTime.now());
    }

    /**
     * Check if this food item will expire soon (within the next 48 hours)
     *
     * @return true if expiring soon, false otherwise
     */
    public boolean isExpiringSoon() {
        LocalDateTime soonThreshold = LocalDateTime.now().plusHours(48);
        return expiryDate.isAfter(LocalDateTime.now()) && expiryDate.isBefore(soonThreshold);
    }

    /**
     * Get the days remaining until this food item expires
     *
     * @return Number of days until expiry, or 0 if already expired
     */
    public long getDaysUntilExpiry() {
        if (isExpired()) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        long daysUntil = java.time.Duration.between(now, expiryDate).toDays();
        return daysUntil;
    }

    /**
     * Get a formatted string of the expiry date
     *
     * @return Formatted date string, or "Not set" if no expiry date
     */
    public String getFormattedExpiryDate() {
        if (expiryDate == null) {
            return "Not set";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
        return expiryDate.format(formatter);
    }

    /**
     * Get a suitable recommendation for this food item based on its expiry
     *
     * @return Recommended action
     */
    public String getRecommendation() {
        if (isExpired()) {
            return "This item has expired. Consider composting.";
        } else if (isExpiringSoon()) {
            return "This item will expire soon. Consider immediate donation.";
        } else if (getDaysUntilExpiry() < 5) {
            return "Schedule a pickup or drop-off soon to avoid waste.";
        } else {
            return "This item has good shelf life. Perfect for donation.";
        }
    }
    
    /**
     * Get the owner of this food item
     *
     * @return The user who owns this food item
     */
    public User getOwner() {
        return owner;
    }
    
    /**
     * Set the owner of this food item
     *
     * @param owner The user who owns this food item
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    @Override
    public String toString() {
        String expiryStr = expiryDate != null ? getFormattedExpiryDate() : "No expiry date";
        return String.format("%s - %.2f %s - Expires: %s - Status: %s",
                name, quantity, quantityUnit, expiryStr, status);
    }
}

