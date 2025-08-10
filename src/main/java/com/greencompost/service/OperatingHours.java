package com.greencompost.service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the operating hours for a local service
 */
public class OperatingHours {
    private Map<DayOfWeek, TimeSlot> schedule;

    public OperatingHours() {
        this.schedule = new HashMap<>();
    }

    /**
     * Add operating hours for a specific day of the week
     *
     * @param day The day of the week
     * @param openTime The opening time
     * @param closeTime The closing time
     */
    public void addHours(DayOfWeek day, LocalTime openTime, LocalTime closeTime) {
        schedule.put(day, new TimeSlot(openTime, closeTime));
    }

    /**
     * Get the operating hours for a specific day
     *
     * @param day The day of the week
     * @return The TimeSlot for that day, or null if not open
     */
    public TimeSlot getHours(DayOfWeek day) {
        return schedule.get(day);
    }

    /**
     * Check if the service is open on a specific day
     *
     * @param day The day of the week
     * @return true if open, false otherwise
     */
    public boolean isOpenOn(DayOfWeek day) {
        return schedule.containsKey(day);
    }

    /**
     * Check if the service is open at a specific time on a specific day
     *
     * @param day The day of the week
     * @param time The time to check
     * @return true if open, false otherwise
     */
    public boolean isOpenAt(DayOfWeek day, LocalTime time) {
        if (!isOpenOn(day)) {
            return false;
        }

        TimeSlot slot = schedule.get(day);
        if (slot == null || slot.getOpenTime() == null || slot.getCloseTime() == null) {
            return false;
        }
        
        return (time.equals(slot.getOpenTime()) || time.isAfter(slot.getOpenTime())) && 
               (time.equals(slot.getCloseTime()) || time.isBefore(slot.getCloseTime()));
    }

    /**
     * Get all days when the service is open
     *
     * @return An array of days when the service is open
     */
    public DayOfWeek[] getOpenDays() {
        return schedule.keySet().toArray(new DayOfWeek[0]);
    }

    /**
     * Remove operating hours for a specific day
     *
     * @param day The day to remove hours for
     */
    public void removeHours(DayOfWeek day) {
        schedule.remove(day);
    }

    /**
     * Clear all operating hours
     */
    public void clearHours() {
        schedule.clear();
    }

    /**
     * Inner class representing a time slot with open and close times
     */
    public static class TimeSlot {
        private LocalTime openTime;
        private LocalTime closeTime;

        public TimeSlot(LocalTime openTime, LocalTime closeTime) {
            this.openTime = openTime;
            this.closeTime = closeTime;
        }
        
        public TimeSlot(String openTimeStr, String closeTimeStr) {
            this.openTime = LocalTime.parse(openTimeStr);
            this.closeTime = LocalTime.parse(closeTimeStr);
        }

        public LocalTime getOpenTime() {
            return openTime;
        }

        public void setOpenTime(LocalTime openTime) {
            this.openTime = openTime;
        }

        public LocalTime getCloseTime() {
            return closeTime;
        }

        public void setCloseTime(LocalTime closeTime) {
            this.closeTime = closeTime;
        }

        /**
         * Calculate the duration of this time slot in hours
         *
         * @return Number of hours the service is open
         */
        public double getDurationHours() {
            return (closeTime.toSecondOfDay() - openTime.toSecondOfDay()) / 3600.0;
        }

        @Override
        public String toString() {
            return openTime + " - " + closeTime;
        }
    }

    /**
     * Get the time slot for a specific day using the day index
     * 
     * @param dayIndex The day index (0-6 for Monday-Sunday)
     * @return The TimeSlot for that day, or null if not open
     */
    public TimeSlot getTimeSlotForDay(int dayIndex) {
        if (dayIndex < 0 || dayIndex > 6) {
            return null;
        }
        
        DayOfWeek day = DayOfWeek.of(dayIndex % 7 + 1); // Convert 0-6 to DayOfWeek enum (1-7)
        return schedule.get(day);
    }
    
    /**
     * Set the time slot for a specific day using the day index
     * 
     * @param dayIndex The day index (0-6 for Monday-Sunday)
     * @param slot The time slot to set
     */
    public void setTimeSlotForDay(int dayIndex, TimeSlot slot) {
        if (dayIndex < 0 || dayIndex > 6 || slot == null) {
            return;
        }
        
        DayOfWeek day = DayOfWeek.of(dayIndex % 7 + 1); // Convert 0-6 to DayOfWeek enum (1-7)
        schedule.put(day, slot);
    }
    
    /**
     * Calculate the total weekly operating hours
     *
     * @return Total hours open per week
     */
    public double getTotalWeeklyHours() {
        double total = 0;
        for (TimeSlot slot : schedule.values()) {
            total += slot.getDurationHours();
        }
        return total;
    }

    @Override
    public String toString() {
        if (schedule.isEmpty()) {
            return "No operating hours specified";
        }

        StringBuilder sb = new StringBuilder();
        schedule.forEach((day, hours) ->
                sb.append(day).append(": ").append(hours).append("\n"));
        return sb.toString();
    }
}