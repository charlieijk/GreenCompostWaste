package com.greencompost.model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.greencompost.User;
import com.greencompost.controller.FoodItem;
import com.greencompost.controller.FoodItem.FoodCategory;
import com.greencompost.controller.FoodItem.ItemStatus;
import com.greencompost.service.LocalService;
import com.greencompost.service.LocalService.ServiceType;
import com.greencompost.service.OperatingHours;
import com.greencompost.service.ScheduledEvent;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:greencompost.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            // Set auto-commit to true by default
            connection.setAutoCommit(true);
            createTables();
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        // Users table
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                name TEXT,
                email TEXT,
                location TEXT,
                latitude REAL,
                longitude REAL,
                createdAt TEXT,
                remember_me INTEGER DEFAULT 0
            )
        """;

        // Food items table
        String createFoodItemsTable = """
            CREATE TABLE IF NOT EXISTS food_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                category TEXT NOT NULL,
                quantity REAL NOT NULL,
                quantityUnit TEXT,
                expirationDate TEXT,
                status TEXT NOT NULL,
                userId INTEGER,
                createdAt TEXT,
                description TEXT,
                FOREIGN KEY (userId) REFERENCES users(id)
            )
        """;

        // Local services table
        String createServicesTable = """
            CREATE TABLE IF NOT EXISTS services (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                description TEXT,
                address TEXT,
                contactInfo TEXT,
                latitude REAL,
                longitude REAL,
                pickupAvailable INTEGER DEFAULT 0,
                pickupRadius REAL DEFAULT 0,
                acceptsFoodDonations INTEGER DEFAULT 0,
                serviceType TEXT NOT NULL
            )
        """;

        // Operating hours table
        String createOperatingHoursTable = """
            CREATE TABLE IF NOT EXISTS operating_hours (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                serviceId INTEGER,
                dayOfWeek INTEGER,
                openTime TEXT,
                closeTime TEXT,
                FOREIGN KEY (serviceId) REFERENCES services(id)
            )
        """;
        
        // Accepted items table
        String createAcceptedItemsTable = """
            CREATE TABLE IF NOT EXISTS accepted_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                serviceId INTEGER,
                itemName TEXT NOT NULL,
                FOREIGN KEY (serviceId) REFERENCES services(id)
            )
        """;
        
        // Non-accepted items table
        String createNonAcceptedItemsTable = """
            CREATE TABLE IF NOT EXISTS non_accepted_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                serviceId INTEGER,
                itemName TEXT NOT NULL,
                FOREIGN KEY (serviceId) REFERENCES services(id)
            )
        """;
        
        // Donation guidelines table
        String createDonationGuidelinesTable = """
            CREATE TABLE IF NOT EXISTS donation_guidelines (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                serviceId INTEGER,
                guideline TEXT NOT NULL,
                FOREIGN KEY (serviceId) REFERENCES services(id)
            )
        """;

        // Events table
        String createEventsTable = """
            CREATE TABLE IF NOT EXISTS events (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                location TEXT,
                startTime TEXT,
                endTime TEXT,
                serviceId INTEGER,
                FOREIGN KEY (serviceId) REFERENCES services(id)
            )
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            stmt.execute(createFoodItemsTable);
            stmt.execute(createServicesTable);
            stmt.execute(createOperatingHoursTable);
            stmt.execute(createAcceptedItemsTable);
            stmt.execute(createNonAcceptedItemsTable);
            stmt.execute(createDonationGuidelinesTable);
            stmt.execute(createEventsTable);
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    // User operations
    public void saveUser(User user) {
        String sql = "INSERT OR REPLACE INTO users (username, password, name, email, location, latitude, longitude, createdAt, remember_me) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getEmail());
            pstmt.setString(5, user.getLocation());
            pstmt.setDouble(6, user.getLatitude());
            pstmt.setDouble(7, user.getLongitude());
            pstmt.setString(8, LocalDateTime.now().toString());
            pstmt.setInt(9, user.isRememberMe() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving user: " + e.getMessage());
        }
    }

    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setLocation(rs.getString("location"));
                user.setLatitude(rs.getDouble("latitude"));
                user.setLongitude(rs.getDouble("longitude"));
                user.setRememberMe(rs.getInt("remember_me") == 1);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user: " + e.getMessage());
        }
        
        return null;
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setLocation(rs.getString("location"));
                user.setLatitude(rs.getDouble("latitude"));
                user.setLongitude(rs.getDouble("longitude"));
                user.setRememberMe(rs.getInt("remember_me") == 1);
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all users: " + e.getMessage());
        }
        
        return users;
    }
    
    /**
     * Get a user with "Remember Me" enabled
     * 
     * @return The remembered user, or null if none
     */
    public User getRememberedUser() {
        String sql = "SELECT * FROM users WHERE remember_me = 1 LIMIT 1";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setLocation(rs.getString("location"));
                user.setLatitude(rs.getDouble("latitude"));
                user.setLongitude(rs.getDouble("longitude"));
                user.setRememberMe(true);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting remembered user: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Update a user's password in the database
     * 
     * @param user The user with the updated password
     * @return true if the update was successful, false otherwise
     */
    public boolean updateUserPassword(User user) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getPassword());
            pstmt.setString(2, user.getUsername());
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating user password: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Find a user by their email address
     * 
     * @param email The email to search for
     * @return The user with the given email, or null if not found
     */
    public User getUserByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                User user = new User();
                user.setUsername(rs.getString("username"));
                user.setPassword(rs.getString("password"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setLocation(rs.getString("location"));
                user.setLatitude(rs.getDouble("latitude"));
                user.setLongitude(rs.getDouble("longitude"));
                user.setRememberMe(rs.getInt("remember_me") == 1);
                return user;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user by email: " + e.getMessage());
        }
        
        return null;
    }

    // Food item operations
    public void saveFoodItem(FoodItem item) {
        String sql = "INSERT OR REPLACE INTO food_items (name, category, quantity, quantityUnit, expirationDate, status, userId, createdAt, description) " +
                      "VALUES (?, ?, ?, ?, ?, ?, (SELECT id FROM users WHERE username = ?), ?, ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setString(2, item.getCategory().toString());
            pstmt.setDouble(3, item.getQuantity());
            pstmt.setString(4, item.getQuantityUnit());
            pstmt.setString(5, item.getExpiryDate().toString());
            pstmt.setString(6, item.getStatus().toString());
            pstmt.setString(7, item.getOwner().getUsername());
            pstmt.setString(8, LocalDateTime.now().toString());
            pstmt.setString(9, item.getDescription());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving food item: " + e.getMessage());
        }
    }

    public List<FoodItem> getFoodItemsByUser(User user) {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT * FROM food_items WHERE userId = (SELECT id FROM users WHERE username = ?)";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                FoodItem item = new FoodItem();
                item.setName(rs.getString("name"));
                item.setCategory(FoodCategory.valueOf(rs.getString("category")));
                item.setQuantity(rs.getDouble("quantity"));
                item.setQuantityUnit(rs.getString("quantityUnit"));
                String expiryDateStr = rs.getString("expirationDate");
                if (expiryDateStr != null && !expiryDateStr.isEmpty()) { // Check for null or empty
                    item.setExpiryDate(LocalDateTime.parse(expiryDateStr));
                } else {
                    item.setExpiryDate(null); // Or handle as appropriate
                }
                item.setStatus(ItemStatus.valueOf(rs.getString("status")));
                item.setOwner(user);
                item.setDescription(rs.getString("description"));
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting food items by user: " + e.getMessage());
        }
        
        return items;
    }

    public List<FoodItem> getAllFoodItems() {
        List<FoodItem> items = new ArrayList<>();
        String sql = "SELECT f.*, u.username FROM food_items f JOIN users u ON f.userId = u.id";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                FoodItem item = new FoodItem();
                item.setName(rs.getString("name"));
                item.setCategory(FoodCategory.valueOf(rs.getString("category")));
                item.setQuantity(rs.getDouble("quantity"));
                item.setQuantityUnit(rs.getString("quantityUnit"));
                String expiryDateStr = rs.getString("expirationDate");
                if (expiryDateStr != null && !expiryDateStr.isEmpty()) {
                    item.setExpiryDate(LocalDateTime.parse(expiryDateStr));
                } else {
                    item.setExpiryDate(null);
                }
                item.setStatus(ItemStatus.valueOf(rs.getString("status")));
                item.setDescription(rs.getString("description"));
                
                String username = rs.getString("username");
                User owner = getUserByUsername(username);
                item.setOwner(owner);
                
                items.add(item);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all food items: " + e.getMessage());
        }
        
        return items;
    }

    // Local service operations
    public void saveLocalService(LocalService service) {
        boolean previousAutoCommit = true;
        try {
            // Check the current auto-commit mode
            previousAutoCommit = connection.getAutoCommit();
            
            // Only change if it's not already what we want
            if (previousAutoCommit) {
                connection.setAutoCommit(false);
            }
            
            // Save service
            String serviceSql = "INSERT OR REPLACE INTO services (name, description, address, contactInfo, latitude, longitude, pickupAvailable, pickupRadius, acceptsFoodDonations, serviceType) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = connection.prepareStatement(serviceSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, service.getName());
                pstmt.setString(2, service.getDescription());
                pstmt.setString(3, service.getAddress());
                pstmt.setString(4, service.getContactInfo());
                pstmt.setDouble(5, service.getLatitude());
                pstmt.setDouble(6, service.getLongitude());
                pstmt.setInt(7, service.isPickupAvailable() ? 1 : 0);
                pstmt.setDouble(8, service.getPickupRadius());
                pstmt.setInt(9, service.acceptsFoodDonations() ? 1 : 0);
                pstmt.setString(10, service.getType().toString());
                pstmt.executeUpdate();
                
                // Get the generated service ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    long serviceId = generatedKeys.next() ? generatedKeys.getLong(1) : 0;
                    
                    // Delete existing operating hours for this service
                    String deleteHoursSql = "DELETE FROM operating_hours WHERE serviceId = ?";
                    try (PreparedStatement deleteStmt = connection.prepareStatement(deleteHoursSql)) {
                        deleteStmt.setLong(1, serviceId);
                        deleteStmt.executeUpdate();
                    }
                    
                    // Save operating hours
                    if (service.getHours() != null) {
                        String hoursSql = "INSERT INTO operating_hours (serviceId, dayOfWeek, openTime, closeTime) VALUES (?, ?, ?, ?)";
                        try (PreparedStatement hoursStmt = connection.prepareStatement(hoursSql)) {
                            for (int day = 0; day < 7; day++) {
                                OperatingHours.TimeSlot slot = service.getHours().getTimeSlotForDay(day);
                                if (slot != null) {
                                    hoursStmt.setLong(1, serviceId);
                                    hoursStmt.setInt(2, day);
                                    hoursStmt.setString(3, slot.getOpenTime().toString());
                                    hoursStmt.setString(4, slot.getCloseTime().toString());
                                    hoursStmt.executeUpdate();
                                }
                            }
                        }
                    }
                    
                    // Delete existing accepted items
                    String deleteAcceptedItemsSql = "DELETE FROM accepted_items WHERE serviceId = ?";
                    try (PreparedStatement deleteAcceptedStmt = connection.prepareStatement(deleteAcceptedItemsSql)) {
                        deleteAcceptedStmt.setLong(1, serviceId);
                        deleteAcceptedStmt.executeUpdate();
                    }
                    
                    // Save accepted items
                    if (service.getAcceptedItems() != null && !service.getAcceptedItems().isEmpty()) {
                        String acceptedItemsSql = "INSERT INTO accepted_items (serviceId, itemName) VALUES (?, ?)";
                        try (PreparedStatement acceptedItemsStmt = connection.prepareStatement(acceptedItemsSql)) {
                            for (String item : service.getAcceptedItems()) {
                                acceptedItemsStmt.setLong(1, serviceId);
                                acceptedItemsStmt.setString(2, item);
                                acceptedItemsStmt.executeUpdate();
                            }
                        }
                    }
                    
                    // Delete existing non-accepted items
                    String deleteNonAcceptedItemsSql = "DELETE FROM non_accepted_items WHERE serviceId = ?";
                    try (PreparedStatement deleteNonAcceptedStmt = connection.prepareStatement(deleteNonAcceptedItemsSql)) {
                        deleteNonAcceptedStmt.setLong(1, serviceId);
                        deleteNonAcceptedStmt.executeUpdate();
                    }
                    
                    // Save non-accepted items
                    if (service.getNonAcceptedItems() != null && !service.getNonAcceptedItems().isEmpty()) {
                        String nonAcceptedItemsSql = "INSERT INTO non_accepted_items (serviceId, itemName) VALUES (?, ?)";
                        try (PreparedStatement nonAcceptedItemsStmt = connection.prepareStatement(nonAcceptedItemsSql)) {
                            for (String item : service.getNonAcceptedItems()) {
                                nonAcceptedItemsStmt.setLong(1, serviceId);
                                nonAcceptedItemsStmt.setString(2, item);
                                nonAcceptedItemsStmt.executeUpdate();
                            }
                        }
                    }
                    
                    // Delete existing donation guidelines
                    String deleteDonationGuidelinesSql = "DELETE FROM donation_guidelines WHERE serviceId = ?";
                    try (PreparedStatement deleteDonationGuidelinesStmt = connection.prepareStatement(deleteDonationGuidelinesSql)) {
                        deleteDonationGuidelinesStmt.setLong(1, serviceId);
                        deleteDonationGuidelinesStmt.executeUpdate();
                    }
                    
                    // Save donation guidelines
                    if (service.getDonationGuidelines() != null && !service.getDonationGuidelines().isEmpty()) {
                        String donationGuidelinesSql = "INSERT INTO donation_guidelines (serviceId, guideline) VALUES (?, ?)";
                        try (PreparedStatement donationGuidelinesStmt = connection.prepareStatement(donationGuidelinesSql)) {
                            for (String guideline : service.getDonationGuidelines()) {
                                donationGuidelinesStmt.setLong(1, serviceId);
                                donationGuidelinesStmt.setString(2, guideline);
                                donationGuidelinesStmt.executeUpdate();
                            }
                        }
                    }
                }
            }
            
            connection.commit();
        } catch (SQLException e) {
            try {
                // Rollback the transaction
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error saving local service: " + e.getMessage());
        } finally {
            try {
                // Only reset auto-commit if we changed it
                if (connection.getAutoCommit() != previousAutoCommit) {
                    connection.setAutoCommit(previousAutoCommit);
                }
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }

    public List<LocalService> getAllLocalServices() {
        List<LocalService> services = new ArrayList<>();
        String sql = "SELECT * FROM services";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                LocalService service = new LocalService();
                service.setName(rs.getString("name"));
                service.setDescription(rs.getString("description"));
                service.setAddress(rs.getString("address"));
                service.setContactInfo(rs.getString("contactInfo"));
                service.setLatitude(rs.getDouble("latitude"));
                service.setLongitude(rs.getDouble("longitude"));
                service.setPickupAvailable(rs.getInt("pickupAvailable") == 1);
                service.setPickupRadius(rs.getDouble("pickupRadius"));
                service.setType(ServiceType.valueOf(rs.getString("serviceType")));
                service.setAcceptsFoodDonations(rs.getInt("acceptsFoodDonations") == 1);
                
                // Get operating hours for this service
                long serviceId = rs.getLong("id");
                OperatingHours hours = getOperatingHoursForService(serviceId);
                service.setHours(hours);
                
                // Get accepted items
                loadAcceptedItemsForService(service, serviceId);
                
                // Get non-accepted items
                loadNonAcceptedItemsForService(service, serviceId);
                
                // Get donation guidelines
                loadDonationGuidelinesForService(service, serviceId);
                
                services.add(service);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all local services: " + e.getMessage());
        }
        
        return services;
    }

    private OperatingHours getOperatingHoursForService(long serviceId) {
        String sql = "SELECT * FROM operating_hours WHERE serviceId = ?";
        OperatingHours hours = new OperatingHours();
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, serviceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                int dayOfWeek = rs.getInt("dayOfWeek");
                String openTimeStr = rs.getString("openTime");
                String closeTimeStr = rs.getString("closeTime");
                
                // Convert time strings to LocalTime
                OperatingHours.TimeSlot slot = new OperatingHours.TimeSlot(openTimeStr, closeTimeStr);
                hours.setTimeSlotForDay(dayOfWeek, slot);
            }
        } catch (SQLException e) {
            System.err.println("Error getting operating hours: " + e.getMessage());
        }
        
        return hours;
    }

    // Event operations
    public void saveEvent(ScheduledEvent event) {
        String sql = "INSERT OR REPLACE INTO events (title, description, location, startTime, endTime, serviceId) " +
                     "VALUES (?, ?, ?, ?, ?, (SELECT id FROM services WHERE name = ?))";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, event.getTitle());
            pstmt.setString(2, event.getDescription());
            pstmt.setString(3, event.getLocation());
            pstmt.setString(4, event.getStartTime().toString());
            pstmt.setString(5, event.getEndTime().toString());
            pstmt.setString(6, event.getHostingService().getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error saving event: " + e.getMessage());
        }
    }

    public List<ScheduledEvent> getAllEvents() {
        List<ScheduledEvent> events = new ArrayList<>();
        String sql = "SELECT e.*, s.name as serviceName FROM events e JOIN services s ON e.serviceId = s.id";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ScheduledEvent event = new ScheduledEvent();
                event.setTitle(rs.getString("title"));
                event.setDescription(rs.getString("description"));
                event.setLocation(rs.getString("location"));
                event.setStartTime(LocalDateTime.parse(rs.getString("startTime")));
                event.setEndTime(LocalDateTime.parse(rs.getString("endTime")));
                
                String serviceName = rs.getString("serviceName");
                LocalService service = getServiceByName(serviceName);
                event.setHostingService(service);
                
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error getting all events: " + e.getMessage());
        }
        
        return events;
    }

    private LocalService getServiceByName(String name) {
        String sql = "SELECT * FROM services WHERE name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                LocalService service = new LocalService();
                service.setName(rs.getString("name"));
                service.setDescription(rs.getString("description"));
                service.setAddress(rs.getString("address"));
                service.setContactInfo(rs.getString("contactInfo"));
                service.setLatitude(rs.getDouble("latitude"));
                service.setLongitude(rs.getDouble("longitude"));
                service.setPickupAvailable(rs.getInt("pickupAvailable") == 1);
                service.setPickupRadius(rs.getDouble("pickupRadius"));
                service.setType(ServiceType.valueOf(rs.getString("serviceType")));
                service.setAcceptsFoodDonations(rs.getInt("acceptsFoodDonations") == 1);
                
                // Get operating hours for this service
                long serviceId = rs.getLong("id");
                OperatingHours hours = getOperatingHoursForService(serviceId);
                service.setHours(hours);
                
                // Get accepted items
                loadAcceptedItemsForService(service, serviceId);
                
                // Get non-accepted items
                loadNonAcceptedItemsForService(service, serviceId);
                
                // Get donation guidelines
                loadDonationGuidelinesForService(service, serviceId);
                
                return service;
            }
        } catch (SQLException e) {
            System.err.println("Error getting service by name: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Load accepted items for a service
     * 
     * @param service The service to load items for
     * @param serviceId The service ID
     */
    private void loadAcceptedItemsForService(LocalService service, long serviceId) {
        String sql = "SELECT itemName FROM accepted_items WHERE serviceId = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, serviceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                service.addAcceptedItem(rs.getString("itemName"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading accepted items: " + e.getMessage());
        }
    }
    
    /**
     * Load non-accepted items for a service
     * 
     * @param service The service to load items for
     * @param serviceId The service ID
     */
    private void loadNonAcceptedItemsForService(LocalService service, long serviceId) {
        String sql = "SELECT itemName FROM non_accepted_items WHERE serviceId = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, serviceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                service.addNonAcceptedItem(rs.getString("itemName"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading non-accepted items: " + e.getMessage());
        }
    }
    
    /**
     * Load donation guidelines for a service
     * 
     * @param service The service to load guidelines for
     * @param serviceId The service ID
     */
    private void loadDonationGuidelinesForService(LocalService service, long serviceId) {
        String sql = "SELECT guideline FROM donation_guidelines WHERE serviceId = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, serviceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                service.addDonationGuideline(rs.getString("guideline"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading donation guidelines: " + e.getMessage());
        }
    }

    // Method to initialize the database with sample data
    public void initializeSampleData() {
        boolean previousAutoCommit = true;
        try {
            // Check the current auto-commit mode
            previousAutoCommit = connection.getAutoCommit();
            
            // Only change if it's not already what we want
            if (previousAutoCommit) {
                connection.setAutoCommit(false);
            }
            
            // Add sample users
            for (User user : User.allUsers) {
                saveUser(user);
            }

            // Add sample food items
            for (FoodItem item : FoodItem.allFoodItems) {
                saveFoodItem(item);
            }

            // Add sample services
            for (LocalService service : LocalService.availableServices) {
                saveLocalService(service);
            }

            connection.commit();
            System.out.println("Sample data initialized successfully");
        } catch (SQLException e) {
            try {
                // Rollback the transaction
                connection.rollback();
            } catch (SQLException rollbackEx) {
                System.err.println("Error during rollback: " + rollbackEx.getMessage());
            }
            System.err.println("Error initializing sample data: " + e.getMessage());
        } finally {
            try {
                // Only reset auto-commit if we changed it
                if (connection.getAutoCommit() != previousAutoCommit) {
                    connection.setAutoCommit(previousAutoCommit);
                }
            } catch (SQLException e) {
                System.err.println("Error resetting auto-commit: " + e.getMessage());
            }
        }
    }
}