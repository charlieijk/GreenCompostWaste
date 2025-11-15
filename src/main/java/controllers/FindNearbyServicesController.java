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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.application.Platform;

import java.net.URL;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for the find nearby services view - allows users to find services near their location
 */
public class FindNearbyServicesController implements Initializable {

    @FXML private Slider radiusSlider;
    @FXML private Label radiusLabel;
    @FXML private Button searchButton;
    
    // Service type filters
    @FXML private CheckBox foodBankCheck;
    @FXML private CheckBox compostCheck;
    @FXML private CheckBox gardenCheck;
    @FXML private CheckBox farmCheck;
    @FXML private CheckBox restaurantCheck;
    @FXML private CheckBox fridgeCheck;
    @FXML private CheckBox showAllCheck;
    
    // Table
    @FXML private TableView<LocalService> servicesTable;
    @FXML private TableColumn<LocalService, String> nameColumn;
    @FXML private TableColumn<LocalService, String> typeColumn;
    @FXML private TableColumn<LocalService, String> addressColumn;
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
    @FXML private VBox hoursPane;
    
    private User currentUser;
    private ObservableList<LocalService> services;
    private List<LocalService> allServices;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("==== FindNearbyServicesController initializing ====");
        
        // Get current user
        currentUser = MainController.getCurrentUser();
        System.out.println("Current user: " + (currentUser != null ? currentUser.getUsername() : "null"));
        System.out.println("Current user city: " + (currentUser != null ? currentUser.getCity() : "null"));
        
        // Initialize services list
        services = FXCollections.observableArrayList();
        servicesTable.setItems(services);
        
        // Show placeholder text
        servicesTable.setPlaceholder(new Label("Click 'Search' to find nearby services"));
        
        // Setup table columns first
        setupTableColumns();
        
        // Add table selection listener with Platform.runLater to ensure UI updates
        servicesTable.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    showServiceDetails(newValue);
                    System.out.println("Selected service: " + (newValue != null ? newValue.getName() : "none"));
                });
            }
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
        
        // Set up the search button handler (only need to do this once)
        searchButton.setOnAction(event -> {
            System.out.println("Search button clicked");
            handleSearch();
        });
        
        // Setup radius slider with automatic refresh
        radiusSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            radiusLabel.setText(String.format("%.0f km", newValue.doubleValue()));
            // Auto-refresh on slider change, with a small delay
            if (Math.abs(newValue.doubleValue() - oldValue.doubleValue()) >= 5.0) {
                System.out.println("Radius changed significantly, refreshing results");
                handleSearch();
            }
        });
        
        // Setup filter checkbox listeners for auto-refreshing
        CheckBox[] checkboxes = new CheckBox[] {
            foodBankCheck, compostCheck, gardenCheck, farmCheck, 
            restaurantCheck, fridgeCheck, showAllCheck
        };
        
        for (CheckBox checkbox : checkboxes) {
            checkbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
                String checkboxName = checkbox.getText() != null ? checkbox.getText() : "showAllCheck";
                System.out.println("Filter checkbox changed: " + checkboxName + " = " + newValue);
                handleSearch(); // Auto-refresh when a filter is changed
            });
        }
        
        // Finally, create and show all city services on startup - do this last
        // to ensure UI is fully set up first
        Platform.runLater(() -> {
            System.out.println("Initializing services from all cities on startup");
            handleSearch(); // Automatically call handleSearch to load all services
        });
        
        System.out.println("==== FindNearbyServicesController initialization COMPLETE ====");
    }
    
    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        System.out.println("Setting up table columns in FindNearbyServicesController");
        
        // Basic string property
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        System.out.println("Name column setup complete");
        
        // Custom type display
        typeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() != null && cellData.getValue().getType() != null) {
                return new SimpleStringProperty(cellData.getValue().getType().getDisplayName());
            }
            return new SimpleStringProperty("Unknown");
        });
        System.out.println("Type column setup complete");
        
        // Show combined address with city
        addressColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null) {
                return new SimpleStringProperty("");
            }
            LocalService service = cellData.getValue();
            return new SimpleStringProperty(service.getAddress() + " (" + service.getCity() + ")");
        });
        System.out.println("Address column setup complete");
        
        // Distance column now uses the pre-calculated distance stored in the service
        distanceColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() == null) {
                return new SimpleStringProperty("");
            }
            
            LocalService service = cellData.getValue();
            double distance = service.getCalculatedDistance();
            
            // For debugging - print distance values to console
            System.out.println("Distance for " + service.getName() + ": " + distance + " km");
            
            // For very large distances (international cities), add more context
            if (distance > 100) {
                return new SimpleStringProperty(String.format("%.0f km (%s)", 
                    distance, getDistanceDescription(distance)));
            } else {
                return new SimpleStringProperty(String.format("%.1f km", distance));
            }
        });
        
        // Add cell factory to color-code distances
        distanceColumn.setCellFactory(column -> new TableCell<LocalService, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    
                    // Parse the distance value from the string (just get the numeric part)
                    double distance = 0;
                    try {
                        String numericPart = item.split(" ")[0];
                        distance = Double.parseDouble(numericPart);
                    } catch (Exception e) {
                        // If parsing fails, just don't apply color
                    }
                    
                    // Apply color based on distance
                    if (distance < 5) {
                        // Nearby - green
                        setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;"); 
                    } else if (distance < 20) {
                        // Medium distance - blue
                        setStyle("-fx-text-fill: #1976d2;"); 
                    } else if (distance < 100) {
                        // Far - orange
                        setStyle("-fx-text-fill: #ff8f00;");
                    } else {
                        // Very far - gray
                        setStyle("-fx-text-fill: #757575;");
                    }
                }
            }
        });
        System.out.println("Distance column setup complete");
        
        // Add sorting capability
        servicesTable.getSortOrder().add(distanceColumn);
        
        System.out.println("Table columns setup completed successfully");
    }
    
    /**
     * Calculate simple distance based on city matching
     * This implementation is consistent with the one in LocalService class
     * 
     * @param userCity The user's city
     * @param serviceCity The service's city
     * @return An estimated distance in kilometers
     */
    private double calculateSimpleDistance(String userCity, String serviceCity) {
        if (userCity == null || serviceCity == null) {
            return 999; // Unknown distance if cities are null
        }
        
        // Normalize city names (trim and lowercase)
        userCity = userCity.trim().toLowerCase();
        serviceCity = serviceCity.trim().toLowerCase();
        
        // Extract city from address if it contains a comma
        if (serviceCity.contains(",")) {
            serviceCity = serviceCity.split(",")[0].trim();
        }
        
        // Same city - very close
        if (userCity.equals(serviceCity)) {
            // Random distance between 1-10 km within same city
            return 1 + (Math.random() * 9);
        }
        
        // Cities in same country
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
     * This implementation is consistent with the one in LocalService class
     * 
     * @param city1 First city name
     * @param city2 Second city name
     * @return true if cities are in the same country, false otherwise
     */
    private boolean isSameCountry(String city1, String city2) {
        // Make sure city names are normalized to lowercase for comparison
        city1 = city1.toLowerCase().trim();
        city2 = city2.toLowerCase().trim();
        
        // Lists of cities by country (all lowercase)
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
        
        // Check if both cities are in the same country list
        return (irishCities.contains(city1) && irishCities.contains(city2)) ||
               (ukCities.contains(city1) && ukCities.contains(city2)) ||
               (usCities.contains(city1) && usCities.contains(city2)) ||
               (frenchCities.contains(city1) && frenchCities.contains(city2)) ||
               (germanCities.contains(city1) && germanCities.contains(city2));
    }
    
    /**
     * Get a textual description of distance
     */
    private String getDistanceDescription(double distance) {
        if (distance < 5) {
            return "Very close";
        } else if (distance < 20) {
            return "Nearby";
        } else if (distance < 50) {
            return "Same region";
        } else if (distance < 100) {
            return "Same country";
        } else if (distance < 1000) {
            return "International";
        } else {
            return "Long distance";
        }
    }
    
    /**
     * Calculate distance between two points using Haversine formula
     *
     * @deprecated This method is no longer used as we've switched to city-based distance calculation
     * @param lat1 First point latitude
     * @param lon1 First point longitude
     * @param lat2 Second point latitude
     * @param lon2 Second point longitude
     * @return Distance in kilometers
     */
    @Deprecated
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
     * Handle searching for services
     * This method creates services if needed, filters them based on selected criteria,
     * and displays the results in the table.
     */
    @FXML
    public void handleSearch() {
        System.out.println("Searching for services with filtering");
        
        try {
            // Create services if they don't exist
            if (LocalService.getAllServices().isEmpty()) {
                createAllCityServices();
            }
            
            // Get all available services
            List<LocalService> allCityServices = LocalService.getAllServices();
            System.out.println("Found " + allCityServices.size() + " total services");
            
            // Get filter settings
            boolean showAll = showAllCheck.isSelected();
            double radius = radiusSlider.getValue();
            String userCity = (currentUser != null) ? currentUser.getCity() : "Dublin";
            
            System.out.println("Filter settings: Show All = " + showAll + 
                              ", Radius = " + radius + " km, User City = " + userCity);
            
            // Count selected service types (for result label)
            int selectedTypeCount = 0;
            if (foodBankCheck.isSelected()) selectedTypeCount++;
            if (compostCheck.isSelected()) selectedTypeCount++;
            if (gardenCheck.isSelected()) selectedTypeCount++;
            if (farmCheck.isSelected()) selectedTypeCount++;
            if (restaurantCheck.isSelected()) selectedTypeCount++;
            if (fridgeCheck.isSelected()) selectedTypeCount++;
            
            // Create a filtered list
            List<LocalService> filteredServices = new ArrayList<>();
            
            // Process each service
            for (LocalService service : allCityServices) {
                // Check if service type is selected
                boolean typeSelected = false;
                
                if (service.getType() != null) {
                    switch (service.getType()) {
                        case FOOD_BANK:
                            typeSelected = foodBankCheck.isSelected();
                            break;
                        case COMPOSTING_FACILITY:
                            typeSelected = compostCheck.isSelected();
                            break;
                        case COMMUNITY_GARDEN:
                            typeSelected = gardenCheck.isSelected();
                            break;
                        case URBAN_FARM:
                            typeSelected = farmCheck.isSelected();
                            break;
                        case RESTAURANT:
                            typeSelected = restaurantCheck.isSelected();
                            break;
                        case COMMUNITY_FRIDGE:
                            typeSelected = fridgeCheck.isSelected();
                            break;
                        default:
                            typeSelected = true; // Include other types by default
                    }
                }
                
                // If type isn't selected, skip this service
                if (!typeSelected) {
                    System.out.println("Skipping " + service.getName() + " - type not selected");
                    continue;
                }
                
                // Calculate and store distance
                double distance = calculateSimpleDistance(userCity, service.getCity());
                service.setCalculatedDistance(distance);
                
                // Check if we should include this service
                if (showAll) {
                    // When "Show All" is checked, include all services regardless of distance
                    filteredServices.add(service);
                    System.out.println("Including " + service.getName() + " (" + service.getCity() + 
                                     ") - Show All mode - Distance: " + String.format("%.1f", distance) + " km");
                } else if (distance <= radius) {
                    // Only include services within radius when not in "Show All" mode
                    filteredServices.add(service);
                    System.out.println("Including " + service.getName() + " (" + service.getCity() + 
                                     ") - Within radius - Distance: " + String.format("%.1f", distance) + " km");
                } else {
                    System.out.println("Skipping " + service.getName() + " (" + service.getCity() + 
                                     ") - Too far (" + String.format("%.1f", distance) + " km)");
                }
            }
            
            // Update UI with filtered results - ENSURE this happens on the JavaFX thread
            final List<LocalService> finalFilteredServices = filteredServices;
            final int finalSelectedTypeCount = selectedTypeCount;
            final boolean finalShowAll = showAll;
            final double finalRadius = radius;
            final String finalUserCity = userCity;
            
            // Use Platform.runLater to ensure UI updates happen on the JavaFX application thread
            Platform.runLater(() -> {
                try {
                    // Update the table data
                    ObservableList<LocalService> displayServices = FXCollections.observableArrayList(finalFilteredServices);
                    servicesTable.setItems(displayServices);
                    servicesTable.refresh();
                    
                    // Update result label with detailed information
                    if (finalFilteredServices.isEmpty()) {
                        if (finalSelectedTypeCount == 0) {
                            // No service types selected
                            resultsLabel.setText("No results - select at least one service type");
                        } else if (!finalShowAll && finalRadius < 10) {
                            // Radius too small
                            resultsLabel.setText("No results - try increasing search radius");
                        } else {
                            // General case - no matching services
                            resultsLabel.setText("No services found matching your criteria");
                        }
                    } else if (finalShowAll) {
                        // Show all mode
                        if (finalSelectedTypeCount < 6) {
                            // Some types filtered
                            resultsLabel.setText(finalFilteredServices.size() + " services (filtered by type, all cities)");
                        } else {
                            // No type filtering
                            resultsLabel.setText(finalFilteredServices.size() + " services (from all cities)");
                        }
                    } else {
                        // Distance-based search
                        resultsLabel.setText(finalFilteredServices.size() + " services within " + 
                                           String.format("%.0f", finalRadius) + " km of " + finalUserCity);
                    }
                    
                    System.out.println("UI updated with " + finalFilteredServices.size() + " services");
                } catch (Exception e) {
                    System.err.println("Error updating UI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            System.out.println("Displayed " + filteredServices.size() + " services after filtering");
            
        } catch (Exception e) {
            System.err.println("Error searching for services: " + e.getMessage());
            e.printStackTrace();
            resultsLabel.setText("Error showing services");
        }
    }
    
    /**
     * Create services for all cities directly
     */
    private void createAllCityServices() {
        System.out.println("Creating services for ALL CITIES");
        
        // Clear existing services
        LocalService.availableServices.clear();
        
        // Dublin (3 services)
        LocalService service1 = new LocalService("Dublin Food Bank", "123 Main St, Dublin", "info@dub.ie", "Dublin", ServiceType.FOOD_BANK);
        service1.setCalculatedDistance(5.0); // Pre-set a default distance
        
        LocalService service2 = new LocalService("Dublin Garden Project", "45 Park Ave, Dublin", "garden@dub.ie", "Dublin", ServiceType.COMMUNITY_GARDEN);
        service2.setCalculatedDistance(7.5);
        
        LocalService service3 = new LocalService("Dublin Compost Co", "78 Green St, Dublin", "compost@dub.ie", "Dublin", ServiceType.COMPOSTING_FACILITY);
        service3.setCalculatedDistance(3.2);
        
        // Cork (3 services)
        LocalService service4 = new LocalService("Cork Food Share", "10 River St, Cork", "food@cork.ie", "Cork", ServiceType.FOOD_BANK);
        service4.setCalculatedDistance(65.8);
        
        LocalService service5 = new LocalService("Cork Urban Farm", "209 Hill Road, Cork", "farm@cork.ie", "Cork", ServiceType.URBAN_FARM);
        service5.setCalculatedDistance(67.3);
        
        LocalService service6 = new LocalService("Cork Restaurant Collective", "56 Main St, Cork", "food@corkrest.ie", "Cork", ServiceType.RESTAURANT);
        service6.setCalculatedDistance(66.1);
        
        // Limerick (3 services)
        LocalService service7 = new LocalService("Limerick Food Bank", "22 Shannon St, Limerick", "food@limerick.ie", "Limerick", ServiceType.FOOD_BANK);
        service7.setCalculatedDistance(95.0);
        
        LocalService service8 = new LocalService("Limerick Community Garden", "45 Abbey Road, Limerick", "garden@limerick.ie", "Limerick", ServiceType.COMMUNITY_GARDEN);
        service8.setCalculatedDistance(93.7);
        
        LocalService service9 = new LocalService("Limerick Fridge Network", "33 Castle St, Limerick", "fridge@limerick.ie", "Limerick", ServiceType.COMMUNITY_FRIDGE);
        service9.setCalculatedDistance(92.5);
        
        // Belfast (3 services)
        LocalService service10 = new LocalService("Belfast Food Network", "10 Falls Road, Belfast", "food@belfast.uk", "Belfast", ServiceType.FOOD_BANK);
        service10.setCalculatedDistance(85.6);
        
        LocalService service11 = new LocalService("Belfast City Farm", "22 Queens Road, Belfast", "farm@belfast.uk", "Belfast", ServiceType.URBAN_FARM);
        service11.setCalculatedDistance(86.3);
        
        LocalService service12 = new LocalService("Belfast Compost Collective", "55 Castle Place, Belfast", "compost@belfast.uk", "Belfast", ServiceType.COMPOSTING_FACILITY);
        service12.setCalculatedDistance(87.1);
        
        // San Francisco (3 services)
        LocalService service13 = new LocalService("SF Food Bank", "2550 Market St, San Francisco", "food@sf.org", "San Francisco", ServiceType.FOOD_BANK);
        service13.setCalculatedDistance(8300.0);
        
        LocalService service14 = new LocalService("Mission Community Garden", "18th & Valencia, San Francisco", "garden@sf.org", "San Francisco", ServiceType.COMMUNITY_GARDEN);
        service14.setCalculatedDistance(8320.0);
        
        LocalService service15 = new LocalService("SF Food Recovery", "1650 Bryant St, San Francisco", "recovery@sf.org", "San Francisco", ServiceType.FOOD_DONATION_CENTER);
        service15.setCalculatedDistance(8310.0);
        
        // London (3 services)
        LocalService service16 = new LocalService("London Food Bank", "123 Oxford St, London", "food@london.uk", "London", ServiceType.FOOD_BANK);
        service16.setCalculatedDistance(465.0);
        
        LocalService service17 = new LocalService("London Urban Farms", "45 Brick Lane, London", "farm@london.uk", "London", ServiceType.URBAN_FARM);
        service17.setCalculatedDistance(467.2);
        
        LocalService service18 = new LocalService("London Community Fridge", "67 Camden High St, London", "fridge@london.uk", "London", ServiceType.COMMUNITY_FRIDGE);
        service18.setCalculatedDistance(468.5);
        
        // Paris (3 services)
        LocalService service19 = new LocalService("Paris Food Bank", "45 Rue de Rivoli, Paris", "food@paris.fr", "Paris", ServiceType.FOOD_BANK);
        service19.setCalculatedDistance(790.0);
        
        LocalService service20 = new LocalService("Paris Urban Agriculture", "22 Blvd Saint-Michel, Paris", "farm@paris.fr", "Paris", ServiceType.URBAN_FARM);
        service20.setCalculatedDistance(792.3);
        
        LocalService service21 = new LocalService("Paris Compost Collective", "88 Rue de Charonne, Paris", "compost@paris.fr", "Paris", ServiceType.COMPOSTING_FACILITY);
        service21.setCalculatedDistance(788.5);
        
        // Berlin (3 services)
        LocalService service22 = new LocalService("Berlin Food Share", "45 Friedrichstrasse, Berlin", "food@berlin.de", "Berlin", ServiceType.FOOD_BANK);
        service22.setCalculatedDistance(1450.0);
        
        LocalService service23 = new LocalService("Berlin Community Gardens", "22 Alexanderplatz, Berlin", "garden@berlin.de", "Berlin", ServiceType.COMMUNITY_GARDEN);
        service23.setCalculatedDistance(1452.3);
        
        LocalService service24 = new LocalService("Berlin Food Rescue", "88 Warschauer Str, Berlin", "rescue@berlin.de", "Berlin", ServiceType.FOOD_DONATION_CENTER);
        service24.setCalculatedDistance(1448.7);
        
        // New York (3 services)
        LocalService service25 = new LocalService("NYC Food Bank", "500 8th Avenue, New York", "food@nyc.org", "New York", ServiceType.FOOD_BANK);
        service25.setCalculatedDistance(5100.0);
        
        LocalService service26 = new LocalService("Brooklyn Urban Farm", "45 DeKalb Ave, New York", "farm@nyc.org", "New York", ServiceType.URBAN_FARM);
        service26.setCalculatedDistance(5110.0);
        
        LocalService service27 = new LocalService("Manhattan Compost Project", "22 Washington Square, New York", "compost@nyc.org", "New York", ServiceType.COMPOSTING_FACILITY);
        service27.setCalculatedDistance(5095.0);
        
        System.out.println("Created total of " + LocalService.availableServices.size() + " services across all cities");
    }
    
    /**
     * Create sample services data if the database is empty
     * @deprecated This method is replaced by createAllCityServices()
     */
    @Deprecated
    private void createSampleData() {
        // Redirect to the newer implementation
        createAllCityServices();
    }
    
    // Removed old methods that are no longer needed - functionality integrated into handleSearch()
    
    // Method functionality moved directly into handleSearch for better thread handling
    
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
        addressLabel.setText(service.getAddress() + " (" + service.getCity() + ")");
        contactLabel.setText(service.getContactInfo());
        
        // Use the calculated distance that was stored during search
        double distance = service.getCalculatedDistance();
        
        // Format the distance with a descriptive label
        String distanceDescription = getDistanceDescription(distance);
        if (distance > 100) {
            distanceLabel.setText(String.format("%.0f km (%s)", distance, distanceDescription));
        } else {
            distanceLabel.setText(String.format("%.1f km (%s)", distance, distanceDescription));
        }
        
        pickupLabel.setText(service.isPickupAvailable() ? 
            "Yes (within " + service.getPickupRadius() + " km)" : "No");
        
        // Show operating hours
        hoursPane.getChildren().clear();
        if (service.getOperatingHours() != null) {
            for (DayOfWeek day : service.getOperatingHours().getOpenDays()) {
                String timeSlot = service.getOperatingHours().getHours(day).toString();
                Label hourLabel = new Label(day + ": " + timeSlot);
                hoursPane.getChildren().add(hourLabel);
            }
        } else {
            Label noHoursLabel = new Label("No operating hours available");
            hoursPane.getChildren().add(noHoursLabel);
        }
        
        // Show details pane
        detailsPane.setVisible(true);
        noSelectionPane.setVisible(false);
    }
}