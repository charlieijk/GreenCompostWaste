package com.greencompost.service;

import com.greencompost.User;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Represents a local service that accepts food waste for composting or donation
 */
public class LocalService {
    private UUID id;
    private String name;
    private String description;
    private String address;
    private String contactInfo;
    private String city; // New field replacing lat/long
    private double latitude; // Legacy field - keeping for compatibility
    private double longitude; // Legacy field - keeping for compatibility
    private ServiceType type;
    private OperatingHours operatingHours;
    private List<String> acceptedItems;
    private List<String> nonAcceptedItems;
    private boolean pickupAvailable;
    private double pickupRadius; // in kilometers
    private boolean acceptsFoodDonations;
    private List<String> donationGuidelines;
    private double calculatedDistance; // For storing distance calculated during searches
    public static final List<LocalService> availableServices = new ArrayList<>();
    private static final double EARTH_RADIUS_KM = 6371.0; // Earth's radius in kilometers

    /**
     * Types of services
     */
    public enum ServiceType {
        FOOD_BANK("Food Bank"),
        COMMUNITY_GARDEN("Community Garden"),
        COMPOSTING_FACILITY("Composting Facility"),
        URBAN_FARM("Urban Farm"),
        RESTAURANT("Restaurant"),
        COMMUNITY_FRIDGE("Community Fridge"),
        FOOD_DONATION_CENTER("Food Donation Center"),
        SOUP_KITCHEN("Soup Kitchen"),
        FOOD_PANTRY("Food Pantry");

        private final String displayName;

        ServiceType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    /**
     * Default constructor
     */
    public LocalService() {
        this.id = UUID.randomUUID();
        this.operatingHours = new OperatingHours();
        // Set default operating hours Monday-Friday: 9am-5pm
        for (DayOfWeek day : new DayOfWeek[]{
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            this.operatingHours.addHours(day, LocalTime.of(9, 0), LocalTime.of(17, 0));
        }
        this.acceptedItems = new ArrayList<>();
        this.nonAcceptedItems = new ArrayList<>();
        this.donationGuidelines = new ArrayList<>();
        this.name = "Default Service";
        this.type = ServiceType.FOOD_BANK; // Default type
        this.acceptsFoodDonations = false;
        availableServices.add(this);
    }

    /**
     * Constructor with required fields
     * 
     * @param name Service name
     * @param address Service address
     * @param contactInfo Contact information
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @param type Service type
     */
    /**
     * Constructor with required fields - using city instead of coordinates
     * 
     * @param name Service name
     * @param address Service address
     * @param contactInfo Contact information
     * @param city Service city
     * @param type Service type
     */
    public LocalService(String name, String address, String contactInfo, 
                       String city, ServiceType type) {
        this();
        setName(name);
        setAddress(address);
        setContactInfo(contactInfo);
        setCity(city); // Using city instead of coordinates
        setType(type);
    }
    
    /**
     * Legacy constructor (kept for compatibility)
     */
    public LocalService(String name, String address, String contactInfo, 
                       double latitude, double longitude, ServiceType type) {
        this();
        setName(name);
        setAddress(address);
        setContactInfo(contactInfo);
        // Extract city from address
        setCity(extractCityFromAddress(address));
        setType(type);
    }
    
    /**
     * Extract city name from an address string
     */
    private String extractCityFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return "Unknown";
        }
        
        // Try to extract city from address format like "123 Main St, Dublin, Ireland"
        String[] parts = address.split(",");
        if (parts.length >= 2) {
            return parts[1].trim();
        }
        
        return "Unknown";
    }

    /**
     * Get all available services
     * 
     * @return List of all services
     */
    public static List<LocalService> getAllServices() {
        return Collections.unmodifiableList(availableServices);
    }

    /**
     * Find services near a user
     * 
     * @param user The user
     * @param radius Radius in kilometers
     * @return List of services within the radius
     */
    /**
     * Find services near a user - using city matching instead of coordinates
     * 
     * @param user The user
     * @param radius Radius in kilometers
     * @return List of services within the radius
     */
    public static List<LocalService> findNearbyServicesForUser(User user, double radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Radius must be positive");
        }
        
        List<LocalService> result = new ArrayList<>();
        String userCity = user.getCity(); // Using city instead of coordinates
        
        for (LocalService service : availableServices) {
            // Simplified distance calculation based on city matching
            double distance = calculateSimplifiedDistance(userCity, service.getCity());
            
            // Store calculated distance for display purposes
            service.setCalculatedDistance(distance);
            
            if (distance <= radius) {
                result.add(service);
            }
        }
        return result;
    }
    
    /**
     * Calculate simplified distance based on city matching
     */
    private static double calculateSimplifiedDistance(String userCity, String serviceCity) {
        if (userCity == null || serviceCity == null) {
            return 999; // Unknown distance if cities are null
        }
        
        // Same city - very close
        if (userCity.equalsIgnoreCase(serviceCity)) {
            // Random distance between 1-10 km within same city
            return 1 + (Math.random() * 9);
        }
        
        // Cities in same country (simplified check)
        if (isSameCountry(userCity, serviceCity)) {
            // Random distance between 20-100 km for different cities in same country
            return 20 + (Math.random() * 80);
        }
        
        // International cities - far away
        // Random distance between 500-5000 km for international cities
        return 500 + (Math.random() * 4500);
    }
    
    /**
     * Check if two cities are in the same country (simplified version)
     */
    private static boolean isSameCountry(String city1, String city2) {
        // Irish cities
        List<String> irishCities = Arrays.asList("dublin", "cork", "galway", "limerick", "waterford");
        
        // UK cities
        List<String> ukCities = Arrays.asList("london", "belfast", "glasgow", "edinburgh", "cardiff");
        
        // US cities
        List<String> usCities = Arrays.asList("new york", "san francisco", "los angeles", "chicago", "boston");
        
        // French cities
        List<String> frenchCities = Arrays.asList("paris", "lyon", "marseille", "bordeaux", "nice");
        
        // German cities
        List<String> germanCities = Arrays.asList("berlin", "munich", "hamburg", "cologne", "frankfurt");
        
        city1 = city1.toLowerCase();
        city2 = city2.toLowerCase();
        
        if (irishCities.contains(city1) && irishCities.contains(city2)) {
            return true;
        }
        
        if (ukCities.contains(city1) && ukCities.contains(city2)) {
            return true;
        }
        
        if (usCities.contains(city1) && usCities.contains(city2)) {
            return true;
        }
        
        if (frenchCities.contains(city1) && frenchCities.contains(city2)) {
            return true;
        }
        
        if (germanCities.contains(city1) && germanCities.contains(city2)) {
            return true;
        }
        
        return false;
    }

    /**
     * Recommend the best service of a specific type for a user
     * 
     * @param user The user
     * @param type The type of service to recommend
     * @return The recommended service, or null if none found
     */
    public static LocalService recommendBestService(User user, ServiceType type) {
        if (user == null || type == null) {
            throw new IllegalArgumentException("User and type cannot be null");
        }

        LocalService closest = null;
        double minDistance = Double.MAX_VALUE;
        double userLat = user.getLatitude();
        double userLon = user.getLongitude();

        for (LocalService service : availableServices) {
            if (service.getType() == type) {
                double distance = calculateDistance(userLat, userLon,
                                                 service.getLatitude(), service.getLongitude());
                if (distance < minDistance) {
                    minDistance = distance;
                    closest = service;
                }
            }
        }
        return closest;
    }

    /**
     * Get the name of this service
     *
     * @return The name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get the type of this service
     *
     * @return The service type
     */
    public ServiceType getType() {
        return type;
    }

    /**
     * Get the operating hours of this service
     *
     * @return The operating hours
     */
    public OperatingHours getOperatingHours() {
        return operatingHours;
    }
    
    /**
     * Get the operating hours of this service (alias for getOperatingHours)
     *
     * @return The operating hours
     */
    public OperatingHours getHours() {
        return operatingHours;
    }
    
    /**
     * Set the operating hours for this service
     *
     * @param hours The new operating hours
     */
    public void setHours(OperatingHours hours) {
        this.operatingHours = hours;
    }
    
    /**
     * Get the description of this service
     *
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set the description for this service
     *
     * @param description The new description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format("%s - %s", name, operatingHours);
    }

    // Getters and Setters with validation
    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        this.name = name.trim();
    }

    public void setAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            throw new IllegalArgumentException("Address cannot be null or empty");
        }
        this.address = address.trim();
    }

    public void setContactInfo(String contactInfo) {
        if (contactInfo == null || contactInfo.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact info cannot be null or empty");
        }
        this.contactInfo = contactInfo.trim();
    }

    public void setLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }
        this.longitude = longitude;
    }

    public void setType(ServiceType type) {
        if (type == null) {
            throw new IllegalArgumentException("Service type cannot be null");
        }
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public String getContactInfo() {
        return contactInfo;
    }

    /**
     * Get the city of this service
     *
     * @return The city
     */
    public String getCity() {
        return city;
    }
    
    /**
     * Set the city for this service
     *
     * @param city The city
     */
    public void setCity(String city) {
        this.city = city;
    }
    
    /**
     * Legacy getter for latitude
     * @return Latitude value (may be 0)
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Legacy getter for longitude
     * @return Longitude value (may be 0)
     */
    public double getLongitude() {
        return longitude;
    }

    public UUID getId() {
        return id;
    }
    
    /**
     * Get the calculated distance during search operations
     * 
     * @return The calculated distance in kilometers
     */
    public double getCalculatedDistance() {
        return calculatedDistance;
    }
    
    /**
     * Set the calculated distance during search operations
     * 
     * @param calculatedDistance The distance in kilometers
     */
    public void setCalculatedDistance(double calculatedDistance) {
        this.calculatedDistance = calculatedDistance;
    }

    public boolean isPickupAvailable() {
        return pickupAvailable;
    }

    public void setPickupAvailable(boolean pickupAvailable) {
        this.pickupAvailable = pickupAvailable;
    }

    public double getPickupRadius() {
        return pickupRadius;
    }

    public void setPickupRadius(double pickupRadius) {
        if (pickupRadius < 0) {
            throw new IllegalArgumentException("Pickup radius cannot be negative");
        }
        this.pickupRadius = pickupRadius;
    }

    public List<String> getAcceptedItems() {
        return Collections.unmodifiableList(acceptedItems);
    }

    public void addAcceptedItem(String item) {
        if (item == null || item.trim().isEmpty()) {
            throw new IllegalArgumentException("Item cannot be null or empty");
        }
        this.acceptedItems.add(item.trim());
    }

    public void removeAcceptedItem(String item) {
        this.acceptedItems.remove(item);
    }

    public List<String> getNonAcceptedItems() {
        return Collections.unmodifiableList(nonAcceptedItems);
    }

    public void addNonAcceptedItem(String item) {
        if (item == null || item.trim().isEmpty()) {
            throw new IllegalArgumentException("Item cannot be null or empty");
        }
        this.nonAcceptedItems.add(item.trim());
    }

    public void removeNonAcceptedItem(String item) {
        this.nonAcceptedItems.remove(item);
    }
    
    /**
     * Check if this service accepts food donations
     *
     * @return true if the service accepts food donations
     */
    public boolean acceptsFoodDonations() {
        return acceptsFoodDonations;
    }

    /**
     * Set whether this service accepts food donations
     *
     * @param acceptsFoodDonations true if the service accepts food donations
     */
    public void setAcceptsFoodDonations(boolean acceptsFoodDonations) {
        this.acceptsFoodDonations = acceptsFoodDonations;
    }

    /**
     * Get donation guidelines for this service
     *
     * @return The donation guidelines
     */
    public List<String> getDonationGuidelines() {
        return Collections.unmodifiableList(donationGuidelines);
    }

    /**
     * Add a donation guideline for this service
     *
     * @param guideline The guideline to add
     */
    public void addDonationGuideline(String guideline) {
        if (guideline == null || guideline.trim().isEmpty()) {
            throw new IllegalArgumentException("Guideline cannot be null or empty");
        }
        this.donationGuidelines.add(guideline.trim());
    }

    /**
     * Remove a donation guideline from this service
     *
     * @param guideline The guideline to remove
     */
    public void removeDonationGuideline(String guideline) {
        this.donationGuidelines.remove(guideline);
    }
    
    /**
     * Find services that accept food donations
     * 
     * @return List of services that accept food donations
     */
    public static List<LocalService> findFoodDonationServices() {
        return availableServices.stream()
                .filter(service -> service.acceptsFoodDonations() || 
                        service.getType() == ServiceType.FOOD_BANK || 
                        service.getType() == ServiceType.FOOD_DONATION_CENTER || 
                        service.getType() == ServiceType.SOUP_KITCHEN || 
                        service.getType() == ServiceType.FOOD_PANTRY)
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Calculate distance between two points using Haversine formula
     * 
     * @param lat1 First latitude
     * @param lon1 First longitude
     * @param lat2 Second latitude
     * @param lon2 Second longitude
     * @return Distance in kilometers
     */
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(lat1) * Math.cos(lat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}