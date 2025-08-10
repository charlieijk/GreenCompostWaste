package com.greencompost.main;
import com.greencompost.controller.FoodItem;
import com.greencompost.model.DatabaseManager;
import com.greencompost.service.LocalService;
import com.greencompost.service.OperatingHours;
import com.greencompost.User;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Scanner;

/**
 * Main class for the GreenCompost Connect application
 * use this to compile and run:  java -cp bin Main
 */
public class Main extends Application {
    private static FoodItem foodItemController;
    private static User currentUser;
    // Scanner for console input - used by commented out console methods
    private static final Scanner scanner = new Scanner(System.in);

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialize components
        foodItemController = new FoodItem();
        
        // Initialize SQLite database
        DatabaseManager dbManager = DatabaseManager.getInstance();
        
        // Initialize only service data for demo purposes, but don't create users
        // This will allow the login controller to handle user creation and authentication
        initializeSampleServices();
        
        // Initialize database with sample data
        dbManager.initializeSampleData();
        
        // Load the login FXML file first
        URL loginViewURL = null;
        
        // Try different paths to find the FXML
        String[] fxmlPaths = {
            "/views/LoginView.fxml",
            "views/LoginView.fxml",
            "/main/resources/views/LoginView.fxml",
            "main/resources/views/LoginView.fxml",
            "../resources/views/LoginView.fxml",
            "../../../resources/views/LoginView.fxml"
        };
        
        for (String path : fxmlPaths) {
            loginViewURL = getClass().getResource(path);
            if (loginViewURL != null) {
                System.out.println("Found FXML at: " + path);
                break;
            }
            loginViewURL = getClass().getClassLoader().getResource(path);
            if (loginViewURL != null) {
                System.out.println("Found FXML with ClassLoader at: " + path);
                break;
            }
        }
        
        if (loginViewURL == null) {
            throw new IOException("Cannot find LoginView.fxml. Please check resources path.");
        }
        
        FXMLLoader loader = new FXMLLoader(loginViewURL);
        Parent root = loader.load();
        
        // Set up the primary scene - try different paths for CSS
        Scene scene = new Scene(root, 900, 600);
        
        URL cssURL = null;
        
        // Try different paths to find the CSS
        String[] cssPaths = {
            "/styles/styles.css",
            "styles/styles.css",
            "/main/resources/styles/styles.css",
            "main/resources/styles/styles.css",
            "../resources/styles/styles.css",
            "../../../resources/styles/styles.css"
        };
        
        for (String path : cssPaths) {
            cssURL = getClass().getResource(path);
            if (cssURL != null) {
                System.out.println("Found CSS at: " + path);
                break;
            }
            cssURL = getClass().getClassLoader().getResource(path);
            if (cssURL != null) {
                System.out.println("Found CSS with ClassLoader at: " + path);
                break;
            }
        }
        
        if (cssURL != null) {
            scene.getStylesheets().add(cssURL.toExternalForm());
        } else {
            System.out.println("Warning: Could not find styles.css. Please check resources path.");
        }
        
        // Configure and show the primary stage
        primaryStage.setTitle("GreenCompost Connect - Login");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    @Override
    public void stop() {
        // Close database connection when application exits
        DatabaseManager.getInstance().closeConnection();
        // Close the scanner resource
        scanner.close();
    }
    
    /**
     * Get the current user
     * @return The current user
     */
    public static User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Set the current user (called by LoginController)
     * @param user The authenticated user
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }
    
    /**
     * Get the food item controller
     * @return The food item controller
     */
    public static FoodItem getFoodItemController() {
        return foodItemController;
    }

    
    public static void initializeSampleDataForUser() {
        // Don't initialize if user hasn't been set yet
        if (currentUser == null) {
            return;
        }
        
        // Check if user already has food items
        if (!currentUser.getFoodItems().isEmpty()) {
            // User already has data, don't duplicate
            return;
        }
        
        // Add sample food items
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        LocalDateTime nextWeek = LocalDateTime.now().plusDays(7);
        LocalDateTime twoWeeks = LocalDateTime.now().plusDays(14);
        
        foodItemController.addFoodItem(currentUser, "Apples", 2.5, "kg", tomorrow, FoodItem.FoodCategory.FRUIT);
        foodItemController.addFoodItem(currentUser, "Bread", 1.0, "loaf", tomorrow, FoodItem.FoodCategory.GRAIN);
        foodItemController.addFoodItem(currentUser, "Milk", 2.0, "liters", LocalDateTime.now().plusDays(3), FoodItem.FoodCategory.DAIRY);
        foodItemController.addFoodItem(currentUser, "Pasta", 0.5, "kg", nextWeek, FoodItem.FoodCategory.GRAIN);
        foodItemController.addFoodItem(currentUser, "Leftover Lasagna", 1.0, "portion", tomorrow, FoodItem.FoodCategory.LEFTOVER_MEAL);
        foodItemController.addFoodItem(currentUser, "Chicken", 1.2, "kg", LocalDateTime.now().plusDays(2), FoodItem.FoodCategory.PROTEIN);
        foodItemController.addFoodItem(currentUser, "Potatoes", 3.0, "kg", twoWeeks, FoodItem.FoodCategory.VEGETABLE);
    }
    
    /**
     * Initialize only service data for demo purposes
     */
    private static void initializeSampleServices() {
        // Create some sample services only - no user data yet
        createSampleServices();
    }
    
    /**
     * Create sample local services
     */
    private static void createSampleServices() {
        // ===== CORK SERVICES =====
        // Food bank
        LocalService foodBank = new LocalService(
            "Cork Food Bank", 
            "123 Main Street, Cork", 
            "foodbank@example.com", 
            51.89, 
            -8.47, 
            LocalService.ServiceType.FOOD_BANK
        );
        foodBank.setPickupAvailable(true);
        foodBank.setPickupRadius(5.0);
        foodBank.addAcceptedItem("Canned goods");
        foodBank.addAcceptedItem("Fresh produce");
        foodBank.addAcceptedItem("Bread");
        foodBank.addAcceptedItem("Non-perishable items");
        
        // Set specific operating hours for food bank
        OperatingHours foodBankHours = new OperatingHours();
        foodBankHours.addHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        foodBankHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        foodBankHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        foodBankHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(19, 0)); // Extended hours
        foodBankHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        foodBankHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(10, 0), LocalTime.of(14, 0));
        foodBank.setHours(foodBankHours);
        
        // Composting facility
        LocalService compost = new LocalService(
            "Green Earth Composting", 
            "45 Garden Road, Cork", 
            "compost@example.com", 
            51.92, 
            -8.49, 
            LocalService.ServiceType.COMPOSTING_FACILITY
        );
        compost.setPickupAvailable(false);
        compost.addAcceptedItem("Fruit and vegetable scraps");
        compost.addAcceptedItem("Coffee grounds");
        compost.addAcceptedItem("Eggshells");
        compost.addAcceptedItem("Yard waste");
        compost.addNonAcceptedItem("Meat");
        compost.addNonAcceptedItem("Dairy");
        
        // Set specific operating hours for composting facility
        OperatingHours compostHours = new OperatingHours();
        compostHours.addHours(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(16, 0));
        compostHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(16, 0));
        compostHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(16, 0));
        compostHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(16, 0));
        compostHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(16, 0));
        compostHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(12, 0));
        compost.setHours(compostHours);
        
        // Community garden
        LocalService garden = new LocalService(
            "Community Garden Co-op", 
            "78 Park Lane, Cork", 
            "garden@example.com", 
            51.88, 
            -8.45, 
            LocalService.ServiceType.COMMUNITY_GARDEN
        );
        garden.setPickupAvailable(false);
        garden.addAcceptedItem("Fruit and vegetable scraps");
        garden.addAcceptedItem("Coffee grounds");
        
        // Set specific operating hours for community garden
        OperatingHours gardenHours = new OperatingHours();
        gardenHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(14, 0), LocalTime.of(18, 0));
        gardenHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        gardenHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        garden.setHours(gardenHours);
        
        // Restaurant
        LocalService restaurant = new LocalService(
            "Green Plate Restaurant", 
            "203 River Street, Cork", 
            "greenplate@example.com", 
            51.87, 
            -8.48, 
            LocalService.ServiceType.RESTAURANT
        );
        restaurant.setPickupAvailable(false);
        restaurant.addAcceptedItem("Fresh produce");
        restaurant.addAcceptedItem("Herbs");
        
        // Set specific operating hours for restaurant
        OperatingHours restaurantHours = new OperatingHours();
        restaurantHours.addHours(DayOfWeek.MONDAY, LocalTime.of(11, 0), LocalTime.of(22, 0));
        restaurantHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(11, 0), LocalTime.of(22, 0));
        restaurantHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(11, 0), LocalTime.of(22, 0));
        restaurantHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(11, 0), LocalTime.of(22, 0));
        restaurantHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(11, 0), LocalTime.of(23, 0));
        restaurantHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(11, 0), LocalTime.of(23, 0));
        restaurantHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(12, 0), LocalTime.of(21, 0));
        restaurant.setHours(restaurantHours);
        
        // ===== DUBLIN SERVICES =====
        // Food Bank
        LocalService dublinFoodBank = new LocalService(
            "Dublin Community Food Bank",
            "42 O'Connell Street, Dublin",
            "dublinfoodbank@example.com",
            53.349,
            -6.260,
            LocalService.ServiceType.FOOD_BANK
        );
        dublinFoodBank.setPickupAvailable(true);
        dublinFoodBank.setPickupRadius(6.0);
        dublinFoodBank.setAcceptsFoodDonations(true);
        dublinFoodBank.addAcceptedItem("Canned goods");
        dublinFoodBank.addAcceptedItem("Pasta and rice");
        dublinFoodBank.addAcceptedItem("Fresh produce");
        dublinFoodBank.addAcceptedItem("Baby food");
        dublinFoodBank.addDonationGuideline("No expired items");
        dublinFoodBank.addDonationGuideline("Please check packaging is intact");
        
        OperatingHours dublinFoodBankHours = new OperatingHours();
        dublinFoodBankHours.addHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        dublinFoodBankHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        dublinFoodBankHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        dublinFoodBankHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        dublinFoodBankHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        dublinFoodBankHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        dublinFoodBank.setHours(dublinFoodBankHours);
        
        // Community Fridge
        LocalService dublinFridge = new LocalService(
            "Dublin Community Fridge",
            "87 Grafton Street, Dublin",
            "dublinfridge@example.com",
            53.341,
            -6.259,
            LocalService.ServiceType.COMMUNITY_FRIDGE
        );
        dublinFridge.setPickupAvailable(false);
        dublinFridge.setAcceptsFoodDonations(true);
        dublinFridge.addAcceptedItem("Fresh produce");
        dublinFridge.addAcceptedItem("Sealed packaged foods");
        dublinFridge.addAcceptedItem("Dairy products");
        dublinFridge.addNonAcceptedItem("Open containers");
        dublinFridge.addNonAcceptedItem("Homemade meals");
        dublinFridge.addDonationGuideline("Items must have at least 48 hours before expiry");
        
        OperatingHours dublinFridgeHours = new OperatingHours();
        dublinFridgeHours.addHours(DayOfWeek.MONDAY, LocalTime.of(7, 0), LocalTime.of(22, 0));
        dublinFridgeHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(7, 0), LocalTime.of(22, 0));
        dublinFridgeHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(7, 0), LocalTime.of(22, 0));
        dublinFridgeHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(7, 0), LocalTime.of(22, 0));
        dublinFridgeHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(7, 0), LocalTime.of(22, 0));
        dublinFridgeHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(8, 0), LocalTime.of(22, 0));
        dublinFridgeHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(8, 0), LocalTime.of(22, 0));
        dublinFridge.setHours(dublinFridgeHours);
        
        // Urban Farm
        LocalService dublinFarm = new LocalService(
            "Dublin City Farm",
            "15 Phoenix Park, Dublin",
            "dublinfarm@example.com",
            53.356,
            -6.319,
            LocalService.ServiceType.URBAN_FARM
        );
        dublinFarm.setPickupAvailable(false);
        dublinFarm.setAcceptsFoodDonations(true);
        dublinFarm.addAcceptedItem("Vegetable scraps");
        dublinFarm.addAcceptedItem("Coffee grounds");
        dublinFarm.addAcceptedItem("Eggshells");
        dublinFarm.addNonAcceptedItem("Meat");
        dublinFarm.addNonAcceptedItem("Dairy");
        dublinFarm.addNonAcceptedItem("Citrus peels");
        
        OperatingHours dublinFarmHours = new OperatingHours();
        dublinFarmHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        dublinFarmHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        dublinFarmHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        dublinFarm.setHours(dublinFarmHours);
        
        // Soup Kitchen
        LocalService dublinSoupKitchen = new LocalService(
            "Dublin Soup Kitchen",
            "123 North Circular Road, Dublin",
            "dublinsoup@example.com",
            53.359,
            -6.267,
            LocalService.ServiceType.SOUP_KITCHEN
        );
        dublinSoupKitchen.setPickupAvailable(false);
        dublinSoupKitchen.setAcceptsFoodDonations(true);
        dublinSoupKitchen.addAcceptedItem("Fresh produce");
        dublinSoupKitchen.addAcceptedItem("Bread");
        dublinSoupKitchen.addAcceptedItem("Packaged foods");
        dublinSoupKitchen.addAcceptedItem("Meats");
        dublinSoupKitchen.addDonationGuideline("Fresh food must have at least 3 days before expiry");
        
        OperatingHours dublinSoupHours = new OperatingHours();
        dublinSoupHours.addHours(DayOfWeek.MONDAY, LocalTime.of(17, 0), LocalTime.of(20, 0));
        dublinSoupHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(17, 0), LocalTime.of(20, 0));
        dublinSoupHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(17, 0), LocalTime.of(20, 0));
        dublinSoupHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(12, 0), LocalTime.of(15, 0));
        dublinSoupKitchen.setHours(dublinSoupHours);
        
        // ===== GALWAY SERVICES =====
        // Food Pantry
        LocalService galwayPantry = new LocalService(
            "Galway Food Pantry",
            "25 Shop Street, Galway",
            "galwaypantry@example.com",
            53.272,
            -9.049,
            LocalService.ServiceType.FOOD_PANTRY
        );
        galwayPantry.setPickupAvailable(true);
        galwayPantry.setPickupRadius(3.0);
        galwayPantry.setAcceptsFoodDonations(true);
        galwayPantry.addAcceptedItem("Canned goods");
        galwayPantry.addAcceptedItem("Dry goods");
        galwayPantry.addAcceptedItem("Cooking oils");
        galwayPantry.addDonationGuideline("Non-perishable items preferred");
        
        OperatingHours galwayPantryHours = new OperatingHours();
        galwayPantryHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        galwayPantryHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        galwayPantryHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(13, 0));
        galwayPantry.setHours(galwayPantryHours);
        
        // Composting Facility
        LocalService galwayCompost = new LocalService(
            "Galway Green Composting",
            "45 Salthill Road, Galway",
            "galwaycompost@example.com",
            53.260,
            -9.067,
            LocalService.ServiceType.COMPOSTING_FACILITY
        );
        galwayCompost.setPickupAvailable(true);
        galwayCompost.setPickupRadius(4.0);
        galwayCompost.addAcceptedItem("All food waste");
        galwayCompost.addAcceptedItem("Yard trimmings");
        galwayCompost.addAcceptedItem("Paper products");
        
        OperatingHours galwayCompostHours = new OperatingHours();
        galwayCompostHours.addHours(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));
        galwayCompostHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));
        galwayCompostHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));
        galwayCompostHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));
        galwayCompostHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(18, 0));
        galwayCompost.setHours(galwayCompostHours);
        
        // Restaurant
        LocalService galwayRestaurant = new LocalService(
            "Galway Sustainable Eats",
            "12 Quay Street, Galway",
            "galwayeats@example.com",
            53.271,
            -9.054,
            LocalService.ServiceType.RESTAURANT
        );
        galwayRestaurant.setPickupAvailable(false);
        galwayRestaurant.setAcceptsFoodDonations(true);
        galwayRestaurant.addAcceptedItem("Local produce");
        galwayRestaurant.addAcceptedItem("Fresh seafood");
        galwayRestaurant.addDonationGuideline("Must be collected within 24 hours");
        
        OperatingHours galwayRestaurantHours = new OperatingHours();
        galwayRestaurantHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(12, 0), LocalTime.of(21, 0));
        galwayRestaurantHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(12, 0), LocalTime.of(21, 0));
        galwayRestaurantHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(12, 0), LocalTime.of(21, 0));
        galwayRestaurantHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(12, 0), LocalTime.of(22, 0));
        galwayRestaurantHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(12, 0), LocalTime.of(22, 0));
        galwayRestaurantHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(12, 0), LocalTime.of(20, 0));
        galwayRestaurant.setHours(galwayRestaurantHours);
        
        // ===== LIMERICK SERVICES =====
        // Food Donation Center
        LocalService limerickDonation = new LocalService(
            "Limerick Food Share",
            "78 O'Connell Street, Limerick",
            "limerickfoodshare@example.com",
            52.662,
            -8.623,
            LocalService.ServiceType.FOOD_DONATION_CENTER
        );
        limerickDonation.setPickupAvailable(true);
        limerickDonation.setPickupRadius(5.0);
        limerickDonation.setAcceptsFoodDonations(true);
        limerickDonation.addAcceptedItem("All packaged foods");
        limerickDonation.addAcceptedItem("Fresh produce");
        limerickDonation.addDonationGuideline("All items must be in original packaging");
        
        OperatingHours limerickDonationHours = new OperatingHours();
        limerickDonationHours.addHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        limerickDonationHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        limerickDonationHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        limerickDonation.setHours(limerickDonationHours);
        
        // Community Garden
        LocalService limerickGarden = new LocalService(
            "Limerick Community Garden",
            "45 People's Park, Limerick",
            "limerickgarden@example.com",
            52.658,
            -8.630,
            LocalService.ServiceType.COMMUNITY_GARDEN
        );
        limerickGarden.setPickupAvailable(false);
        limerickGarden.addAcceptedItem("Vegetable scraps");
        limerickGarden.addAcceptedItem("Coffee grounds");
        limerickGarden.addAcceptedItem("Eggshells");
        
        OperatingHours limerickGardenHours = new OperatingHours();
        limerickGardenHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        limerickGardenHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        limerickGarden.setHours(limerickGardenHours);
        
        // ===== BELFAST SERVICES =====
        // Food Bank
        LocalService belfastFoodBank = new LocalService(
            "Belfast Food Bank",
            "123 Royal Avenue, Belfast",
            "belfastfoodbank@example.com",
            54.600,
            -5.930,
            LocalService.ServiceType.FOOD_BANK
        );
        belfastFoodBank.setPickupAvailable(true);
        belfastFoodBank.setPickupRadius(7.0);
        belfastFoodBank.setAcceptsFoodDonations(true);
        belfastFoodBank.addAcceptedItem("Canned goods");
        belfastFoodBank.addAcceptedItem("Pasta and rice");
        belfastFoodBank.addAcceptedItem("Baby food");
        belfastFoodBank.addAcceptedItem("Toiletries");
        
        OperatingHours belfastFoodBankHours = new OperatingHours();
        belfastFoodBankHours.addHours(DayOfWeek.MONDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        belfastFoodBankHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        belfastFoodBankHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        belfastFoodBankHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        belfastFoodBankHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        belfastFoodBank.setHours(belfastFoodBankHours);
        
        // Community Fridge
        LocalService belfastFridge = new LocalService(
            "Belfast Community Fridge",
            "45 Botanic Avenue, Belfast",
            "belfastfridge@example.com",
            54.585,
            -5.935,
            LocalService.ServiceType.COMMUNITY_FRIDGE
        );
        belfastFridge.setPickupAvailable(false);
        belfastFridge.setAcceptsFoodDonations(true);
        belfastFridge.addAcceptedItem("Fresh produce");
        belfastFridge.addAcceptedItem("Dairy products");
        belfastFridge.addAcceptedItem("Packaged foods");
        belfastFridge.addNonAcceptedItem("Open items");
        belfastFridge.addNonAcceptedItem("Expired food");
        
        OperatingHours belfastFridgeHours = new OperatingHours();
        belfastFridgeHours.addHours(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(20, 0));
        belfastFridgeHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(20, 0));
        belfastFridgeHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(20, 0));
        belfastFridgeHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(20, 0));
        belfastFridgeHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(20, 0));
        belfastFridgeHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(18, 0));
        belfastFridgeHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        belfastFridge.setHours(belfastFridgeHours);
        
        // ===== SAN FRANCISCO SERVICES =====
        // Food Bank
        LocalService sfFoodBank = new LocalService(
            "San Francisco-Marin Food Bank",
            "900 Pennsylvania Avenue, San Francisco",
            "info@sfmfoodbank.org",
            37.754,
            -122.393,
            LocalService.ServiceType.FOOD_BANK
        );
        sfFoodBank.setPickupAvailable(true);
        sfFoodBank.setPickupRadius(8.0);
        sfFoodBank.setAcceptsFoodDonations(true);
        sfFoodBank.addAcceptedItem("Canned goods");
        sfFoodBank.addAcceptedItem("Dried beans and rice");
        sfFoodBank.addAcceptedItem("Fresh produce");
        sfFoodBank.addAcceptedItem("Protein items");
        sfFoodBank.addDonationGuideline("All items must be unopened and within expiration date");
        sfFoodBank.addDonationGuideline("No homemade or prepared foods");
        
        OperatingHours sfFoodBankHours = new OperatingHours();
        sfFoodBankHours.addHours(DayOfWeek.MONDAY, LocalTime.of(8, 0), LocalTime.of(17, 0));
        sfFoodBankHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(8, 0), LocalTime.of(17, 0));
        sfFoodBankHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(8, 0), LocalTime.of(17, 0));
        sfFoodBankHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(8, 0), LocalTime.of(17, 0));
        sfFoodBankHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(8, 0), LocalTime.of(17, 0));
        sfFoodBankHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(16, 0));
        sfFoodBank.setHours(sfFoodBankHours);
        
        // Community Garden
        LocalService sfGarden = new LocalService(
            "Hayes Valley Farm",
            "450 Laguna Street, San Francisco",
            "info@hayesvalleyfarm.org",
            37.776,
            -122.426,
            LocalService.ServiceType.COMMUNITY_GARDEN
        );
        sfGarden.setPickupAvailable(false);
        sfGarden.setAcceptsFoodDonations(false);
        sfGarden.addAcceptedItem("Food scraps for composting");
        sfGarden.addAcceptedItem("Coffee grounds");
        sfGarden.addAcceptedItem("Yard waste");
        sfGarden.addNonAcceptedItem("Meat products");
        sfGarden.addNonAcceptedItem("Dairy products");
        sfGarden.addNonAcceptedItem("Oil and fatty foods");
        
        OperatingHours sfGardenHours = new OperatingHours();
        sfGardenHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(12, 0), LocalTime.of(17, 0));
        sfGardenHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        sfGardenHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(10, 0), LocalTime.of(16, 0));
        sfGarden.setHours(sfGardenHours);
        
        // Food Donation Center
        LocalService sfDonationCenter = new LocalService(
            "Food Runners SF",
            "2579 Washington Street, San Francisco",
            "contact@foodrunners.org",
            37.791,
            -122.434,
            LocalService.ServiceType.FOOD_DONATION_CENTER
        );
        sfDonationCenter.setPickupAvailable(true);
        sfDonationCenter.setPickupRadius(10.0);
        sfDonationCenter.setAcceptsFoodDonations(true);
        sfDonationCenter.addAcceptedItem("Prepared foods from restaurants");
        sfDonationCenter.addAcceptedItem("Packaged foods");
        sfDonationCenter.addAcceptedItem("Fresh produce");
        sfDonationCenter.addAcceptedItem("Baked goods");
        sfDonationCenter.addDonationGuideline("Prepared foods must be properly stored and handled");
        sfDonationCenter.addDonationGuideline("Foods must not have been previously served");
        
        OperatingHours sfDonationHours = new OperatingHours();
        sfDonationHours.addHours(DayOfWeek.MONDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        sfDonationHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        sfDonationHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        sfDonationHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        sfDonationHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(9, 0), LocalTime.of(17, 0));
        sfDonationCenter.setHours(sfDonationHours);
        
        // Urban Farm
        LocalService sfFarm = new LocalService(
            "Alemany Farm",
            "700 Alemany Blvd, San Francisco",
            "community@alemanyfarm.org",
            37.733,
            -122.413,
            LocalService.ServiceType.URBAN_FARM
        );
        sfFarm.setPickupAvailable(false);
        sfFarm.setAcceptsFoodDonations(false);
        sfFarm.addAcceptedItem("Compostable materials");
        sfFarm.addAcceptedItem("Plant cuttings");
        sfFarm.addAcceptedItem("Seeds for donation");
        
        OperatingHours sfFarmHours = new OperatingHours();
        sfFarmHours.addHours(DayOfWeek.MONDAY, LocalTime.of(12, 0), LocalTime.of(17, 0));
        sfFarmHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(12, 0), LocalTime.of(17, 0));
        sfFarmHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(9, 0), LocalTime.of(16, 0));
        sfFarmHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(12, 0), LocalTime.of(16, 0));
        sfFarm.setHours(sfFarmHours);
        
        // Community Fridge
        LocalService sfFridge = new LocalService(
            "Freedom Community Fridge",
            "3789 24th Street, San Francisco",
            "freedomfridge@example.com",
            37.752,
            -122.429,
            LocalService.ServiceType.COMMUNITY_FRIDGE
        );
        sfFridge.setPickupAvailable(false);
        sfFridge.setAcceptsFoodDonations(true);
        sfFridge.addAcceptedItem("Fresh produce");
        sfFridge.addAcceptedItem("Unopened packaged foods");
        sfFridge.addAcceptedItem("Beverages");
        sfFridge.addNonAcceptedItem("Homemade foods without labels");
        sfFridge.addNonAcceptedItem("Raw meat or seafood");
        sfFridge.addNonAcceptedItem("Opened foods");
        sfFridge.addDonationGuideline("All items must be labeled with contents and date");
        sfFridge.addDonationGuideline("No expired food");
        
        OperatingHours sfFridgeHours = new OperatingHours();
        sfFridgeHours.addHours(DayOfWeek.MONDAY, LocalTime.of(0, 0), LocalTime.of(23, 59));
        sfFridgeHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(0, 0), LocalTime.of(23, 59));
        sfFridgeHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(0, 0), LocalTime.of(23, 59));
        sfFridgeHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(0, 0), LocalTime.of(23, 59));
        sfFridgeHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(0, 0), LocalTime.of(23, 59));
        sfFridgeHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(0, 0), LocalTime.of(23, 59));
        sfFridgeHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(0, 0), LocalTime.of(23, 59));
        sfFridge.setHours(sfFridgeHours);
        
        // Restaurant with food waste program
        LocalService sfRestaurant = new LocalService(
            "Green Table Restaurant",
            "432 Market Street, San Francisco",
            "info@greentablesf.com",
            37.794,
            -122.398,
            LocalService.ServiceType.RESTAURANT
        );
        sfRestaurant.setPickupAvailable(false);
        sfRestaurant.setAcceptsFoodDonations(true);
        sfRestaurant.addAcceptedItem("Excess produce");
        sfRestaurant.addAcceptedItem("Imperfect produce");
        sfRestaurant.addDonationGuideline("Contact by email before bringing donations");
        
        OperatingHours sfRestaurantHours = new OperatingHours();
        sfRestaurantHours.addHours(DayOfWeek.TUESDAY, LocalTime.of(17, 0), LocalTime.of(22, 0));
        sfRestaurantHours.addHours(DayOfWeek.WEDNESDAY, LocalTime.of(17, 0), LocalTime.of(22, 0));
        sfRestaurantHours.addHours(DayOfWeek.THURSDAY, LocalTime.of(17, 0), LocalTime.of(22, 0));
        sfRestaurantHours.addHours(DayOfWeek.FRIDAY, LocalTime.of(17, 0), LocalTime.of(23, 0));
        sfRestaurantHours.addHours(DayOfWeek.SATURDAY, LocalTime.of(17, 0), LocalTime.of(23, 0));
        sfRestaurantHours.addHours(DayOfWeek.SUNDAY, LocalTime.of(11, 0), LocalTime.of(15, 0));
        sfRestaurant.setHours(sfRestaurantHours);
    }
}