package com.greencompost.service;

import com.greencompost.User;
import com.greencompost.controller.FoodItem;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a scheduled pickup or drop-off event
 */
public class ScheduledEvent {
    private UUID id;
    private User user;
    private LocalService service;
    private EventType eventType;
    private LocalDateTime scheduledTime;
    private EventStatus status;
    private List<FoodItem> foodItems;
    private String notes;
    
    // Additional fields needed by DatabaseManager
    private String title;
    private String description;
    private String location;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalService hostingService;
    
    /**
     * Default constructor for DatabaseManager
     */
    public ScheduledEvent() {
        this.id = UUID.randomUUID();
        this.foodItems = new ArrayList<>();
    }
    
    /**
     * Constructor for creating an event with title and description
     *
     * @param title The title of the event
     * @param description The description of the event
     * @param scheduledTime The date and time of the event
     * @param service The service involved
     * @param user The user scheduling the event
     */
    public ScheduledEvent(String title, String description, LocalDateTime scheduledTime,
                          LocalService service, User user) {
        this.id = UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.user = user;
        this.service = service;
        this.hostingService = service;
        this.scheduledTime = scheduledTime;
        this.startTime = scheduledTime;
        this.endTime = scheduledTime.plusHours(1); // Default to 1 hour duration
        
        // Determine event type based on description (pickup vs. drop-off)
        this.eventType = description.toLowerCase().contains("pickup") ? 
                         EventType.PICKUP : EventType.DROP_OFF;
                         
        this.status = EventStatus.SCHEDULED;
        this.foodItems = new ArrayList<>();
        this.notes = "";
        
        // Set location based on service
        this.location = service.getAddress();
    }
    
    /**
     * Gets the title of the event
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the title of the event
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Gets the description of the event
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Sets the description of the event
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Gets the location of the event
     * @return the location
     */
    public String getLocation() {
        return location;
    }
    
    /**
     * Sets the location of the event
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }
    
    /**
     * Gets the start time of the event
     * @return the start time
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    /**
     * Sets the start time of the event
     * @param startTime the start time to set
     */
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    
    /**
     * Gets the end time of the event
     * @return the end time
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }
    
    /**
     * Sets the end time of the event
     * @param endTime the end time to set
     */
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
    /**
     * Gets the hosting service of the event
     * @return the hosting service
     */
    public LocalService getHostingService() {
        return hostingService;
    }
    
    /**
     * Sets the hosting service of the event
     * @param hostingService the hosting service to set
     */
    public void setHostingService(LocalService hostingService) {
        this.hostingService = hostingService;
    }

    /**
     * Types of scheduled events
     */
    public enum EventType {
        PICKUP,
        DROP_OFF
    }

    /**
     * Possible statuses for a scheduled event
     */
    public enum EventStatus {
        SCHEDULED, COMPLETED, CANCELLED
    }

    /**
     * Constructor for creating a new scheduled event
     *
     * @param user The user scheduling the event
     * @param service The service involved
     * @param eventType The type of event (pickup or drop-off)
     * @param scheduledTime The date and time of the event
     */
    public ScheduledEvent(User user, LocalService service, EventType eventType,
                          LocalDateTime scheduledTime) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.service = service;
        this.eventType = eventType;
        this.scheduledTime = scheduledTime;
        this.status = EventStatus.SCHEDULED;
        this.foodItems = new ArrayList<>();
        this.notes = "";
    }

    /**
     * Get the unique identifier for this event
     *
     * @return The event's UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Get the user who scheduled this event
     *
     * @return The user
     */
    public User getUser() {
        return user;
    }

    /**
     * Get the service associated with this event
     *
     * @return The service
     */
    public LocalService getService() {
        return service;
    }

    /**
     * Update the service for this event
     *
     * @param service The new service
     */
    public void setService(LocalService service) {
        this.service = service;
    }

    /**
     * Get the type of this event
     *
     * @return The event type (pickup or drop-off)
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * Update the type of this event
     *
     * @param eventType The new event type
     */
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    /**
     * Get the scheduled date and time for this event
     *
     * @return The scheduled date and time
     */
    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    /**
     * Update the scheduled date and time for this event
     *
     * @param scheduledTime The new scheduled date and time
     */
    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    /**
     * Get the current status of this event
     *
     * @return The event status
     */
    public EventStatus getStatus() {
        return status;
    }

    /**
     * Update the status of this event
     *
     * @param status The new status
     */
    public void setStatus(EventStatus status) {
        this.status = status;
    }

    /**
     * Get all food items associated with this event
     *
     * @return List of food items
     */
    public List<FoodItem> getFoodItems() {
        return new ArrayList<>(foodItems);
    }

    /**
     * Add a food item to this event
     *
     * @param item The food item to add
     */
    public void addFoodItem(FoodItem item) {
        this.foodItems.add(item);
        item.setStatus(FoodItem.ItemStatus.SCHEDULED_FOR_PICKUP);
    }

    /**
     * Remove a food item from this event
     *
     * @param item The food item to remove
     */
    public void removeFoodItem(FoodItem item) {
        this.foodItems.remove(item);
        item.setStatus(FoodItem.ItemStatus.AVAILABLE);
    }

    /**
     * Remove all food items from this event
     */
    public void clearFoodItems() {
        for (FoodItem item : foodItems) {
            item.setStatus(FoodItem.ItemStatus.AVAILABLE);
        }
        foodItems.clear();
    }

    /**
     * Get additional notes for this event
     *
     * @return Notes string
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Update the notes for this event
     *
     * @param notes The new notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Check if this event is upcoming
     *
     * @return true if scheduled in the future, false otherwise
     */
    public boolean isUpcoming() {
        return scheduledTime.isAfter(LocalDateTime.now()) && status == EventStatus.SCHEDULED;
    }

    /**
     * Get the total number of food items in this event
     *
     * @return Count of food items
     */
    public int getItemCount() {
        return foodItems.size();
    }

    /**
     * Calculate the total weight/quantity of all food items
     *
     * @return Total quantity
     */
    public double getTotalQuantity() {
        double total = 0;
        for (FoodItem item : foodItems) {
            total += item.getQuantity();
        }
        return total;
    }

    /**
     * Mark this event as completed
     * Updates the status of all food items accordingly
     */
    public void markCompleted() {
        this.status = EventStatus.COMPLETED;

        // Update status of all food items based on event type and service type
        FoodItem.ItemStatus newItemStatus;
        
        if (eventType == EventType.PICKUP) {
            newItemStatus = FoodItem.ItemStatus.DONATED;
        } else if (service.getType() == LocalService.ServiceType.COMPOSTING_FACILITY) {
            newItemStatus = FoodItem.ItemStatus.COMPOSTED;
        } else {
            newItemStatus = FoodItem.ItemStatus.DONATED;
        }

        for (FoodItem item : foodItems) {
            item.setStatus(newItemStatus);
        }
    }

    /**
     * Cancel this event
     * Resets all food items to available status
     */
    public void cancel() {
        this.status = EventStatus.CANCELLED;
        clearFoodItems();
    }

    public String toString() {
        return String.format("%s with %s on %s - Status: %s - Items: %d",
                eventType, service.getName(), scheduledTime, status, foodItems.size());
    }
}