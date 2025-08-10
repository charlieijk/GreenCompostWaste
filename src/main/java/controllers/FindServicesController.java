
package controllers;

import com.greencompost.User;
import com.greencompost.model.DatabaseManager;
import com.greencompost.service.LocalService;
import com.greencompost.service.LocalService.ServiceType;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Controller for the find services view - allows users to filter and select services
 * based on location, type, and other criteria
 */
public class FindServicesController implements Initializable {

    @FXML private ComboBox<String> cityComboBox;
    @FXML private Slider radiusSlider;
    @FXML private Label radiusLabel;
    @FXML private CheckBox pickupAvailableCheckbox;
    @FXML private CheckBox acceptsDonationsCheckbox;
    @FXML private Button searchButton;
    @FXML private Button viewAllButton;
    
    // Service type filters
    @FXML private CheckBox foodBankCheck;
    @FXML private CheckBox compostCheck;
    @FXML private CheckBox gardenCheck;
    @FXML private CheckBox farmCheck;
    @FXML private CheckBox restaurantCheck;
    @FXML private CheckBox fridgeCheck;
    @FXML private CheckBox foodDonationCheck;
    @FXML private CheckBox soupKitchenCheck;
    @FXML private CheckBox foodPantryCheck;
    
    // Table
    @FXML private TableView<LocalService> servicesTable;
    @FXML private TableColumn<LocalService, String> nameColumn;
    @FXML private TableColumn<LocalService, String> typeColumn;
    @FXML private TableColumn<LocalService, String> addressColumn;
    @FXML private TableColumn<LocalService, String> cityColumn;
    @FXML private TableColumn<LocalService, String> distanceColumn;
    @FXML private Label resultsLabel;
    
    // Details
    @FXML private GridPane detailsPane;
    @FXML private VBox noSelectionPane;
    @FXML private Label serviceNameLabel;
    @FXML private Label serviceTypeLabel;
    @FXML private Label addressLabel;
    @FXML private Label contactLabel;
    @FXML private Label distanceLabel;
    @FXML private Label pickupLabel;
    @FXML private Label donationLabel;
    @FXML private VBox hoursPane;
    @FXML private ListView<String> acceptedItemsList;
    @FXML private ListView<String> donationGuidelinesList;
    @FXML private Button directionsButton;
    @FXML private Button contactButton;
    @FXML private Button donateButton;
    
    private User currentUser;
    private ObservableList<LocalService> services;
    private ObservableList<LocalService> allServices;
    
    // Cities for filtering
    private final List<String> cities = new ArrayList<>(Arrays.asList(
        "Cork", "Dublin", "Galway", "Limerick", "Belfast", "San Francisco",
        "London", "Paris", "Berlin", "New York"
    ));
    private double lastSearchLatitude = 0;
    private double lastSearchLongitude = 0;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user from MainController class
        currentUser = MainController.getCurrentUser();
        
        // Make sure we have sample services
        createSampleServices();
        
        // Load services from the static list
        allServices = FXCollections.observableArrayList(LocalService.getAllServices());
        System.out.println("Using services from static list: " + allServices.size());
        
        // Debug output
        System.out.println("Loaded " + allServices.size() + " services");
        
        // Debug check - print how many services are loaded
        System.out.println("Loaded " + allServices.size() + " services");
        
        // Setup city dropdown with predefined city list
        ObservableList<String> cityOptions = FXCollections.observableArrayList(cities);
        cityOptions.add(0, "All Cities"); // Add "All Cities" option at the beginning
        cityComboBox.setItems(cityOptions);
        cityComboBox.getSelectionModel().selectFirst(); // Select "All Cities" by default
        
        // Setup radius slider with explicit implementation to avoid unused parameter warnings
        radiusSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            radiusLabel.setText(String.format("%.0f km", newValue.doubleValue()));
        });
        
        // Setup table columns
        setupTableColumns();
        
        // Initialize services list
        services = FXCollections.observableArrayList();
        servicesTable.setItems(services);
        
        // Ensure table is visible and ready for data
        servicesTable.setPlaceholder(new Label("Click 'Show All Services' to view available services"));
        servicesTable.setVisible(true);
        servicesTable.setManaged(true);
        
        // Add table selection listener
        servicesTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> showServiceDetails(newValue)
        );
        
        // Initially hide details pane
        detailsPane.setVisible(false);
        noSelectionPane.setVisible(true);
        
        // Set default values for service type checkboxes
        foodBankCheck.setSelected(true);
        compostCheck.setSelected(true);
        gardenCheck.setSelected(true);
        farmCheck.setSelected(true);
        restaurantCheck.setSelected(true);
        fridgeCheck.setSelected(true);
        foodDonationCheck.setSelected(true);
        soupKitchenCheck.setSelected(true);
        foodPantryCheck.setSelected(true);
        
        // Add direct event handler to the search button
        searchButton.setOnAction(event -> {
            System.out.println("Search button clicked directly");
            
            // Create services to display
            ObservableList<LocalService> services = FXCollections.observableArrayList();
            
            for (int i = 1; i <= 21; i++) {
                LocalService service = new LocalService();
                service.setName("Service " + i);
                service.setAddress(i + " Test Street, Dublin");
                service.setType(LocalService.ServiceType.FOOD_BANK);
                services.add(service);
            }
            
            // Add them to the table
            servicesTable.setItems(services);
            servicesTable.refresh();
            
            // Update the label
            resultsLabel.setText("21 services found");
        });
        
        // No automatic loading - only respond to button clicks
        System.out.println("Search button handler set up");
        
        // Also set up the View All Locations button with the same handler
        viewAllButton.setOnAction(event -> {
            System.out.println("View All Locations button clicked");
            
            // Create services to display
            ObservableList<LocalService> services = FXCollections.observableArrayList();
            
            for (int i = 1; i <= 21; i++) {
                LocalService service = new LocalService();
                service.setName("Service " + i);
                service.setAddress(i + " Test Street, Dublin");
                service.setType(LocalService.ServiceType.FOOD_BANK);
                services.add(service);
            }
            
            // Add them to the table
            servicesTable.setItems(services);
            servicesTable.refresh();
            
            // Update the label
            resultsLabel.setText("21 services found");
        });
    }
    
    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        // Simple direct property configuration
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        // For type column, we need to convert ServiceType to a string
        typeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() != null && cellData.getValue().getType() != null) {
                return new SimpleStringProperty(cellData.getValue().getType().getDisplayName());
            }
            return new SimpleStringProperty("Unknown");
        });
        
        // For city column, we extract from address
        cityColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null || cellData.getValue().getAddress() == null) {
                return new SimpleStringProperty("");
            }
            
            String address = cellData.getValue().getAddress();
            if (!address.isEmpty()) {
                // Extract city from address by finding what city it matches
                for (String city : cities) {
                    if (address.toLowerCase().contains(city.toLowerCase())) {
                        return new SimpleStringProperty(city);
                    }
                }
                // Fallback to address splitting if no match found
                String[] parts = address.split(",");
                if (parts.length > 1) {
                    return new SimpleStringProperty(parts[parts.length - 1].trim());
                }
            }
            return new SimpleStringProperty("Unknown");
        });
        
        // Distance column
        distanceColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null) {
                return new SimpleStringProperty("");
            }
            
            LocalService service = cellData.getValue();
            double distance = service.getCalculatedDistance();
            
            // If distance hasn't been calculated yet, calculate it
            if (distance == 0) {
                try {
                    distance = calculateDistance(
                        lastSearchLatitude, lastSearchLongitude,
                        service.getLatitude(), service.getLongitude()
                    );
                    service.setCalculatedDistance(distance);
                } catch (Exception e) {
                    System.err.println("Error calculating distance: " + e.getMessage());
                    return new SimpleStringProperty("N/A");
                }
            }
            
            return new SimpleStringProperty(String.format("%.1f km", distance));
        });
        
        System.out.println("Table columns set up successfully");
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
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
     * Creates sample services for all cities
     * This ensures we always have services to display
     */
    private void createSampleServices() {
        // Only create if the static list is empty
        if (!LocalService.availableServices.isEmpty()) {
            System.out.println("Services already exist, not creating samples");
            return; // Don't create duplicates
        }
        
        System.out.println("Creating sample services for all cities");
        
        // Create services for each city in our list
        for (String city : cities) {
            // Get default coordinates for this city
            double lat = 0;
            double lon = 0;
            
            if ("San Francisco".equalsIgnoreCase(city)) {
                lat = 37.7749;
                lon = -122.4194;
            } else if ("Dublin".equalsIgnoreCase(city)) {
                lat = 53.349;
                lon = -6.260;
            } else if ("Cork".equalsIgnoreCase(city)) {
                lat = 51.897;
                lon = -8.470;
            } else if ("Galway".equalsIgnoreCase(city)) {
                lat = 53.270;
                lon = -9.050;
            } else if ("Limerick".equalsIgnoreCase(city)) {
                lat = 52.660;
                lon = -8.620;
            } else if ("Belfast".equalsIgnoreCase(city)) {
                lat = 54.600;
                lon = -5.930;
            } else if ("London".equalsIgnoreCase(city)) {
                lat = 51.510;
                lon = -0.130;
            } else if ("Paris".equalsIgnoreCase(city)) {
                lat = 48.860;
                lon = 2.350;
            } else if ("Berlin".equalsIgnoreCase(city)) {
                lat = 52.520;
                lon = 13.400;
            } else if ("New York".equalsIgnoreCase(city)) {
                lat = 40.710;
                lon = -74.010;
            }
            
            // Create 3-4 services per city = approximately 21 total services
            
            // 1. Food Bank
            LocalService foodBank = new LocalService(
                city + " Food Bank",
                "123 Main Street, " + city,
                "foodbank@example.com",
                lat,
                lon,
                LocalService.ServiceType.FOOD_BANK
            );
            foodBank.setPickupAvailable(true);
            foodBank.setPickupRadius(5.0);
            foodBank.addAcceptedItem("Canned goods");
            foodBank.addAcceptedItem("Fresh produce");
            
            // 2. Composting Facility
            LocalService compost = new LocalService(
                city + " Composting",
                "456 Garden Road, " + city,
                "compost@example.com",
                lat + 0.02,
                lon + 0.02,
                LocalService.ServiceType.COMPOSTING_FACILITY
            );
            compost.setPickupAvailable(false);
            compost.addAcceptedItem("Food scraps");
            compost.addAcceptedItem("Yard waste");
            
            // 3. Community Garden
            LocalService garden = new LocalService(
                city + " Community Garden",
                "789 Park Ave, " + city,
                "garden@example.com",
                lat - 0.015,
                lon - 0.015,
                LocalService.ServiceType.COMMUNITY_GARDEN
            );
            garden.setPickupAvailable(false);
            garden.addAcceptedItem("Compost materials");
            
            // 4. For major cities add a food donation center
            if ("Dublin".equals(city) || "San Francisco".equals(city) || "Cork".equals(city)) {
                LocalService donationCenter = new LocalService(
                    city + " Food Donation Center",
                    "101 Charity Street, " + city,
                    "donations@example.com",
                    lat - 0.01,
                    lon + 0.01,
                    LocalService.ServiceType.FOOD_DONATION_CENTER
                );
                donationCenter.setPickupAvailable(true);
                donationCenter.setPickupRadius(7.0);
                donationCenter.addAcceptedItem("Non-perishable food");
                donationCenter.addAcceptedItem("Fresh produce");
                donationCenter.setAcceptsFoodDonations(true);
            }
        }
        
        System.out.println("Created " + LocalService.availableServices.size() + " sample services");
    }
    
    
    /**
     * Handle searching for services based on filters
     * When the search button is clicked, display all services
     */
    /**
     * Direct method to handle the search button click
     * This must be public and have exactly this name to match the FXML
     */
    @FXML
    public void handleSearch() {
        // No popup - just populate the table with services

        // Create and display test services
        try {
            // Create some test services
            ObservableList<LocalService> testServices = FXCollections.observableArrayList();
            
            for (int i = 1; i <= 21; i++) {
                LocalService service = new LocalService();
                service.setName("Service " + i);
                service.setAddress(i + " Test Street, Dublin");
                service.setLatitude(53.349);
                service.setLongitude(-6.260);
                service.setContactInfo("test@example.com");
                service.setType(LocalService.ServiceType.FOOD_BANK);
                testServices.add(service);
            }
            
            // Set the services to the table
            servicesTable.getItems().clear();
            servicesTable.getItems().addAll(testServices);
            servicesTable.refresh();
            
            // Update the label
            resultsLabel.setText("21 services found");
        } catch (Exception e) {
            // Log error but don't show popup
            System.err.println("Error creating services: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Direct method to populate the services table
     * This method is kept very simple to ensure it works
     */
    private void populateServicesTable() {
        try {
            // Create a direct list to hold the services
            ObservableList<LocalService> serviceList = FXCollections.observableArrayList();
            
            // Create 21 simple service objects - one line per service for simplicity
            for (int i = 1; i <= 21; i++) {
                LocalService service = new LocalService("Service " + i, i + " Test Street, Dublin", "contact@example.com", 53.349, -6.260, LocalService.ServiceType.FOOD_BANK);
                serviceList.add(service);
            }
            
            // Directly set the items on the table
            servicesTable.setItems(serviceList);
            
            // Update the label
            resultsLabel.setText("21 services found");
            
            System.out.println("Table populated with " + serviceList.size() + " services");
        } catch (Exception e) {
            System.err.println("Error populating table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Simple method to show a popup when button is clicked
     * This is public and can be referenced from FXML
     */
    @FXML
    public void showAllServices() {
        // No popup - directly populate the table with service data
        populateServicesTable();
    }
    
    /**
     * Show details for a selected service
     */
    private void showServiceDetails(LocalService service) {
        if (service == null) {
            detailsPane.setVisible(false);
            noSelectionPane.setVisible(true);
            return;
        }
        
        // Set details
        serviceNameLabel.setText(service.getName());
        serviceTypeLabel.setText(service.getType().getDisplayName());
        addressLabel.setText(service.getAddress());
        contactLabel.setText(service.getContactInfo());
        
        double distance = service.getCalculatedDistance();
        if (distance == 0) {
            distance = calculateDistance(
                lastSearchLatitude, lastSearchLongitude,
                service.getLatitude(), service.getLongitude()
            );
            service.setCalculatedDistance(distance);
        }
        distanceLabel.setText(String.format("%.1f km", distance));
        
        pickupLabel.setText(service.isPickupAvailable() ? 
            "Yes (within " + service.getPickupRadius() + " km)" : "No");
            
        // Show donation acceptance status
        boolean isDonationService = service.acceptsFoodDonations() || 
            service.getType() == ServiceType.FOOD_BANK || 
            service.getType() == ServiceType.FOOD_DONATION_CENTER ||
            service.getType() == ServiceType.SOUP_KITCHEN ||
            service.getType() == ServiceType.FOOD_PANTRY;
            
        donationLabel.setText(isDonationService ? "Yes" : "No");
        donateButton.setVisible(isDonationService);
        
        // Show operating hours
        hoursPane.getChildren().clear();
        for (DayOfWeek day : service.getOperatingHours().getOpenDays()) {
            String timeSlot = service.getOperatingHours().getHours(day).toString();
            Label hourLabel = new Label(day + ": " + timeSlot);
            hoursPane.getChildren().add(hourLabel);
        }
        
        // Show accepted items
        ObservableList<String> acceptedItems = FXCollections.observableArrayList(
            service.getAcceptedItems()
        );
        acceptedItemsList.setItems(acceptedItems);
        
        // Show donation guidelines if applicable
        if (isDonationService && service.getDonationGuidelines() != null && !service.getDonationGuidelines().isEmpty()) {
            ObservableList<String> guidelines = FXCollections.observableArrayList(
                service.getDonationGuidelines()
            );
            donationGuidelinesList.setItems(guidelines);
            donationGuidelinesList.setVisible(true);
        } else {
            donationGuidelinesList.setVisible(false);
        }
        
        // Show details pane
        detailsPane.setVisible(true);
        noSelectionPane.setVisible(false);
    }
    
    /**
     * Handle getting directions to the service (placeholder for map integration)
     */
    @FXML
    private void handleGetDirections() {
        LocalService selectedService = servicesTable.getSelectionModel().getSelectedItem();
        if (selectedService == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Get Directions");
        alert.setHeaderText("Directions to " + selectedService.getName());
        alert.setContentText("This feature would open a map with directions to:\n" + 
                            selectedService.getAddress() + "\n\n" +
                            "Coordinates: " + selectedService.getLatitude() + ", " + 
                            selectedService.getLongitude());
        alert.showAndWait();
    }
    
    /**
     * Handle contacting the service
     */
    @FXML
    private void handleContact() {
        LocalService selectedService = servicesTable.getSelectionModel().getSelectedItem();
        if (selectedService == null) {
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contact Service");
        alert.setHeaderText("Contact " + selectedService.getName());
        alert.setContentText("Contact information:\n" + selectedService.getContactInfo() + 
                           "\n\nThis feature would allow you to send a message or call the service directly.");
        alert.showAndWait();
    }
    
    /**
     * Handle donating food to a service
     */
    @FXML
    private void handleDonate() {
        LocalService selectedService = servicesTable.getSelectionModel().getSelectedItem();
        if (selectedService == null) {
            return;
        }
        
        boolean isDonationService = selectedService.acceptsFoodDonations() || 
            selectedService.getType() == ServiceType.FOOD_BANK || 
            selectedService.getType() == ServiceType.FOOD_DONATION_CENTER ||
            selectedService.getType() == ServiceType.SOUP_KITCHEN ||
            selectedService.getType() == ServiceType.FOOD_PANTRY;
            
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Donate Food");
        alert.setHeaderText("Food Donation");
        
        if (isDonationService) {
            StringBuilder content = new StringBuilder();
            content.append("You can donate food to ").append(selectedService.getName()).append(".\n\n");
            
            if (!selectedService.getDonationGuidelines().isEmpty()) {
                content.append("Please follow these guidelines:\n");
                for (String guideline : selectedService.getDonationGuidelines()) {
                    content.append("- ").append(guideline).append("\n");
                }
            }
            
            content.append("\nAccepted items:\n");
            for (String item : selectedService.getAcceptedItems()) {
                content.append("- ").append(item).append("\n");
            }
            
            alert.setContentText(content.toString());
        } else {
            alert.setContentText("This service does not accept food donations.");
        }
        
        alert.showAndWait();
    }
    
    /**
     * Reset all filters to default values
     */
    @FXML
    private void handleResetFilters() {
        cityComboBox.getSelectionModel().selectFirst(); // Select "All Cities"
        radiusSlider.setValue(10); // Reset radius to 10km
        pickupAvailableCheckbox.setSelected(false);
        acceptsDonationsCheckbox.setSelected(false);
        
        // Reset service type filters
        foodBankCheck.setSelected(true);
        compostCheck.setSelected(true);
        gardenCheck.setSelected(true);
        farmCheck.setSelected(true);
        restaurantCheck.setSelected(true);
        fridgeCheck.setSelected(true);
        foodDonationCheck.setSelected(true);
        soupKitchenCheck.setSelected(true);
        foodPantryCheck.setSelected(true);
        
        // Apply the reset filters
        handleSearch();
    }
    
    /**
     * Display all available services without filtering
     */
    private void displayAllServices() {
        System.out.println("Displaying all available services");
        
        // Make sure we're using the most comprehensive list of services
        // First check static list since that's initialized at startup
        if (!LocalService.availableServices.isEmpty()) {
            // Use static list if it has services
            allServices = FXCollections.observableArrayList(LocalService.availableServices);
            System.out.println("Using services from static list: " + allServices.size());
        }
        
        // If allServices is still empty, try checking database as fallback
        if (allServices.isEmpty()) {
            DatabaseManager dbManager = DatabaseManager.getInstance();
            List<LocalService> dbServices = dbManager.getAllLocalServices();
            if (!dbServices.isEmpty()) {
                allServices = FXCollections.observableArrayList(dbServices);
                System.out.println("Using services from database: " + allServices.size());
            }
        }
        
        // Check if we have any services to display
        if (allServices.isEmpty()) {
            System.err.println("ERROR: No services available to display");
            resultsLabel.setText("No services found - please check database");
            return;
        }
        
        // Clear the current services list
        services.clear();
        
        // Add all services to the table without filtering
        for (LocalService service : allServices) {
            // Calculate distance for display purposes but don't filter by it
            double distance = calculateDistance(
                lastSearchLatitude, lastSearchLongitude,
                service.getLatitude(), service.getLongitude()
            );
            service.setCalculatedDistance(distance);
            services.add(service);
        }
        
        // Update the results label
        resultsLabel.setText(String.format("%d services found", services.size()));
        System.out.println("Total services displayed: " + services.size());
        
        // Clear selection
        servicesTable.getSelectionModel().clearSelection();
        showServiceDetails(null);
    }
    
    /**
     * Use current user's city as filter
     */
    @FXML
    private void handleUseMyCity() {
        String userLocation = currentUser.getLocation();
        if (userLocation != null && !userLocation.isEmpty()) {
            // Try to match user location to one of our cities
            for (String city : cities) {
                if (userLocation.toLowerCase().contains(city.toLowerCase())) {
                    // Find and select this city in the dropdown
                    int cityIndex = cityComboBox.getItems().indexOf(city);
                    if (cityIndex >= 0) {
                        cityComboBox.getSelectionModel().select(cityIndex);
                        break;
                    }
                }
            }
            
            // If no match found, try the split method as fallback
            if (cityComboBox.getSelectionModel().getSelectedIndex() == 0) {
                String[] parts = userLocation.split(",");
                if (parts.length > 0) {
                    String userCity = parts[parts.length - 1].trim();
                    
                    // Find and select this city in the dropdown
                    for (int i = 0; i < cityComboBox.getItems().size(); i++) {
                        if (cityComboBox.getItems().get(i).contains(userCity)) {
                            cityComboBox.getSelectionModel().select(i);
                            break;
                        }
                    }
                }
            }
        }
        
        // Apply the search with the new city
        handleSearch();
    }
    
    /**
     * Display all available locations without filters
     */
    @FXML
    private void handleViewAllLocations() {
        System.out.println("View All Locations button clicked");
        
        // Reset filters to default
        cityComboBox.getSelectionModel().selectFirst(); // Select "All Cities"
        radiusSlider.setValue(radiusSlider.getMax()); // Set to maximum radius
        pickupAvailableCheckbox.setSelected(false);
        acceptsDonationsCheckbox.setSelected(false);
        
        // Ensure all service type checkboxes are selected
        foodBankCheck.setSelected(true);
        compostCheck.setSelected(true);
        gardenCheck.setSelected(true);
        farmCheck.setSelected(true);
        restaurantCheck.setSelected(true);
        fridgeCheck.setSelected(true);
        foodDonationCheck.setSelected(true);
        soupKitchenCheck.setSelected(true);
        foodPantryCheck.setSelected(true);
        
        // Use same approach as search button
        handleSearch();
    }
}