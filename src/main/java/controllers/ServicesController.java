package controllers;

import com.greencompost.User;
import com.greencompost.service.LocalService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.DayOfWeek;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for the services view
 */
public class ServicesController implements Initializable {

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
    @FXML private CheckBox foodDonationCheck;
    @FXML private CheckBox soupKitchenCheck;
    @FXML private CheckBox foodPantryCheck;
    
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
    @FXML private Label donationLabel;
    @FXML private VBox hoursPane;
    @FXML private ListView<String> acceptedItemsList;
    @FXML private ListView<String> donationGuidelinesList;
    @FXML private Button scheduleButton;
    @FXML private Button donateButton;
    
    private User currentUser;
    private ObservableList<LocalService> services;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Get current user from MainController class
        currentUser = controllers.MainController.getCurrentUser();
        
        // Setup radius slider with explicit implementation to avoid unused parameter warnings
        radiusSlider.valueProperty().addListener(new javafx.beans.value.ChangeListener<Number>() {
            @Override
            public void changed(javafx.beans.value.ObservableValue<? extends Number> observable,
                              Number oldValue, Number newValue) {
                radiusLabel.setText(String.format("%.0f km", newValue.doubleValue()));
            }
        });
        
        // Setup table columns
        setupTableColumns();
        
        // Initialize services list
        services = FXCollections.observableArrayList();
        servicesTable.setItems(services);
        
        // Add table selection listener with explicit implementation to avoid unused parameter warnings
        servicesTable.getSelectionModel().selectedItemProperty().addListener(
            new javafx.beans.value.ChangeListener<LocalService>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends LocalService> observable,
                                  LocalService oldValue, LocalService newValue) {
                    showServiceDetails(newValue);
                }
            }
        );
        
        // Initially hide details pane
        detailsPane.setVisible(false);
        noSelectionPane.setVisible(true);
        
        // Load initial data
        handleSearch();
    }
    
    /**
     * Setup table columns
     */
    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        
        typeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getType().getDisplayName())
        );
        
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        
        distanceColumn.setCellValueFactory(cellData -> {
            LocalService service = cellData.getValue();
            double distance = calculateDistance(
                currentUser.getLatitude(), currentUser.getLongitude(),
                service.getLatitude(), service.getLongitude()
            );
            return new SimpleStringProperty(String.format("%.1f km", distance));
        });
    }
    
    /**
     * Calculate distance between two points
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
     * Handle searching for services
     */
    @FXML
    private void handleSearch() {
        double radius = radiusSlider.getValue();
        
        // Get all services within radius
        List<LocalService> nearbyServices = LocalService.findNearbyServicesForUser(currentUser, radius);
        
        // Filter by service type
        List<LocalService> filteredServices = nearbyServices.stream()
            .filter(service -> {
                switch (service.getType()) {
                    case FOOD_BANK:
                        return foodBankCheck.isSelected();
                    case COMPOSTING_FACILITY:
                        return compostCheck.isSelected();
                    case COMMUNITY_GARDEN:
                        return gardenCheck.isSelected();
                    case URBAN_FARM:
                        return farmCheck.isSelected();
                    case RESTAURANT:
                        return restaurantCheck.isSelected();
                    case COMMUNITY_FRIDGE:
                        return fridgeCheck.isSelected();
                    case FOOD_DONATION_CENTER:
                        return foodDonationCheck.isSelected();
                    case SOUP_KITCHEN:
                        return soupKitchenCheck.isSelected();
                    case FOOD_PANTRY:
                        return foodPantryCheck.isSelected();
                    default:
                        return true;
                }
            })
            .collect(Collectors.toList());
        
        // Update table
        services.clear();
        services.addAll(filteredServices);
        
        // Update results label
        resultsLabel.setText(String.format("%d services found", filteredServices.size()));
        
        // Clear selection
        servicesTable.getSelectionModel().clearSelection();
        showServiceDetails(null);
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
        
        double distance = calculateDistance(
            currentUser.getLatitude(), currentUser.getLongitude(),
            service.getLatitude(), service.getLongitude()
        );
        distanceLabel.setText(String.format("%.1f km", distance));
        
        pickupLabel.setText(service.isPickupAvailable() ? 
            "Yes (within " + service.getPickupRadius() + " km)" : "No");
            
        // Show donation acceptance status
        boolean isDonationService = service.acceptsFoodDonations() || 
            service.getType() == LocalService.ServiceType.FOOD_BANK || 
            service.getType() == LocalService.ServiceType.FOOD_DONATION_CENTER ||
            service.getType() == LocalService.ServiceType.SOUP_KITCHEN ||
            service.getType() == LocalService.ServiceType.FOOD_PANTRY;
            
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
     * Handle scheduling a pickup or drop-off
     */
    @FXML
    private void handleSchedule() {
        LocalService selectedService = servicesTable.getSelectionModel().getSelectedItem();
        if (selectedService == null) {
            return;
        }
        
        // This would typically open a dialog to schedule a pickup or drop-off
        // We'll just show an alert for now
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Schedule Pickup/Drop-off");
        alert.setHeaderText("Feature Coming Soon");
        alert.setContentText("This feature is not yet implemented. In the future, you'll be able to schedule pickups or drop-offs at " + selectedService.getName() + ".");
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
        
        // This would typically open a dialog to select food items to donate
        // We'll just show an alert for now
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Donate Food");
        alert.setHeaderText("Food Donation");
        
        boolean isDonationService = selectedService.acceptsFoodDonations() || 
            selectedService.getType() == LocalService.ServiceType.FOOD_BANK || 
            selectedService.getType() == LocalService.ServiceType.FOOD_DONATION_CENTER ||
            selectedService.getType() == LocalService.ServiceType.SOUP_KITCHEN ||
            selectedService.getType() == LocalService.ServiceType.FOOD_PANTRY;
            
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
}