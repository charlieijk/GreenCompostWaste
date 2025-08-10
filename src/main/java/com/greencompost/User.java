package com.greencompost;

import com.greencompost.controller.FoodItem;
import com.greencompost.model.DatabaseManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Represents a user of the GreenCompost system
 */
public class User
{
    private UUID id;
    private String username;
    private String password;
    private String name;
    private String email;
    private String location;
    private String city; // New field replacing lat/long
    private double latitude; // Legacy field - keeping for compatibility
    private double longitude; // Legacy field - keeping for compatibility
    private List<FoodItem> foodItems;
    private boolean rememberMe;
    
    // Static collection to store all users
    public static final List<User> allUsers = new ArrayList<>();
    
    // Email validation pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    /**
     * Constructor for creating a new user
     */
    public User() {
        this.id = UUID.randomUUID();
        this.foodItems = new ArrayList<>();
        allUsers.add(this);
    }

    /**
     * Constructor for creating a new user with known details (using city)
     *
     * @param username Username
     * @param password Password
     * @param email Email address
     * @param location String description of location
     * @param city The user's city
     * @throws IllegalArgumentException if email format is invalid
     */
    public User(String username, String password, String email, String location, String city) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.password = password;
        setEmail(email); // Use setter for validation
        this.location = location;
        this.city = city;
        this.foodItems = new ArrayList<>();
        allUsers.add(this);
        
        // Save to database
        DatabaseManager.getInstance().saveUser(this);
    }
    
    /**
     * Legacy constructor for creating a new user with known details
     *
     * @param username Username
     * @param password Password
     * @param email Email address
     * @param location String description of location
     * @param latitude Latitude coordinate
     * @param longitude Longitude coordinate
     * @throws IllegalArgumentException if email format is invalid or coordinates are out of range
     */
    public User(String username, String password, String email, String location, double latitude, double longitude) {
        this.id = UUID.randomUUID();
        this.username = username;
        this.password = password;
        setEmail(email); // Use setter for validation
        this.location = location;
        
        // Extract city from location
        if (location != null && location.contains(",")) {
            this.city = location.split(",")[0].trim();
        } else {
            this.city = location;
        }
        
        // Keep latitude/longitude for backward compatibility
        this.latitude = latitude;
        this.longitude = longitude;
        
        this.foodItems = new ArrayList<>();
        allUsers.add(this);
        
        // Save to database
        DatabaseManager.getInstance().saveUser(this);
    }

    /**
     * Get the unique identifier for this user
     *
     * @return The user's UUID
     */
    public UUID getId() {
        return id;
    }

    /**
     * Get the username of this user
     *
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Update the username of this user
     *
     * @param username The new username
     */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * Get the password of this user
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Update the password of this user
     *
     * @param password The new password
     */
    public void setPassword(String password) {
        this.password = password;
        
        // Update password in database
        DatabaseManager.getInstance().updateUserPassword(this);
    }
    
    /**
     * Reset password for a user identified by email
     * 
     * @param email The email of the user
     * @param newPassword The new password
     * @return true if password was reset successfully, false otherwise
     */
    public static boolean resetPasswordByEmail(String email, String newPassword) {
        // Find the user with the given email
        User user = findByEmail(email);
        
        if (user != null) {
            // Update the password
            user.setPassword(newPassword);
            return true;
        }
        
        return false;
    }
    
    /**
     * Find a user by their email address
     * 
     * @param email The email to search for
     * @return The user with the given email, or null if not found
     */
    public static User findByEmail(String email) {
        // First check in-memory
        for (User user : allUsers) {
            if (user.getEmail() != null && user.getEmail().equalsIgnoreCase(email)) {
                return user;
            }
        }
        
        // Then check database
        return DatabaseManager.getInstance().getUserByEmail(email);
    }
    
    /**
     * Get the name of this user
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Update the name of this user
     *
     * @param name The new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the email of this user
     *
     * @return The email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Update the email of this user
     *
     * @param email The new email
     * @throws IllegalArgumentException if email format is invalid
     */
    public void setEmail(String email) {
        if (email != null && !email.isEmpty() && !EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
        this.email = email;
    }

    /**
     * Get the location description of this user
     *
     * @return The location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Update the location description of this user
     *
     * @param location The new location
     */
    public void setLocation(String location) {
        this.location = location;
        
        // Try to extract city from location
        if (location != null && location.contains(",")) {
            this.city = location.split(",")[0].trim();
        } else {
            this.city = location;
        }
    }
    
    /**
     * Get the city of this user
     *
     * @return The city
     */
    public String getCity() {
        if (city == null || city.isEmpty()) {
            // Default to Dublin if not set
            return "Dublin";
        }
        return city;
    }
    
    /**
     * Set the city for this user
     *
     * @param city The new city
     */
    public void setCity(String city) {
        this.city = city;
    }

    /**
     * Get the latitude coordinate of this user
     *
     * @return The latitude
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Update the latitude coordinate of this user
     *
     * @param latitude The new latitude
     * @throws IllegalArgumentException if latitude is out of valid range (-90 to 90)
     */
    public void setLatitude(double latitude) {
        if (latitude < -90 || latitude > 90) {
            throw new IllegalArgumentException("Invalid latitude: " + latitude + ". Must be between -90 and 90.");
        }
        this.latitude = latitude;
    }

    /**
     * Get the longitude coordinate of this user
     *
     * @return The longitude
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Update the longitude coordinate of this user
     *
     * @param longitude The new longitude
     * @throws IllegalArgumentException if longitude is out of valid range (-180 to 180)
     */
    public void setLongitude(double longitude) {
        if (longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid longitude: " + longitude + ". Must be between -180 and 180.");
        }
        this.longitude = longitude;
    }
    
    /**
     * Check if the user has selected "Remember Me" option
     *
     * @return True if remember me is enabled, false otherwise
     */
    public boolean isRememberMe() {
        return rememberMe;
    }

    /**
     * Set the "Remember Me" option for this user
     *
     * @param rememberMe True to enable remember me, false to disable
     */
    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    /**
     * Get the list of food items logged by this user
     *
     * @return The list of food items
     */
    public List<FoodItem> getFoodItems() {
        return Collections.unmodifiableList(foodItems);
    }

    /**
     * Add a food item to this user's list
     *
     * @param item The food item to add
     */
    public void addFoodItem(FoodItem item) {
        foodItems.add(item);
    }

    /**
     * Remove a food item from this user's list
     *
     * @param item The food item to remove
     * @return true if the item was removed, false otherwise
     */
    public boolean removeFoodItem(FoodItem item) {
        boolean removed = foodItems.remove(item);
        if (removed) {
            // Also remove from global collection
            FoodItem.removeFoodItem(item);
        }
        return removed;
    }

    @Override
    public String toString() {
        return String.format("User: %s - Location: %s - Items: %d",
                username, location, foodItems.size());
    }
    
    /**
     * Get all users in the system
     * 
     * @return List of all users
     */
    public static List<User> getAllUsers() {
        // Get users from database
        List<User> dbUsers = DatabaseManager.getInstance().getAllUsers();
        
        // Combine with in-memory users
        List<User> allUsersCombined = new ArrayList<>(allUsers);
        for (User dbUser : dbUsers) {
            if (!allUsersCombined.contains(dbUser)) {
                allUsersCombined.add(dbUser);
            }
        }
        
        return Collections.unmodifiableList(allUsersCombined);
    }
    
    /**
     * Find a user by their username
     * 
     * @param username The username to search for
     * @return The user with the given username, or null if not found
     */
    public static User findByUsername(String username) {
        // First check in-memory
        for (User user : allUsers) {
            if (user.getUsername() != null && user.getUsername().equals(username)) {
                return user;
            }
        }
        
        // Then check database
        return DatabaseManager.getInstance().getUserByUsername(username);
    }
    
    /**
     * Find users by location
     * 
     * @param locationKeyword The location keyword to search for
     * @return List of users in that location
     */
    public static List<User> findByLocation(String locationKeyword) {
        List<User> result = new ArrayList<>();
        if (locationKeyword == null || locationKeyword.isEmpty()) {
            return result;
        }
        
        for (User user : allUsers) {
            if (user.getLocation() != null && 
                user.getLocation().toLowerCase().contains(locationKeyword.toLowerCase())) {
                result.add(user);
            }
        }
        return result;
    }
    
    /**
     * Find users within a certain radius of the given coordinates (legacy method)
     * 
     * @param latitude Center latitude
     * @param longitude Center longitude
     * @param radiusKm Radius in kilometers
     * @return List of users within the radius
     */
    public static List<User> findNearby(double latitude, double longitude, double radiusKm) {
        // For backward compatibility - now uses city-based matching
        return findNearbyByCity("Dublin", radiusKm);
    }
    
    /**
     * Find users near a given city
     * 
     * @param city The city to search near
     * @param radiusKm Radius in kilometers
     * @return List of users within the radius
     */
    public static List<User> findNearbyByCity(String city, double radiusKm) {
        List<User> result = new ArrayList<>();
        
        for (User user : allUsers) {
            // Simplified distance calculation based on city matching
            double distance = calculateSimpleDistance(city, user.getCity());
            if (distance <= radiusKm) {
                result.add(user);
            }
        }
        
        return result;
    }
    
    /**
     * Calculate simple distance based on city matching
     */
    private static double calculateSimpleDistance(String city1, String city2) {
        if (city1 == null || city2 == null) {
            return 999; // Unknown distance if cities are null
        }
        
        // Same city - very close
        if (city1.equalsIgnoreCase(city2)) {
            // Random distance between 1-10 km within same city
            return 1 + (Math.random() * 9);
        }
        
        // Cities in same country
        if (isSameCountry(city1, city2)) {
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
     * Calculate the distance between two points using the Haversine formula
     * 
     * @param lat1 First latitude
     * @param lon1 First longitude
     * @param lat2 Second latitude
     * @param lon2 Second longitude
     * @return Distance in kilometers
     */
    private static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371; // in kilometers
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS * c;
    }
    
    /**
     * Find food items from users within a certain radius
     * 
     * @param latitude Center latitude
     * @param longitude Center longitude
     * @param radiusKm Radius in kilometers
     * @return List of food items from nearby users
     */
    public static List<FoodItem> findNearbyFoodItems(double latitude, double longitude, double radiusKm) {
        List<FoodItem> result = new ArrayList<>();
        List<User> nearbyUsers = findNearby(latitude, longitude, radiusKm);
        
        for (User user : nearbyUsers) {
            result.addAll(user.getFoodItems());
        }
        
        return result;
    }
    
    /**
     * Find food items from a user's nearby neighbors
     * 
     * @param user The user
     * @param radiusKm Radius in kilometers
     * @return List of food items from nearby users
     */
    public static List<FoodItem> findNearbyFoodItemsForUser(User user, double radiusKm) {
        return findNearbyFoodItems(user.getLatitude(), user.getLongitude(), radiusKm);
    }
}
